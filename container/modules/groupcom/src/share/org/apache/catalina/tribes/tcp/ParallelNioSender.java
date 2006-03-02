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
package org.apache.catalina.tribes.tcp;


import org.apache.catalina.tribes.ChannelMessage;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.io.XByteBuffer;
import org.apache.catalina.tribes.io.ClusterData;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class ParallelNioSender {
    protected long timeout;
    protected boolean waitForAck;
    
    public ParallelNioSender(long timeout, boolean waitForAck) {
        this.timeout = timeout;
        this.waitForAck = waitForAck;
    }
    
    
    public synchronized void sendMessage(Member mbr, ChannelMessage msg) throws ChannelException {
        long start = System.currentTimeMillis();
        byte[] data = XByteBuffer.createDataPackage((ClusterData)msg);
        
        
    }
    
    protected synchronized

}