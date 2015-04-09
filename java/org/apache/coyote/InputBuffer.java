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

import java.io.IOException;

import org.apache.tomcat.util.buf.ByteChunk;

/**
 * This class is only for internal use in the protocol implementation. All
 * reading from Tomcat (or adapter) should be done using Request.doRead().
 */
public interface InputBuffer {

    /**
     * Read from the input stream into the given buffer.
     * IMPORTANT: the current model assumes that the protocol will 'own' the
     * buffer and return a pointer to it in ByteChunk (i.e. the param will
     * have chunk.getBytes()==null before call, and the result after the call).
     *
     * @param chunk The buffer to read data into.
     *
     * @return The number of bytes that have been added to the buffer or -1 for
     *         end of stream
     *
     * @throws IOException If an I/O error occurs reading from the input stream
     */
    public int doRead(ByteChunk chunk) throws IOException;
}
