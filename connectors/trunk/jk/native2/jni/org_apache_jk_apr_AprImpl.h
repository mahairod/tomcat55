/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class org_apache_jk_apr_AprImpl */

#ifndef _Included_org_apache_jk_apr_AprImpl
#define _Included_org_apache_jk_apr_AprImpl
#ifdef __cplusplus
extern "C" {
#endif
#undef org_apache_jk_apr_AprImpl_OK
#define org_apache_jk_apr_AprImpl_OK 0L
#undef org_apache_jk_apr_AprImpl_LAST
#define org_apache_jk_apr_AprImpl_LAST 1L
#undef org_apache_jk_apr_AprImpl_ERROR
#define org_apache_jk_apr_AprImpl_ERROR 2L
#undef org_apache_jk_apr_AprImpl_HANDLE_RECEIVE_PACKET
#define org_apache_jk_apr_AprImpl_HANDLE_RECEIVE_PACKET 10L
#undef org_apache_jk_apr_AprImpl_HANDLE_SEND_PACKET
#define org_apache_jk_apr_AprImpl_HANDLE_SEND_PACKET 11L
/* Inaccessible static: aprSingleton */
/* Inaccessible static: ok */
/* Inaccessible static: jniMode */
/*
 * Class:     org_apache_jk_apr_AprImpl
 * Method:    initialize
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_apache_jk_apr_AprImpl_initialize
  (JNIEnv *, jobject);

/*
 * Class:     org_apache_jk_apr_AprImpl
 * Method:    terminate
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_apache_jk_apr_AprImpl_terminate
  (JNIEnv *, jobject);


/*
 * Class:     org_apache_jk_apr_AprImpl
 * Method:    unSocketClose
 * Signature: (JJI)J
 */
JNIEXPORT jlong JNICALL Java_org_apache_jk_apr_AprImpl_unSocketClose
  (JNIEnv *, jobject, jlong, jint);

/*
 * Class:     org_apache_jk_apr_AprImpl
 * Method:    unSocketListen
 * Signature: (JLjava/lang/String;I)J
 */
JNIEXPORT jlong JNICALL Java_org_apache_jk_apr_AprImpl_unSocketListen
  (JNIEnv *, jobject, jstring, jint);

/*
 * Class:     org_apache_jk_apr_AprImpl
 * Method:    unSocketConnect
 * Signature: (JLjava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_org_apache_jk_apr_AprImpl_unSocketConnect
  (JNIEnv *, jobject, jstring);

/*
 * Class:     org_apache_jk_apr_AprImpl
 * Method:    unAccept
 * Signature: (JJ)J
 */
JNIEXPORT jlong JNICALL Java_org_apache_jk_apr_AprImpl_unAccept
  (JNIEnv *, jobject, jlong);

/*
 * Class:     org_apache_jk_apr_AprImpl
 * Method:    unRead
 * Signature: (JJ[BII)I
 */
JNIEXPORT jint JNICALL Java_org_apache_jk_apr_AprImpl_unRead
  (JNIEnv *, jobject, jlong, jbyteArray, jint, jint);

/*
 * Class:     org_apache_jk_apr_AprImpl
 * Method:    unWrite
 * Signature: (JJ[BII)I
 */
JNIEXPORT jint JNICALL Java_org_apache_jk_apr_AprImpl_unWrite
  (JNIEnv *, jobject, jlong, jbyteArray, jint, jint);

/*
 * Class:     org_apache_jk_apr_AprImpl
 * Method:    getJkEnv
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_org_apache_jk_apr_AprImpl_getJkEnv
  (JNIEnv *, jobject);

/*
 * Class:     org_apache_jk_apr_AprImpl
 * Method:    releaseJkEnv
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_org_apache_jk_apr_AprImpl_releaseJkEnv
  (JNIEnv *, jobject, jlong);

/*
 * Class:     org_apache_jk_apr_AprImpl
 * Method:    jkRecycle
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_org_apache_jk_apr_AprImpl_jkRecycle
  (JNIEnv *, jobject, jlong, jlong);

/*
 * Class:     org_apache_jk_apr_AprImpl
 * Method:    getJkHandler
 * Signature: (JLjava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_org_apache_jk_apr_AprImpl_getJkHandler
  (JNIEnv *, jobject, jlong, jstring);

/*
 * Class:     org_apache_jk_apr_AprImpl
 * Method:    createJkHandler
 * Signature: (JLjava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_org_apache_jk_apr_AprImpl_createJkHandler
  (JNIEnv *, jobject, jlong, jstring);

/*
 * Class:     org_apache_jk_apr_AprImpl
 * Method:    jkGetId
 * Signature: (JLjava/lang/String;Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_org_apache_jk_apr_AprImpl_jkGetId
  (JNIEnv *, jobject, jlong, jstring, jstring);

/*
 * Class:     org_apache_jk_apr_AprImpl
 * Method:    jkInvoke
 * Signature: (JJJI[BI)I
 */
JNIEXPORT jint JNICALL Java_org_apache_jk_apr_AprImpl_jkInvoke
  (JNIEnv *, jclass, jlong, jlong, jlong, jint, jbyteArray, jint);

#ifdef __cplusplus
}
#endif
#endif
