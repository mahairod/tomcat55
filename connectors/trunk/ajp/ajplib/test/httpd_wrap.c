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

#include "apr.h"
#include "apr_lib.h"
#include "apr_strings.h"
#include "apr_buckets.h"
#include "apr_md5.h"
#include "apr_network_io.h"
#include "apr_pools.h"
#include "apr_strings.h"
#include "apr_uri.h"
#include "apr_date.h"
#include "apr_fnmatch.h"
#define APR_WANT_STRFUNC
#include "apr_want.h"
 
#include "apr_hooks.h"
#include "apr_optional_hooks.h"
#include "apr_buckets.h"

#include "httpd_wrap.h"

int AP_DECLARE_DATA ap_default_loglevel = DEFAULT_LOGLEVEL;

static const char *levels[] = {
    "emerg",
    "alert",
    "crit",
    "error",
    "warn",
    "notice",
    "info",
    "debug",
    NULL
};

static void log_error_core(const char *file, int line, int level,
                           apr_status_t status,
                           const char *fmt, va_list args)
{
    FILE *stream;
    char timstr[32];
    char errstr[MAX_STRING_LEN];
    
    /* Skip the loging for lower levels */
    if (level < 0 || level > ap_default_loglevel)
        return;
    if (level < APLOG_WARNING)
        stream = stderr;
    else
        stream = stdout;
    apr_ctime(&timstr[0], apr_time_now());
    fprintf(stream, "[%s] [%s] ", timstr, levels[level]);
    if (file && level == APLOG_DEBUG) {
#ifndef WIN32
        char *e = strrchr(file, '/');
#else
        char *e = strrchr(file, '\\');
#endif
        if (e)
            fprintf(stream, "%s (%d) ", e + 1, line);
    }

    if (status != 0) {
        if (status < APR_OS_START_EAIERR) {
            fprintf(stream, "(%d)", status);
        }
        else if (status < APR_OS_START_SYSERR) {
            fprintf(stream, "(EAI %d)", status - APR_OS_START_EAIERR);
        }
        else if (status < 100000 + APR_OS_START_SYSERR) {
            fprintf(stream, "(OS %d)", status - APR_OS_START_SYSERR);
        }
        else {
            fprintf(stream, "(os 0x%08x)", status - APR_OS_START_SYSERR);
        }
        apr_strerror(status, errstr, MAX_STRING_LEN);
        fprintf(stream, " %s ", errstr);
    }

    apr_vsnprintf(errstr, MAX_STRING_LEN, fmt, args);
    fputs(errstr, stream);
    fputs("\n", stream);    
    if (level < APLOG_WARNING)
        fflush(stream);

}

AP_DECLARE(void) ap_log_error(const char *file, int line, int level,
                              apr_status_t status, const server_rec *s,
                              const char *fmt, ...)
{
    va_list args;

    va_start(args, fmt);
    log_error_core(file, line, level, status, fmt, args);
    va_end(args);
}

AP_DECLARE(void) ap_log_perror(const char *file, int line, int level,
                               apr_status_t status, apr_pool_t *p,
                               const char *fmt, ...)
{
    va_list args;

    va_start(args, fmt);
    log_error_core(file, line, level, status, fmt, args);
    va_end(args);
}
 

AP_DECLARE(void) ap_log_rerror(const char *file, int line, int level,
                               apr_status_t status, const request_rec *r,
                               const char *fmt, ...)
{
    va_list args;

    va_start(args, fmt);
    log_error_core(file, line, level, status, fmt, args);
    va_end(args);
}

AP_DECLARE(request_rec *) ap_wrap_create_request(conn_rec *conn)
{
    request_rec *r;
    apr_pool_t *p;

    apr_pool_create(&p, conn->pool);
    apr_pool_tag(p, "request");
    r = apr_pcalloc(p, sizeof(request_rec));
    r->pool            = p;
    r->connection      = conn;
    r->server          = conn->base_server;

    r->user            = NULL;
    r->ap_auth_type    = NULL;

    r->headers_in      = apr_table_make(r->pool, 25);
    r->subprocess_env  = apr_table_make(r->pool, 25);
    r->headers_out     = apr_table_make(r->pool, 12);
    r->err_headers_out = apr_table_make(r->pool, 5);
    r->notes           = apr_table_make(r->pool, 5);


    r->status          = HTTP_REQUEST_TIME_OUT;  /* Until we get a request */
    r->the_request     = NULL;

    r->status = HTTP_OK;                         /* Until further notice. */
    return r;
}

AP_DECLARE(process_rec *) ap_wrap_create_process(int argc, const char * const *argv)
{
    process_rec *process;
    apr_pool_t *cntx;
    apr_status_t stat;

    stat = apr_pool_create(&cntx, NULL);
    if (stat != APR_SUCCESS) {
        /* XXX From the time that we took away the NULL pool->malloc mapping
         *     we have been unable to log here without segfaulting.
         */
        ap_log_error(APLOG_MARK, APLOG_ERR, stat, NULL,
                     "apr_pool_create() failed to create "
                     "initial context");
        apr_terminate();
        exit(1);
    }

    apr_pool_tag(cntx, "process");

    process = apr_palloc(cntx, sizeof(process_rec));
    process->pool = cntx;

    apr_pool_create(&process->pconf, process->pool);
    apr_pool_tag(process->pconf, "pconf");
    process->argc = argc;
    process->argv = argv;
    process->short_name = apr_filepath_name_get(argv[0]);
    return process;
}

AP_DECLARE(server_rec *) ap_wrap_create_server(process_rec *process, apr_pool_t *p)
{
    apr_status_t rv;
    server_rec *s = (server_rec *) apr_pcalloc(p, sizeof(server_rec));

    s->process = process;
    s->port = 0;
    s->server_admin = DEFAULT_ADMIN;
    s->server_hostname = NULL;
    s->loglevel = DEFAULT_LOGLEVEL;
    s->limit_req_line = DEFAULT_LIMIT_REQUEST_LINE;
    s->limit_req_fieldsize = DEFAULT_LIMIT_REQUEST_FIELDSIZE;
    s->limit_req_fields = DEFAULT_LIMIT_REQUEST_FIELDS;
    s->timeout = apr_time_from_sec(DEFAULT_TIMEOUT);
    s->keep_alive_timeout = apr_time_from_sec(DEFAULT_KEEPALIVE_TIMEOUT);
    s->keep_alive_max = DEFAULT_KEEPALIVE;
    s->keep_alive = 1;
    s->addrs = apr_pcalloc(p, sizeof(server_addr_rec));

    /* NOT virtual host; don't match any real network interface */
    rv = apr_sockaddr_info_get(&s->addrs->host_addr,
                               NULL, APR_INET, 0, 0, p);

    s->addrs->host_port = 0; /* matches any port */
    s->addrs->virthost = ""; /* must be non-NULL */

    return s;
} 

AP_DECLARE(conn_rec *) ap_run_create_connection(apr_pool_t *ptrans,
                                  server_rec *server,
                                  apr_socket_t *csd, long id, void *sbh,
                                  apr_bucket_alloc_t *alloc)
{
    apr_status_t rv;
    conn_rec *c = (conn_rec *) apr_pcalloc(ptrans, sizeof(conn_rec));

    c->sbh = sbh;

    /* Got a connection structure, so initialize what fields we can
     * (the rest are zeroed out by pcalloc).
     */
    c->notes = apr_table_make(ptrans, 5);

    c->pool = ptrans;

    /* Socket is used only for backend connections
     * Since we don't have client socket skip the 
     * creation of adresses. They will be default
     * to 127.0.0.1:0 both local and remote
     */
    if (csd) {
        if ((rv = apr_socket_addr_get(&c->local_addr, APR_LOCAL, csd))
            != APR_SUCCESS) {
                ap_log_error(APLOG_MARK, APLOG_INFO, rv, server,
                    "apr_socket_addr_get(APR_LOCAL)");
                apr_socket_close(csd);
                return NULL;
         }
         if ((rv = apr_socket_addr_get(&c->remote_addr, APR_REMOTE, csd))
                != APR_SUCCESS) {
            ap_log_error(APLOG_MARK, APLOG_INFO, rv, server,
                    "apr_socket_addr_get(APR_REMOTE)");
                apr_socket_close(csd);
            return NULL;
         }
    } 
    else {
        /* localhost should be reachable on all platforms */
        if ((rv = apr_sockaddr_info_get(&c->local_addr, "localhost",
                                        APR_UNSPEC, 0,
                                        APR_IPV4_ADDR_OK, 
                                        c->pool))
            != APR_SUCCESS) {
                ap_log_error(APLOG_MARK, APLOG_INFO, rv, server,
                    "apr_sockaddr_info_get()");
                return NULL;
         }
         c->remote_addr = c->local_addr;        
    }
    apr_sockaddr_ip_get(&c->local_ip, c->local_addr);
    apr_sockaddr_ip_get(&c->remote_ip, c->remote_addr);
    c->base_server = server;

    c->id = id;
    c->bucket_alloc = alloc;

    return c;
} 

AP_DECLARE(const char *) ap_get_remote_host(conn_rec *conn, void *dir_config,
                                            int type, int *str_is_ip)
{
    int ignored_str_is_ip;

    if (!str_is_ip) { /* caller doesn't want to know */
        str_is_ip = &ignored_str_is_ip;
    }
    *str_is_ip = 0;

    /*
     * Return the desired information; either the remote DNS name, if found,
     * or either NULL (if the hostname was requested) or the IP address
     * (if any identifier was requested).
     */
    if (conn->remote_host != NULL && conn->remote_host[0] != '\0') {
        return conn->remote_host;
    }
    else {
        if (type == REMOTE_HOST || type == REMOTE_DOUBLE_REV) {
            return NULL;
        }
        else {
            *str_is_ip = 1;
            return conn->remote_ip;
        }
    }
} 

AP_DECLARE(const char *) ap_get_server_name(request_rec *r)
{
    /* default */
    return r->server->server_hostname;
} 
