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

/** Config object. It's more-or-less independent of the config source
    or representation. 
 */

#ifndef JK_CONFIG_H
#define JK_CONFIG_H

#include "jk_pool.h"
#include "jk_env.h"
#include "jk_logger.h"

#ifdef __cplusplus
extern "C" {
#endif /* __cplusplus */

struct jk_pool;
struct jk_map;
struct jk_env;
struct jk_config;
typedef struct jk_config jk_config_t;

/**
 *
 */
struct jk_config {
    struct jk_bean *mbean;
    
    /* Parse and process a property. It'll locate the object and call the
     * setAttribute on it.
     */
    int (*setPropertyString)(struct jk_env *env, struct jk_config *cfg,
                             char *name, char *value); 

    /* Set an attribute for a jk object. This should be the
     * only method called to configure objects. The implementation
     * should update the underlying repository in addition to setting
     * the runtime value. Calling setAttribute on the object directly
     * will only set the runtime value.
     */
    int (*setProperty)(struct jk_env *env, struct jk_config *cfg,
                       struct jk_bean *target, char *name, char *value); 
    
    

    /** Read the properties from the file, doing $(prop) substitution
     *  The source can be a file ( or uri ).
     */
    /*     int (*read)(struct jk_env *env, jk_config_t *m, const char *source); */

    /** Write the properties, preserving the original format. Is it possible ?
     */
    /* int (*write)(struct jk_env *env, jk_config_t *m, const char *dest); */

    
    /* ========== Utilities and 'pull' access   ========== */
    
    /** For multi-value properties, return the concatenation
     *  of all values.
     *
     * @param sep Separators used to separate multi-values and
     *       when concatenating the values, NULL for none. The first
     *       char will be used on the result, the other will be
     *       used to split. ( i.e. the map may either have multiple
     *       values or values separated by one of the sep's chars )
     *    
     */
    /*     char *(*getValuesString)(struct jk_env *env, struct jk_map *m, */
    /*                              struct jk_pool *resultPool, */
    /*                              char *name, char *sep ); */
    
    
    /** For multi-value properties, return the array containing
     * all values.
     *
     * @param sep Optional separator, it'll be used to split existing values.
     *            Curently only single-char separators are supported. 
     */
    /*     char **(*getValues)(struct jk_env *env, struct jk_map *m, */
    /*                         struct jk_pool *resultPool, */
    /*                         char *name, char *sep, int *count); */
    
    /**
     *  Replace $(property) and ${property} in value.
     */
    /*     char *(*replaceProperties)(struct jk_env *env, jk_config_t *m, */
    /*                                char *value, struct jk_pool *resultPool ); */
    
    
    
    /* Private data */
    struct jk_pool *pool;
    void *_private;
    struct jk_workerEnv *workerEnv;
    struct jk_map *map;

    char *file;
    
    char *section;
};

/** Util: Split a string in components. */
char **jk2_config_split(struct jk_env *env, struct jk_pool *pool,
                        const char *listStr, const char *sep,
                        unsigned *list_len );

int jk2_config_str2int(struct jk_env *env, char *val );

char *jk2_config_replaceProperties(struct jk_env *env, struct jk_map *m,
                                   struct jk_pool *resultPool, 
                                   char *value);

int jk2_config_read(struct jk_env *env, struct jk_config *cfg,
                    struct jk_map *map, const char *f);

#ifdef __cplusplus
}
#endif /* __cplusplus */

#endif /* JK_CONFIG_H */
