/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.coyote.http2;

import java.util.HashSet;
import java.util.Set;

/**
 * Used to managed prioritisation.
 */
abstract class AbstractStream {

    private final Integer identifier;

    private volatile AbstractStream parentStream = null;
    private final Set<AbstractStream> childStreams = new HashSet<>();
    private long windowSize = ConnectionSettings.DEFAULT_WINDOW_SIZE;
    private final Object windowSizeLock = new Object();

    public Integer getIdentifier() {
        return identifier;
    }


    public AbstractStream(Integer identifier) {
        this.identifier = identifier;
    }


    void detachFromParent() {
        if (parentStream != null) {
            parentStream.getChildStreams().remove(this);
            parentStream = null;
        }
    }


    void addChild(AbstractStream child) {
        child.setParent(this);
        childStreams.add(child);
    }


    private void setParent(AbstractStream parent) {
        this.parentStream = parent;
    }


    boolean isDescendant(AbstractStream stream) {
        if (childStreams.contains(stream)) {
            return true;
        }
        for (AbstractStream child : childStreams) {
            if (child.isDescendant(stream)) {
                return true;
            }
        }
        return false;
    }


    AbstractStream getParentStream() {
        return parentStream;
    }


    void setParentStream(AbstractStream parentStream) {
        this.parentStream = parentStream;
    }


    Set<AbstractStream> getChildStreams() {
        return childStreams;
    }


    protected void setWindowSize(long windowSize) {
        synchronized (windowSizeLock) {
            this.windowSize = windowSize;
        }
    }


    protected long getWindowSize() {
        synchronized (windowSizeLock) {
            return windowSize;
        }
    }


    /**
     * @param increment
     * @throws Http2Exception
     */
    protected void incrementWindowSize(int increment) throws Http2Exception {
        synchronized (windowSizeLock) {
            // Overflow protection
            if (Long.MAX_VALUE - increment > windowSize) {
                windowSize = Long.MAX_VALUE;
            } else {
                windowSize += increment;
            }
        }
    }


    protected void decrementWindowSize(int decrement) {
        // No need for overflow protection here. Decrement can never be larger
        // the Integer.MAX_VALUE and once windowSize does negative no further
        // decrements are permitted
        synchronized (windowSizeLock) {
            windowSize -= decrement;
        }
    }


    protected abstract String getConnectionId();

    protected abstract int getWeight();
}
