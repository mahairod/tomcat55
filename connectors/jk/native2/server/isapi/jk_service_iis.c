/* ========================================================================= *
 *                                                                           *
 *                 The Apache Software License,  Version 1.1                 *
 *                                                                           *
 *          Copyright (c) 1999-2001 The Apache Software Foundation.          *
 *                           All rights reserved.                            *
 *                                                                           *
 * ========================================================================= *
 *                                                                           *
 * Redistribution and use in source and binary forms,  with or without modi- *
 * fication, are permitted provided that the following conditions are met:   *
 *                                                                           *
 * 1. Redistributions of source code  must retain the above copyright notice *
 *    notice, this list of conditions and the following disclaimer.          *
 *                                                                           *
 * 2. Redistributions  in binary  form  must  reproduce the  above copyright *
 *    notice,  this list of conditions  and the following  disclaimer in the *
 *    documentation and/or other materials provided with the distribution.   *
 *                                                                           *
 * 3. The end-user documentation  included with the redistribution,  if any, *
 *    must include the following acknowlegement:                             *
 *                                                                           *
 *       "This product includes  software developed  by the Apache  Software *
 *        Foundation <http://www.apache.org/>."                              *
 *                                                                           *
 *    Alternately, this acknowlegement may appear in the software itself, if *
 *    and wherever such third-party acknowlegements normally appear.         *
 *                                                                           *
 * 4. The names  "The  Jakarta  Project",  "Jk",  and  "Apache  Software     *
 *    Foundation"  must not be used  to endorse or promote  products derived *
 *    from this  software without  prior  written  permission.  For  written *
 *    permission, please contact <apache@apache.org>.                        *
 *                                                                           *
 * 5. Products derived from this software may not be called "Apache" nor may *
 *    "Apache" appear in their names without prior written permission of the *
 *    Apache Software Foundation.                                            *
 *                                                                           *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES *
 * INCLUDING, BUT NOT LIMITED TO,  THE IMPLIED WARRANTIES OF MERCHANTABILITY *
 * AND FITNESS FOR  A PARTICULAR PURPOSE  ARE DISCLAIMED.  IN NO EVENT SHALL *
 * THE APACHE  SOFTWARE  FOUNDATION OR  ITS CONTRIBUTORS  BE LIABLE  FOR ANY *
 * DIRECT,  INDIRECT,   INCIDENTAL,  SPECIAL,  EXEMPLARY,  OR  CONSEQUENTIAL *
 * DAMAGES (INCLUDING,  BUT NOT LIMITED TO,  PROCUREMENT OF SUBSTITUTE GOODS *
 * OR SERVICES;  LOSS OF USE,  DATA,  OR PROFITS;  OR BUSINESS INTERRUPTION) *
 * HOWEVER CAUSED AND  ON ANY  THEORY  OF  LIABILITY,  WHETHER IN  CONTRACT, *
 * STRICT LIABILITY, OR TORT  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN *
 * ANY  WAY  OUT OF  THE  USE OF  THIS  SOFTWARE,  EVEN  IF  ADVISED  OF THE *
 * POSSIBILITY OF SUCH DAMAGE.                                               *
 *                                                                           *
 * ========================================================================= *
 *                                                                           *
 * This software  consists of voluntary  contributions made  by many indivi- *
 * duals on behalf of the  Apache Software Foundation.  For more information *
 * on the Apache Software Foundation, please see <http://www.apache.org/>.   *
 *                                                                           *
 * ========================================================================= */

/**
 * Description: IIS Jk2 Service 
 * Author:      Gal Shachor <shachor@il.ibm.com>                           
 *              Henri Gomez <hgomez@slib.fr> 
 *				Ignacio J. Ortega <nacho@apache.org>
 * Version:     $Revision$                                           
 */

// This define is needed to include wincrypt,h, needed to get client certificates
#define _WIN32_WINNT 0x0400

#include <httpext.h>
#include <httpfilt.h>
#include <wininet.h>

#include "jk_global.h"
//#include "jk_util.h"
#include "jk_map.h"
#include "jk_pool.h"
#include "jk_env.h"
#include "jk_service.h"
#include "jk_worker.h"

#include "iis.h"
//#include "jk_uri_worker_map.h"


static char *SERVER_NAME = "SERVER_NAME";
static char *SERVER_SOFTWARE = "SERVER_SOFTWARE";

static const char begin_cert [] = 
	"-----BEGIN CERTIFICATE-----\r\n";

static const char end_cert [] = 
	"-----END CERTIFICATE-----\r\n";

static const char basis_64[] =
    "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

static int base64_encode_cert_len(int len)
{
	int n = ((len + 2) / 3 * 4) + 1; // base64 encoded size
	n += (n + 63 / 64) * 2; // add CRLF's
	n += sizeof(begin_cert) + sizeof(end_cert) - 2;  // add enclosing strings.
    return n;
}

static int base64_encode_cert(char *encoded,
                              const unsigned char *string, int len)
{
    int i,c;
    char *p;
	const char *t;

    p = encoded;

	t = begin_cert;
	while (*t != '\0')
		*p++ = *t++;

    c = 0;
    for (i = 0; i < len - 2; i += 3) {
        *p++ = basis_64[(string[i] >> 2) & 0x3F];
        *p++ = basis_64[((string[i] & 0x3) << 4) |
                        ((int) (string[i + 1] & 0xF0) >> 4)];
        *p++ = basis_64[((string[i + 1] & 0xF) << 2) |
                        ((int) (string[i + 2] & 0xC0) >> 6)];
        *p++ = basis_64[string[i + 2] & 0x3F];
        c += 4;
        if ( c >= 64 ) {
            *p++ = '\r';
            *p++ = '\n';
            c = 0;
		}
    }
    if (i < len) {
        *p++ = basis_64[(string[i] >> 2) & 0x3F];
        if (i == (len - 1)) {
            *p++ = basis_64[((string[i] & 0x3) << 4)];
            *p++ = '=';
        }
        else {
            *p++ = basis_64[((string[i] & 0x3) << 4) |
                            ((int) (string[i + 1] & 0xF0) >> 4)];
            *p++ = basis_64[((string[i + 1] & 0xF) << 2)];
        }
        *p++ = '=';
        c++;
    }
    if ( c != 0 ) {
        *p++ = '\r';
        *p++ = '\n';
    }

	t = end_cert;
	while (*t != '\0')
		*p++ = *t++;

    *p++ = '\0';
    return p - encoded;
}


static char x2c(const char *what)
{
    register char digit;

    digit = ((what[0] >= 'A') ? ((what[0] & 0xdf) - 'A') + 10 : (what[0] - '0'));
    digit *= 16;
    digit += (what[1] >= 'A' ? ((what[1] & 0xdf) - 'A') + 10 : (what[1] - '0'));
    return (digit);
}

static int unescape_url(char *url)
{
    register int x, y, badesc, badpath;

    badesc = 0;
    badpath = 0;
    for (x = 0, y = 0; url[y]; ++x, ++y) {
        if (url[y] != '%')
            url[x] = url[y];
        else {
            if (!isxdigit(url[y + 1]) || !isxdigit(url[y + 2])) {
                badesc = 1;
                url[x] = '%';
            }
            else {
                url[x] = x2c(&url[y + 1]);
                y += 2;
                if (url[x] == '/' || url[x] == '\0')
                    badpath = 1;
            }
        }
    }
    url[x] = '\0';
    if (badesc)
            return BAD_REQUEST;
    else if (badpath)
            return BAD_PATH;
    else
            return 0;
}

static void getparents(char *name)
{
    int l, w;

    /* Four paseses, as per RFC 1808 */
    /* a) remove ./ path segments */

    for (l = 0, w = 0; name[l] != '\0';) {
        if (name[l] == '.' && name[l + 1] == '/' && (l == 0 || name[l - 1] == '/'))
            l += 2;
        else
            name[w++] = name[l++];
    }

    /* b) remove trailing . path, segment */
    if (w == 1 && name[0] == '.')
        w--;
    else if (w > 1 && name[w - 1] == '.' && name[w - 2] == '/')
        w--;
    name[w] = '\0';

    /* c) remove all xx/../ segments. (including leading ../ and /../) */
    l = 0;

    while (name[l] != '\0') {
        if (name[l] == '.' && name[l + 1] == '.' && name[l + 2] == '/' &&
            (l == 0 || name[l - 1] == '/')) {
            register int m = l + 3, n;

            l = l - 2;
            if (l >= 0) {
                while (l >= 0 && name[l] != '/')
                    l--;
                l++;
            }
            else
                l = 0;
            n = l;
            while ((name[n] = name[m]))
                (++n, ++m);
        }
        else
            ++l;
    }

    /* d) remove trailing xx/.. segment. */
    if (l == 2 && name[0] == '.' && name[1] == '.')
        name[0] = '\0';
    else if (l > 2 && name[l - 1] == '.' && name[l - 2] == '.' && name[l - 3] == '/') {
        l = l - 4;
        if (l >= 0) {
            while (l >= 0 && name[l] != '/')
                l--;
            l++;
        }
        else
            l = 0;
        name[l] = '\0';
    }
}

/* Apache code to escape a URL */

#define T_OS_ESCAPE_PATH	(4)

static const unsigned char test_char_table[256] = {
    0,14,14,14,14,14,14,14,14,14,15,14,14,14,14,14,14,14,14,14,
    14,14,14,14,14,14,14,14,14,14,14,14,14,0,7,6,1,6,1,1,
    9,9,1,0,8,0,0,10,0,0,0,0,0,0,0,0,0,0,8,15,
    15,8,15,15,8,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,15,15,15,7,0,7,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,15,7,15,1,14,6,6,6,6,6,6,6,6,6,6,6,6,
    6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,
    6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,
    6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,
    6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,
    6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,
    6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6 
};

#define TEST_CHAR(c, f)	(test_char_table[(unsigned)(c)] & (f))

static const char c2x_table[] = "0123456789abcdef";

static unsigned char *c2x(unsigned what, unsigned char *where)
{
    *where++ = '%';
    *where++ = c2x_table[what >> 4];
    *where++ = c2x_table[what & 0xf];
    return where;
}

static int escape_url(const char *path, char *dest, int destsize)
{
    const unsigned char *s = (const unsigned char *)path;
    unsigned char *d = (unsigned char *)dest;
    unsigned char *e = dest + destsize - 1;
    unsigned char *ee = dest + destsize - 3;
    unsigned c;

    while ((c = *s)) {
	if (TEST_CHAR(c, T_OS_ESCAPE_PATH)) {
            if (d >= ee )
                return JK_FALSE;
	    d = c2x(c, d);
	}
	else {
            if (d >= e )
                return JK_FALSE;
	    *d++ = c;
	}
	++s;
    }
    *d = '\0';
    return JK_TRUE;
}



static void write_error_response(PHTTP_FILTER_CONTEXT pfc,char *status,char * msg)
{
    char crlf[3] = { (char)13, (char)10, '\0' };
    char ctype[30];
    DWORD len = strlen(msg);

    sprintf(ctype, 
            "Content-Type:text/html%s%s", 
            crlf, 
            crlf);

    /* reject !!! */
    pfc->ServerSupportFunction(pfc, 
                               SF_REQ_SEND_RESPONSE_HEADER,
                               status,
                               (DWORD)crlf,
                               (DWORD)ctype);
    pfc->WriteClient(pfc, msg, &len, 0);
}


static int JK_METHOD jk2_service_iis_head(jk_env_t *env, jk_ws_service_t *s ){
    static char crlf[3] = { (char)13, (char)10, '\0' };
	char *reason;
    env->l->jkLog(env,env->l, JK_LOG_DEBUG, 
           "Into jk_ws_service_t::start_response\n");

    if (s->status< 100 || s->status > 1000) {
        env->l->jkLog(env,env->l, JK_LOG_ERROR, 
               "jk_ws_service_t::jk2_service_iis_head, invalid status %d\n", s->status);
        return JK_FALSE;
    }

    if (s && s->ws_private) {
        isapi_private_data_t *p = s->ws_private;
        if (!p->request_started) {
            DWORD len_of_status;
            char *status_str;
            char *headers_str;

            p->request_started = JK_TRUE;

            /*
             * Create the status line
             */
            if (!s->msg) {
                reason = "";
            } else {
				reason = s->msg;
			}
            status_str = (char *)_alloca((6 + strlen(reason)) * sizeof(char));
            sprintf(status_str, "%d %s", s->status, reason);
            len_of_status = strlen(status_str); 
        
            /*
             * Create response headers string
             */
            if (s->headers_out->size) {
                int i;
                unsigned len_of_headers;
                for(i = 0 , len_of_headers = 0 ; i < s->headers_out->size(env,s->headers_out) ; i++) {
                    len_of_headers += strlen(s->headers_out->nameAt(env,s->headers_out,i));
                    len_of_headers += strlen(s->headers_out->valueAt(env,s->headers_out,i));
                    len_of_headers += 4; /* extra for colon, space and crlf */
                }

                len_of_headers += 3;  /* crlf and terminating null char */
                headers_str = (char *)_alloca(len_of_headers * sizeof(char));
                headers_str[0] = '\0';

                for(i = 0 ; i < s->headers_out->size(env,s->headers_out) ; i++) {
                    strcat(headers_str, s->headers_out->nameAt(env,s->headers_out,i));
                    strcat(headers_str, ": ");
                    strcat(headers_str, s->headers_out->valueAt(env,s->headers_out,i));
                    strcat(headers_str, crlf);
                }
                strcat(headers_str, crlf);
            } else {
                headers_str = crlf;
            }

            if (!p->lpEcb->ServerSupportFunction(p->lpEcb->ConnID, 
                                                HSE_REQ_SEND_RESPONSE_HEADER,
                                                status_str,
                                                (LPDWORD)&len_of_status,
                                                (LPDWORD)headers_str)) {
                env->l->jkLog(env,env->l, JK_LOG_ERROR, 
                       "jk_ws_service_t::jk2_service_iis_head, ServerSupportFunction failed\n");
                return JK_FALSE;
            }       


        }
        return JK_TRUE;

    }

    env->l->jkLog(env,env->l, JK_LOG_ERROR, 
           "jk_ws_service_t::jk2_service_iis_head, NULL parameters\n");

    return JK_FALSE;
}

static int JK_METHOD jk2_service_iis_read(jk_env_t *env, jk_ws_service_t *s,
                          void *b,
                          unsigned l,
                          unsigned *a)
{
    env->l->jkLog(env,env->l, JK_LOG_DEBUG, 
           "Into jk_ws_service_t::read\n");

    if (s && s->ws_private && b && a) {
        isapi_private_data_t *p = s->ws_private;
        
        *a = 0;
        if (l) {
            char *buf = b;
            DWORD already_read = p->lpEcb->cbAvailable - p->bytes_read_so_far;
            
            if (already_read >= l) {
                memcpy(buf, p->lpEcb->lpbData + p->bytes_read_so_far, l);
                p->bytes_read_so_far += l;
                *a = l;
            } else {
                /*
                 * Try to copy what we already have 
                 */
                if (already_read > 0) {
                    memcpy(buf, p->lpEcb->lpbData + p->bytes_read_so_far, already_read);
                    buf += already_read;
                    l   -= already_read;
                    p->bytes_read_so_far = p->lpEcb->cbAvailable;
                    
                    *a = already_read;
                }
                
                /*
                 * Now try to read from the client ...
                 */
                if (p->lpEcb->ReadClient(p->lpEcb->ConnID, buf, &l)) {
                    *a += l;            
                } else {
                    env->l->jkLog(env,env->l, JK_LOG_ERROR, 
                           "jk_ws_service_t::read, ReadClient failed\n");
                    return JK_FALSE;
                }                   
            }
        }
        return JK_TRUE;
    }

    env->l->jkLog(env,env->l, JK_LOG_ERROR, 
           "jk_ws_service_t::read, NULL parameters\n");
    return JK_FALSE;
}

static int JK_METHOD jk2_service_iis_write(jk_env_t *env,jk_ws_service_t *s,
                           const void *b,
                           unsigned l)
{
    env->l->jkLog(env,env->l, JK_LOG_DEBUG, 
           "Into jk_ws_service_t::write\n");

    if (s && s->ws_private && b) {
        isapi_private_data_t *p = s->ws_private;

        if (l) {
            unsigned written = 0;           
            char *buf = (char *)b;

            if (!p->request_started) {
                jk2_service_iis_head(env, s );
            }

            while(written < l) {
                DWORD try_to_write = l - written;
                if (!p->lpEcb->WriteClient(p->lpEcb->ConnID, 
                                          buf + written, 
                                          &try_to_write, 
                                          0)) {
                    env->l->jkLog(env,env->l, JK_LOG_ERROR, 
                           "jk_ws_service_t::write, WriteClient failed\n");
                    return JK_FALSE;
                }
                written += try_to_write;
            }
        }

        return JK_TRUE;

    }

    env->l->jkLog(env,env->l, JK_LOG_ERROR, 
           "jk_ws_service_t::write, NULL parameters\n");

    return JK_FALSE;
}


int JK_METHOD jk2_service_iis_init(jk_env_t *env, jk_ws_service_t *s)
{
    if(s==NULL ) {
        return JK_FALSE;
    }

    s->head   = jk2_service_iis_head;
    s->read   = jk2_service_iis_read;
    s->write  = jk2_service_iis_write;
    s->init   = jk2_service_iis_init_ws_service;
    
    return JK_TRUE;
}


static int get_server_value(LPEXTENSION_CONTROL_BLOCK lpEcb,
                            char *name,
                            char  *buf,
                            DWORD bufsz,
                            char  *def_val)
{
    if (!lpEcb->GetServerVariable(lpEcb->ConnID, 
                                 name,
                                 buf,
                                 (LPDWORD)&bufsz)) {
        strcpy(buf, def_val);
        return JK_FALSE;
    }

    if (bufsz > 0) {
        buf[bufsz - 1] = '\0';
    }

    return JK_TRUE;
}


static int JK_METHOD jk2_service_iis_init_ws_service( struct jk_env *env, jk_ws_service_t *s,
                 struct jk_worker *w, void *serverObj )

/* */

{
    isapi_private_data_t *private_data=serverObj;
    char huge_buf[16 * 1024]; /* should be enough for all */
	char **worker_name;
	
    DWORD huge_buf_sz;

    s->jvm_route = NULL;
    
    GET_SERVER_VARIABLE_VALUE(HTTP_WORKER_HEADER_NAME, (*worker_name));           
    GET_SERVER_VARIABLE_VALUE(HTTP_URI_HEADER_NAME, s->req_uri);     
    GET_SERVER_VARIABLE_VALUE(HTTP_QUERY_HEADER_NAME, s->query_string);     

    if (s->req_uri == NULL) {
        s->query_string = private_data->lpEcb->lpszQueryString;
        *worker_name    = DEFAULT_WORKER_NAME;
        GET_SERVER_VARIABLE_VALUE("URL", s->req_uri);       
        if (unescape_url(s->req_uri) < 0)
            return JK_FALSE;
        getparents(s->req_uri);
    }
    
    GET_SERVER_VARIABLE_VALUE("AUTH_TYPE", s->auth_type);
    GET_SERVER_VARIABLE_VALUE("REMOTE_USER", s->remote_user);
    GET_SERVER_VARIABLE_VALUE("SERVER_PROTOCOL", s->protocol);
    GET_SERVER_VARIABLE_VALUE("REMOTE_HOST", s->remote_host);
    GET_SERVER_VARIABLE_VALUE("REMOTE_ADDR", s->remote_addr);
    GET_SERVER_VARIABLE_VALUE(SERVER_NAME, s->server_name);
    GET_SERVER_VARIABLE_VALUE_INT("SERVER_PORT", s->server_port, 80)
    GET_SERVER_VARIABLE_VALUE(SERVER_SOFTWARE, s->server_software);
    GET_SERVER_VARIABLE_VALUE_INT("SERVER_PORT_SECURE", s->is_ssl, 0);

    s->method           = private_data->lpEcb->lpszMethod;
    s->content_length   = private_data->lpEcb->cbTotalBytes;

    s->ssl_cert     = NULL;
    s->ssl_cert_len = 0;
    s->ssl_cipher   = NULL;
    s->ssl_session  = NULL;
	s->ssl_key_size = -1;

    jk2_map_default_create(env, &s->headers_in, s->pool );
//    s->headers_values   = NULL;
//  s->num_headers      = 0;
    
    /*
     * Add SSL IIS environment
     */
    if (s->is_ssl) {         
        char *ssl_env_names[9] = {
            "CERT_ISSUER", 
            "CERT_SUBJECT", 
            "CERT_COOKIE", 
            "HTTPS_SERVER_SUBJECT", 
            "CERT_FLAGS", 
            "HTTPS_SECRETKEYSIZE", 
            "CERT_SERIALNUMBER", 
            "HTTPS_SERVER_ISSUER", 
            "HTTPS_KEYSIZE"
        };
        char *ssl_env_values[9] = {
            NULL, 
            NULL, 
            NULL, 
            NULL, 
            NULL, 
            NULL, 
            NULL, 
            NULL, 
            NULL
        };
        unsigned i;
        unsigned num_of_vars = 0;

        for(i = 0 ; i < 9 ; i++) {
            GET_SERVER_VARIABLE_VALUE(ssl_env_names[i], ssl_env_values[i]);
            if (ssl_env_values[i]) {
                num_of_vars++;
            }
        }
        if (num_of_vars) {
            unsigned j;

			jk2_map_default_create(env, &s->attributes, s->pool );

            j = 0;
            for(i = 0 ; i < 9 ; i++) {                
                if (ssl_env_values[i]) {
                    s->attributes->put(env,s->attributes,ssl_env_names[i],ssl_env_values[i],NULL);
                    j++;
                }
            }
 			if (ssl_env_values[4] && ssl_env_values[4][0] == '1') {
				CERT_CONTEXT_EX cc;
				DWORD cc_sz = sizeof(cc);
				cc.cbAllocated = sizeof(huge_buf);
				cc.CertContext.pbCertEncoded = (BYTE*) huge_buf;
				cc.CertContext.cbCertEncoded = 0;

				if (private_data->lpEcb->ServerSupportFunction(private_data->lpEcb->ConnID,
											 (DWORD)HSE_REQ_GET_CERT_INFO_EX,                               
											 (LPVOID)&cc,NULL,NULL) != FALSE)
				{
					env->l->jkLog(env,env->l, JK_LOG_DEBUG,"Client Certificate encoding:%d sz:%d flags:%ld\n",
								cc.CertContext.dwCertEncodingType & X509_ASN_ENCODING ,
								cc.CertContext.cbCertEncoded,
								cc.dwCertificateFlags);
                    s->ssl_cert=private_data->p.alloc(env,&private_data->p,
                                base64_encode_cert_len(cc.CertContext.cbCertEncoded));

                    s->ssl_cert_len = base64_encode_cert(s->ssl_cert,
                                huge_buf,cc.CertContext.cbCertEncoded) - 1;
				}
			}
        }
    }

	huge_buf_sz = sizeof(huge_buf);         
    if (get_server_value(private_data->lpEcb,
                        "ALL_HTTP",             
                        huge_buf,           
                        huge_buf_sz,        
                        "")) {              
        unsigned cnt = 0;
        char *tmp;

        for(tmp = huge_buf ; *tmp ; tmp++) {
            if (*tmp == '\n'){
                cnt++;
            }
        }

        if (cnt) {
            char *headers_buf = private_data->p.pstrdup(env,&private_data->p, huge_buf);
            unsigned i;
            unsigned len_of_http_prefix = strlen("HTTP_");
            BOOL need_content_length_header = (s->content_length == 0);
            
            cnt -= 2; /* For our two special headers */
            /* allocate an extra header slot in case we need to add a content-length header */
/*
            s->headers_names  = jk_pool_alloc(&private_data->p, (cnt + 1) * sizeof(char *));
            s->headers_values = jk_pool_alloc(&private_data->p, (cnt + 1) * sizeof(char *));

            if (!s->headers_names || !s->headers_values || !headers_buf) {
                return JK_FALSE;
            }

 */
            for(i = 0, tmp = headers_buf ; *tmp && i < cnt ; ) {
                int real_header = JK_TRUE;
				char *name;

                /* Skipp the HTTP_ prefix to the beginning of th header name */
                tmp += len_of_http_prefix;

                if (!strnicmp(tmp, URI_HEADER_NAME, strlen(URI_HEADER_NAME)) ||
                   !strnicmp(tmp, WORKER_HEADER_NAME, strlen(WORKER_HEADER_NAME))) {
                    real_header = JK_FALSE;
                } else if(need_content_length_header &&
                   !strnicmp(tmp, CONTENT_LENGTH, strlen(CONTENT_LENGTH))) {
                    need_content_length_header = FALSE;
                    name  = tmp;
                } else if (!strnicmp(tmp, TOMCAT_TRANSLATE_HEADER_NAME,
                                          strlen(TOMCAT_TRANSLATE_HEADER_NAME))) {
                    tmp += 6; /* TOMCAT */
                    name  = tmp;
                } else {
                    name  = tmp;
                }

                while(':' != *tmp && *tmp) {
                    if ('_' == *tmp) {
                        *tmp = '-';
                    } else {
                        *tmp = tolower(*tmp);
                    }
                    tmp++;
                }
                *tmp = '\0';
                tmp++;

                /* Skip all the WS chars after the ':' to the beginning of th header value */
                while(' ' == *tmp || '\t' == *tmp || '\v' == *tmp) {
                    tmp++;
                }

                if (real_header) {
                    s->headers_in->put(env,s->headers_in,name,tmp,NULL);
                }
                
                while(*tmp != '\n' && *tmp != '\r') {
                    tmp++;
                }
                *tmp = '\0';
                tmp++;

                /* skipp CR LF */
                while(*tmp == '\n' || *tmp == '\r') {
                    tmp++;
                }

                if (real_header) {
                    i++;
                }
            }
            /* Add a content-length = 0 header if needed.
             * Ajp13 assumes an absent content-length header means an unknown,
             * but non-zero length body.
             */
            if(need_content_length_header) {
                s->headers_in->put(env,s->headers_in,"content-length","0",NULL);
                cnt++;
            }
        } else {
            /* We must have our two headers */
            return JK_FALSE;
        }
    } else {
        return JK_FALSE;
    }
    
    return JK_TRUE;
}


