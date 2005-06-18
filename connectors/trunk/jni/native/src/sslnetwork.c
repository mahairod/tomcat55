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

/** SSL network wrapper
 *
 * @author Mladen Turk
 * @version $Revision$, $Date$
 */

#include "tcn.h"
#include "apr_thread_mutex.h"
#include "apr_poll.h"


#ifdef HAVE_OPENSSL
#include "ssl_private.h"

#ifdef TCN_DO_STATISTICS
#include "apr_atomic.h"

static volatile apr_uint32_t ssl_created  = 0;
static volatile apr_uint32_t ssl_closed   = 0;
static volatile apr_uint32_t ssl_cleared  = 0;
static volatile apr_uint32_t ssl_accepted = 0;

void ssl_network_dump_statistics()
{
    fprintf(stderr, "SSL Network Statistics ..\n");
    fprintf(stderr, "Sockets created         : %d\n", ssl_created);
    fprintf(stderr, "Sockets accepted        : %d\n", ssl_accepted);
    fprintf(stderr, "Sockets closed          : %d\n", ssl_closed);
    fprintf(stderr, "Sockets cleared         : %d\n", ssl_cleared);
}

#endif

static int ssl_smart_shutdown(SSL *ssl, int shutdown_type)
{
    int i;
    int rc = 0;

    switch (shutdown_type) {
        case SSL_SHUTDOWN_TYPE_UNCLEAN:
            /* perform no close notify handshake at all
             * (violates the SSL/TLS standard!)
             */
            shutdown_type = SSL_SENT_SHUTDOWN|SSL_RECEIVED_SHUTDOWN;
        break;
        case SSL_SHUTDOWN_TYPE_ACCURATE:
            /* send close notify and wait for clients close notify
             * (standard compliant, but usually causes connection hangs)
             */
            shutdown_type = 0;
        break;
        default:
            /*
             * case SSL_SHUTDOWN_TYPE_UNSET:
             * case SSL_SHUTDOWN_TYPE_STANDARD:
             * send close notify, but don't wait for clients close notify
             * (standard compliant and safe, so it's the DEFAULT!)
             */
            shutdown_type = SSL_RECEIVED_SHUTDOWN;
        break;
    }

    SSL_set_shutdown(ssl, shutdown_type);
    /*
     * Repeat the calls, because SSL_shutdown internally dispatches through a
     * little state machine. Usually only one or two interation should be
     * needed, so we restrict the total number of restrictions in order to
     * avoid process hangs in case the client played bad with the socket
     * connection and OpenSSL cannot recognize it.
     *  max 2x pending + 2x data = 4
     */
    for (i = 0; i < 4; i++) {
        if ((rc = SSL_shutdown(ssl)))
            break;
    }
    return rc;
}

static apr_status_t ssl_cleanup(void *data)
{
    tcn_ssl_conn_t *con = (tcn_ssl_conn_t *)data;

    if (con) {
        if (con->ssl) {
            ssl_smart_shutdown(con->ssl, con->shutdown_type);
            SSL_free(con->ssl);
            con->ssl = NULL;
        }
        if (con->peer) {
            X509_free(con->peer);
            con->peer = NULL;
        }
    }

#ifdef TCN_DO_STATISTICS
    apr_atomic_inc32(&ssl_cleared);
#endif
    return APR_SUCCESS;
}

static tcn_ssl_conn_t *ssl_create(JNIEnv *env, tcn_ssl_ctxt_t *ctx, apr_pool_t *pool)
{
    tcn_ssl_conn_t *con;
    SSL *ssl;

    if ((con = apr_pcalloc(pool, sizeof(tcn_ssl_conn_t))) == NULL) {
        tcn_ThrowAPRException(env, apr_get_os_error());
        return NULL;
    }
    if ((ssl = SSL_new(ctx->ctx)) == NULL) {
        char err[256];
        ERR_error_string(ERR_get_error(), err);
        tcn_Throw(env, "SSL_new failed (%s)", err);
        con = NULL;
        return NULL;
    }
    SSL_clear(ssl);
    con->pool = pool;
    con->ctx  = ctx;
    con->ssl  = ssl;
    con->shutdown_type = ctx->shutdown_type;
    apr_pollset_create(&(con->pollset), 1, pool, 0);

    SSL_set_app_data(ssl, (void *)con);

    if (ctx->mode) {
        /*
         *  Configure callbacks for SSL connection
         */
        SSL_set_tmp_rsa_callback(ssl, SSL_callback_tmp_RSA);
        SSL_set_tmp_dh_callback(ssl,  SSL_callback_tmp_DH);
        SSL_set_session_id_context(ssl, &(ctx->context_id[0]),
                                   MD5_DIGEST_LENGTH);
    }
    SSL_set_verify_result(ssl, X509_V_OK);
    SSL_rand_seed(ctx->rand_file);

#ifdef TCN_DO_STATISTICS
    ssl_created++;
#endif
    return con;
}

static apr_status_t wait_for_io_or_timeout(tcn_ssl_conn_t *con,
                                           int for_what)
{
    apr_interval_time_t timeout;
    apr_pollfd_t pfd;
    int type;
    apr_status_t status;

    /* Figure out the the poll direction */
    switch (for_what) {
        case SSL_ERROR_WANT_WRITE:
        case SSL_ERROR_WANT_CONNECT:
        case SSL_ERROR_WANT_ACCEPT:
            type = APR_POLLOUT;
        break;
        case SSL_ERROR_WANT_READ:
            type = APR_POLLIN;
        break;
        default:
            return APR_EINVAL;
        break;
    }

    apr_socket_timeout_get(con->sock, &timeout);
    pfd.desc_type = APR_POLL_SOCKET;
    pfd.desc.s    = con->sock;
    pfd.reqevents = type;

    /* Remove the object if it was in the pollset, then add in the new
     * object with the correct reqevents value. Ignore the status result
     * on the remove, because it might not be in there (yet).
     */
    apr_pollset_remove(con->pollset, &pfd);

    /* ### check status code */
    apr_pollset_add(con->pollset, &pfd);

    do {
        int numdesc;
        const apr_pollfd_t *pdesc;

        status = apr_pollset_poll(con->pollset, timeout, &numdesc, &pdesc);
        if (numdesc == 1 && (pdesc[0].rtnevents & type) != 0)
            return APR_SUCCESS;
    } while (APR_STATUS_IS_EINTR(status));

    return status;
}

static apr_status_t APR_THREAD_FUNC
ssl_socket_shutdown(apr_socket_t *sock, apr_shutdown_how_e how)
{
    apr_status_t rv = APR_SUCCESS;
    tcn_ssl_conn_t *con = (tcn_ssl_conn_t *)sock;

    if (con->ssl) {
        if (how < 1)
            how = con->shutdown_type;
        rv = ssl_smart_shutdown(con->ssl, how);
        /* TODO: Translate OpenSSL Error codes */
        SSL_free(con->ssl);
        con->ssl = NULL;
    }
    return rv;
}

static apr_status_t APR_THREAD_FUNC
ssl_socket_close(apr_socket_t *sock)
{
    tcn_ssl_conn_t *con = (tcn_ssl_conn_t *)sock;
    apr_status_t rv = APR_SUCCESS;

#ifdef TCN_DO_STATISTICS
    apr_atomic_inc32(&ssl_closed);
#endif
    if (con->ssl) {
        rv = ssl_smart_shutdown(con->ssl, con->shutdown_type);
        SSL_free(con->ssl);
        con->ssl = NULL;
    }
    if (con->peer) {
        X509_free(con->peer);
        con->peer = NULL;
    }
    return rv;
}

TCN_IMPLEMENT_CALL(jint, SSLSocket, handshake)(TCN_STDARGS, jlong sock)
{
    tcn_socket_t *ss = J2P(sock, tcn_socket_t *);
    tcn_ssl_conn_t *con;
    int s;
    apr_status_t rv;
    X509 *peer;

    UNREFERENCED_STDARGS;
    TCN_ASSERT(sock != 0);
    if (ss->type != TCN_SOCKET_SSL)
        return APR_EINVAL;
    con = (tcn_ssl_conn_t *)ss->opaque;
    while (!SSL_is_init_finished(con->ssl)) {
        if ((s = SSL_do_handshake(con->ssl)) <= 0) {
            int i = SSL_get_error(con->ssl, s);
            switch (i) {
                case SSL_ERROR_NONE:
                    return APR_SUCCESS;
                break;
                case SSL_ERROR_WANT_READ:
                case SSL_ERROR_WANT_WRITE:
                    if ((rv = wait_for_io_or_timeout(con, i)) != APR_SUCCESS) {
                        return rv;
                    }
                break;
                case SSL_ERROR_SYSCALL:
                    s = apr_get_netos_error();
                    if (!APR_STATUS_IS_EAGAIN(s) &&
                        !APR_STATUS_IS_EINTR(s)) {
                        con->shutdown_type = SSL_SHUTDOWN_TYPE_STANDARD;
                        return s;
                    }
                break;
                default:
                    /*
                    * Anything else is a fatal error
                    */
                    con->shutdown_type = SSL_SHUTDOWN_TYPE_STANDARD;
                    return SSL_TO_APR_ERROR(i);
                break;
            }
        }
        /*
        * Check for failed client authentication
        */
        if (SSL_get_verify_result(con->ssl) != X509_V_OK) {
            /* TODO: Log SSL client authentication failed */
            con->shutdown_type = SSL_SHUTDOWN_TYPE_STANDARD;
            /* TODO: Figure out the correct return value */
            return APR_EGENERAL;
        }

        /*
         * Remember the peer certificate
         */
        if ((peer = SSL_get_peer_certificate(con->ssl)) != NULL) {
            if (con->peer)
                X509_free(con->peer);
            con->peer = peer;
        }
    }
    return APR_SUCCESS;
}

static apr_status_t APR_THREAD_FUNC
ssl_socket_recv(apr_socket_t *sock, char *buf, apr_size_t *len)
{
    tcn_ssl_conn_t *con = (tcn_ssl_conn_t *)sock;
    int s, rd = (int)(*len);
    apr_status_t rv = APR_SUCCESS;

    for (;;) {
        if ((s = SSL_read(con->ssl, buf, rd)) <= 0) {
            int i = SSL_get_error(con->ssl, s);
            /* Special case if the "close notify" alert send by peer */
            if (s == 0 && (con->ssl->shutdown & SSL_RECEIVED_SHUTDOWN)) {
                *len = 0;
                return APR_EOF;
            }
            switch (i) {
                case SSL_ERROR_ZERO_RETURN:
                    *len = 0;
                    return APR_EOF;
                break;
                case SSL_ERROR_WANT_READ:
                case SSL_ERROR_WANT_WRITE:
                    if ((rv = wait_for_io_or_timeout(con, i)) != APR_SUCCESS) {
                        return rv;
                    }
                break;
                case SSL_ERROR_SYSCALL:
                    s = apr_get_netos_error();
                    if (!APR_STATUS_IS_EAGAIN(s) &&
                        !APR_STATUS_IS_EINTR(s)) {
                        con->shutdown_type = SSL_SHUTDOWN_TYPE_STANDARD;
                        return s;
                    }
                break;
                default:
                    return apr_get_netos_error();
                break;
            }
        }
        else {
            *len = s;
            break;
        }
    }
    return rv;
}

static apr_status_t APR_THREAD_FUNC
ssl_socket_send(apr_socket_t *sock, const char *buf,
                apr_size_t *len)
{
    tcn_ssl_conn_t *con = (tcn_ssl_conn_t *)sock;
    int s, rd = (int)(*len);
    apr_status_t rv = APR_SUCCESS;

    for (;;) {
        if ((s = SSL_write(con->ssl, buf, rd)) <= 0) {
            int i = SSL_get_error(con->ssl, s);
            switch (i) {
                case SSL_ERROR_ZERO_RETURN:
                    *len = 0;
                    return APR_EOF;
                break;
                case SSL_ERROR_WANT_READ:
                case SSL_ERROR_WANT_WRITE:
                    if ((rv = wait_for_io_or_timeout(con, i)) != APR_SUCCESS) {
                        return rv;
                    }
                break;
                case SSL_ERROR_SYSCALL:
                    s = apr_get_netos_error();
                    if (!APR_STATUS_IS_EAGAIN(s) &&
                        !APR_STATUS_IS_EINTR(s)) {
                        con->shutdown_type = SSL_SHUTDOWN_TYPE_STANDARD;
                        return s;
                    }
                break;
                default:
                    return apr_get_netos_error();
                break;
            }
        }
        else {
            *len = s;
            break;
        }
    }
    return rv;
}

static apr_status_t APR_THREAD_FUNC
ssl_socket_sendv(apr_socket_t *sock,
                 const struct iovec *vec,
                 apr_int32_t nvec, apr_size_t *len)
{
    tcn_ssl_conn_t *con = (tcn_ssl_conn_t *)sock;
    apr_status_t rv;
    apr_size_t readed = 0;
    apr_int32_t i;

    for (i = 0; i < nvec; i++) {
        apr_size_t rd = vec[i].iov_len;
        if ((rv = ssl_socket_send((apr_socket_t *)con,
                                  vec[i].iov_base, &rd)) != APR_SUCCESS) {
            *len = readed;
            return rv;
        }
        readed += rd;
    }
    *len = readed;
    return APR_SUCCESS;
}


TCN_IMPLEMENT_CALL(jint, SSLSocket, attach)(TCN_STDARGS, jlong ctx,
                                            jlong sock)
{
    tcn_ssl_ctxt_t *c = J2P(ctx, tcn_ssl_ctxt_t *);
    tcn_socket_t *s   = J2P(sock, tcn_socket_t *);
    tcn_ssl_conn_t *con;
    apr_os_sock_t  oss;
    apr_status_t rv;

    UNREFERENCED(o);
    TCN_ASSERT(ctx != 0);
    TCN_ASSERT(sock != 0);

    if ((rv = apr_os_sock_get(&oss, s->sock)) != APR_SUCCESS)
        return rv;
    if ((con = ssl_create(e, c, s->pool)) == NULL)
        return APR_EGENERAL;
    con->sock = s->sock;

    SSL_set_fd(con->ssl, (int)oss);
    if (c->mode)
        SSL_set_accept_state(con->ssl);
    else
        SSL_set_connect_state(con->ssl);
    /* Change socket type */
    s->type     = TCN_SOCKET_SSL;
    s->cleanup  = ssl_cleanup;
    s->recv     = ssl_socket_recv;
    s->send     = ssl_socket_send;
    s->sendv    = ssl_socket_sendv;
    s->shutdown = ssl_socket_shutdown;
    s->close    = ssl_socket_close;
    s->opaque   = con;

    return APR_SUCCESS;
}

#else
/* OpenSSL is not supported
 * If someday we make OpenSSL optional
 * APR_ENOTIMPL will go here
 */
#error "No OpenSSL Toolkit defined."
#endif
