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

/***************************************************************************
 * Description: Apache 2 plugin for Jakarta/Tomcat                         *
 * Author:      Gal Shachor <shachor@il.ibm.com>                           *
 *                 Henri Gomez <hgomez@slib.fr>                               *
 * Version:     $Revision$                                           *
 ***************************************************************************/

/*
 * mod_jk: keeps all servlet/jakarta related ramblings together.
 */
#include "apu_compat.h"
#include "ap_config.h"
#include "apr_lib.h"
#include "apr_date.h"
#include "apr_strings.h"

#include "httpd.h"
#include "http_config.h"
#include "http_request.h"
#include "http_core.h"
#include "http_protocol.h"
#include "http_main.h"
#include "http_log.h"

#include "util_script.h"
/*
 * Jakarta (jk_) include files
 */
#include "jk_global.h"
#include "jk_map.h"
#include "jk_pool.h"
#include "jk_env.h"
#include "jk_service.h"
#include "jk_worker.h"
#include "jk_workerEnv.h"
#include "jk_uriMap.h"
#include "jk_requtil.h"

#include "jk_apache2.h"

#define JK_HANDLER          ("jakarta-servlet")
#define JK_CTL_HANDLER      ("jakarta-ctl")
#define JK_MAGIC_TYPE       ("application/x-jakarta-servlet")

module AP_MODULE_DECLARE_DATA jk_module;

/* ==================== Options setters ==================== */

/*
 * The JK module command processors
 *
 * The below are all installed so that Apache calls them while it is
 * processing its config files.  This allows configuration info to be
 * copied into a jk_server_conf_t object, which is then used for request
 * filtering/processing.
 *
 * See jk_cmds definition below for explanations of these options.
 */


/*
 * JkMountCopy directive handling
 *
 * JkMountCopy On/Off
 */
static const char *jk_set_mountcopy(cmd_parms *cmd, 
                                    void *dummy, 
                                    int flag) 
{
    server_rec *s = cmd->server;
    jk_workerEnv_t *workerEnv =
        (jk_workerEnv_t *)ap_get_module_config(s->module_config, &jk_module);
    
    /* Set up our value */
    workerEnv->mountcopy = flag ? JK_TRUE : JK_FALSE;

    return NULL;
}

/*
 * JkMount directive handling
 *
 * JkMount URI(context) worker
 */
static const char *jk_mount_context(cmd_parms *cmd, void *dummy, 
                                    const char *context,
                                    const char *worker,
                                    const char *maybe_cookie)
{
    server_rec *s = cmd->server;
    jk_workerEnv_t *workerEnv =
        (jk_workerEnv_t *)ap_get_module_config(s->module_config, &jk_module);
    
    if (context[0]!='/') return "Context must start with /";

    workerEnv->init_data->put( NULL, workerEnv->init_data,
                               ap_pstrdup(cmd->pool,context),
                               ap_pstrdup(cmd->pool,worker),
                               NULL );

    return NULL;
}

/** XXX This should be JkWebapp, it 'defines' the app 
 */
static const char *jk_set_worker(cmd_parms *cmd, void *per_dir, 
                                 const char *workerName)
{
    jk_uriEnv_t *uriEnv=(jk_uriEnv_t *)per_dir;
    jk_webapp_t *webapp;

    server_rec *s = cmd->server;
    
    jk_workerEnv_t *workerEnv =
        (jk_workerEnv_t *)ap_get_module_config(s->module_config, &jk_module);

    /* Do we know the url ? */
    webapp=workerEnv->createWebapp( workerEnv->globalEnv, workerEnv,
                                    NULL, NULL, NULL );
    uriEnv->webapp=webapp;
    webapp->workerName=ap_pstrdup(cmd->pool, workerName);
    
    uriEnv->workerEnv=workerEnv;
    
    fprintf(stderr, "XXX Set worker %p %s %s dir=%s args=%s\n",
            uriEnv, workerName, cmd->path, cmd->directive->directive,
            cmd->directive->args);

    return NULL;
}


/*
 * JkWorkersFile Directive Handling
 *
 * JkWorkersFile file
 */
static const char *jk_set_worker_file(cmd_parms *cmd, void *dummy, 
                                      const char *worker_file)
{
    server_rec *s = cmd->server;
    struct stat statbuf;
    jk_env_t *env;

    jk_workerEnv_t *workerEnv =
        (jk_workerEnv_t *)ap_get_module_config(s->module_config, &jk_module);
    
    /* we need an absolut path (ap_server_root_relative does the ap_pstrdup) */
    workerEnv->worker_file = ap_server_root_relative(cmd->pool,worker_file);

    env=workerEnv->globalEnv;
    
    if (workerEnv->worker_file == NULL)
        return "JkWorkersFile file_name invalid";

    if (stat(workerEnv->worker_file, &statbuf) == -1)
        return "Can't find the workers file specified";

    /** Read worker files
     */
    env->l->jkLog(env, env->l, JK_LOG_DEBUG, "Reading map %s %d\n",
                  workerEnv->worker_file,
                  workerEnv->init_data->size(env, workerEnv->init_data) );
    
    if( workerEnv->worker_file != NULL ) {
        int err=jk_map_readFileProperties(env, workerEnv->init_data,
                                          workerEnv->worker_file);
        if( err==JK_TRUE ) {
            env->l->jkLog(env, env->l, JK_LOG_DEBUG, 
                          "Read map %s %d\n", workerEnv->worker_file,
                          workerEnv->init_data->size( env, workerEnv->init_data ) );
        } else {
            env->l->jkLog(env, env->l, JK_LOG_ERROR, "Error reading map %s %d\n",
                          workerEnv->worker_file,
                          workerEnv->init_data->size( env, workerEnv->init_data ) );
        }
    }

    return NULL;
}

/*
 * JkSet name value
 */
static const char *jk_worker_property(cmd_parms *cmd,
                                      void *dummy,
                                      const char *name,
                                      const char *value)
{
    server_rec *s = cmd->server;
    struct stat statbuf;
    char *oldv;
    int rc;
    jk_env_t *env;

    jk_workerEnv_t *workerEnv =
        (jk_workerEnv_t *)ap_get_module_config(s->module_config, &jk_module);
    
    jk_map_t *m=workerEnv->init_data;
    
    env=workerEnv->globalEnv;
    
    value = jk_map_replaceProperties(env, m, m->pool, value);

    oldv = jk_map_getString(env, m, name, NULL);

    if(oldv) {
        char *tmpv = apr_palloc( cmd->pool,
                                strlen(value) + strlen(oldv) + 3);
        if(tmpv) {
            char sep = '*';
            if(jk_is_some_property(env, name, "path")) {
                sep = PATH_SEPERATOR;
            } else if(jk_is_some_property(env, name, "cmd_line")) {
                sep = ' ';
            }
            
            sprintf(tmpv, "%s%c%s", 
                    oldv, sep, value);
        }                                
        value = tmpv;
    } else {
        value = ap_pstrdup( cmd->pool, value);
    }
    
    if(value) {
        m->put(env, m,
               ap_pstrdup( cmd->pool, name),
               ap_pstrdup( cmd->pool, value),
               NULL);
        /*printf("Setting %s %s\n", name, value);*/
    } 
    return NULL;
}


/*
 * JkLogFile Directive Handling
 *
 * JkLogFile file
 */
static const char *jk_set_log_file(cmd_parms *cmd, 
                                   void *dummy, 
                                   const char *log_file)
{
    server_rec *s = cmd->server;
    jk_workerEnv_t *workerEnv =
        (jk_workerEnv_t *)ap_get_module_config(s->module_config, &jk_module);
    char *logFileA;

    /* we need an absolut path */
    logFileA = ap_server_root_relative(cmd->pool,log_file);

    if (logFileA == NULL)
        return "JkLogFile file_name invalid";

    workerEnv->init_data->put( NULL, workerEnv->init_data, "logger.file.name", logFileA, NULL);
 
    return NULL;
}

/*
 * JkLogLevel Directive Handling
 *
 * JkLogLevel debug/info/error/emerg
 */

static const char *jk_set_log_level(cmd_parms *cmd, 
                                    void *dummy, 
                                    const char *log_level)
{
    server_rec *s = cmd->server;
    jk_env_t *env;
    jk_workerEnv_t *workerEnv =
        (jk_workerEnv_t *)ap_get_module_config(s->module_config, &jk_module);

    env=workerEnv->globalEnv;
    
    workerEnv->init_data->put( env, workerEnv->init_data, "logger.file.level",
                               ap_pstrdup(cmd->pool, log_level), NULL);

    if(0 == strcasecmp(log_level, JK_LOG_INFO_VERB)) {
        env->l->level=JK_LOG_INFO_LEVEL;
    }
    if(0 == strcasecmp(log_level, JK_LOG_DEBUG_VERB)) {
        env->l->level=JK_LOG_DEBUG_LEVEL;
    }
    
    return NULL;
}

/*
 * JkLogStampFormat Directive Handling
 *
 * JkLogStampFormat "[%a %b %d %H:%M:%S %Y] "
 */
static const char * jk_set_log_fmt(cmd_parms *cmd,
                      void *dummy,
                      const char * log_format)
{
    server_rec *s = cmd->server;
    jk_workerEnv_t *workerEnv =
        (jk_workerEnv_t *)ap_get_module_config(s->module_config, &jk_module);

    workerEnv->init_data->put( NULL, workerEnv->init_data, "logger.file.timeFormat",
                               ap_pstrdup(cmd->pool,log_format), NULL);
    return NULL;
}

/*
 * JkExtractSSL Directive Handling
 *
 * JkExtractSSL On/Off
 */

static const char *jk_set_enable_ssl(cmd_parms *cmd,
                                     void *dummy,
                                     int flag)
{
    server_rec *s = cmd->server;
    jk_workerEnv_t *workerEnv =
        (jk_workerEnv_t *)ap_get_module_config(s->module_config, &jk_module);
   
    /* Set up our value */
    workerEnv->ssl_enable = flag ? JK_TRUE : JK_FALSE;

    return NULL;
}

/*
 * JkHTTPSIndicator Directive Handling
 *
 * JkHTTPSIndicator HTTPS
 */

static const char *jk_set_https_indicator(cmd_parms *cmd,
                                          void *dummy,
                                          const char *indicator)
{
    server_rec *s = cmd->server;
    jk_workerEnv_t *workerEnv =
        (jk_workerEnv_t *)ap_get_module_config(s->module_config, &jk_module);

    workerEnv->https_indicator = ap_pstrdup(cmd->pool,indicator);

    return NULL;
}

/*
 * JkCERTSIndicator Directive Handling
 *
 * JkCERTSIndicator SSL_CLIENT_CERT
 */

static const char *jk_set_certs_indicator(cmd_parms *cmd,
                                          void *dummy,
                                          const char *indicator)
{
    server_rec *s = cmd->server;
    jk_workerEnv_t *workerEnv =
        (jk_workerEnv_t *)ap_get_module_config(s->module_config, &jk_module);

    workerEnv->certs_indicator = ap_pstrdup(cmd->pool,indicator);

    return NULL;
}

/*
 * JkCIPHERIndicator Directive Handling
 *
 * JkCIPHERIndicator SSL_CIPHER
 */

static const char *jk_set_cipher_indicator(cmd_parms *cmd,
                                           void *dummy,
                                           const char *indicator)
{
    server_rec *s = cmd->server;
    jk_workerEnv_t *workerEnv =
        (jk_workerEnv_t *)ap_get_module_config(s->module_config, &jk_module);

    workerEnv->cipher_indicator = ap_pstrdup(cmd->pool,indicator);

    return NULL;
}

/*
 * JkSESSIONIndicator Directive Handling
 *
 * JkSESSIONIndicator SSL_SESSION_ID
 */

static const char *jk_set_session_indicator(cmd_parms *cmd,
                                           void *dummy,
                                           const char *indicator)
{
    server_rec *s = cmd->server;
    jk_workerEnv_t *workerEnv =
        (jk_workerEnv_t *)ap_get_module_config(s->module_config, &jk_module);

    workerEnv->session_indicator = ap_pstrdup(cmd->pool,indicator);

    return NULL;
}

/*
 * JkKEYSIZEIndicator Directive Handling
 *
 * JkKEYSIZEIndicator SSL_CIPHER_USEKEYSIZE
 */

static const char *jk_set_key_size_indicator(cmd_parms *cmd,
                                           void *dummy,
                                           const char *indicator)
{
    server_rec *s = cmd->server;
    jk_workerEnv_t *workerEnv =
        (jk_workerEnv_t *)ap_get_module_config(s->module_config, &jk_module);

    workerEnv->key_size_indicator = ap_pstrdup(cmd->pool,indicator);

    return NULL;
}

/*
 * JkOptions Directive Handling
 *
 *
 * +ForwardSSLKeySize        => Forward SSL Key Size, to follow 2.3 specs but may broke old TC 3.2
 * -ForwardSSLKeySize        => Don't Forward SSL Key Size, will make mod_jk works with all TC release
 *  ForwardURICompat         => Forward URI normally, less spec compliant but mod_rewrite compatible (old TC)
 *  ForwardURICompatUnparsed => Forward URI as unparsed, spec compliant but broke mod_rewrite (old TC)
 *  ForwardURIEscaped       => Forward URI escaped and Tomcat (3.3 rc2) stuff will do the decoding part
 */
static const char *jk_set_options(cmd_parms *cmd,
                                  void *dummy,
                                  const char *line)
{
    int  opt = 0;
    int  mask = 0;
    char action;
    char *w;

    server_rec *s = cmd->server;
    jk_workerEnv_t *workerEnv =
        (jk_workerEnv_t *)ap_get_module_config(s->module_config, &jk_module);

    while (line[0] != 0) {
        w = ap_getword_conf(cmd->pool, &line);
        action = 0;

        if (*w == '+' || *w == '-') {
            action = *(w++);
        }

        mask = 0;

        if (!strcasecmp(w, "ForwardKeySize")) {
            opt = JK_OPT_FWDKEYSIZE;
        }
        else if (!strcasecmp(w, "ForwardURICompat")) {
            opt = JK_OPT_FWDURICOMPAT;
            mask = JK_OPT_FWDURIMASK;
        }
        else if (!strcasecmp(w, "ForwardURICompatUnparsed")) {
            opt = JK_OPT_FWDURICOMPATUNPARSED;
            mask = JK_OPT_FWDURIMASK;
        }
        else if (!strcasecmp(w, "ForwardURIEscaped")) {
            opt = JK_OPT_FWDURIESCAPED;
            mask = JK_OPT_FWDURIMASK;
        }
        else
            return ap_pstrcat(cmd->pool, "JkOptions: Illegal option '", w, "'", NULL);

        workerEnv->options &= ~mask;

        if (action == '-') {
            workerEnv->options &= ~opt;
        }
        else if (action == '+') {
            workerEnv->options |=  opt;
        }
        else {
            /* for now +Opt == Opt */
            workerEnv->options |=  opt;
        }
    }
    return NULL;
}

/*
 * JkEnvVar Directive Handling
 *
 * JkEnvVar MYOWNDIR
 */
static const char *jk_add_env_var(cmd_parms *cmd,
                                  void *dummy,
                                  const char *env_name,
                                  const char *default_value)
{
    server_rec *s = cmd->server;
    jk_workerEnv_t *workerEnv =
        (jk_workerEnv_t *)ap_get_module_config(s->module_config, &jk_module);

    workerEnv->envvars_in_use = JK_TRUE;

    workerEnv->envvars->put(NULL, workerEnv->envvars, env_name,
                            ap_pstrdup(cmd->pool,default_value) , NULL);

    return NULL;
}
    
static const command_rec jk_cmds[] =
    {
    /*
     * JkWorkersFile specifies a full path to the location of the jk
     * properties file.
     */
    AP_INIT_TAKE1(
        "JkWorkersFile", jk_set_worker_file, NULL, RSRC_CONF,
        "the name of a worker file for the Jakarta servlet containers"),
    AP_INIT_TAKE1(
        "JkProperties", jk_set_worker_file, NULL, RSRC_CONF,
        "the name of a worker file for the Jakarta servlet containers"),

    /*
     * JkWorker allows you to specify worker properties in server.xml.
     * They are added before any property in JkWorkersFile ( if any ), 
     * as a more convenient way to configure
     */
    AP_INIT_TAKE2(
        "JkSet", jk_worker_property, NULL, RSRC_CONF,
        "Set a jk property, same syntax and rules as in JkWorkersFile"),

    /*
     * JkMount mounts a url prefix to a worker (the worker need to be
     * defined in the worker properties file.
     */
    AP_INIT_TAKE23(
        "JkMount", jk_mount_context, NULL, RSRC_CONF,
        "A mount point from a context to a Tomcat worker"),

    /*
     * JkWorker sets the worker associated with a <Location> directive.
     */
    AP_INIT_TAKE1(
        "JkWorker", jk_set_worker, NULL, ACCESS_CONF,
        "Worker to be used for a webapp declared as Location"),

    /*
     * JkMountCopy specifies if mod_jk should copy the mount points
     * from the main server to the virtual servers.
     */
    AP_INIT_FLAG(
        "JkMountCopy", jk_set_mountcopy, NULL, RSRC_CONF,
        "Should the base server mounts be copied to the virtual server"),

    /* -------------------- Should be part of workers.properties ------------ */
    /*
     * JkLogFile & JkLogLevel specifies to where should the plugin log
     * its information and how much.
     * JkLogStampFormat specify the time-stamp to be used on log
     * XXX We should use error.log !
     */
    AP_INIT_TAKE1(
        "JkLogFile", jk_set_log_file, NULL, RSRC_CONF,
        "Full path to the Jakarta Tomcat module log file"),
    AP_INIT_TAKE1(
        "JkLogLevel", jk_set_log_level, NULL, RSRC_CONF,
        "The Jakarta Tomcat module log level, can be debug, "
        "info, error or emerg"),
    AP_INIT_TAKE1(
        "JkLogStampFormat", jk_set_log_fmt, NULL, RSRC_CONF,
        "The Jakarta Tomcat module log format, follow strftime synthax"),

    /*
     * Apache has multiple SSL modules (for example apache_ssl, stronghold
     * IHS ...). Each of these can have a different SSL environment names
     * The following properties let the administrator specify the envoiroment
     * variables names.
     *
     * HTTPS - indication for SSL
     * CERTS - Base64-Der-encoded client certificates.
     * CIPHER - A string specifing the ciphers suite in use.
     * KEYSIZE - Size of Key used in dialogue (#bits are secure)
     * SESSION - A string specifing the current SSL session.
     */
    AP_INIT_TAKE1(
        "JkHTTPSIndicator", jk_set_https_indicator, NULL, RSRC_CONF,
        "Name of the Apache environment that contains SSL indication"),
    AP_INIT_TAKE1(
        "JkCERTSIndicator", jk_set_certs_indicator, NULL, RSRC_CONF,
        "Name of the Apache environment that contains SSL client certificates"),
    AP_INIT_TAKE1(
         "JkCIPHERIndicator", jk_set_cipher_indicator, NULL, RSRC_CONF,
        "Name of the Apache environment that contains SSL client cipher"),
    AP_INIT_TAKE1(
        "JkSESSIONIndicator", jk_set_session_indicator, NULL, RSRC_CONF,
        "Name of the Apache environment that contains SSL session"),
    AP_INIT_TAKE1(
        "JkKEYSIZEIndicator", jk_set_key_size_indicator, NULL, RSRC_CONF,
        "Name of the Apache environment that contains SSL key size in use"),
    AP_INIT_FLAG(
        "JkExtractSSL", jk_set_enable_ssl, NULL, RSRC_CONF,
        "Turns on SSL processing and information gathering by mod_jk"),

    /*
     * Options to tune mod_jk configuration
     * for now we understand :
     * +ForwardSSLKeySize        => Forward SSL Key Size, to follow 2.3
                                    specs but may broke old TC 3.2
     * -ForwardSSLKeySize        => Don't Forward SSL Key Size, will make
                                    mod_jk works with all TC release
     *  ForwardURICompat         => Forward URI normally, less spec compliant
                                    but mod_rewrite compatible (old TC)
     *  ForwardURICompatUnparsed => Forward URI as unparsed, spec compliant
                                    but broke mod_rewrite (old TC)
     *  ForwardURIEscaped        => Forward URI escaped and Tomcat (3.3 rc2)
                                    stuff will do the decoding part
     */
    AP_INIT_RAW_ARGS(
        "JkOptions", jk_set_options, NULL, RSRC_CONF, 
        "Set one of more options to configure the mod_jk module"),

    /*
     * JkEnvVar let user defines envs var passed from WebServer to
     * Servlet Engine
     */
    AP_INIT_TAKE2(
        "JkEnvVar", jk_add_env_var, NULL, RSRC_CONF,
        "Adds a name of environment variable that should be sent "
        "to servlet-engine"),

    {NULL}
};

static void *create_jk_dir_config(apr_pool_t *p, char *dummy)
{
    jk_uriEnv_t *new =
        (jk_uriEnv_t *)apr_pcalloc(p, sizeof(jk_uriEnv_t));

    printf("XXX Create dir config %s %p\n", dummy, new);
    new->uri = dummy;
    return new;
}


static void *merge_jk_dir_config(apr_pool_t *p, void *basev, void *addv)
{
    jk_uriEnv_t *base =(jk_uriEnv_t *)basev;
    jk_uriEnv_t *add = (jk_uriEnv_t *)addv;
    jk_uriEnv_t *new = (jk_uriEnv_t *)apr_pcalloc(p,sizeof(jk_uriEnv_t));
    
    
    /* XXX */
    printf("XXX Merged dir config %p %p\n", base, new);
    return add;
}

/** Create default jk_config. XXX This is mostly server-independent,
    all servers are using something similar - should go to common.

    This is the first thing called ( or should be )
 */
static void *create_jk_config(apr_pool_t *p, server_rec *s)
{
    /* XXX Do we need both env and workerEnv ? */
    jk_env_t *env;
    jk_workerEnv_t *workerEnv;
    jk_logger_t *l;
    jk_pool_t *globalPool;
    jk_pool_t *workerEnvPool;

    /** First create a pool
     */
#ifdef NO_APACHE_POOL
    jk_pool_create( NULL, &globalPool, NULL, 2048 );
#else
    jk_pool_apr_create( NULL, &globalPool, NULL, p );
#endif

    /** Create the global environment. This will register the default
        factories
    */
    env=jk_env_getEnv( NULL, globalPool );

    /* Optional. Register more factories ( or replace existing ones ) */


    /* Init the environment. */
    
    /* Create the logger */
#ifdef NO_APACHE_LOGGER
    l = env->getInstance( env, env->globalPool, "logger", "file");
#else
    jk_logger_apache2_factory( env, env->globalPool, &l, "logger", "file");
    l->logger_private=s;
#endif
    
    env->l=l;
    l->level=JK_LOG_ERROR_LEVEL;
    
    /* Create the workerEnv */
    workerEnvPool=
        env->globalPool->create( env, env->globalPool, HUGE_POOL_SIZE );    
    workerEnv= env->getInstance( env,
                                 workerEnvPool,
                                 "workerEnv", "default");

    workerEnv->globalEnv=env;
    if( workerEnv==NULL ) {
        env->l->jkLog(env, env->l, JK_LOG_ERROR, "Error creating workerEnv\n");
        return NULL;
    }

    /* Local initialization */
    workerEnv->_private = s;

    env->l->jkLog(env, env->l, JK_LOG_INFO, "mod_jk.create_jk_config()\n" ); 
    return workerEnv;
}

/** Standard apache callback, merge jk options specified in 
    <Host> context. Used to set per virtual host configs
 */
static void *merge_jk_config(apr_pool_t *p, 
                             void *basev, 
                             void *overridesv)
{
    jk_workerEnv_t *base = (jk_workerEnv_t *) basev;
    jk_workerEnv_t *overrides = (jk_workerEnv_t *)overridesv;
    

    /* XXX Commented out for now. It'll be reimplemented after we
       add per/dir config and merge. 
    if(base->ssl_enable) {
        overrides->ssl_enable        = base->ssl_enable;
        overrides->https_indicator   = base->https_indicator;
        overrides->certs_indicator   = base->certs_indicator;
        overrides->cipher_indicator  = base->cipher_indicator;
        overrides->session_indicator = base->session_indicator;
    }

    overrides->options = base->options;

    if(overrides->workerEnvmountcopy) {
        copy_jk_map(p, overrides->s, base->uri_to_context, 
                    overrides->uri_to_context);
        copy_jk_map(p, overrides->s, base->automount, overrides->automount);
    }

    if(base->envvars_in_use) {
        overrides->envvars_in_use = JK_TRUE;
        overrides->envvars = apr_table_overlay(p, overrides->envvars, 
                                               base->envvars);
    }

    if(overrides->log_file && overrides->log_level >= 0) {
        if(!jk_open_file_logger(&(overrides->log), overrides->log_file, 
                                overrides->log_level)) {
            overrides->log = NULL;
        }
    }
    if(!uri_worker_map_alloc(&(overrides->uw_map), 
                             overrides->uri_to_context, 
                             overrides->log)) {
    }
    
    if (base->secret_key)
        overrides->secret_key = base->secret_key;
    */

    return overrides;
}

/** Standard apache callback, initialize jk.
 */
static void jk_child_init(apr_pool_t *pconf, 
                          server_rec *s)
{
    jk_workerEnv_t *workerEnv =
        (jk_workerEnv_t *)ap_get_module_config(s->module_config, &jk_module);
    jk_env_t *env=workerEnv->globalEnv;

    env->l->jkLog(env, env->l, JK_LOG_INFO, "mod_jk child init\n" );
    
    /* init_jk( pconf, conf, s );
       do we need jk_child_init? For ajp14? */
}

/** Initialize jk, using worker.properties. 
    We also use apache commands ( JkWorker, etc), but this use is 
    deprecated, as we'll try to concentrate all config in
    workers.properties, urimap.properties, and ajp14 autoconf.
    
    Apache config will only be used for manual override, using 
    SetHandler and normal apache directives ( but minimal jk-specific
    stuff )
*/
static void init_jk( jk_env_t *env, apr_pool_t *pconf,
                     jk_workerEnv_t *workerEnv, server_rec *s )
{
    int err;

    env->l->jkLog(env, env->l, JK_LOG_INFO, "mod_jk.init_jk()\n" ); 

    env->l->open( env, env->l, workerEnv->init_data );

    /* local initialization */
    workerEnv->virtual       = "*";     /* for now */
    workerEnv->server_name   = (char *)ap_get_server_version();

    /* Init() - post-config initialization ( now all options are set ) */
    workerEnv->init( env, workerEnv ); 

    err=workerEnv->uriMap->init(env, workerEnv->uriMap,
                                workerEnv,
                                workerEnv->init_data );
    
    ap_add_version_component(pconf, JK_EXPOSED_VERSION);
    return;
}

/* Apache will first validate the config then restart.
   That will unload all .so modules - including ourself.
   Keeping 'was_initialized' in workerEnv is pointless, since both
   will disapear.
*/
static int jk_apache2_isValidating(apr_pool_t *gPool, apr_pool_t **mainPool) {
    apr_pool_t *tmpPool=NULL;
    void *data=NULL;
    int i;
    
    for( i=0; i<10; i++ ) {
        tmpPool=apr_pool_get_parent( gPool );
        if( tmpPool == NULL ) {
            break;
        }
        gPool=tmpPool;
    }

    if( tmpPool == NULL ) {
        /* We can't detect the root pool */
        return JK_FALSE;
    }
    if(mainPool != NULL )
        *mainPool=gPool;
    
    /* We have a global pool ! */
    apr_pool_userdata_get( &data, "mod_jk_init", gPool );
    if( data==NULL ) {
        return JK_TRUE;
    } else {
        return JK_FALSE;
    }
}

static int jk_post_config(apr_pool_t *pconf, 
                           apr_pool_t *plog, 
                           apr_pool_t *ptemp, 
                           server_rec *s)
{
    jk_workerEnv_t *workerEnv;
    apr_pool_t *gPool=NULL;
    void *data=NULL;
    int rc;
    jk_env_t *env;

    if(s->is_virtual) 
        return OK;

    workerEnv =
        (jk_workerEnv_t *)ap_get_module_config(s->module_config, &jk_module);

    env=workerEnv->globalEnv;
    
    rc=jk_apache2_isValidating( plog, &gPool );
    if( rc == JK_TRUE ) {
        /* This is the first step */
        env->l->jkLog(env, env->l, JK_LOG_INFO,
                      "mod_jk.post_config() first invocation\n");
        apr_pool_userdata_set( "INITOK", "mod_jk_init", NULL, gPool );
        return OK;
    }
        
    env->l->jkLog(env, env->l, JK_LOG_INFO,
                  "mod_jk.post_config() second invocation\n" ); 
    
    if(!workerEnv->was_initialized) {
        workerEnv->was_initialized = JK_TRUE;        
        init_jk( env, pconf, workerEnv, s );
    }
    return OK;
}

/* ========================================================================= */
/* The JK module handlers                                                    */
/* ========================================================================= */

/** Util - cleanup endpoint. Used with per/thread endpoints.
 */
static apr_status_t jk_cleanup_endpoint( void *data ) {
    jk_endpoint_t *end = (jk_endpoint_t *)data;    
    printf("XXX jk_cleanup1 %p\n", data);
    /* XXX get env */
    end->done(NULL, end);  
    return 0;
}

/** handler for 'ctl' requests. 
 */
static int jk_ctl_handler(request_rec *r)
{
    jk_workerEnv_t *workerEnv;
    jk_logger_t      *l;
    jk_env_t *env;

    if( strcmp( r->handler, JK_CTL_HANDLER ) != 0 )
        return DECLINED;
    
    workerEnv=(jk_workerEnv_t *)ap_get_module_config(r->server->module_config,
                                                     &jk_module);
    /* XXX Get a 'local' env instance */
    env = workerEnv->globalEnv;

    /* Find what 'ctl' request we have */


    /* 'Ping' clt - update the status for all workers, send ping
       message. This will update 'up/down' state and give tomcat
       an option to update the mapping tables. The ctl handler
       will output an xhtml status page */
    
    return DECLINED;
}
/** Main service method, called to forward a request to tomcat
 */
static int jk_handler(request_rec *r)
{   
    const char       *worker_name;
    jk_logger_t      *l=NULL;
    int              rc;
    jk_worker_t *worker=NULL;
    jk_endpoint_t *end = NULL;
    jk_uriEnv_t *uriEnv;
    jk_uriEnv_t *dirEnv;
    jk_env_t *env;
    jk_workerEnv_t *workerEnv;

    uriEnv=ap_get_module_config( r->request_config, &jk_module );

    /* not for me, try next handler */
    if(uriEnv==NULL || strcmp(r->handler,JK_HANDLER)!= 0 )
      return DECLINED;
    
    /* If this is a proxy request, we'll notify an error */
    if(r->proxyreq) {
        return HTTP_INTERNAL_SERVER_ERROR;
    }

    workerEnv=(jk_workerEnv_t *)ap_get_module_config(r->server->module_config, 
                                                     &jk_module);
    /* XXX Get an env instance */
    env = workerEnv->globalEnv;

    /* Set up r->read_chunked flags for chunked encoding, if present */
    if(rc = ap_setup_client_block(r, REQUEST_CHUNKED_DECHUNK)) {
        env->l->jkLog(env, env->l, JK_LOG_INFO,
                      "mod_jk.handler() Can't setup client block %d\n", rc);
        return rc;
    }

    if( uriEnv == NULL ) {
        /* SetHandler case - per_dir config should have the worker*/
        worker =  workerEnv->defaultWorker;
        env->l->jkLog(env, env->l, JK_LOG_INFO, 
                      "mod_jk.handler() Default worker for %s %s\n",
                      r->uri, worker->name); 
    } else {
        worker=uriEnv->webapp->worker;
        if( worker==NULL && uriEnv->webapp->workerName != NULL ) {
            worker=workerEnv->getWorkerForName( env, workerEnv,
                                                uriEnv->webapp->workerName);
            uriEnv->webapp->worker=worker;
        }
    }

    if(worker==NULL ) {
        env->l->jkLog(env, env->l, JK_LOG_ERROR, 
                      "No worker for %s\n", r->uri); 
        return 500;
    }

    worker->get_endpoint(env, worker, &end);

    {
        jk_ws_service_t sOnStack;
        jk_ws_service_t *s=&sOnStack;
        int is_recoverable_error = JK_FALSE;

        jk_service_apache2_factory( env, end->cPool, &s,
                                    "service", "apache2");
        
        s->init( env, s, end, r );
        
        rc = end->service(env, end, s,  &is_recoverable_error);

        s->afterRequest(env, s);
    }

    end->done(env, end); 

    if(rc==JK_TRUE) {
        return OK;    /* NOT r->status, even if it has changed. */
    }

    env->l->jkLog(env, env->l, JK_LOG_ERROR,
             "mod_jk.handler() Error connecting to tomcat %d\n", rc);
    return 500;
}

/** Use the internal mod_jk mappings to find if this is a request for
 *    tomcat and what worker to use. 
 */
static int jk_translate(request_rec *r)
{
    jk_workerEnv_t *workerEnv;
    jk_uriEnv_t *uriEnv;
    jk_env_t *env;
            
    if(r->proxyreq) {
        return DECLINED;
    }
    
    uriEnv=ap_get_module_config( r->per_dir_config, &jk_module );
    workerEnv=(jk_workerEnv_t *)ap_get_module_config(r->server->module_config,
                                                     &jk_module);
    /* XXX get_env() */
    env=workerEnv->globalEnv;
        
    /* This has been mapped to a location that has a 'webapp' property,
       i.e. belongs to a tomcat webapp.
       We'll use the webapp uriMap to find if it's a static page and
       to parse the request.
       XXX for now just forward to tomcat
    */
    if( uriEnv!= NULL && uriEnv->webapp!=NULL ) {
        jk_uriMap_t *uriMap=uriEnv->webapp->uriMap;

        if( uriMap!=NULL ) {
            /* Again, we have 2 choices. Either use our map, or again
               let apache. The second is probably faster, but requires
               using some APIs I'm not familiar with ( to modify apache's
               config on the fly ). After we learn the new APIs we can
               switch to the second method.
            */
            /* XXX Cut the context path ? */
            jk_uriEnv_t *target=uriMap->mapUri( env, uriMap, NULL, r->uri );
            if( target == NULL ) 
                return DECLINED;
            uriEnv=target;
        }

        env->l->jkLog(env, env->l, JK_LOG_INFO, 
                      "PerDir mapping  %s=%s\n",
                      r->uri, uriEnv->webapp->workerName);
        
        ap_set_module_config( r->request_config, &jk_module, uriEnv );        
        r->handler=JK_HANDLER;
        return OK;
    }

    /* One idea was to use "SetHandler jakarta-servlet". This doesn't
       allow the setting of the worker. Having a specific SetWorker directive
       at location level is more powerfull. If anyone can figure any reson
       to support SetHandler, we can add it back easily */

    /* Check JkMount directives, if any */
    if( workerEnv->uriMap->size == 0 )
        return DECLINED;
    
    /* XXX TODO: Split mapping, similar with tomcat. First step will
       be a quick test ( the context mapper ), with no allocations.
       If positive, we'll fill a ws_service_t and do the rewrite and
       the real mapping. 
    */
    uriEnv = workerEnv->uriMap->mapUri(env, workerEnv->uriMap,NULL,r->uri );
    
    if(uriEnv==NULL ) {
        return DECLINED;
    }

    ap_set_module_config( r->request_config, &jk_module, uriEnv );
    r->handler=JK_HANDLER;

    env->l->jkLog(env, env->l, JK_LOG_INFO, 
                  "mod_jk.translate(): uriMap %s %s\n",
                  r->uri, uriEnv->webapp->worker->name);

    return OK;
}

/* XXX Can we use type checker step to set our stuff ? */

/* bypass the directory_walk and file_walk for non-file requests */
static int jk_map_to_storage(request_rec *r)
{
    jk_uriEnv_t *uriEnv=ap_get_module_config( r->request_config, &jk_module );
    
    if( uriEnv != NULL ) {
        r->filename = (char *)apr_filename_of_pathname(r->uri);
        if( uriEnv->debug > 0 ) {
            /*   env->l->jkLog(env, env->l, JK_LOG_INFO,  */
            /*     "mod_jk.map_to_storage(): map %s %s\n", */
            /*                  r->uri, r->filename); */
        }
        return OK;
    }
    return DECLINED;
}

static void jk_register_hooks(apr_pool_t *p)
{
    ap_hook_handler(jk_handler, NULL, NULL, APR_HOOK_MIDDLE);
    ap_hook_post_config(jk_post_config,NULL,NULL,APR_HOOK_MIDDLE);
    ap_hook_child_init(jk_child_init,NULL,NULL,APR_HOOK_MIDDLE);
    ap_hook_translate_name(jk_translate,NULL,NULL,APR_HOOK_FIRST);
    ap_hook_map_to_storage(jk_map_to_storage, NULL, NULL, APR_HOOK_MIDDLE);
}

module AP_MODULE_DECLARE_DATA jk_module =
{
    STANDARD20_MODULE_STUFF,
    create_jk_dir_config,/*  dir config creater */
    NULL,  /* merge_jk_dir_config dir merger --- default is to override */
    create_jk_config,    /* server config */
    merge_jk_config,     /* merge server config */
    jk_cmds,             /* command ap_table_t */
    jk_register_hooks    /* register hooks */
};

