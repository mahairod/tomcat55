/*
 * Copyright 1999,2004-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.catalina.cluster.group;

/**
 * The channel coordinator object coordinates the membership service,
 * the sender and the receiver.
 * This is the last interceptor in the chain.
 * @author Filip Hanik
 * @version $Revision: 304032 $, $Date: 2005-07-27 10:11:55 -0500 (Wed, 27 Jul 2005) $
 */
public class ChannelCoordinator extends ChannelInterceptorBase {
    public ChannelCoordinator() {
        super();
    }
   
}
