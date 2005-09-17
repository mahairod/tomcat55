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
 * This method is registered in the JNI environment to provide logging
 * falcilities. Actual logging is performed by jsvc_error wich is system
 * dependant.
 *
 * @param env The JNI environment.
 * @param obj The object that is calling this native method.
 * @param msg The message to be logged.
 */
void jsvc_log(JNIEnv *env, jobject obj, jstring msg) {
    jboolean copy=JNI_TRUE;
    const char *message=(*env)->GetStringUTFChars(env, msg, &copy);

    jsvc_error(JSVC_MARK, message);
    (*env)->ReleaseStringUTFChars(env, msg, message);
}

/**
 * Register native methods and prepare the Virtual Machine to invoke the 
 * service, getting a hold on the helper class.
 *
 * @param cvs A pointer to a jsvc_config structure.
 * @return TRUE if preparation was successful, FALSE otherwise.
 */
boolean jsvc_prepare(jsvc_config *cfg) {
    JNINativeMethod log;
    jclass class;

    // Register the native logging method for messages
    jsvc_debug(JSVC_MARK, "Registering natives");
    log.name="log";
    log.signature="(Ljava/lang/String;)V";
    log.fnPtr=jsvc_log;
    class=(*cfg->jnienv)->FindClass(cfg->jnienv, JSVC_MANAGER);
    if (class==NULL) {
        jsvc_error(JSVC_MARK, "Cannot find class \"%s\"",JSVC_MANAGER);
        return(FALSE);
    }
    if ((*cfg->jnienv)->RegisterNatives(cfg->jnienv,class,&log,1)!=0) {
        jsvc_error(JSVC_MARK, "Can't register native methods");
        return(FALSE);
    }

    // Try to get a hold on the service helper class
    jsvc_debug(JSVC_MARK, "Resolving class \"%s\"",JSVC_HELPER);
    cfg->jniclass=(*cfg->jnienv)->FindClass(cfg->jnienv, JSVC_HELPER);
    if (cfg->jniclass==NULL) {
        jsvc_error(JSVC_MARK, "Cannot find class \"%s\"",JSVC_HELPER);
        return(FALSE);
    }

    return(TRUE);
}

/**
 * Display the Virtual Machine version information, calling the version method
 * in the helper class.
 * 
 * @param cfg A pointer to a jsvc_config structure.
 * @return TRUE if we can load the service, FALSE if we need to exit.
 */
boolean jsvc_version(jsvc_config *cfg) {
    jmethodID method;

    if (cfg->showversion==JSVC_VERSION_NO) return(TRUE);

    // Retrieve the version static method in the helper class
    jsvc_debug(JSVC_MARK, "Locating version method in \"%s\"",JSVC_HELPER);
    method=(*cfg->jnienv)->GetStaticMethodID(cfg->jnienv,cfg->jniclass,"version","()V");
    if (method==NULL) {
        jsvc_error(JSVC_MARK, "Cannot locate version method");
        return(FALSE);
    }

    // Call the init method in the helper class
    jsvc_debug(JSVC_MARK, "Calling version method in \"%s\"",JSVC_HELPER);
    (*cfg->jnienv)->CallStaticBooleanMethod(cfg->jnienv, cfg->jniclass, method);

    // Normalize the return value (just in case)
    if (cfg->showversion==JSVC_VERSION_EXIT) return(FALSE);
    if (cfg->showversion==JSVC_VERSION_SHOW) return(TRUE);
    jsvc_error(JSVC_MARK, "Unknown value for showversion %d",cfg->showversion);
    return(FALSE);
}

/**
 * Initialize the service.
 * 
 * @param cfg A pointer to a jsvc_config structure.
 * @return TRUE if the service was initialized, FALSE otherwise.
 */
boolean jsvc_init(jsvc_config *cfg) {
    jmethodID method;
    jstring name;
    jboolean ret;
    jobjectArray arg;
    int x;

    // Retrieve the init static method in the helper class
    jsvc_debug(JSVC_MARK, "Locating init method in \"%s\"",JSVC_HELPER);
    method=(*cfg->jnienv)->GetStaticMethodID(cfg->jnienv,cfg->jniclass,"init",
                                   "(Ljava/lang/String;[Ljava/lang/String;)Z");
    if (method==NULL) {
        jsvc_error(JSVC_MARK, "Cannot locate init method");
        return(FALSE);
    }

    // Prepare a string containing the class name of the service
    name=(*cfg->jnienv)->NewStringUTF(cfg->jnienv, cfg->class);

    // Prepare an array of strings with the command line options
    arg=(*cfg->jnienv)->NewObjectArray(cfg->jnienv, cfg->clargc,
        (*cfg->jnienv)->FindClass(cfg->jnienv, "java/lang/String"),NULL);
    for (x=0; x< cfg->clargc; x++)
        (*cfg->jnienv)->SetObjectArrayElement(cfg->jnienv, arg, x,
            (*cfg->jnienv)->NewStringUTF(cfg->jnienv, cfg->clargv[x]));

    // Call the init method in the helper class
    jsvc_debug(JSVC_MARK, "Calling init method in \"%s\"",JSVC_HELPER);
    ret=(*cfg->jnienv)->CallStaticBooleanMethod(cfg->jnienv, cfg->jniclass, method,
                                                    name, arg);

    // Normalize the return value (just in case)
    if (ret) return(TRUE);
    else return(FALSE);
}

boolean jsvc_start(jsvc_config *cfg) {
    jmethodID method;
    jboolean ret;

    // Retrieve the start static method in the helper class
    jsvc_debug(JSVC_MARK, "Locating start method in \"%s\"",JSVC_HELPER);
    method=(*cfg->jnienv)->GetStaticMethodID(cfg->jnienv,cfg->jniclass,"start","()Z");
    if (method==NULL) {
        jsvc_error(JSVC_MARK, "Cannot locate start method");
        return(FALSE);
    }

    // Call the init method in the helper class
    jsvc_debug(JSVC_MARK, "Calling start method in \"%s\"",JSVC_HELPER);
    ret=(*cfg->jnienv)->CallStaticBooleanMethod(cfg->jnienv, cfg->jniclass, method);

    // Normalize the return value (just in case)
    if (ret) return(TRUE);
    else return(FALSE);
}

boolean jsvc_restart(jsvc_config *cfg) {
    jmethodID method;
    jboolean ret;

    // Retrieve the restart static method in the helper class
    jsvc_debug(JSVC_MARK, "Locating restart method in \"%s\"",JSVC_HELPER);
    method=(*cfg->jnienv)->GetStaticMethodID(cfg->jnienv,cfg->jniclass,"restart","()Z");
    if (method==NULL) {
        jsvc_error(JSVC_MARK, "Cannot locate restart method");
        return(FALSE);
    }

    // Call the restart method in the helper class
    jsvc_debug(JSVC_MARK, "Calling restart method in \"%s\"",JSVC_HELPER);
    ret=(*cfg->jnienv)->CallStaticBooleanMethod(cfg->jnienv, cfg->jniclass, method);

    // Normalize the return value (just in case)
    if (ret) return(TRUE);
    else return(FALSE);
}

boolean jsvc_stop(jsvc_config *cfg) {
    jmethodID method;
    jboolean ret;

    // Retrieve the stop static method in the helper class
    jsvc_debug(JSVC_MARK, "Locating stop method in \"%s\"",JSVC_HELPER);
    method=(*cfg->jnienv)->GetStaticMethodID(cfg->jnienv,cfg->jniclass,"stop","()Z");
    if (method==NULL) {
        jsvc_error(JSVC_MARK, "Cannot locate stop method");
        return(FALSE);
    }

    // Call the stop method in the helper class
    jsvc_debug(JSVC_MARK, "Calling stop method in \"%s\"",JSVC_HELPER);
    ret=(*cfg->jnienv)->CallStaticBooleanMethod(cfg->jnienv, cfg->jniclass, method);

    // Normalize the return value (just in case)
    if (ret) return(TRUE);
    else return(FALSE);
}
