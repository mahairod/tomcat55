/* Copyright 2000-2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 *
 * @author Mladen Turk
 * @version $Revision$, $Date$
 */

#include "apr.h"
#include "apr_pools.h"
#include "apr_poll.h"
#include "tcn.h"


#ifdef TCN_DO_STATISTICS
static int sp_created       = 0;
static int sp_destroyed     = 0;
static int sp_cleared       = 0;
#endif

/* Internal poll structure for queryset
 */

typedef struct tcn_pollset {
    apr_pool_t    *pool;
    apr_int32_t   nelts;
    apr_int32_t   nalloc;
    apr_pollset_t *pollset;
    apr_pollfd_t  *socket_set;
    apr_interval_time_t *socket_ttl;
    apr_interval_time_t max_ttl;
#ifdef TCN_DO_STATISTICS
    int sp_added;
    int sp_max_count;
    int sp_poll;
    int sp_polled;
    int sp_max_polled;
    int sp_remove;
    int sp_removed;
    int sp_maintained;
    int sp_max_maintained;
    int sp_err_poll;
    int sp_poll_timeout;
    int sp_overflow;
    int sp_equals;
#endif
} tcn_pollset_t;

#ifdef TCN_DO_STATISTICS
static void sp_poll_statistics(tcn_pollset_t *p)
{
    fprintf(stderr, "Pollset Statistics ......\n");
    fprintf(stderr, "Number of added sockets : %d\n", p->sp_added);
    fprintf(stderr, "Max. number of sockets  : %d\n", p->sp_max_count);
    fprintf(stderr, "Poll calls              : %d\n", p->sp_poll);
    fprintf(stderr, "Poll timeouts           : %d\n", p->sp_poll_timeout);
    fprintf(stderr, "Poll errors             : %d\n", p->sp_err_poll);
    fprintf(stderr, "Poll overflows          : %d\n", p->sp_overflow);
    fprintf(stderr, "Polled sockets          : %d\n", p->sp_polled);
    fprintf(stderr, "Max. Polled sockets     : %d\n", p->sp_max_polled);
    fprintf(stderr, "Poll remove             : %d\n", p->sp_remove);
    fprintf(stderr, "Total removed           : %d\n", p->sp_removed);
    fprintf(stderr, "Maintained              : %d\n", p->sp_maintained);
    fprintf(stderr, "Max. maintained         : %d\n", p->sp_max_maintained);
    fprintf(stderr, "Number of duplicates    : %d\n", p->sp_equals);

}

static apr_status_t sp_poll_cleanup(void *data)
{
    sp_cleared++;
    sp_poll_statistics(data);
    return APR_SUCCESS;
}

void sp_poll_dump_statistics()
{
    fprintf(stderr, "Poll Statistics .........\n");
    fprintf(stderr, "Polls created           : %d\n", sp_created);
    fprintf(stderr, "Polls destroyed         : %d\n", sp_destroyed);
    fprintf(stderr, "Polls cleared           : %d\n", sp_cleared);
}
#endif

TCN_IMPLEMENT_CALL(jlong, Poll, create)(TCN_STDARGS, jint size,
                                        jlong pool, jint flags,
                                        jlong ttl)
{
    apr_pool_t *p = J2P(pool, apr_pool_t *);
    apr_pollset_t *pollset = NULL;
    tcn_pollset_t *tps = NULL;
    apr_uint32_t f = (apr_uint32_t)flags;
    UNREFERENCED(o);
    TCN_ASSERT(pool != 0);

    if (f & APR_POLLSET_THREADSAFE) {
        apr_status_t rv = apr_pollset_create(&pollset, (apr_uint32_t)size, p, f);
        if (rv == APR_ENOTIMPL)
            f &= ~APR_POLLSET_THREADSAFE;
        else if (rv != APR_SUCCESS) {
            tcn_ThrowAPRException(e, rv);
            goto cleanup;
        }
    }
    if (pollset == NULL) {
        TCN_THROW_IF_ERR(apr_pollset_create(&pollset,
                         (apr_uint32_t)size, p, f), pollset);
    }
    tps = apr_pcalloc(p, sizeof(tcn_pollset_t));
    tps->pollset = pollset;
    tps->socket_set = apr_palloc(p, size * sizeof(apr_pollfd_t));
    tps->socket_ttl = apr_palloc(p, size * sizeof(apr_interval_time_t));
    tps->nelts  = 0;
    tps->nalloc = size;
    tps->pool   = p;
    tps->max_ttl = J2T(ttl);
#ifdef TCN_DO_STATISTICS
    sp_created++;
    apr_pool_cleanup_register(p, (const void *)tps,
                              sp_poll_cleanup,
                              apr_pool_cleanup_null);
#endif
cleanup:
    return P2J(tps);
}

TCN_IMPLEMENT_CALL(jint, Poll, destroy)(TCN_STDARGS, jlong pollset)
{
    tcn_pollset_t *p = J2P(pollset,  tcn_pollset_t *);

    UNREFERENCED_STDARGS;
    TCN_ASSERT(pollset != 0);
#ifdef TCN_DO_STATISTICS
    sp_destroyed++;
    apr_pool_cleanup_kill(p->pool, p, sp_poll_cleanup);
    sp_poll_statistics(p);
#endif
    return (jint)apr_pollset_destroy(p->pollset);
}

TCN_IMPLEMENT_CALL(jint, Poll, add)(TCN_STDARGS, jlong pollset,
                                    jlong socket, jlong data,
                                    jint reqevents)
{
    tcn_pollset_t *p = J2P(pollset,  tcn_pollset_t *);
    apr_pollfd_t fd;

    UNREFERENCED_STDARGS;
    TCN_ASSERT(socket != 0);

    if (p->nelts == p->nalloc) {
#ifdef TCN_DO_STATISTICS
        p->sp_overflow++;
#endif
        return APR_ENOMEM;
    }
    memset(&fd, 0, sizeof(apr_pollfd_t));
    fd.desc_type = APR_POLL_SOCKET;
    fd.reqevents = (apr_int16_t)reqevents;
    fd.desc.s = J2P(socket, apr_socket_t *);
    fd.client_data = J2P(data, void *);
    p->socket_ttl[p->nelts] = apr_time_now();
    p->socket_set[p->nelts] = fd;
    p->nelts++;
#ifdef TCN_DO_STATISTICS
    p->sp_added++;
    p->sp_max_count = TCN_MAX(p->sp_max_count, p->sp_added);
#endif
    return (jint)apr_pollset_add(p->pollset, &fd);
}

static apr_status_t do_remove(tcn_pollset_t *p, const apr_pollfd_t *fd)
{
    apr_int32_t i;

    for (i = 0; i < p->nelts; i++) {
        if (fd->desc.s == p->socket_set[i].desc.s) {
            /* Found an instance of the fd: remove this and any other copies */
            apr_int32_t dst = i;
            apr_int32_t old_nelts = p->nelts;
            p->nelts--;
#ifdef TCN_DO_STATISTICS
            p->sp_removed++;
#endif
            for (i++; i < old_nelts; i++) {
                if (fd->desc.s == p->socket_set[i].desc.s) {
#ifdef TCN_DO_STATISTICS
                    p->sp_equals++;
#endif
                    p->nelts--;
                }
                else {
                    p->socket_set[dst] = p->socket_set[i];
                    dst++;
                }
            }
            break;
        }
    }
    return apr_pollset_remove(p->pollset, fd);
}

TCN_IMPLEMENT_CALL(jint, Poll, remove)(TCN_STDARGS, jlong pollset,
                                       jlong socket)
{
    tcn_pollset_t *p = J2P(pollset,  tcn_pollset_t *);
    apr_pollfd_t fd;

    UNREFERENCED_STDARGS;
    TCN_ASSERT(socket != 0);

    memset(&fd, 0, sizeof(apr_pollfd_t));
    fd.desc_type = APR_POLL_SOCKET;
    fd.desc.s = J2P(socket, apr_socket_t *);
#ifdef TCN_DO_STATISTICS
    p->sp_remove++;
#endif

    return (jint)do_remove(p, &fd);
}


TCN_IMPLEMENT_CALL(jint, Poll, poll)(TCN_STDARGS, jlong pollset,
                                     jlong timeout, jlongArray set,
                                     jboolean remove)
{
    const apr_pollfd_t *fd = NULL;
    tcn_pollset_t *p = J2P(pollset,  tcn_pollset_t *);
    jlong *pset = (*e)->GetLongArrayElements(e, set, NULL);
    apr_int32_t  i, num = 0;
    apr_status_t rv = APR_SUCCESS;

    UNREFERENCED(o);
    TCN_ASSERT(pollset != 0);

#ifdef TCN_DO_STATISTICS
     p->sp_poll++;
#endif
    if ((rv = apr_pollset_poll(p->pollset, J2T(timeout), &num, &fd)) != APR_SUCCESS) {
        if (APR_STATUS_IS_TIMEUP(rv))
            rv = TCN_TIMEUP;
        else if (APR_STATUS_IS_EAGAIN(rv))
            rv = TCN_EAGAIN;
#ifdef TCN_DO_STATISTICS
        if (rv == TCN_TIMEUP)
            p->sp_poll_timeout++;
        else
            p->sp_err_poll++;
#endif
        num = (apr_int32_t)(-rv);
    }
    if (num > 0) {
#ifdef TCN_DO_STATISTICS
         p->sp_polled += num;
         p->sp_max_polled = TCN_MAX(p->sp_max_polled, num);
#endif
        for (i = 0; i < num; i++) {
            pset[i*4+0] = (jlong)(fd->rtnevents);
            pset[i*4+1] = P2J(fd->desc.s);
            pset[i*4+2] = P2J(fd->client_data);
            if (remove)
                do_remove(p, fd);
            fd ++;
        }
        (*e)->ReleaseLongArrayElements(e, set, pset, 0);
    }
    else
        (*e)->ReleaseLongArrayElements(e, set, pset, JNI_ABORT);

    return (jint)num;
}

TCN_IMPLEMENT_CALL(jint, Poll, maintain)(TCN_STDARGS, jlong pollset,
                                         jlongArray set, jboolean remove)
{
    tcn_pollset_t *p = J2P(pollset,  tcn_pollset_t *);
    jlong *pset = (*e)->GetLongArrayElements(e, set, NULL);
    apr_int32_t  i = 0, num = 0;
    apr_time_t now = apr_time_now();
    apr_pollfd_t fd;

    UNREFERENCED(o);
    TCN_ASSERT(pollset != 0);

    /* Check for timeout sockets */
    if (p->max_ttl > 0) {
        for (i = 0; i < p->nelts; i++) {
            if ((now - p->socket_ttl[i]) > p->max_ttl) {
                p->socket_set[i].rtnevents = APR_POLLHUP | APR_POLLIN;
                if (num < p->nelts) {
                    fd = p->socket_set[i];
                    pset[num*4+0] = (jlong)(fd.rtnevents);
                    pset[num*4+1] = P2J(fd.desc.s);
                    pset[num*4+2] = P2J(fd.client_data);
                    num++;
                }
            }
        }
        if (remove && num) {
            memset(&fd, 0, sizeof(apr_pollfd_t));
#ifdef TCN_DO_STATISTICS
             p->sp_maintained += num;
             p->sp_max_maintained = TCN_MAX(p->sp_max_maintained, num);
#endif
            for (i = 0; i < num; i++) {
                fd.desc_type = APR_POLL_SOCKET;
                fd.desc.s = J2P(pset[i*4+1], apr_socket_t *);
                do_remove(p, &fd);
            }
        }
    }
    if (num)
        (*e)->ReleaseLongArrayElements(e, set, pset, 0);
    else
        (*e)->ReleaseLongArrayElements(e, set, pset, JNI_ABORT);
    return (jint)num;
}

TCN_IMPLEMENT_CALL(void, Poll, setTtl)(TCN_STDARGS, jlong pollset,
                                       jlong ttl)
{
    tcn_pollset_t *p = J2P(pollset,  tcn_pollset_t *);
    UNREFERENCED_STDARGS;
    p->max_ttl = J2T(ttl);
}

TCN_IMPLEMENT_CALL(jlong, Poll, getTtl)(TCN_STDARGS, jlong pollset)
{
    tcn_pollset_t *p = J2P(pollset,  tcn_pollset_t *);
    UNREFERENCED_STDARGS;
    return (jlong)p->max_ttl;
}
