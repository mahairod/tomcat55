/*
 *  Copyright 1999-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/***************************************************************************
 * Description: Status worker header file                                  *
 * Author:      Mladen Turk <mturk@jboss.com>                              * 
 * Version:     $Revision$                                           *
 ***************************************************************************/

#ifndef JK_STATUS_H
#define JK_STATUS_H

#include "jk_logger.h"
#include "jk_service.h"

#ifdef __cplusplus
extern "C"
{
#endif                          /* __cplusplus */

#define JK_STATUS_WORKER_NAME     ("status")
#define JK_STATUS_DEF_DOMAIN_NAME ("unknown")

int JK_METHOD status_worker_factory(jk_worker_t **w,
                                    const char *name, jk_logger_t *l);

#ifdef __cplusplus
}
#endif                          /* __cplusplus */
#endif                          /* JK_STATUS_H */
