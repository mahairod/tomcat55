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
package org.apache.catalina.cluster;

import java.io.Serializable;

public interface ChannelListener {

    /**
     * Receive a message from the cluster.
     * @param msg ClusterMessage
     * @return ClusterMessage - response to the message sent. <br>
     * The response object may be ignored and is not required for the 
     * implementation to send back to the sender.
     */
    public void messageReceived(Serializable msg, Member sender);

    public boolean accept(Serializable msg, Member sender);

    public boolean equals(Object listener);

    public int hashCode();

}
