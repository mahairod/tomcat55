/* ========================================================================= *
 *                                                                           *
 *                 The Apache Software License,  Version 1.1                 *
 *                                                                           *
 *         Copyright (c) 1999, 2000  The Apache Software Foundation.         *
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
 * 4. The names  "The  Jakarta  Project",  "Tomcat",  and  "Apache  Software *
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

// CVS $Id$
// Author: Pier Fumagalli <mailto:pier.fumagalli@eng.sun.com>

#include <jsvc.h>

// Temporary variable used to process the classpath entry
static boolean classpath=FALSE;

jsvc_config *jsvc_parse(char *b, char *r, char *h, int argc, char *argv[]) {
    jsvc_config *config=(jsvc_config *)malloc(sizeof(jsvc_config));
    char *buffer=(char *)malloc(PATH_MAX*sizeof(char));
    int x=0;

    // Fill the first element of the configuration structure
    if (b==NULL) {
        jsvc_error(JSVC_MARK, "Binary filename unspecified");
        return(NULL);
    } else config->binary=strdup(b);

    if (r==NULL) {
        jsvc_error(JSVC_MARK, "Default configuration filename unspecified");
        return(NULL);
    } else config->root=strdup(r);

    if (h==NULL) {
        jsvc_error(JSVC_MARK, "Virtual machine home path unspecified");
        return(NULL);
    } else config->home=strdup(h);

    config->vm=NULL;

    sprintf(buffer, "%s/%s.conf", config->root, config->binary);
    config->parfile=strdup(buffer);
#ifndef WIN32
    sprintf(buffer, "%s/%s.pid", config->root, config->binary);
    config->pidfile=strdup(buffer);
    config->user=NULL;
    config->group=NULL;
    config->detach=TRUE;
#endif
    config->showversion=JSVC_VERSION_NO;
    config->class=NULL;
    config->vmargc=0;
    config->vmargv[0]=NULL;
    config->clargc=0;
    config->clargv[0]=NULL;

    // Check if we have some parameters on command line (otherwise parse
    // default config file)
    if (argc==0) {
        // We don't have any command line argument, parse the default file
        if (!jsvc_parse_file("",config)) return(NULL);
    } else {
        // We have some command line arguments, parse them one by one
        for (x=0; x<argc; x++)
            if (!jsvc_parse_argument(argv[x], config)) return(NULL);
    }

    return(config);
}

boolean jsvc_parse_argument(char *a, jsvc_config *conf) {
    if (a==NULL) {
        jsvc_error(JSVC_MARK, "Null argument specified");
        return(FALSE);
    }

    jsvc_debug(JSVC_MARK,"Command line arg: \"%s\" Classpath: %d",a,classpath);

    if (a[0]=='@') {
        jsvc_debug(JSVC_MARK,"Reading command lines from \"%s\"",a+1);
        return(jsvc_parse_file(a+1,conf));
    }

    if (conf->class!=NULL) {
        jsvc_debug(JSVC_MARK,"Class specific command line argument \"%s\"",a);
        conf->clargv[conf->clargc++]=strdup(a);
        return(TRUE);
    }

    if (classpath) {
        jsvc_debug(JSVC_MARK, "Class path argument: \"%s\"",a);
        classpath=FALSE;
        conf->vmargv[conf->vmargc]=(char *)malloc((strlen(a)+20)*sizeof(char));
        sprintf(conf->vmargv[conf->vmargc],"-Djava.class.path=%s",a);
        conf->vmargc++;
        return(TRUE);
    }

    if ((strcmp(a,"-help")==0)||(strcmp(a,"-h")==0)||(strcmp(a,"-?")==0)) {
        jsvc_debug(JSVC_MARK,"Display help screen");
        jsvc_help(conf);
        exit(0);
    }

    if ((strcmp(a,"-cp")==0)||(strcmp(a,"-classpath")==0)) {
        jsvc_debug(JSVC_MARK,"Setup classpath from next argument");
        classpath=TRUE;
        return(TRUE);
    }

    if (strcmp(a,"-version")==0) {
        jsvc_debug(JSVC_MARK,"Display version information and exit");
        conf->showversion=JSVC_VERSION_EXIT;
        return(TRUE);
    }

    if (strcmp(a,"-showversion")==0) {
        jsvc_debug(JSVC_MARK,"Display version information and continue");
        conf->showversion=JSVC_VERSION_SHOW;
        return(TRUE);
    }

    if (strstr(a,"-verbose")==a) {
        jsvc_debug(JSVC_MARK,"Enabling verbose output");
        conf->vmargv[conf->vmargc++]=strdup(a);
        return(TRUE);
    }

    if (strstr(a,"-D")==a) {
        jsvc_debug(JSVC_MARK,"Adding system property \"%s\"",a+2);
        conf->vmargv[conf->vmargc++]=strdup(a);
        return(TRUE);
    }

    if (strstr(a,"-vm:")==a) {
        jsvc_debug(JSVC_MARK,"Setting virtual machine to \"%s\"",a+4);
        conf->vm=strdup(a+4);
        return(TRUE);
    }

    if (strstr(a,"-X")==a) {
        jsvc_debug(JSVC_MARK,"Non standard vm option \"%s\"",a);
        conf->vmargv[conf->vmargc++]=strdup(a);
        return(TRUE);
    }

#ifndef WIN32
    if (strcmp(a,"-nodetach")==0) {
        jsvc_debug(JSVC_MARK,"Don't detach and fork in background");
        conf->detach=FALSE;
        return(TRUE);
    }

    if (strstr(a,"-user:")==a) {
        jsvc_debug(JSVC_MARK,"Setting process user to \"%s\"",a+6);
        conf->user=strdup(a+6);
        return(TRUE);
    }

    if (strstr(a,"-group:")==a) {
        jsvc_debug(JSVC_MARK,"Setting process group to \"%s\"",a+6);
        conf->group=strdup(a+6);
        return(TRUE);
    }
#endif

    if (a[0]=='-') {
        jsvc_error(JSVC_MARK,"Unknown command line argument %s",a);
        return(FALSE);
    }

    conf->class=strdup(a);
    return(TRUE);
}

boolean jsvc_parse_file(char *f, jsvc_config *conf) {
    char *filename=f;

    if (filename==NULL) {
        jsvc_error(JSVC_MARK, "Null filename specified");
        return(FALSE);
    }

    if (strlen(filename)==0) filename=conf->parfile;

    jsvc_error(JSVC_MARK, "Cannot read parameters from \"%s\"", filename);
    jsvc_error(JSVC_MARK, "Method not implemented");

    return(FALSE);
}
