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

/**
 * Display the help page file.
 *
 * @param config The service configuration member.
 */
void jsvc_help(jsvc_config *config) {
    printf("Usage: %s [@file] [-options] class [args...]\n",config->binary);
    printf("\n");
    printf("Where options include:\n");
    printf("\n");
    printf("    @[file]\n");
    printf("        read command line options from the specified file\n");
    printf("        if [file] wasn't specified, or there were no other\n");
    printf("        arguments specified on the command line, the default\n");
    printf("        default arguments are read from the default file:\n");
    printf("        %s\n",config->parfile);
    printf("\n");
    printf("    -h, -? or -help\n");
    printf("        print this help message and exit\n");
    printf("\n");
    printf("    -vm:[server|client|<jvm>]\n");
    printf("        select the server, client or another virtual machine,\n");
    printf("        where <jvm> is the name of the virtual machine to be\n");
    printf("        used\n");
    printf("\n");
    printf("    -cp <classpath> or -classpath <classpath>\n");
    printf("        set search path for application classes and resources\n");
    printf("        this is equal to -Djava.class.path=<classpath> where\n");
    printf("        <classpath> is a list of directories and/or jar/zip\n");
    printf("        files separated by \":\"\n");
    printf("        (example: -classpath /usr/java/lib:/home/user/lib.jar)\n");
    printf("\n");
    printf("    -verbose:[class|gc|jni]\n");
    printf("        enable verbose options\n");
    printf("        multiple options can be separated with \",\"\n");
    printf("        (example: -verbose:class,gc)\n");
    printf("\n");
    printf("    -version\n");
    printf("        print product version and exit\n");
    printf("\n");
    printf("    -showversion\n");
    printf("        print product version and continue\n");
    printf("\n");
#ifndef WIN32
    printf("    -user:<username>\n");
    printf("        change the user of the virtual machine process after\n");
    printf("        startup\n");
    printf("\n");
    printf("    -group:<username>\n");
    printf("        change the group of the virtual machine process after\n");
    printf("        startup\n");
    printf("\n");
    printf("    -pidfile:<file>\n");
    printf("        specify the file name where the pid will be written to\n");
    printf("        this option is ignored in case -nodetach is specified\n");
    printf("        (default: %s)\n",config->pidfile);
    printf("\n");
    printf("    -nodetach\n");
    printf("        don't detach from the console and fork in background\n");
    printf("        after initialization\n");
    printf("\n");
#endif
    printf("    -D<name>=<value>\n");
    printf("        set a system property\n");
    printf("\n");
    printf("    -X<...>\n");
    printf("        set a non standard java virtual machine option\n");
    printf("        to check what non standard options your java virtual\n");
    printf("        supports try to invoke \"java -X\"\n");
    printf("\n");
}
