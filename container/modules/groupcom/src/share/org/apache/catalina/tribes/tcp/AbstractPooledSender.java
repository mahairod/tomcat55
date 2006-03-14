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
package org.apache.catalina.tribes.tcp;



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
public abstract class AbstractPooledSender extends PooledSender implements MultiPointSender{
    protected boolean suspect;
    protected boolean useDirectBuffer = true;
    protected int maxRetryAttempts;
    protected boolean autoConnect;
    protected int keepAliveCount;
    public AbstractPooledSender() {
        super();
    }
    
    public void setSuspect(boolean suspect) {
        this.suspect = suspect;
    }

    public void setUseDirectBuffer(boolean useDirectBuffer) {
        this.useDirectBuffer = useDirectBuffer;
    }

    public void setMaxRetryAttempts(int maxRetryAttempts) {
        this.maxRetryAttempts = maxRetryAttempts;
    }

    public void setAutoConnect(boolean autoConnect) {
        this.autoConnect = autoConnect;
    }

    public void setKeepAliveCount(int keepAliveCount) {
        this.keepAliveCount = keepAliveCount;
    }

    public boolean getSuspect() {
        return suspect;
    }

    public boolean getUseDirectBuffer() {
        return useDirectBuffer;
    }

    public int getMaxRetryAttempts() {
        return maxRetryAttempts;
    }

    public boolean isAutoConnect() {
        return autoConnect;
    }

    public int getKeepAliveCount() {
        return keepAliveCount;
    }
}
