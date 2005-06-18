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

#include "tcn.h"

#ifdef TCN_DO_STATISTICS
#include "apr_atomic.h"
static volatile apr_uint32_t sp_created  = 0;
static volatile apr_uint32_t sp_closed   = 0;
static volatile apr_uint32_t sp_cleared  = 0;
static volatile apr_uint32_t sp_accepted = 0;
static volatile apr_uint32_t sp_max_send = 0;
static volatile apr_uint32_t sp_min_send = 10000000;
static volatile apr_uint32_t sp_num_send = 0;
static volatile apr_off_t    sp_tot_send = 0;
static volatile apr_uint32_t sp_max_recv = 0;
static volatile apr_uint32_t sp_min_recv = 10000000;
static volatile apr_uint32_t sp_num_recv = 0;
static volatile apr_off_t    sp_tot_recv = 0;
static volatile apr_uint32_t sp_err_recv = 0;
static volatile apr_uint32_t sp_tmo_recv = 0;

/* Fake private pool struct to deal with APR private's socket
 * struct not exposing function to access the pool.
 */
typedef struct
{
    apr_pool_t *pool;
} fake_apr_socket_t;
#endif

#if  !APR_HAVE_IPV6
#define APR_INET6 APR_INET
#endif

#define GET_S_FAMILY(T, F)           \
    if (F == 0) T = APR_UNSPEC;      \
    else if (F == 1) T = APR_INET;   \
    else if (F == 2) T = APR_INET6;  \
    else T = F

#define GET_S_TYPE(T, F)             \
    if (F == 0) T = SOCK_STREAM;     \
    else if (F == 1) T = SOCK_DGRAM; \
    else T = F

#ifdef TCN_DO_STATISTICS

void sp_network_dump_statistics()
{
    fprintf(stderr, "Network Statistics ......\n");
    fprintf(stderr, "Sockets created         : %d\n", sp_created);
    fprintf(stderr, "Sockets accepted        : %d\n", sp_accepted);
    fprintf(stderr, "Sockets closed          : %d\n", sp_closed);
    fprintf(stderr, "Sockets cleared         : %d\n", sp_cleared);
    fprintf(stderr, "Total send calls        : %d\n", sp_num_send);
    fprintf(stderr, "Minimum send lenght     : %d\n", sp_min_send);
    fprintf(stderr, "Maximum send lenght     : %d\n", sp_max_send);
    fprintf(stderr, "Average send lenght     : %.2f\n", (double)sp_tot_send/(double)sp_num_send);
    fprintf(stderr, "Total recv calls        : %d\n", sp_num_recv);
    fprintf(stderr, "Minimum recv lenght     : %d\n", sp_min_recv);
    fprintf(stderr, "Maximum recv lenght     : %d\n", sp_max_recv);
    fprintf(stderr, "Average recv lenght     : %.2f\n", (double)sp_tot_recv/(double)sp_num_recv);
    fprintf(stderr, "Receive timeouts        : %d\n", sp_tmo_recv);
    fprintf(stderr, "Receive errors          : %d\n", sp_err_recv);
}

#endif

TCN_IMPLEMENT_CALL(jlong, Address, info)(TCN_STDARGS,
                                         jstring hostname,
                                         jint family, jint port,
                                         jint flags, jlong pool)
{
    apr_pool_t *p = J2P(pool, apr_pool_t *);
    TCN_ALLOC_CSTRING(hostname);
    apr_sockaddr_t *sa = NULL;
    apr_int32_t f;


    UNREFERENCED(o);
    GET_S_FAMILY(f, family);
    TCN_THROW_IF_ERR(apr_sockaddr_info_get(&sa,
            J2S(hostname), f, (apr_port_t)port,
            (apr_int32_t)flags, p), sa);

cleanup:
    TCN_FREE_CSTRING(hostname);
    return P2J(sa);
}

TCN_IMPLEMENT_CALL(jstring, Address, getnameinfo)(TCN_STDARGS,
                                                  jlong sa, jint flags)
{
    apr_sockaddr_t *s = J2P(sa, apr_sockaddr_t *);
    char *hostname;

    UNREFERENCED(o);
    if (apr_getnameinfo(&hostname, s, (apr_int32_t)flags) == APR_SUCCESS)
        return AJP_TO_JSTRING(hostname);
    else
        return NULL;
}

TCN_IMPLEMENT_CALL(jstring, Address, getip)(TCN_STDARGS, jlong sa)
{
    apr_sockaddr_t *s = J2P(sa, apr_sockaddr_t *);
    char *ipaddr;

    UNREFERENCED(o);
    if (apr_sockaddr_ip_get(&ipaddr, s) == APR_SUCCESS)
        return AJP_TO_JSTRING(ipaddr);
    else
        return NULL;
}

TCN_IMPLEMENT_CALL(jlong, Address, get)(TCN_STDARGS, jint which,
                                        jlong sock)
{
    tcn_socket_t *s = J2P(sock, tcn_socket_t *);
    apr_sockaddr_t *sa = NULL;

    UNREFERENCED(o);
    TCN_THROW_IF_ERR(apr_socket_addr_get(&sa,
                        (apr_interface_e)which, s->sock), sa);
cleanup:
    return P2J(sa);
}

TCN_IMPLEMENT_CALL(jint, Address, equal)(TCN_STDARGS,
                                         jlong a, jlong b)
{
    apr_sockaddr_t *sa = J2P(a, apr_sockaddr_t *);
    apr_sockaddr_t *sb = J2P(b, apr_sockaddr_t *);

    UNREFERENCED_STDARGS;
    return apr_sockaddr_equal(sa, sb) ? JNI_TRUE : JNI_FALSE;
}

TCN_IMPLEMENT_CALL(jint, Address, getservbyname)(TCN_STDARGS,
                                                 jlong sa, jstring servname)
{
    apr_sockaddr_t *s = J2P(sa, apr_sockaddr_t *);
    TCN_ALLOC_CSTRING(servname);
    apr_status_t rv;

    UNREFERENCED(o);
    rv = apr_getservbyname(s, J2S(servname));
    TCN_FREE_CSTRING(servname);
    return (jint)rv;
}

static apr_status_t sp_socket_cleanup(void *data)
{
    tcn_socket_t *s = (tcn_socket_t *)data;

    if (s->cleanup) {
        (*s->cleanup)(s->opaque);
        s->cleanup = NULL;
    }
    if (s->sock) {
        apr_socket_close(s->sock);
        s->sock = NULL;
    }
#ifdef TCN_DO_STATISTICS
    apr_atomic_inc32(&sp_cleared);
#endif
    return APR_SUCCESS;
}

#if defined(DEBUG) || defined(_DEBUG)
static APR_INLINE apr_status_t APR_THREAD_FUNC
APR_socket_send(void *sock, const char *buf, apr_size_t *len)
{
    return apr_socket_send((apr_socket_t *)sock, buf, len);
}

static APR_INLINE apr_status_t APR_THREAD_FUNC
APR_socket_recv(void *sock, char *buf, apr_size_t *len)
{
    return apr_socket_recv((apr_socket_t *)sock, buf, len);
}

static APR_INLINE apr_status_t APR_THREAD_FUNC
APR_socket_sendv(void *sock, const struct iovec *vec,
                 apr_int32_t nvec, apr_size_t *len)
{
    return apr_socket_sendv((apr_socket_t *)sock, vec, nvec, len);
}

static APR_INLINE apr_status_t APR_THREAD_FUNC
APR_socket_shutdown(void *sock, apr_shutdown_how_e how)
{
    return apr_socket_shutdown((apr_socket_t *)sock, how);
}

#else
#define APR_socket_send      apr_socket_send
#define APR_socket_recv      apr_socket_recv
#define APR_socket_sendv     apr_socket_sendv
#define APR_socket_shutdown  apr_socket_shutdown
#endif

TCN_IMPLEMENT_CALL(jlong, Socket, create)(TCN_STDARGS, jint family,
                                          jint type, jint protocol,
                                          jlong pool)
{
    apr_pool_t *p = J2P(pool, apr_pool_t *);
    apr_socket_t *s = NULL;
    tcn_socket_t *a = NULL;
    apr_int32_t f, t;

    UNREFERENCED(o);
    TCN_ASSERT(pool != 0);
    GET_S_FAMILY(f, family);
    GET_S_TYPE(t, type);

    TCN_THROW_IF_ERR(apr_socket_create(&s,
                     f, t, protocol, p), a);

#ifdef TCN_DO_STATISTICS
    sp_created++;
#endif
    a = (tcn_socket_t *)apr_pcalloc(p, sizeof(tcn_socket_t));
    a->sock = s;
    a->pool = p;
    a->type = TCN_SOCKET_APR;
    a->recv     = APR_socket_recv;
    a->send     = APR_socket_send;
    a->sendv    = APR_socket_sendv;
    a->shutdown = APR_socket_shutdown;
    a->close    = NULL;
    a->opaque   = s;
    apr_pool_cleanup_register(p, (const void *)a,
                              sp_socket_cleanup,
                              apr_pool_cleanup_null);

cleanup:
    return P2J(a);

}

TCN_IMPLEMENT_CALL(void, Socket, destroy)(TCN_STDARGS, jlong sock)
{
    tcn_socket_t *s = J2P(sock, tcn_socket_t *);
    UNREFERENCED_STDARGS;
    apr_pool_destroy(s->pool);
}

TCN_IMPLEMENT_CALL(jlong, Socket, pool)(TCN_STDARGS, jlong sock)
{
    tcn_socket_t *s = J2P(sock, tcn_socket_t *);
    apr_pool_t *n;

    UNREFERENCED(o);
    TCN_THROW_IF_ERR(apr_pool_create(&n, s->pool), n);
cleanup:
    return P2J(n);
}

TCN_IMPLEMENT_CALL(jint, Socket, shutdown)(TCN_STDARGS, jlong sock,
                                           jint how)
{
    tcn_socket_t *s = J2P(sock, tcn_socket_t *);

    UNREFERENCED_STDARGS;
    TCN_ASSERT(sock != 0);
    return (jint)(*s->shutdown)(s->opaque, how);
}

TCN_IMPLEMENT_CALL(jint, Socket, close)(TCN_STDARGS, jlong sock)
{
    tcn_socket_t *s = J2P(sock, tcn_socket_t *);
    jint rv = APR_SUCCESS;
    UNREFERENCED_STDARGS;
    TCN_ASSERT(sock != 0);

#ifdef TCN_DO_STATISTICS
    apr_atomic_inc32(&sp_closed);
#endif
    if (s->close)
        rv = (*s->close)(s->opaque);
    if (s->sock) {
        rv = (jint)apr_socket_close(s->sock);
        s->sock = NULL;
    }
    return rv;
}

TCN_IMPLEMENT_CALL(jint, Socket, bind)(TCN_STDARGS, jlong sock,
                                       jlong sa)
{
    tcn_socket_t *s = J2P(sock, tcn_socket_t *);
    apr_sockaddr_t *a = J2P(sa, apr_sockaddr_t *);

    UNREFERENCED_STDARGS;
    TCN_ASSERT(sock != 0);
    return (jint)apr_socket_bind(s->sock, a);
}

TCN_IMPLEMENT_CALL(jint, Socket, listen)(TCN_STDARGS, jlong sock,
                                         jint backlog)
{
    tcn_socket_t *s = J2P(sock, tcn_socket_t *);

    UNREFERENCED_STDARGS;
    TCN_ASSERT(sock != 0);
    return (jint)apr_socket_listen(s->sock, backlog);
}

TCN_IMPLEMENT_CALL(jlong, Socket, acceptx)(TCN_STDARGS, jlong sock,
                                           jlong pool)
{
    tcn_socket_t *s = J2P(sock, tcn_socket_t *);
    apr_pool_t   *p = J2P(pool, apr_pool_t *);
    apr_socket_t *n = NULL;
    tcn_socket_t *a = NULL;

    UNREFERENCED(o);
    TCN_ASSERT(sock != 0);

    TCN_THROW_IF_ERR(apr_socket_accept(&n, s->sock, p), n);

    if (n) {
#ifdef TCN_DO_STATISTICS
        apr_atomic_inc32(&sp_accepted);
#endif
        a = (tcn_socket_t *)apr_pcalloc(p, sizeof(tcn_socket_t));
        a->sock = n;
        a->pool = p;
        a->type = TCN_SOCKET_APR;
        a->recv     = APR_socket_recv;
        a->send     = APR_socket_send;
        a->sendv    = APR_socket_sendv;
        a->shutdown = APR_socket_shutdown;
        a->close    = NULL;
        a->opaque   = n;
        apr_pool_cleanup_register(p, (const void *)a,
                                  sp_socket_cleanup,
                                  apr_pool_cleanup_null);
    }

cleanup:
    return P2J(a);
}

TCN_IMPLEMENT_CALL(jlong, Socket, accept)(TCN_STDARGS, jlong sock)
{
    tcn_socket_t *s = J2P(sock, tcn_socket_t *);
    apr_pool_t   *p = NULL;
    apr_socket_t *n = NULL;
    tcn_socket_t *a = NULL;

    UNREFERENCED(o);
    TCN_ASSERT(sock != 0);

    TCN_THROW_IF_ERR(apr_pool_create(&p, s->pool), p);
    TCN_THROW_IF_ERR(apr_socket_accept(&n, s->sock, p), n);

    if (n) {
#ifdef TCN_DO_STATISTICS
        apr_atomic_inc32(&sp_accepted);
#endif
        a = (tcn_socket_t *)apr_pcalloc(p, sizeof(tcn_socket_t));
        a->sock = n;
        a->pool = p;
        a->type = TCN_SOCKET_APR;
        a->recv     = APR_socket_recv;
        a->send     = APR_socket_send;
        a->sendv    = APR_socket_sendv;
        a->shutdown = APR_socket_shutdown;
        a->close    = NULL;
        a->opaque   = n;
        apr_pool_cleanup_register(p, (const void *)a,
                                  sp_socket_cleanup,
                                  apr_pool_cleanup_null);
    }
    else if (p)
        apr_pool_destroy(p);

cleanup:
    return P2J(a);
}

TCN_IMPLEMENT_CALL(jint, Socket, connect)(TCN_STDARGS, jlong sock,
                                          jlong sa)
{
    tcn_socket_t *s = J2P(sock, tcn_socket_t *);
    apr_sockaddr_t *a = J2P(sa, apr_sockaddr_t *);

    UNREFERENCED_STDARGS;
    TCN_ASSERT(sock != 0);
    return (jint)apr_socket_connect(s->sock, a);
}

TCN_IMPLEMENT_CALL(jint, Socket, send)(TCN_STDARGS, jlong sock,
                                      jbyteArray buf, jint offset, jint tosend)
{
    tcn_socket_t *s = J2P(sock, tcn_socket_t *);
    apr_size_t nbytes = (apr_size_t)tosend;
    apr_status_t ss;

    UNREFERENCED(o);
    TCN_ASSERT(sock != 0);
#ifdef TCN_DO_STATISTICS
    sp_max_send = TCN_MAX(sp_max_send, nbytes);
    sp_min_send = TCN_MIN(sp_min_send, nbytes);
    sp_tot_send += nbytes;
    sp_num_send++;
#endif

    if (tosend <= TCN_BUFFER_SZ) {
        char sb[TCN_BUFFER_SZ];
        (*e)->GetByteArrayRegion(e, buf, offset, tosend, (jbyte *)sb);
        ss = apr_socket_send(s->sock, sb, &nbytes);
    }
    else {
        jbyte *bytes;
        apr_int32_t nb;
        apr_socket_opt_get(s->sock, APR_SO_NONBLOCK, &nb);
        if (nb)
            bytes = (*e)->GetPrimitiveArrayCritical(e, buf, NULL);
        else
            bytes = (*e)->GetByteArrayElements(e, buf, NULL);
        ss = (*s->send)(s->opaque, bytes + offset, &nbytes);
        if (nb)
            (*e)->ReleasePrimitiveArrayCritical(e, buf, bytes, JNI_ABORT);
        else
            (*e)->ReleaseByteArrayElements(e, buf, bytes, JNI_ABORT);
    }
    if (ss == APR_SUCCESS)
        return (jint)nbytes;
    else {
        TCN_ERROR_WRAP(ss);
        return -(jint)ss;
    }
}

TCN_IMPLEMENT_CALL(jint, Socket, sendb)(TCN_STDARGS, jlong sock,
                                        jobject buf, jint offset, jint len)
{
    tcn_socket_t *s = J2P(sock, tcn_socket_t *);
    apr_size_t nbytes = (apr_size_t)len;
    char *bytes;
    apr_status_t ss;

    UNREFERENCED(o);
    TCN_ASSERT(sock != 0);
    TCN_ASSERT(buf != NULL);
#ifdef TCN_DO_STATISTICS
    sp_max_send = TCN_MAX(sp_max_send, nbytes);
    sp_min_send = TCN_MIN(sp_min_send, nbytes);
    sp_tot_send += nbytes;
    sp_num_send++;
#endif

    bytes  = (char *)(*e)->GetDirectBufferAddress(e, buf);
    ss = (*s->send)(s->opaque, bytes + offset, &nbytes);

    if (ss == APR_SUCCESS)
        return (jint)nbytes;
    else {
        TCN_ERROR_WRAP(ss);
        return -(jint)ss;
    }
}

TCN_IMPLEMENT_CALL(jint, Socket, sendv)(TCN_STDARGS, jlong sock,
                                        jobjectArray bufs)
{
    tcn_socket_t *s = J2P(sock, tcn_socket_t *);
    jsize nvec;
    jsize i;
    struct iovec vec[APR_MAX_IOVEC_SIZE];
    jobject ba[APR_MAX_IOVEC_SIZE];
    apr_size_t written = 0;
    apr_status_t ss;

    UNREFERENCED(o);
    TCN_ASSERT(sock != 0);

    nvec = (*e)->GetArrayLength(e, bufs);
    if (nvec >= APR_MAX_IOVEC_SIZE)
        return (jint)(-APR_ENOMEM);

    for (i = 0; i < nvec; i++) {
        ba[i] = (*e)->GetObjectArrayElement(e, bufs, i);
        vec[i].iov_len  = (*e)->GetArrayLength(e, ba[i]);
        vec[i].iov_base = (*e)->GetByteArrayElements(e, ba[i], NULL);
    }

    ss = (*s->sendv)(s->opaque, vec, nvec, &written);

    for (i = 0; i < nvec; i++) {
        (*e)->ReleaseByteArrayElements(e, ba[i], vec[i].iov_base, JNI_ABORT);
    }
    if (ss == APR_SUCCESS)
        return (jint)written;
    else {
        TCN_ERROR_WRAP(ss);
        return -(jint)ss;
    }
}

TCN_IMPLEMENT_CALL(jint, Socket, sendto)(TCN_STDARGS, jlong sock,
                                         jlong where, jint flag,
                                         jbyteArray buf, jint offset, jint tosend)
{
    tcn_socket_t *s = J2P(sock, tcn_socket_t *);
    apr_sockaddr_t *w = J2P(where, apr_sockaddr_t *);
    apr_size_t nbytes = (apr_size_t)tosend;
    jbyte *bytes;
    apr_int32_t nb;
    apr_status_t ss;

    UNREFERENCED(o);
    TCN_ASSERT(sock != 0);

    bytes = (*e)->GetByteArrayElements(e, buf, NULL);
    TCN_ASSERT(bytes != NULL);
    apr_socket_opt_get(s->sock, APR_SO_NONBLOCK, &nb);
    if (nb)
         bytes = (*e)->GetPrimitiveArrayCritical(e, buf, NULL);
    else
         bytes = (*e)->GetByteArrayElements(e, buf, NULL);
    ss = apr_socket_sendto(s->sock, w, flag, bytes + offset, &nbytes);

    if (nb)
        (*e)->ReleasePrimitiveArrayCritical(e, buf, bytes, 0);
    else
        (*e)->ReleaseByteArrayElements(e, buf, bytes, JNI_ABORT);
    if (ss == APR_SUCCESS)
        return (jint)nbytes;
    else {
        TCN_ERROR_WRAP(ss);
        return -(jint)ss;
    }
}

TCN_IMPLEMENT_CALL(jint, Socket, recv)(TCN_STDARGS, jlong sock,
                                       jbyteArray buf, jint offset, jint toread)
{
    tcn_socket_t *s = J2P(sock, tcn_socket_t *);
    apr_size_t nbytes = (apr_size_t)toread;
    apr_status_t ss;

    UNREFERENCED(o);
    TCN_ASSERT(sock != 0);

    if (toread <= TCN_BUFFER_SZ) {
        char sb[TCN_BUFFER_SZ];

        if ((ss = (*s->recv)(s->opaque, sb, &nbytes)) == APR_SUCCESS)
            (*e)->SetByteArrayRegion(e, buf, offset, (jsize)nbytes, sb);
    }
    else {
        jbyte *bytes = (*e)->GetByteArrayElements(e, buf, NULL);
        ss = (*s->recv)(s->opaque, bytes + offset, &nbytes);
        (*e)->ReleaseByteArrayElements(e, buf, bytes,
                                       nbytes ? 0 : JNI_ABORT);
    }
#ifdef TCN_DO_STATISTICS
    if (ss == APR_SUCCESS) {
        sp_max_recv = TCN_MAX(sp_max_recv, nbytes);
        sp_min_recv = TCN_MIN(sp_min_recv, nbytes);
        sp_tot_recv += nbytes;
        sp_num_recv++;
    }
    else {
        if (APR_STATUS_IS_ETIMEDOUT(ss) ||
            APR_STATUS_IS_TIMEUP(ss))
            sp_tmo_recv++;
        else
            sp_err_recv++;
    }
#endif
    if (ss == APR_SUCCESS)
        return (jint)nbytes;
    else {
        TCN_ERROR_WRAP(ss);
        return -(jint)ss;
    }
}

TCN_IMPLEMENT_CALL(jint, Socket, recvt)(TCN_STDARGS, jlong sock,
                                        jbyteArray buf, jint offset,
                                        jint toread, jlong timeout)
{
    tcn_socket_t *s = J2P(sock, tcn_socket_t *);
    apr_size_t nbytes = (apr_size_t)toread;
    apr_status_t ss;
    apr_interval_time_t t;

    UNREFERENCED(o);
    TCN_ASSERT(sock != 0);
    TCN_ASSERT(buf != NULL);

    if ((ss = apr_socket_timeout_get(s->sock, &t)) != APR_SUCCESS)
        goto cleanup;
    if ((ss = apr_socket_timeout_set(s->sock, J2T(timeout))) != APR_SUCCESS)
        goto cleanup;
    if (toread <= TCN_BUFFER_SZ) {
        char sb[TCN_BUFFER_SZ];
        ss = (*s->recv)(s->opaque, sb, &nbytes);
        (*e)->SetByteArrayRegion(e, buf, offset, (jsize)nbytes, sb);
    }
    else {
        jbyte *bytes = (*e)->GetByteArrayElements(e, buf, NULL);
        ss = (*s->recv)(s->opaque, bytes + offset, &nbytes);
        (*e)->ReleaseByteArrayElements(e, buf, bytes,
                                       nbytes ? 0 : JNI_ABORT);
    }
    /* Resore the original timeout */
    apr_socket_timeout_set(s->sock, t);
#ifdef TCN_DO_STATISTICS
    if (ss == APR_SUCCESS) {
        sp_max_recv = TCN_MAX(sp_max_recv, nbytes);
        sp_min_recv = TCN_MIN(sp_min_recv, nbytes);
        sp_tot_recv += nbytes;
        sp_num_recv++;
    }
    else {
        if (APR_STATUS_IS_ETIMEDOUT(ss) ||
            APR_STATUS_IS_TIMEUP(ss))
            sp_tmo_recv++;
        else
            sp_err_recv++;
    }
#endif
cleanup:
    if (ss == APR_SUCCESS)
        return (jint)nbytes;
    else {
        TCN_ERROR_WRAP(ss);
        return -(jint)ss;
    }
}

TCN_IMPLEMENT_CALL(jint, Socket, recvb)(TCN_STDARGS, jlong sock,
                                        jobject buf, jint offset, jint len)
{
    tcn_socket_t *s = J2P(sock, tcn_socket_t *);
    apr_status_t ss;
    apr_size_t nbytes = (apr_size_t)len;
    char *bytes;

    UNREFERENCED(o);
    TCN_ASSERT(sock != 0);
    TCN_ASSERT(buf != NULL);

    bytes  = (char *)(*e)->GetDirectBufferAddress(e, buf);
    TCN_ASSERT(bytes != NULL);
    ss = (*s->recv)(s->opaque, bytes + offset, &nbytes);
#ifdef TCN_DO_STATISTICS
    if (ss == APR_SUCCESS) {
        sp_max_recv = TCN_MAX(sp_max_recv, nbytes);
        sp_min_recv = TCN_MIN(sp_min_recv, nbytes);
        sp_tot_recv += nbytes;
        sp_num_recv++;
    }
    else {
        if (APR_STATUS_IS_ETIMEDOUT(ss) ||
            APR_STATUS_IS_TIMEUP(ss))
            sp_tmo_recv++;
        else
            sp_err_recv++;
    }
#endif
    if (ss == APR_SUCCESS)
        return (jint)nbytes;
    else {
        TCN_ERROR_WRAP(ss);
        return -(jint)ss;
    }
}

TCN_IMPLEMENT_CALL(jint, Socket, recvbt)(TCN_STDARGS, jlong sock,
                                         jobject buf, jint offset,
                                         jint len, jlong timeout)
{
    tcn_socket_t *s = J2P(sock, tcn_socket_t *);
    apr_status_t ss;
    apr_size_t nbytes = (apr_size_t)len;
    char *bytes;
    apr_interval_time_t t;

    UNREFERENCED(o);
    TCN_ASSERT(sock != 0);
    TCN_ASSERT(buf != NULL);

    bytes  = (char *)(*e)->GetDirectBufferAddress(e, buf);
    TCN_ASSERT(bytes != NULL);

    if ((ss = apr_socket_timeout_get(s->sock, &t)) != APR_SUCCESS)
         return -(jint)ss;
    if ((ss = apr_socket_timeout_set(s->sock, J2T(timeout))) != APR_SUCCESS)
         return -(jint)ss;
    ss = (*s->recv)(s->opaque, bytes + offset, &nbytes);
    /* Resore the original timeout */
    apr_socket_timeout_set(s->sock, t);
#ifdef TCN_DO_STATISTICS
    if (ss == APR_SUCCESS) {
        sp_max_recv = TCN_MAX(sp_max_recv, nbytes);
        sp_min_recv = TCN_MIN(sp_min_recv, nbytes);
        sp_tot_recv += nbytes;
        sp_num_recv++;
    }
    else {
        if (APR_STATUS_IS_ETIMEDOUT(ss) ||
            APR_STATUS_IS_TIMEUP(ss))
            sp_tmo_recv++;
        else
            sp_err_recv++;
    }
#endif
    if (ss == APR_SUCCESS)
        return (jint)nbytes;
    else {
        TCN_ERROR_WRAP(ss);
        return -(jint)ss;
    }
}

TCN_IMPLEMENT_CALL(jint, Socket, recvfrom)(TCN_STDARGS, jlong from,
                                          jlong sock, jint flags,
                                          jbyteArray buf, jint offset, jint toread)
{
    tcn_socket_t *s = J2P(sock, tcn_socket_t *);
    apr_sockaddr_t *f = J2P(from, apr_sockaddr_t *);
    apr_size_t nbytes = (apr_size_t)toread;
    jbyte *bytes = (*e)->GetByteArrayElements(e, buf, NULL);
    apr_status_t ss;

    UNREFERENCED(o);
    TCN_ASSERT(sock != 0);
    TCN_ASSERT(buf != NULL);
    ss = apr_socket_recvfrom(f, s->sock, (apr_int32_t)flags, bytes + offset, &nbytes);

    (*e)->ReleaseByteArrayElements(e, buf, bytes,
                                   nbytes ? 0 : JNI_ABORT);
    if (ss == APR_SUCCESS)
        return (jint)nbytes;
    else {
        TCN_ERROR_WRAP(ss);
        return -(jint)ss;
    }
}

TCN_IMPLEMENT_CALL(jint, Socket, optSet)(TCN_STDARGS, jlong sock,
                                         jint opt, jint on)
{
    tcn_socket_t *s = J2P(sock, tcn_socket_t *);

    UNREFERENCED_STDARGS;
    TCN_ASSERT(sock != 0);
    return (jint)apr_socket_opt_set(s->sock, (apr_int32_t)opt, (apr_int32_t)on);
}

TCN_IMPLEMENT_CALL(jint, Socket, optGet)(TCN_STDARGS, jlong sock,
                                         jint opt)
{
    tcn_socket_t *s = J2P(sock, tcn_socket_t *);
    apr_int32_t on;

    UNREFERENCED(o);
    TCN_ASSERT(sock != 0);
    TCN_THROW_IF_ERR(apr_socket_opt_get(s->sock, (apr_int32_t)opt,
                                        &on), on);
cleanup:
    return (jint)on;
}

TCN_IMPLEMENT_CALL(jint, Socket, timeoutSet)(TCN_STDARGS, jlong sock,
                                             jlong timeout)
{
    tcn_socket_t *s = J2P(sock, tcn_socket_t *);

    UNREFERENCED_STDARGS;
    TCN_ASSERT(sock != 0);
    return (jint)apr_socket_timeout_set(s->sock, J2T(timeout));
}

TCN_IMPLEMENT_CALL(jlong, Socket, timeoutGet)(TCN_STDARGS, jlong sock)
{
    tcn_socket_t *s = J2P(sock, tcn_socket_t *);
    apr_interval_time_t timeout;

    UNREFERENCED(o);
    TCN_ASSERT(sock != 0);
    TCN_THROW_IF_ERR(apr_socket_timeout_get(s->sock, &timeout), timeout);

cleanup:
    return (jlong)timeout;
}

TCN_IMPLEMENT_CALL(jboolean, Socket, atmark)(TCN_STDARGS, jlong sock)
{
    tcn_socket_t *s = J2P(sock, tcn_socket_t *);
    apr_int32_t mark;

    UNREFERENCED_STDARGS;
    TCN_ASSERT(sock != 0);
    if (apr_socket_atmark(s->sock, &mark) != APR_SUCCESS)
        return JNI_FALSE;
    return mark ? JNI_TRUE : JNI_FALSE;
}


TCN_IMPLEMENT_CALL(jlong, Socket, sendfile)(TCN_STDARGS, jlong sock,
                                            jlong file,
                                            jobjectArray headers,
                                            jobjectArray trailers,
                                            jlong offset, jlong len,
                                            jint flags)
{
    tcn_socket_t *s = J2P(sock, tcn_socket_t *);
    apr_file_t *f = J2P(file, apr_file_t *);
    jsize nh = 0;
    jsize nt = 0;
    jsize i;
    struct iovec hvec[APR_MAX_IOVEC_SIZE];
    struct iovec tvec[APR_MAX_IOVEC_SIZE];
    jobject hba[APR_MAX_IOVEC_SIZE];
    jobject tba[APR_MAX_IOVEC_SIZE];
    apr_off_t off = (apr_off_t)offset;
    apr_size_t written = (apr_size_t)len;
    apr_hdtr_t hdrs;
    apr_status_t ss;

    UNREFERENCED(o);
    TCN_ASSERT(sock != 0);
    TCN_ASSERT(file != 0);

    if (s->type != TCN_SOCKET_APR)
        return (jint)(-APR_ENOTIMPL);
    if (headers)
        nh = (*e)->GetArrayLength(e, headers);
    if (trailers)
        nt = (*e)->GetArrayLength(e, trailers);
    /* Check for overflow */
    if (nh >= APR_MAX_IOVEC_SIZE || nt >= APR_MAX_IOVEC_SIZE)
        return (jint)(-APR_ENOMEM);

    for (i = 0; i < nh; i++) {
        hba[i] = (*e)->GetObjectArrayElement(e, headers, i);
        hvec[i].iov_len  = (*e)->GetArrayLength(e, hba[i]);
        hvec[i].iov_base = (*e)->GetByteArrayElements(e, hba[i], NULL);
    }
    for (i = 0; i < nt; i++) {
        tba[i] = (*e)->GetObjectArrayElement(e, trailers, i);
        tvec[i].iov_len  = (*e)->GetArrayLength(e, tba[i]);
        tvec[i].iov_base = (*e)->GetByteArrayElements(e, tba[i], NULL);
    }
    hdrs.headers = &hvec[0];
    hdrs.numheaders = nh;
    hdrs.trailers = &tvec[0];
    hdrs.numtrailers = nt;

    ss = apr_socket_sendfile(s->sock, f, &hdrs, &off, &written, (apr_int32_t)flags);

    for (i = 0; i < nh; i++) {
        (*e)->ReleaseByteArrayElements(e, hba[i], hvec[i].iov_base, JNI_ABORT);
    }

    for (i = 0; i < nt; i++) {
        (*e)->ReleaseByteArrayElements(e, tba[i], tvec[i].iov_base, JNI_ABORT);
    }
    /* Return Number of bytes actually sent,
     * including headers, file, and trailers
     */
    if (ss == APR_SUCCESS)
        return (jlong)written;
    else {
        TCN_ERROR_WRAP(ss);
        return -(jlong)ss;
    }
}

TCN_IMPLEMENT_CALL(jint, Socket, acceptfilter)(TCN_STDARGS,
                                               jlong sock,
                                               jstring name,
                                               jstring args)
{
#if APR_HAS_SO_ACCEPTFILTER
    tcn_socket_t *s = J2P(sock, tcn_socket_t *);
    TCN_ALLOC_CSTRING(name);
    TCN_ALLOC_CSTRING(args);
    apr_status_t rv;


    UNREFERENCED(o);
    rv = apr_socket_accept_filter(s->sock, J2S(name),
                                  J2S(args) ? J2S(args) : "");
    TCN_FREE_CSTRING(name);
    TCN_FREE_CSTRING(args);
    return (jint)rv;
#else
    UNREFERENCED_STDARGS;
    UNREFERENCED(sock);
    UNREFERENCED(name);
    UNREFERENCED(args);
    return (jint)APR_ENOTIMPL;
#endif
}

TCN_IMPLEMENT_CALL(jint, Mulicast, join)(TCN_STDARGS,
                                         jlong sock, jlong join,
                                         jlong iface, jlong source)
{
    tcn_socket_t *s = J2P(sock, tcn_socket_t *);
    apr_sockaddr_t *ja = J2P(join, apr_sockaddr_t *);
    apr_sockaddr_t *ia = J2P(iface, apr_sockaddr_t *);
    apr_sockaddr_t *sa = J2P(source, apr_sockaddr_t *);
    UNREFERENCED_STDARGS;
    return (jint)apr_mcast_join(s->sock, ja, ia, sa);
};

TCN_IMPLEMENT_CALL(jint, Mulicast, leave)(TCN_STDARGS,
                                          jlong sock, jlong addr,
                                          jlong iface, jlong source)
{
    tcn_socket_t *s = J2P(sock, tcn_socket_t *);
    apr_sockaddr_t *aa = J2P(addr, apr_sockaddr_t *);
    apr_sockaddr_t *ia = J2P(iface, apr_sockaddr_t *);
    apr_sockaddr_t *sa = J2P(source, apr_sockaddr_t *);
    UNREFERENCED_STDARGS;
    return (jint)apr_mcast_leave(s->sock, aa, ia, sa);
};

TCN_IMPLEMENT_CALL(jint, Mulicast, hops)(TCN_STDARGS,
                                         jlong sock, jint ttl)
{
    tcn_socket_t *s = J2P(sock, tcn_socket_t *);
    UNREFERENCED_STDARGS;
    return (jint)apr_mcast_hops(s->sock, (apr_byte_t)ttl);
};

TCN_IMPLEMENT_CALL(jint, Mulicast, loopback)(TCN_STDARGS,
                                             jlong sock, jboolean opt)
{
    tcn_socket_t *s = J2P(sock, tcn_socket_t *);
    UNREFERENCED_STDARGS;
    return (jint)apr_mcast_loopback(s->sock, opt == JNI_TRUE ? 1 : 0);
};

TCN_IMPLEMENT_CALL(jint, Mulicast, ointerface)(TCN_STDARGS,
                                               jlong sock, jlong iface)
{
    tcn_socket_t *s = J2P(sock, tcn_socket_t *);
    apr_sockaddr_t *ia = J2P(iface, apr_sockaddr_t *);
    UNREFERENCED_STDARGS;
    return (jint)apr_mcast_interface(s->sock, ia);
};
