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
 * 4. The names  "The  Jakarta  Project",  "WebApp",  and  "Apache  Software *
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

/* @version $Id$ */
#include <wa.h>

/* Allocate a new request structure. */
const char *wa_ralloc(wa_request **r, wa_handler *h, void *d) {
    apr_pool_t *pool=NULL;
    wa_request *req=NULL;

    if(apr_pool_create(&pool,wa_pool)!=APR_SUCCESS)
        return("Cannot create request memory pool");
    if((req=apr_palloc(pool,sizeof(wa_request)))==NULL) {
        apr_pool_destroy(pool);
        return("Cannot allocate memory for the request structure");
    }

    if (h==NULL) return("Invalid request handler specified");

    /* Set up the server host data record */
    if((req->serv=apr_palloc(pool,sizeof(wa_hostdata)))==NULL) {
        apr_pool_destroy(pool);
        return("Cannot allocate memory for server host data structure");
    } else {
        req->serv->host=NULL;
        req->serv->addr=NULL;
        req->serv->port=-1;
    }

    /* Set up the server host data record */
    if((req->clnt=apr_palloc(pool,sizeof(wa_hostdata)))==NULL) {
        apr_pool_destroy(pool);
        return("Cannot allocate memory for client host data structure");
    } else {
        req->clnt->host=NULL;
        req->clnt->addr=NULL;
        req->clnt->port=-1;
    }

    /* Set up the headers table */
    req->hdrs=apr_table_make(pool,0);

    /* Set up all other request members */
    req->pool=pool;
    req->hand=h;
    req->data=d;
    req->meth=NULL;
    req->ruri=NULL;
    req->args=NULL;
    req->prot=NULL;
    req->schm=NULL;
    req->user=NULL;
    req->auth=NULL;
    req->clen=0;
    req->rlen=0;

    /* All done */
    *r=req;
    return(NULL);
}

/* Clean up and free the memory used by a request structure. */
const char *wa_rfree(wa_request *r) {
    if (r==NULL) return("Invalid request member");
    apr_pool_destroy(r->pool);
    return(NULL);
}


int wa_rerror_headers(void *d, const char *key, const char *val) {
    wa_request *r=(wa_request *)d;

    wa_rprintf(r,"   <DD>%s: %s</DD>\n",key,val);
    return(TRUE);
}

/* Dump an error response */
int wa_rerror(wa_request *r, int s, const char *fmt, ...) {
    va_list ap;
    char buf[1024];

    va_start(ap,fmt);
    vsnprintf(buf,1024,fmt,ap);
    va_end(ap);

    r->hand->log(r,WA_MARK,buf);

    wa_rsetstatus(r,s);
    wa_rsetctype(r,"text/html");
    wa_rcommit(r);

    wa_rprintf(r,"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\">");
    wa_rprintf(r,"<HTML>\n");
    wa_rprintf(r," <HEAD>\n");
    wa_rprintf(r,"  <TITLE>WebApp: %d Error</TITLE",s);
    wa_rprintf(r," </HEAD>\n");
    wa_rprintf(r," <BODY>\n");
    wa_rprintf(r,"  <DIV ALIGN=\"CENTER\">");
    wa_rprintf(r,"    <H1>WebApp: %d Error</H1>",s);
    wa_rprintf(r,"  </DIV>\n");
    wa_rprintf(r,"  <HR>\n");
    wa_rprintf(r,"  %s\n",buf);
    wa_rprintf(r,"  <HR>\n");
    wa_rprintf(r,"  <DL>\n");
    wa_rprintf(r,"   <DT>Your Request:</DT>\n");
    wa_rprintf(r,"   <DD>Server Host: \"%s\"</DD>\n",r->serv->host);
    wa_rprintf(r,"   <DD>Server Address: \"%s\"</DD>\n",r->serv->addr);
    wa_rprintf(r,"   <DD>Server Port: \"%d\"</DD>\n",r->serv->port);
    wa_rprintf(r,"   <DD>Client Host: \"%s\"</DD>\n",r->clnt->host);
    wa_rprintf(r,"   <DD>Client Address: \"%s\"</DD>\n",r->clnt->addr);
    wa_rprintf(r,"   <DD>Client Port: \"%d\"</DD>\n",r->clnt->port);
    wa_rprintf(r,"   <DD>Request Method: \"%s\"</DD>\n",r->meth);
    wa_rprintf(r,"   <DD>Request URI: \"%s\"</DD>\n",r->ruri);
    wa_rprintf(r,"   <DD>Request Arguments: \"%s\"</DD>\n",r->args);
    wa_rprintf(r,"   <DD>Request Protocol: \"%s\"</DD>\n",r->prot);
    wa_rprintf(r,"   <DD>Request Scheme: \"%s\"</DD>\n",r->schm);
    wa_rprintf(r,"   <DD>Request User: \"%s\"</DD>\n",r->user);
    wa_rprintf(r,"   <DD>Request Authentication Mech.: \"%s\"</DD>\n",r->auth);
    wa_rprintf(r,"   <DD>Request Content-Length: \"%d\"</DD>\n",r->clen);
    wa_rprintf(r,"   <DT>Your Headers:</DT>\n");
    apr_table_do(wa_rerror_headers,r,r->hdrs,NULL);
    wa_rprintf(r,"  </DL>\n");
    wa_rprintf(r,"  <HR>\n");
    wa_rprintf(r," </BODY>\n");
    wa_rprintf(r,"</HTML>\n");
    wa_rflush(r);

    return(s);
}

/* Invoke a request in a web application. */
int wa_rinvoke(wa_request *r, wa_application *a) {
    return(a->conn->prov->handle(r,a));
}

void wa_rlog(wa_request *r, const char *f, const int l, const char *fmt, ...) {
    va_list ap;
    char buf[1024];

    va_start(ap,fmt);
    vsnprintf(buf,1024,fmt,ap);
    va_end(ap);

    r->hand->log(r,f,l,buf);
}

void wa_rsetstatus(wa_request *r, int status) {
    r->hand->setstatus(r,status);
}

void wa_rsetctype(wa_request *r, char *type) {
    r->hand->setctype(r,type);
}

void wa_rsetheader(wa_request *r, char *name, char *value) {
    r->hand->setheader(r,name,value);
}

void wa_rcommit(wa_request *r) {
    r->hand->commit(r);
}

void wa_rflush(wa_request *r) {
    r->hand->flush(r);
}

int wa_rread(wa_request *r, char *buf, int len) {
    return(r->hand->read(r,buf,len));
}

int wa_rwrite(wa_request *r, char *buf, int len) {
    return(r->hand->write(r,buf,len));
}

int wa_rprintf(wa_request *r, const char *fmt, ...) {
    va_list ap;
    char buf[1024];
    int ret=0;

    va_start(ap,fmt);
    ret=vsnprintf(buf,1024,fmt,ap);
    va_end(ap);

    return(r->hand->write(r,buf,ret));
}
