/*
 * Copyright 1999,2004-2006 The Apache Software Foundation.
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
package org.apache.catalina.tribes.tcp.nio;

import java.io.IOException;

import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.ChannelMessage;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.tcp.AbstractPooledSender;
import org.apache.catalina.tribes.tcp.DataSender;
import org.apache.catalina.tribes.tcp.MultiPointSender;

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
public class PooledParallelSender extends AbstractPooledSender implements MultiPointSender {
    public PooledParallelSender() {
        super();
    }
    
    public void sendMessage(Member[] destination, ChannelMessage message) throws ChannelException {
        ParallelNioSender sender = (ParallelNioSender)getSender();
        try {
            sender.sendMessage(destination, message);
        }finally {
            returnSender(sender);
        }
    }

    public DataSender getNewDataSender() {
        try {
            ParallelNioSender sender = new ParallelNioSender();
            sender.setTimeout(getTimeout());
            sender.setWaitForAck(getWaitForAck());
            sender.setMaxRetryAttempts(getMaxRetryAttempts()); 
            sender.setUseDirectBuffer(getUseDirectBuffer());
            sender.setRxBufSize(getRxBufSize());
            sender.setTxBufSize(getTxBufSize());
            return sender;
        } catch ( IOException x ) {
            throw new IllegalStateException("Unable to open NIO selector.",x);
        }
    }

    public void memberAdded(Member member) {
    
    }
    
    public void memberDisappeared(Member member) {
        //disconnect senders
    }    
}