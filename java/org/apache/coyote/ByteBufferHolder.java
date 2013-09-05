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
package org.apache.coyote;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simple wrapper for a {@link ByteBuffer} that remembers if the buffer has been
 * flipped or not.
 */
public class ByteBufferHolder {

    private final ByteBuffer buf;
    private final AtomicBoolean flipped;

    public ByteBufferHolder(ByteBuffer buf, boolean flipped) {
       this.buf = buf;
       this.flipped = new AtomicBoolean(flipped);
    }


    public ByteBuffer getBuf() {
        return buf;
    }


    public boolean isFlipped() {
        return flipped.get();
    }


    public boolean flip() {
        if (flipped.compareAndSet(false, true)) {
            buf.flip();
            return true;
        } else {
            return false;
        }
    }


    public boolean hasData() {
        if (flipped.get()) {
            return buf.remaining()>0;
        } else {
            return buf.position()>0;
        }
    }
}