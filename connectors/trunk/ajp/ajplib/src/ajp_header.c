/*
 *  Copyright 1999-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

#include "ajp_header.h"
#include "ajp.h"

static const char *response_trans_headers[] = {
    "Content-Type", 
    "Content-Language", 
    "Content-Length", 
    "Date", 
    "Last-Modified", 
    "Location", 
    "Set-Cookie", 
    "Set-Cookie2", 
    "Servlet-Engine", 
    "Status", 
    "WWW-Authenticate"
};

static const char *long_res_header_for_sc(int sc) 
{
    const char *rc = NULL;
    if(sc <= SC_RES_HEADERS_NUM && sc > 0) {
        rc = response_trans_headers[sc - 1];
    }

    return rc;
}


static apr_status_t sc_for_req_method(const char *method,
                                      unsigned char *sc) 
{
    apr_status_t rc = APR_SUCCESS;
    if(0 == strcmp(method, "GET")) {
        *sc = SC_M_GET;
    } else if(0 == strcmp(method, "POST")) {
        *sc = SC_M_POST;
    } else if(0 == strcmp(method, "HEAD")) {
        *sc = SC_M_HEAD;
    } else if(0 == strcmp(method, "PUT")) {
        *sc = SC_M_PUT;
    } else if(0 == strcmp(method, "DELETE")) {
        *sc = SC_M_DELETE;
    } else if(0 == strcmp(method, "OPTIONS")) {
        *sc = SC_M_OPTIONS;
    } else if(0 == strcmp(method, "TRACE")) {
        *sc = SC_M_TRACE;
    } else if(0 == strcmp(method, "PROPFIND")) {
        *sc = SC_M_PROPFIND;
    } else if(0 == strcmp(method, "PROPPATCH")) {
        *sc = SC_M_PROPPATCH;
    } else if(0 == strcmp(method, "MKCOL")) {
        *sc = SC_M_MKCOL;
    } else if(0 == strcmp(method, "COPY")) {
        *sc = SC_M_COPY;
    } else if(0 == strcmp(method, "MOVE")) {
        *sc = SC_M_MOVE;
    } else if(0 == strcmp(method, "LOCK")) {
        *sc = SC_M_LOCK;
    } else if(0 == strcmp(method, "UNLOCK")) {
        *sc = SC_M_UNLOCK;
    } else if(0 == strcmp(method, "ACL")) {
        *sc = SC_M_ACL;
    } else if(0 == strcmp(method, "REPORT")) {
        *sc = SC_M_REPORT;
    } else if(0 == strcmp(method, "VERSION-CONTROL")) {
        *sc = SC_M_VERSION_CONTROL;
    } else if(0 == strcmp(method, "CHECKIN")) {
        *sc = SC_M_CHECKIN;
    } else if(0 == strcmp(method, "CHECKOUT")) {
        *sc = SC_M_CHECKOUT;
    } else if(0 == strcmp(method, "UNCHECKOUT")) {
        *sc = SC_M_UNCHECKOUT;
    } else if(0 == strcmp(method, "SEARCH")) {
        *sc = SC_M_SEARCH;
    } else if(0 == strcmp(method, "MKWORKSPACE")) {
        *sc = SC_M_MKWORKSPACE;
    } else if(0 == strcmp(method, "UPDATE")) {
        *sc = SC_M_UPDATE;
    } else if(0 == strcmp(method, "LABEL")) {
        *sc = SC_M_LABEL;
    } else if(0 == strcmp(method, "MERGE")) {
        *sc = SC_M_MERGE;
    } else if(0 == strcmp(method, "BASELINE-CONTROL")) {
        *sc = SC_M_BASELINE_CONTROL;
    } else if(0 == strcmp(method, "MKACTIVITY")) {
        *sc = SC_M_MKACTIVITY;
    } else {
        rc = APR_EGENERAL;
    }

    return rc;
}

static apr_status_t sc_for_req_header(const char *header_name,
                                      apr_uint16_t *sc) 
{
    switch((tolower(header_name[0]))) {
        case 'a':
            if('c' ==tolower(header_name[1]) &&
               'c' ==tolower(header_name[2]) &&
               'e' ==tolower(header_name[3]) &&
               'p' ==tolower(header_name[4]) &&
               't' ==tolower(header_name[5])) {
                if ('-' == header_name[6]) {
                    if(!strcasecmp(header_name + 7, "charset")) {
                        *sc = SC_ACCEPT_CHARSET;
                    } else if(!strcasecmp(header_name + 7, "encoding")) {
                        *sc = SC_ACCEPT_ENCODING;
                    } else if(!strcasecmp(header_name + 7, "language")) {
                        *sc = SC_ACCEPT_LANGUAGE;
                    } else {
                        return APR_EGENERAL;
                    }
                } else if ('\0' == header_name[6]) {
                    *sc = SC_ACCEPT;
                } else {
                    return APR_EGENERAL;
                }
            } else if (!strcasecmp(header_name, "authorization")) {
                *sc = SC_AUTHORIZATION;
            } else {
                return APR_EGENERAL;
            }
        break;

        case 'c':
            if(!strcasecmp(header_name, "cookie")) {
                *sc = SC_COOKIE;
            } else if(!strcasecmp(header_name, "connection")) {
                *sc = SC_CONNECTION;
            } else if(!strcasecmp(header_name, "content-type")) {
                *sc = SC_CONTENT_TYPE;
            } else if(!strcasecmp(header_name, "content-length")) {
                *sc = SC_CONTENT_LENGTH;
            } else if(!strcasecmp(header_name, "cookie2")) {
                *sc = SC_COOKIE2;
            } else {
                return APR_EGENERAL;
            }
        break;

        case 'h':
            if(!strcasecmp(header_name, "host")) {
                *sc = SC_HOST;
            } else {
                return APR_EGENERAL;
            }
        break;

        case 'p':
            if(!strcasecmp(header_name, "pragma")) {
                *sc = SC_PRAGMA;
            } else {
                return APR_EGENERAL;
            }
        break;

        case 'r':
            if(!strcasecmp(header_name, "referer")) {
                *sc = SC_REFERER;
            } else {
                return APR_EGENERAL;
            }
        break;

        case 'u':
            if(!strcasecmp(header_name, "user-agent")) {
                *sc = SC_USER_AGENT;
            } else {
                return APR_EGENERAL;
            }
        break;

        default:
            return APR_EGENERAL;
    }

    return APR_SUCCESS;
}


/*
 * Message structure
 *
 *
AJPV13_REQUEST/AJPV14_REQUEST=
    request_prefix (1) (byte)
    method         (byte)
    protocol       (string)
    req_uri        (string)
    remote_addr    (string)
    remote_host    (string)
    server_name    (string)
    server_port    (short)
    is_ssl         (boolean)
    num_headers    (short)
    num_headers*(req_header_name header_value)

    ?context       (byte)(string)
    ?servlet_path  (byte)(string)
    ?remote_user   (byte)(string)
    ?auth_type     (byte)(string)
    ?query_string  (byte)(string)
    ?jvm_route     (byte)(string)
    ?ssl_cert      (byte)(string)
    ?ssl_cipher    (byte)(string)
    ?ssl_session   (byte)(string)
    ?ssl_key_size  (byte)(int)      via JkOptions +ForwardKeySize
    request_terminator (byte)
    ?body          content_length*(var binary)

 */

static apr_status_t ajp_marshal_into_msgb(ajp_msg_t    *msg,
                                 request_rec *r)
{
    unsigned char method;
    apr_uint32_t i, num_headers = 0;
    apr_byte_t is_ssl;
    char *remote_host;
    

    ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, r->server,
                         "Into ajp_marshal_into_msgb");

    if (!sc_for_req_method(r->method, &method)) { 
        ap_log_error(APLOG_MARK, APLOG_ERR, 0, r->server,
               "Error ajp_marshal_into_msgb - No such method %s",
               r->method);
        return APR_EGENERAL;
    }

    /* XXXX need something */
    is_ssl = (apr_byte_t) 0; /* s->is_ssl */

    if (r->headers_in && apr_table_elts(r->headers_in)) {
        const apr_array_header_t *t = apr_table_elts(r->headers_in);
        num_headers = t->nelts;
    }
    remote_host = (char *)ap_get_remote_host(r->connection, r->per_dir_config, REMOTE_HOST, NULL);

    if (ajp_msg_append_uint8(msg, AJP13_FORWARD_REQUEST)         ||
        ajp_msg_append_uint8(msg, method)                        ||
        ajp_msg_append_string(msg, r->protocol)                  ||
        ajp_msg_append_string(msg, r->uri)                       ||
        ajp_msg_append_string(msg, r->connection->remote_ip)     ||
        ajp_msg_append_string(msg, remote_host)                  ||
        ajp_msg_append_string(msg, ap_get_server_name(r))        ||
        ajp_msg_append_uint16(msg, (apr_uint16_t)r->connection->local_addr->port) ||
        ajp_msg_append_uint8(msg, is_ssl)                        ||
        ajp_msg_append_uint16(msg, (apr_uint16_t) num_headers)) {

        ap_log_error(APLOG_MARK, APLOG_ERR, 0, r->server,
               "Error ajp_marshal_into_msgb - "
               "Error appending the message begining");
        return APR_EGENERAL;
    }

    for (i = 0 ; i < num_headers ; i++) {
        apr_uint16_t sc;
        const apr_array_header_t *t = apr_table_elts(r->headers_in);
        const apr_table_entry_t *elts = (apr_table_entry_t *)t->elts;

        if (sc_for_req_header(elts[i].key, &sc)) {
            if (ajp_msg_append_uint16(msg, sc)) {
                ap_log_error(APLOG_MARK, APLOG_ERR, 0, r->server,
                       "Error ajp_marshal_into_msgb - "
                       "Error appending the header name");
                return APR_EGENERAL;
            }
        } else {
            if (ajp_msg_append_string(msg, elts[i].key)) {
                ap_log_error(APLOG_MARK, APLOG_ERR, 0, r->server,
                       "Error ajp_marshal_into_msgb - "
                       "Error appending the header name");
                return APR_EGENERAL;
            }
        }
        
        if (ajp_msg_append_string(msg, elts[i].val)) {
            ap_log_error(APLOG_MARK, APLOG_ERR, 0, r->server,
                   "Error ajp_marshal_into_msgb - "
                   "Error appending the header value");
            return APR_EGENERAL;
        }
    }

/* XXXX need to figure out how to do this
    if (s->secret) {
        if (ajp_msg_append_uint8(msg, SC_A_SECRET) ||
            ajp_msg_append_string(msg, s->secret)) {
            ap_log_error(APLOG_MARK, APLOG_ERR, 0, r->server,
                   "Error ajp_marshal_into_msgb - "
                   "Error appending secret");
            return APR_EGENERAL;
        }
    }
 */
        
    if (r->user) {
        if (ajp_msg_append_uint8(msg, SC_A_REMOTE_USER) ||
            ajp_msg_append_string(msg, r->user)) {
            ap_log_error(APLOG_MARK, APLOG_ERR, 0, r->server,
                   "Error ajp_marshal_into_msgb - "
                   "Error appending the remote user");
            return APR_EGENERAL;
        }
    }
    if (r->ap_auth_type) {
        if (ajp_msg_append_uint8(msg, SC_A_AUTH_TYPE) ||
            ajp_msg_append_string(msg, r->ap_auth_type)) {
            ap_log_error(APLOG_MARK, APLOG_ERR, 0, r->server,
                   "Error ajp_marshal_into_msgb - "
                   "Error appending the auth type");
            return APR_EGENERAL;
        }
    }
    /* XXXX  ebcdic (args converted?) */
    if (r->args) {
        if (ajp_msg_append_uint8(msg, SC_A_QUERY_STRING) ||
            ajp_msg_append_string(msg, r->args)) {
            ap_log_error(APLOG_MARK, APLOG_ERR, 0, r->server,
                   "Error ajp_marshal_into_msgb - "
                   "Error appending the query string");
            return APR_EGENERAL;
        }
    }
/* XXXX ignored for the moment
    if (s->jvm_route) {
        if (ajp_msg_append_uint8(msg, SC_A_JVM_ROUTE) ||
            ajp_msg_append_string(msg, s->jvm_route)) {
            ap_log_error(APLOG_MARK, APLOG_ERR, 0, r->server,
                   "Error ajp_marshal_into_msgb - "
                   "Error appending the jvm route");
            return APR_EGENERAL;
        }
    }
    if (s->ssl_cert_len) {
        if (ajp_msg_append_uint8(msg, SC_A_SSL_CERT) ||
            ajp_msg_append_string(msg, s->ssl_cert)) {
            ap_log_error(APLOG_MARK, APLOG_ERR, 0, r->server,
                   "Error ajp_marshal_into_msgb - "
                   "Error appending the SSL certificates\n");
            return APR_EGENERAL;
        }
    }

    if (s->ssl_cipher) {
        if (ajp_msg_append_uint8(msg, SC_A_SSL_CIPHER) ||
            ajp_msg_append_string(msg, s->ssl_cipher)) {
            ap_log_error(APLOG_MARK, APLOG_ERR, 0, r->server,
                   "Error ajp_marshal_into_msgb - "
                   "Error appending the SSL ciphers");
            return APR_EGENERAL;
        }
    }
    if (s->ssl_session) {
        if (ajp_msg_append_uint8(msg, SC_A_SSL_SESSION) ||
            ajp_msg_append_string(msg, s->ssl_session)) {
            ap_log_error(APLOG_MARK, APLOG_ERR, 0, r->server,
                   "Error ajp_marshal_into_msgb - "
                   "Error appending the SSL session");
            return APR_EGENERAL;
        }
    }
 */

    /*
     * ssl_key_size is required by Servlet 2.3 API
     * added support only in ajp14 mode
     * JFC removed: ae->proto == AJP14_PROTO
     */
 /* XXXX ignored for the moment
    if (s->ssl_key_size != -1) {
        if (ajp_msg_append_uint8(msg, SC_A_SSL_KEY_SIZE) ||
            ajp_msg_append_uint16(msg, (unsigned short) s->ssl_key_size)) {
            ap_log_error(APLOG_MARK, APLOG_ERR, 0, r->server,
                   "Error ajp_marshal_into_msgb - "
                   "Error appending the SSL key size");
            return APR_EGENERAL;
        }
    }
 */

 /* XXXX ignored for the moment
    if (s->num_attributes > 0) {
        for (i = 0 ; i < s->num_attributes ; i++) {
            if (ajp_msg_append_uint8(msg, SC_A_REQ_ATTRIBUTE)       ||
                ajp_msg_append_string(msg, s->attributes_names[i]) ||
                ajp_msg_append_string(msg, s->attributes_values[i])) {
                ap_log_error(APLOG_MARK, APLOG_ERR, 0, r->server,
                      "Error ajp_marshal_into_msgb - "
                      "Error appending attribute %s=%s",
                      s->attributes_names[i], s->attributes_values[i]);
                return APR_EGENERAL;
            }
        }
    }
  */

    if (ajp_msg_append_uint8(msg, SC_A_ARE_DONE)) {
        ap_log_error(APLOG_MARK, APLOG_ERR, 0, r->server,
               "Error ajp_marshal_into_msgb - "
               "Error appending the message end");
        return APR_EGENERAL;
    }
    
    ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, r->server,
           "ajp_marshal_into_msgb - Done");
    return APR_SUCCESS;
}

/*
AJPV13_RESPONSE/AJPV14_RESPONSE:=
    response_prefix (2)
    status          (short)
    status_msg      (short)
    num_headers     (short)
    num_headers*(res_header_name header_value)
    *body_chunk
    terminator      boolean <! -- recycle connection or not  -->

req_header_name := 
    sc_req_header_name | (string)

res_header_name := 
    sc_res_header_name | (string)

header_value :=
    (string)

body_chunk :=
    length  (short)
    body    length*(var binary)

 */


static apr_status_t ajp_unmarshal_response(ajp_msg_t   *msg,
                                  request_rec  *r)
{
    apr_uint16_t status;
    apr_status_t rc;
    char *ptr;
    apr_uint16_t  num_headers;
    int i;

    rc = ajp_msg_get_uint16(msg,&status);

    if (rc != APR_SUCCESS) {
         ap_log_error(APLOG_MARK, APLOG_ERR, 0, r->server,
               "Error ajp_unmarshal_response - Null status");
        return APR_EGENERAL;
    }
    r->status = status;

    rc = ajp_msg_get_string(msg,&ptr);
    if (rc == APR_SUCCESS) {
        r->status_line = apr_pstrdup(r->connection->pool,ptr);
#if defined(AS400) || defined(_OSD_POSIX)
        ap_xlate_proto_from_ascii(r->status_line, strlen(r->status_line));
#endif
    } else {
        r->status_line = NULL;
    }

    ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, r->server,
           "ajp_unmarshal_response: status = %d", status);

    rc = ajp_msg_get_uint16(msg,&num_headers);
    if (rc == APR_SUCCESS) {
        r->headers_out = apr_table_make(r->pool,num_headers);
    } else {
        r->headers_out = NULL;
        num_headers = 0;
    }

    ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, r->server,
           "ajp_unmarshal_response: Number of headers is = %d",
           num_headers);

    for(i = 0 ; i < (int) num_headers ; i++) {
        apr_uint16_t name;
        char *stringname;
        char *value;
        rc  = ajp_msg_peek_uint16(msg, &name);
        if (rc != APR_SUCCESS) {
            return APR_EGENERAL;
        }
                
        if ((name & 0XFF00) == 0XA000) {
            ajp_msg_peek_uint16(msg, &name);
            name = name & 0X00FF;
            if (name <= SC_RES_HEADERS_NUM) {
                stringname = (char *)long_res_header_for_sc(name);
            } else {
                ap_log_error(APLOG_MARK, APLOG_ERR, 0, r->server,
                       "Error ajp_unmarshal_response - "
                       "No such sc (%d)",
                       name);
                return APR_EGENERAL;
            }
        } else {
            rc = ajp_msg_get_string(msg,&stringname);
            if (rc != APR_SUCCESS) {
                ap_log_error(APLOG_MARK, APLOG_ERR, 0, r->server,
                       "Error ajp_unmarshal_response - "
                       "Null header name");
                return APR_EGENERAL;
            }
#if defined(AS400) || defined(_OSD_POSIX)
            ap_xlate_proto_from_ascii(stringname, strlen(stringname));
#endif
        }

        rc = ajp_msg_get_string(msg,&value);
        if (rc != APR_SUCCESS) {
            ap_log_error(APLOG_MARK, APLOG_ERR, 0, r->server,
                   "Error ajp_unmarshal_response - "
                   "Null header value");
            return APR_EGENERAL;
        }

#if defined(AS400) || defined(_OSD_POSIX)
        ap_xlate_proto_from_ascii(value,strlen(value));
#endif
        ap_log_error(APLOG_MARK, APLOG_ERR, 0, r->server,
               "ajp_unmarshal_response: Header[%d] [%s] = [%s]\n", 
                       i, stringname, value);
        apr_table_add(r->headers_out, stringname, value);
    }

    return APR_SUCCESS;
}
