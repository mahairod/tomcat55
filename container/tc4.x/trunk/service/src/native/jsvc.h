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

#ifndef _JSVC_H_
#define _JSVC_H_

#include <sys/systeminfo.h>
#include <strings.h>
#include <stdarg.h>
#include <stdlib.h>
#include <string.h>
#include <limits.h>
#include <stdio.h>
#include <fcntl.h>
#include <dlfcn.h>
#include <jni.h>

#ifndef TRUE
#define TRUE 1
#endif

#ifndef FALSE
#define FALSE 0
#endif

typedef int boolean;

#define JSVC_VERSION_NO   0 // Don't show any version information
#define JSVC_VERSION_EXIT 1 // Show version information and exit
#define JSVC_VERSION_SHOW 2 // Show version information and continue

#define JSVC_MARK __FILE__, __LINE__    // Marker for debug information

/* ********************************************* */
/* The main Java Service configuration structure */
/* ********************************************* */
typedef struct jsvc_config {
    char *binary;       // The name of the binary file
    char *root;         // The root path of jservice
    char *home;         // The root path of the JDK or JRE
    char *vm;           // The Java Virtual Machine name
    char *parfile;      // The default file where command line params are read
#ifndef WIN32
    char *pidfile;      // The default file where the PID is written
    char *user;         // The user name of the JVM process
    char *group;        // The group name of the JVM process
    boolean detach;     // Wether to detach and fork in background or not
#endif
    int showversion;    // How to handle version display (no, show, show/exit)
    char *class;        // The class to execute
    int vmargc;         // The number of arguments in vmargv
    char *vmargv[1024]; // The java virtual machine command line arguments
    int clargc;         // The number of arguments in clargv
    char *clargv[1024]; // The class command line arguments
    JavaVM *jnivm;      // The pointer to the Java Virtual Machine
    JNIEnv *jnienv;     // The pointer to the Java Native Interface Environment
    jclass jniclass;    // The helper class accessed thru JNI
} jsvc_config;

/* ***************************************************** */
/* The native service helper and service manager classes */
/* ***************************************************** */
#define JSVC_HELPER  "org.apache.service.helpers.NativeServiceHelper"
#define JSVC_MANAGER "org.apache.service.helpers.NativeServiceManager"

/* ********************** */
/* Defined in jsvc_help.c */
/* ********************** */
// Display the standard help message
int jsvc_help(jsvc_config *);

/* *********************** */
/* Defined in jsvc_parse.c */
/* *********************** */
// Parse command line options and return a jsvc_config pointer or null
jsvc_config *jsvc_parse(char *, char *, char *, int, char **);
// Parse one single command line argument
boolean jsvc_parse_argument(char *, jsvc_config *);
// Parse command line arguments from a file
boolean jsvc_parse_file(char *, jsvc_config *);

/* **************************************** */
/* Defined in jsvc_unix.c or jsvc_windows.c */
/* **************************************** */
// System dependand virtual machine creation
boolean jsvc_createvm(jsvc_config *);
// System dependant error display
void jsvc_error(const char *, int, const char *, ...);
// Used for dumping debug information
void jsvc_debug(const char *, int, const char *, ...);

/* ************************* */
/* Defined in jsvc_service.c */
/* ************************* */
// Register native methods and retrieve the native service helper class
boolean jsvc_prepare(jsvc_config *);
// Display version information depending on jsvc_config.showversion
boolean jsvc_version(jsvc_config *);
// Initialize the service
boolean jsvc_init(jsvc_config *);
// Starts the service
boolean jsvc_start(jsvc_config *);
// Restarts the service
boolean jsvc_restart(jsvc_config *);
// Stop the service
boolean jsvc_stop(jsvc_config *);

#endif // #ifndef _JSVC_H_