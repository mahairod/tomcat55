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
 * Description: Context handling (Autoconf)                                *
 * Author:      Henri Gomez <hgomez@slib.fr>                               *
 * Version:     $Revision$                                           *
 ***************************************************************************/

#include "jk_global.h"
#include "jk_context.h"
#include "jk_ajp_common.h"

/*
 * Init the context info struct
 */

int context_open(jk_context_t *c)
{
    if (c) {
        jk_open_pool(&c->p, c->buf, sizeof(jk_pool_atom_t) * SMALL_POOL_SIZE);
		c->virtual  = NULL;
		c->cbase  	= NULL,
        c->status   = AJP14_CONTEXT_DOWN;
        c->size  	= 0;
        c->capacity = 0;
        c->uris     = NULL;
        return JK_TRUE;
    }

    return JK_FALSE;
}


/*
 * Create the context info struct
 */

int context_alloc(jk_context_t **c)
{
    if (c) {
        return context_open(*c = (jk_context_t *)malloc(sizeof(jk_context_t)));
    }
    
    return JK_FALSE;
}
 

/*
 * Close the context info struct
 */

int context_close(jk_context_t *c)
{
    if (c) {
        jk_close_pool(&c->p);
        return JK_TRUE;
    }

    return JK_FALSE;
}


/*
 * Delete the context info struct
 */

int context_free(jk_context_t **c)
{
    if (c && *c) {
        context_close(*c);  
        free(*c);
        *c = NULL;
		return JK_TRUE;

    }
    
    return JK_FALSE;
}


/*
 * Find a context in the Contextes List
 */

jk_context_t * context_find(jk_context_list_t *l, char * virtual, char * cbase)
{
	int	i;

	if (! l)
		return NULL;

	for (i = 0; i < l->ncontext; i++) {
		if (virtual) 
			if (strcmp(l->contexts[i]->virtual, virtual))
				continue;

		if (cbase) 
			if (! strcmp(l->contexts[i]->cbase, cbase))
				return (l->contexts[i]);
	}

	return NULL;
}

/*
 * Context Memory Managment
 */

static int context_realloc(jk_context_t *c)
{
    if (c->size == c->capacity) {
        char **uris;
        int  capacity = c->capacity + CONTEXT_INC_SIZE;

        uris = (char **)jk_pool_alloc(&c->p, sizeof(char *) * capacity);

        if (! uris)
			return JK_FALSE;

		memcpy(uris, c->uris, sizeof(char *) * c->capacity);

        c->uris = uris;
        c->capacity = capacity;
    }

    return JK_TRUE;
}


/*
 * Add an URI to context
 */

int  context_add_uri(jk_context_t *c, char * uri)
{
    int i;

    if (! c || ! uri)
        return JK_FALSE;

	for (i = 0 ; i < c->size ; i++) {
		if (! strcmp(c->uris[i], uri)) {
                return JK_TRUE;
            }
        }

	context_realloc(c);

	if (c->size >= c->capacity) 
		return JK_FALSE;

	c->uris[c->size] = jk_pool_strdup(&c->p, uri);
	c->size++;
	return JK_TRUE;
}


