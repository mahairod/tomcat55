#include <jsvc.h>

void jsvc_log(JNIEnv *env, jobject obj, jstring msg) {
    jboolean copy=JNI_TRUE;
    const char *message=(*env)->GetStringUTFChars(env, msg, &copy);

    jsvc_error(JSVC_MARK, message);
    (*env)->ReleaseStringUTFChars(env, msg, message);
}

boolean jsvc_prepare(jsvc_config *cfg) {
    JNINativeMethod log;
    jmethodID method;
    jstring name;
    jclass class;
    jboolean ret;
    jobjectArray arg;
    int x;

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

boolean jsvc_version(jsvc_config *cfg) {
    JNINativeMethod log;
    jmethodID method;
    jstring name;
    jboolean ret;
    jobjectArray arg;
    int x;

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

boolean jsvc_init(jsvc_config *cfg) {
    JNINativeMethod log;
    jmethodID method;
    jstring name;
    jclass class;
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
    JNINativeMethod log;
    jmethodID method;
    jstring name;
    jboolean ret;
    jobjectArray arg;
    int x;

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
    JNINativeMethod log;
    jmethodID method;
    jstring name;
    jboolean ret;
    jobjectArray arg;
    int x;

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
    JNINativeMethod log;
    jmethodID method;
    jstring name;
    jboolean ret;
    jobjectArray arg;
    int x;

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
