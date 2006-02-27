/*
 * Copyright 1999,2004 The Apache Software Foundation.
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

package org.apache.catalina.tribes.io;

import org.apache.catalina.tribes.ChannelMessage;



/**
 * The listen callback interface is used by the replication system
 * when data has been received. The interface does not care about
 * objects and marshalling and just passes the bytes straight through.
 * @author Filip Hanik
 * @version $Revision: 303987 $, $Date: 2005-07-08 15:50:30 -0500 (Fri, 08 Jul 2005) $
 */
public interface ListenCallback
{
    /**
     * This method is invoked on the callback object to notify it that new data has
     * been received from one of the cluster nodes.
     * @param data - the message bytes received from the cluster/replication system
     */
     public void messageDataReceived(ChannelMessage data);
     
}