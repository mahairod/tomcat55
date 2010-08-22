/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
package org.apache.catalina.tribes.group.interceptors;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.catalina.tribes.ChannelMessage;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.group.InterceptorPayload;
import org.apache.catalina.tribes.transport.bio.util.LinkObject;

/**
 * 
 * Same implementation as the MessageDispatchInterceptor
 * except it uses an atomic long for the currentSize calculation
 * and uses a thread pool for message sending.
 * 
 * @author Filip Hanik
 * @version 1.0
 */

public class MessageDispatch15Interceptor extends MessageDispatchInterceptor {

    protected AtomicLong currentSize = new AtomicLong(0);
    protected ThreadPoolExecutor executor = null;
    protected int maxThreads = 10;
    protected int maxSpareThreads = 2;
    protected long keepAliveTime = 5000;
    protected LinkedBlockingQueue<Runnable> runnablequeue = new LinkedBlockingQueue<Runnable>();

    @Override
    public long getCurrentSize() {
        return currentSize.get();
    }

    @Override
    public long addAndGetCurrentSize(long inc) {
        return currentSize.addAndGet(inc);
    }

    @Override
    public long setAndGetCurrentSize(long value) {
        currentSize.set(value);
        return value;
    }
    
    @Override
    public boolean addToQueue(ChannelMessage msg, Member[] destination, InterceptorPayload payload) {
        final LinkObject obj = new LinkObject(msg,destination,payload);
        Runnable r = new Runnable() {
            public void run() {
                sendAsyncData(obj);
            }
        };
        executor.execute(r);
        return true;
    }

    @Override
    public LinkObject removeFromQueue() {
        return null; //not used, thread pool contains its own queue.
    }

    @Override
    public void startQueue() {
        if ( run ) return;
        executor = new ThreadPoolExecutor(maxSpareThreads,maxThreads,keepAliveTime,TimeUnit.MILLISECONDS,runnablequeue);
        run = true;
    }

    @Override
    public void stopQueue() {
        run = false;
        executor.shutdownNow();
        setAndGetCurrentSize(0);
        runnablequeue.clear();
    }

    public long getKeepAliveTime() {
        return keepAliveTime;
    }

    public int getMaxSpareThreads() {
        return maxSpareThreads;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public void setKeepAliveTime(long keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }

    public void setMaxSpareThreads(int maxSpareThreads) {
        this.maxSpareThreads = maxSpareThreads;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

}