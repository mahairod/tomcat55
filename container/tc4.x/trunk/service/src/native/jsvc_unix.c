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

int main(int argc, char *argv[]) {
    jsvc_config *config;
    char *bin;
    char *root;
    char *home;
    int x;

    // Resolve the real full path name of the binary
    root=(char *)malloc(PATH_MAX*sizeof(char));
    if (realpath(argv[0], root)==NULL) {
       // jsvc_error(JSVC_MARK, "Cannot resolve base path name");
        return(1);
    }

    // Split the full path name of the binary in file and dir
    bin=strrchr(root,'/');
    bin[0]='\0';
    bin++;

    // Check out the JAVA_HOME variable
    home=getenv("JAVA_HOME");
    if (home==NULL) home="/usr/java";

    // Create a configuration structure from command line arguments
    config=jsvc_parse(bin, root, home, argc-1, &argv[1]);
    if (config==NULL) {
        jsvc_error(JSVC_MARK, "Cannot process command line parameters");
        return(1);
    }

    // Dump the current configuration if we're debugging
#ifdef DEBUG
    jsvc_debug(JSVC_MARK, "config->binary      \"%s\"",
        config->binary==NULL?"[NULL]":config->binary);
    jsvc_debug(JSVC_MARK, "config->root        \"%s\"",
        config->root==NULL?"[NULL]":config->root);
    jsvc_debug(JSVC_MARK, "config->home        \"%s\"",
        config->home==NULL?"[NULL]":config->home);
    jsvc_debug(JSVC_MARK, "config->vm          \"%s\"",
        config->vm==NULL?"[NULL]":config->vm);
    jsvc_debug(JSVC_MARK, "config->parfile     \"%s\"",
        config->parfile==NULL?"[NULL]":config->parfile);
    jsvc_debug(JSVC_MARK, "config->pidfile     \"%s\"",
        config->pidfile==NULL?"[NULL]":config->pidfile);
    jsvc_debug(JSVC_MARK, "config->user        \"%s\"",
        config->user==NULL?"[NULL]":config->user);
    jsvc_debug(JSVC_MARK, "config->group       \"%s\"",
        config->group==NULL?"[NULL]":config->group);
    jsvc_debug(JSVC_MARK, "config->detach      \"%d\"",
        config->detach);
    jsvc_debug(JSVC_MARK, "config->showversion \"%d\"",
        config->showversion);
    jsvc_debug(JSVC_MARK, "config->class       \"%s\"",
        config->class==NULL?"[NULL]":config->class);
    jsvc_debug(JSVC_MARK, "config->vmargc      \"%d\"",
        config->vmargc);
    for(x=0; x<config->vmargc; x++)
        jsvc_debug(JSVC_MARK,"config->vmargv[%3d] \"%s\"",x,
            config->vmargv[x]==NULL?"[NULL]":config->vmargv[x]);
    jsvc_debug(JSVC_MARK, "config->clargc      \"%d\"", config->clargc);
    for(x=0; x<config->clargc; x++)
        jsvc_debug(JSVC_MARK,"config->clargv[%3d] \"%s\"",x,
            config->clargv[x]==NULL?"[NULL]":config->clargv[x]);
#endif

    if (!jsvc_createvm(config)) return(1);

    if (!jsvc_prepare(config)) return(1);

    if (!jsvc_version(config)) return(1);

    if (!jsvc_init(config)) return(1);

    if (!jsvc_start(config)) return(1);

    while(1) pause();

    return(0);

}

boolean jsvc_createvm(jsvc_config *cfg) {
    char lib[PATH_MAX];
    char arc[32];
    char *xvm;
    void *hnd;
    JavaVMInitArgs *arg;
    JavaVMOption *opt;
    int x=-1;
    jint (*cre)(JavaVM **,JNIEnv **, void *);

    // Check if a VM variant was specified
    if (cfg->vm==NULL) xvm="";
    else xvm=strcat(strdup(cfg->vm),"/");

    // Check the processor architecture
    sysinfo(SI_ARCHITECTURE,arc,32);

    // Calculate the Java Virtual Machine library filename (for JRE standard)
    sprintf(lib,"%s/lib/%s/%slibjvm.so",cfg->home,arc,xvm);
    jsvc_debug(JSVC_MARK, "Attempting to load library from \"%s\"",lib);
    x=open(lib,O_RDONLY);
    if (x<0) {
        sprintf(lib,"%s/jre/lib/%s/%slibjvm.so",cfg->home,arc,xvm);
        jsvc_debug(JSVC_MARK, "Attempting to load library from \"%s\"",lib);
        x=open(lib,O_RDONLY);
        if (x<0) {
            jsvc_error(JSVC_MARK, "VM library \"libjvm.so\" not found");
            return(FALSE);
        } else close(x);
    } else close(x);

    // We found the appropriate libjvm.so, attempt to load and link
    hnd=dlopen(lib,RTLD_NOW|RTLD_GLOBAL);
    if (hnd==NULL) {
        jsvc_error(JSVC_MARK, "Cannot load VM \"%s\": %s\n", lib, dlerror());
        return(FALSE);
    }

    // Check if we can find the JNI_CreateJavaVM in the library
    cre=dlsym(hnd,"JNI_CreateJavaVM");
    if (cre==NULL) {
        jsvc_error(JSVC_MARK, "Cannot found JNI_CreateJavaVM in \"%s\"",lib);
        return(FALSE);
    }

    // Setup the Java VM initialization arguments
    arg=(JavaVMInitArgs *)malloc(sizeof(JavaVMInitArgs));
    arg->version=JNI_VERSION_1_2;
    arg->ignoreUnrecognized=JNI_FALSE;
    arg->nOptions=cfg->vmargc;
    opt=(JavaVMOption *)malloc((arg->nOptions)*sizeof(JavaVMOption));
    for (x=0; x<arg->nOptions; x++) opt[x].optionString=cfg->vmargv[x];
    arg->options=opt;

    // Attempt to create the virtual machine
    if((*cre)(&cfg->jnivm,&cfg->jnienv,arg)<0) {
        printf("Cannot create virtual machine\n");
        return(FALSE);
    }

    return(TRUE);
}

void jsvc_error(const char *file, int line, const char *fmt, ...) {
    va_list args;

#ifdef DEBUG
    fprintf(stderr, "[%s %d] ", file, line);
#endif
    va_start(args, fmt);
    vfprintf(stderr, fmt, args);
    va_end(args);
    fprintf(stderr, "\n");
    fflush(stderr);
}

void jsvc_debug(const char *file, int line, const char *fmt, ...) {
#ifdef DEBUG
    va_list args;

    fprintf(stderr, "[%s %d] ", file, line);
    va_start(args, fmt);
    vfprintf(stderr, fmt, args);
    va_end(args);
    fprintf(stderr, "\n");
    fflush(stderr);
#endif
}

