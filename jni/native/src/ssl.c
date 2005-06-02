/* Copyright 2000-2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 *
 * @author Mladen Turk
 * @version $Revision$, $Date$
 */

#include "apr.h"
#include "apr_pools.h"
#include "apr_file_io.h"
#include "apr_portable.h"
#include "apr_thread_mutex.h"
#include "apr_strings.h"
#include "apr_atomic.h"
#include "apr_hash.h"

#include "tcn.h"

#ifdef HAVE_OPENSSL
#include "ssl_private.h"

static int ssl_initialized = 0;
extern apr_pool_t *tcn_global_pool;
ENGINE *tcn_ssl_engine = NULL;

apr_hash_t  *tcn_private_keys = NULL;
apr_hash_t  *tcn_public_certs = NULL;

TCN_IMPLEMENT_CALL(jint, SSL, version)(TCN_STDARGS)
{
    UNREFERENCED_STDARGS;
    return OPENSSL_VERSION_NUMBER;
}

TCN_IMPLEMENT_CALL(jstring, SSL, versionString)(TCN_STDARGS)
{
    UNREFERENCED(o);
    return AJP_TO_JSTRING(OPENSSL_VERSION_TEXT);
}

/*
 *  the various processing hooks
 */
static apr_status_t ssl_init_cleanup(void *data)
{
    UNREFERENCED(data);

    if (!ssl_initialized)
        return APR_SUCCESS;
    ssl_initialized = 0;
    /*
     * Try to kill the internals of the SSL library.
     */
#if OPENSSL_VERSION_NUMBER >= 0x00907001
    /* Corresponds to OPENSSL_load_builtin_modules():
     * XXX: borrowed from apps.h, but why not CONF_modules_free()
     * which also invokes CONF_modules_finish()?
     */
    CONF_modules_unload(1);
#endif
    /* Corresponds to SSL_library_init: */
    EVP_cleanup();
#if HAVE_ENGINE_LOAD_BUILTIN_ENGINES
    ENGINE_cleanup();
#endif
#if OPENSSL_VERSION_NUMBER >= 0x00907001
    CRYPTO_cleanup_all_ex_data();
#endif
    ERR_remove_state(0);

    /* Don't call ERR_free_strings here; ERR_load_*_strings only
     * actually load the error strings once per process due to static
     * variable abuse in OpenSSL. */

    /*
     * TODO: determine somewhere we can safely shove out diagnostics
     *       (when enabled) at this late stage in the game:
     * CRYPTO_mem_leaks_fp(stderr);
     */
    return APR_SUCCESS;
}

#ifndef OPENSSL_NO_ENGINE
/* Try to load an engine in a shareable library */
static ENGINE *ssl_try_load_engine(const char *engine)
{
    ENGINE *e = ENGINE_by_id("dynamic");
    if (e) {
        if (!ENGINE_ctrl_cmd_string(e, "SO_PATH", engine, 0)
            || !ENGINE_ctrl_cmd_string(e, "LOAD", NULL, 0)) {
            ENGINE_free(e);
            e = NULL;
        }
    }
    return e;
}
#endif

/*
 * To ensure thread-safetyness in OpenSSL
 */

static apr_thread_mutex_t **ssl_lock_cs;
static int                  ssl_lock_num_locks;

static void ssl_thread_lock(int mode, int type,
                            const char *file, int line)
{
    UNREFERENCED(file);
    UNREFERENCED(line);
    if (type < ssl_lock_num_locks) {
        if (mode & CRYPTO_LOCK) {
            apr_thread_mutex_lock(ssl_lock_cs[type]);
        }
        else {
            apr_thread_mutex_unlock(ssl_lock_cs[type]);
        }
    }
}

static unsigned long ssl_thread_id(void)
{
    /* OpenSSL needs this to return an unsigned long.  On OS/390, the pthread
     * id is a structure twice that big.  Use the TCB pointer instead as a
     * unique unsigned long.
     */
#ifdef __MVS__
    struct PSA {
        char unmapped[540];
        unsigned long PSATOLD;
    } *psaptr = 0;

    return psaptr->PSATOLD;
#else
    return (unsigned long)((jlong)apr_os_thread_current());
#endif
}

static apr_status_t ssl_thread_cleanup(void *data)
{
    UNREFERENCED(data);
    CRYPTO_set_locking_callback(NULL);
    CRYPTO_set_id_callback(NULL);
    /* Let the registered mutex cleanups do their own thing
     */
    return APR_SUCCESS;
}

static void ssl_thread_setup(apr_pool_t *p)
{
    int i;

    ssl_lock_num_locks = CRYPTO_num_locks();
    ssl_lock_cs = apr_palloc(p, ssl_lock_num_locks * sizeof(*ssl_lock_cs));

    for (i = 0; i < ssl_lock_num_locks; i++) {
        apr_thread_mutex_create(&(ssl_lock_cs[i]),
                                APR_THREAD_MUTEX_DEFAULT, p);
    }

    CRYPTO_set_id_callback(ssl_thread_id);
    CRYPTO_set_locking_callback(ssl_thread_lock);

    apr_pool_cleanup_register(p, NULL, ssl_thread_cleanup,
                              apr_pool_cleanup_null);
}

static int ssl_rand_choosenum(int l, int h)
{
    int i;
    char buf[50];

    apr_snprintf(buf, sizeof(buf), "%.0f",
                 (((double)(rand()%RAND_MAX)/RAND_MAX)*(h-l)));
    i = atoi(buf)+1;
    if (i < l) i = l;
    if (i > h) i = h;
    return i;
}

static int ssl_rand_load_file(const char *file)
{
    char buffer[200];
    int n;

    if (file == NULL)
        file = RAND_file_name(buffer, sizeof(buffer));
    else if ((n = RAND_egd(file)) > 0) {
        return n;
    }
    if (file && (n = RAND_load_file(file, -1)) > 0)
        return n;
    else
        return -1;
}

/*
 * writes a number of random bytes (currently 1024) to
 * file which can be used to initialize the PRNG by calling
 * RAND_load_file() in a later session
 */
static int ssl_rand_save_file(const char *file)
{
    char buffer[200];
    int n;

    if (file == NULL)
        file = RAND_file_name(buffer, sizeof(buffer));
    else if ((n = RAND_egd(file)) > 0) {
        return 0;
    }
    if (file == NULL || !RAND_write_file(file))
        return 0;
    else
        return 1;
}

static int ssl_rand_seed(const char *file)
{
    unsigned char stackdata[256];
    static volatile apr_uint32_t counter = 0;

    if (ssl_rand_load_file(file) < 0) {
        int n;
        struct {
            apr_time_t    t;
            pid_t         p;
            unsigned long i;
            apr_uint32_t  u;
        } _ssl_seed;
        _ssl_seed.t = apr_time_now();
        _ssl_seed.p = getpid();
        _ssl_seed.i = ssl_thread_id();
        apr_atomic_inc32(&counter);
        _ssl_seed.u = counter;
        RAND_seed((unsigned char *)&_ssl_seed, sizeof(_ssl_seed));
        /*
         * seed in some current state of the run-time stack (128 bytes)
         */
        n = ssl_rand_choosenum(0, sizeof(stackdata)-128-1);
        RAND_seed(stackdata + n, 128);
    }
    return RAND_status();
}

static int ssl_rand_make(const char *file, int len, int base64)
{
    int r;
    int num = len;
    BIO *out = NULL;

    out = BIO_new(BIO_s_file());
    if (out == NULL)
        return 0;
    if ((r = BIO_write_filename(out, (char *)file)) < 0) {
        BIO_free_all(out);
        return 0;
    }
    if (base64) {
        BIO *b64 = BIO_new(BIO_f_base64());
        if (b64 == NULL) {
            BIO_free_all(out);
            return 0;
        }
        out = BIO_push(b64, out);
    }
    while (num > 0) {
        unsigned char buf[4096];
        int len = num;
        if (len > sizeof(buf))
            len = sizeof(buf);
        r = RAND_bytes(buf, len);
        if (r <= 0) {
            BIO_free_all(out);
            return 0;
        }
        BIO_write(out, buf, len);
        num -= len;
    }
    BIO_flush(out);
    BIO_free_all(out);
    return 1;
}

TCN_IMPLEMENT_CALL(jint, SSL, initialize)(TCN_STDARGS, jstring engine)
{
    TCN_ALLOC_CSTRING(engine);

    UNREFERENCED(o);
    if (!tcn_global_pool) {
        TCN_FREE_CSTRING(engine);
        return (jint)APR_EINVAL;
    }
    /* Check if already initialized */
    if (ssl_initialized++) {
        TCN_FREE_CSTRING(engine);
        return (jint)APR_SUCCESS;
    }
    /* We must register the library in full, to ensure our configuration
     * code can successfully test the SSL environment.
     */
    CRYPTO_malloc_init();
    ERR_load_crypto_strings();
    SSL_load_error_strings();
    SSL_library_init();
#if HAVE_ENGINE_LOAD_BUILTIN_ENGINES
    ENGINE_load_builtin_engines();
#endif
#if OPENSSL_VERSION_NUMBER >= 0x00907001
    OPENSSL_load_builtin_modules();
#endif

#ifndef OPENSSL_NO_ENGINE
    if (J2S(engine)) {
        ENGINE *ee = NULL;
        apr_status_t err = APR_SUCCESS;
        if(strcmp(J2S(engine), "auto") == 0) {
            ENGINE_register_all_complete();
        }
        else {
            if ((ee = ENGINE_by_id(J2S(engine))) == NULL
                && (ee = ssl_try_load_engine(J2S(engine))) == NULL)
                err = APR_ENOTIMPL;
            else {
                if (strcmp(J2S(engine), "chil") == 0)
                    ENGINE_ctrl(ee, ENGINE_CTRL_CHIL_SET_FORKCHECK, 1, 0, 0);
                if (!ENGINE_set_default(ee, ENGINE_METHOD_ALL))
                    err = APR_ENOTIMPL;
            }
            /* Free our "structural" reference. */
            if (ee)
                ENGINE_free(ee);
        }
        if (err != APR_SUCCESS) {
            TCN_FREE_CSTRING(engine);
            ssl_init_cleanup(NULL);
            return (jint)err;
        }
        tcn_ssl_engine = ee;
    }
#endif
    /* Initialize PRNG */
    ssl_rand_seed(NULL);
    /* For SSL_get_app_data2() at request time */
    SSL_init_app_data2_idx();
    
    /* Create tables for global private keys and certs */
    tcn_private_keys = apr_hash_make(tcn_global_pool);
    tcn_public_certs = apr_hash_make(tcn_global_pool);
    /*
     * Let us cleanup the ssl library when the library is unloaded
     */
    apr_pool_cleanup_register(tcn_global_pool, NULL,
                              ssl_init_cleanup,
                              apr_pool_cleanup_null);
    /* Initialize thread support */
    ssl_thread_setup(tcn_global_pool);
    TCN_FREE_CSTRING(engine);
    return (jint)APR_SUCCESS;
}

TCN_IMPLEMENT_CALL(jboolean, SSL, randLoad)(TCN_STDARGS, jstring file)
{
    TCN_ALLOC_CSTRING(file);
    int r;
    UNREFERENCED(o);
    r = ssl_rand_seed(J2S(file));
    TCN_FREE_CSTRING(file);
    return r ? JNI_TRUE : JNI_FALSE;
}

TCN_IMPLEMENT_CALL(jboolean, SSL, randSave)(TCN_STDARGS, jstring file)
{
    TCN_ALLOC_CSTRING(file);
    int r;
    UNREFERENCED(o);
    r = ssl_rand_save_file(J2S(file));
    TCN_FREE_CSTRING(file);
    return r ? JNI_TRUE : JNI_FALSE;
}

TCN_IMPLEMENT_CALL(jboolean, SSL, randMake)(TCN_STDARGS, jstring file,
                                            jint length, jboolean base64)
{
    TCN_ALLOC_CSTRING(file);
    int r;
    UNREFERENCED(o);
    r = ssl_rand_make(J2S(file), length, base64);
    TCN_FREE_CSTRING(file);
    return r ? JNI_TRUE : JNI_FALSE;
}

/* OpenSSL Java Stream BIO */

typedef struct  {
    int            refcount;
    apr_pool_t     *pool;
    tcn_callback_t cb;
} BIO_JAVA;


static apr_status_t generic_bio_cleanup(void *data)
{
    BIO *b = (BIO *)data;

    if (b) {
        BIO_free(b);
    }
    return APR_SUCCESS;
}

void SSL_BIO_close(BIO *bi)
{
    if (bi == NULL)
        return;
    if (bi->ptr != NULL && (bi->flags & SSL_BIO_FLAG_CALLBACK)) {
        BIO_JAVA *j = (BIO_JAVA *)bi->ptr;
        j->refcount--;
        if (j->refcount == 0) {
            if (j->pool)
                apr_pool_cleanup_run(j->pool, bi, generic_bio_cleanup);
            else
                BIO_free(bi);
        }
    }
    else
        BIO_free(bi);
}

void SSL_BIO_doref(BIO *bi)
{
    if (bi == NULL)
        return;
    if (bi->ptr != NULL && (bi->flags & SSL_BIO_FLAG_CALLBACK)) {
        BIO_JAVA *j = (BIO_JAVA *)bi->ptr;
        j->refcount++;
    }
}


static int jbs_new(BIO *bi)
{
    BIO_JAVA *j;

    if ((j = OPENSSL_malloc(sizeof(BIO_JAVA))) == NULL)
        return 0;
    j->pool      = NULL;
    j->refcount  = 1;
    bi->shutdown = 1;
    bi->init     = 0;
    bi->num      = -1;
    bi->ptr      = (char *)j;

    return 1;
}

static int jbs_free(BIO *bi)
{
    if (bi == NULL)
        return 0;
    if (bi->ptr != NULL) {
        BIO_JAVA *j = (BIO_JAVA *)bi->ptr;
        if (bi->init) {
            bi->init = 0;
            TCN_UNLOAD_CLASS(j->cb.env, j->cb.obj);
        }
        OPENSSL_free(bi->ptr);
    }
    bi->ptr = NULL;
    return 1;
}

static int jbs_write(BIO *b, const char *in, int inl)
{
    int ret = 0;
    if (b->init && in != NULL) {
        BIO_JAVA *j = (BIO_JAVA *)b->ptr;
        JNIEnv   *e = j->cb.env;
        if ((*e)->CallIntMethod(e, j->cb.obj,
                                j->cb.mid[0],
                                tcn_new_string(e, in, inl)))
            ret = inl;
    }
    return ret;
}

static int jbs_read(BIO *b, char *out, int outl)
{
    int ret = 0;
    if (b->init && out != NULL) {
        BIO_JAVA *j = (BIO_JAVA *)b->ptr;
        JNIEnv   *e = j->cb.env;
        jobject  o;
        if ((o = (*e)->CallObjectMethod(e, j->cb.obj,
                            j->cb.mid[1], (jint)(outl - 1)))) {
            TCN_ALLOC_CSTRING(o);
            if (J2S(o)) {
                int l = (int)strlen(J2S(o));
                ret = TCN_MIN(outl, l);
                memcpy(out, J2S(o), ret);
            }
            TCN_FREE_CSTRING(o);
        }
    }
    return ret;
}

static int jbs_puts(BIO *b, const char *in)
{
    int ret = 0;
    if (b->init && in != NULL) {
        BIO_JAVA *j = (BIO_JAVA *)b->ptr;
        JNIEnv   *e = j->cb.env;
        ret = (*e)->CallIntMethod(e, j->cb.obj,
                                  j->cb.mid[2],
                                  tcn_new_string(e, in, -1));
    }
    return ret;
}

static int jbs_gets(BIO *b, char *out, int outl)
{
    int ret = 0;
    if (b->init && out != NULL) {
        BIO_JAVA *j = (BIO_JAVA *)b->ptr;
        JNIEnv   *e = j->cb.env;
        jobject  o;
        if ((o = (*e)->CallObjectMethod(e, j->cb.obj,
                            j->cb.mid[3], (jint)(outl - 1)))) {
            TCN_ALLOC_CSTRING(o);
            if (J2S(o)) {
                int l = (int)strlen(J2S(o));
                if (l < outl) {
                    strcpy(out, J2S(o));
                    ret = outl;
                }
            }
            TCN_FREE_CSTRING(o);
        }
    }
    return ret;
}

static long jbs_ctrl(BIO *b, int cmd, long num, void *ptr)
{
    return 0;
}

static BIO_METHOD jbs_methods = {
    BIO_TYPE_FILE,
    "Java Callback",
    jbs_write,
    jbs_read,
    jbs_puts,
    jbs_gets,
    jbs_ctrl,
    jbs_new,
    jbs_free,
    NULL
};

static BIO_METHOD *BIO_jbs()
{
    return(&jbs_methods);
}

TCN_IMPLEMENT_CALL(jlong, SSL, newBIO)(TCN_STDARGS, jlong pool,
                                       jobject callback)
{
    BIO *bio = NULL;
    BIO_JAVA *j;
    jclass cls;

    UNREFERENCED(o);

    if ((bio = BIO_new(BIO_jbs())) == NULL) {
        tcn_ThrowException(e, "Create BIO failed");
        goto init_failed;
    }
    j = (BIO_JAVA *)bio->ptr;
    if ((j = (BIO_JAVA *)bio->ptr) == NULL) {
        tcn_ThrowException(e, "Create BIO failed");
        goto init_failed;
    }
    j->pool = J2P(pool, apr_pool_t *);
    if (j->pool) {
        apr_pool_cleanup_register(j->pool, (const void *)bio,
                                  generic_bio_cleanup,
                                  apr_pool_cleanup_null);
    }

    cls = (*e)->GetObjectClass(e, callback);
    j->cb.env    = e;
    j->cb.mid[0] = (*e)->GetMethodID(e, cls, "write", "(Ljava/lang/String;)I");
    j->cb.mid[1] = (*e)->GetMethodID(e, cls, "read",  "(I)Ljava/lang/String;");
    j->cb.mid[2] = (*e)->GetMethodID(e, cls, "puts",  "(Ljava/lang/String;)I");
    j->cb.mid[3] = (*e)->GetMethodID(e, cls, "gets",  "(I)Ljava/lang/String;");
    /* TODO: Check if method id's are valid */
    j->cb.obj    = (*e)->NewGlobalRef(e, callback);

    bio->init  = 1;
    bio->flags = SSL_BIO_FLAG_CALLBACK;
    return P2J(bio);
init_failed:
    return 0;
}

TCN_IMPLEMENT_CALL(jint, SSL, closeBIO)(TCN_STDARGS, jlong bio)
{
    BIO *b = J2P(bio, BIO *);
    UNREFERENCED_STDARGS;
    SSL_BIO_close(b);
    return APR_SUCCESS;
}

#else
/* OpenSSL is not supported
 * If someday we make OpenSSL optional
 * APR_ENOTIMPL will go here
 */
#error "No OpenSSL Toolkit defined."
#endif
