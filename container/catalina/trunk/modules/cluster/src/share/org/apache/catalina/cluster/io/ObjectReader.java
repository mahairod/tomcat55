/*
 * $Header$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */

package org.apache.catalina.cluster.io;

/**
 * The object reader object is an object used in conjunction with
 * java.nio TCP messages. This object stores the message bytes in a
 * <code>XByteBuffer</code> until a full package has been received.
 * When a full package has been received, the append method will call messageDataReceived
 * on the callback object associated with this object reader.<BR>
 * This object uses an XByteBuffer which is an extendable object buffer that also allows
 * for message encoding and decoding.
 *
 * @author Filip Hanik
 * @version $Revision$, $Date$
 */

import java.nio.channels.SocketChannel;
import java.nio.channels.Selector;
import java.nio.ByteBuffer;
import java.io.IOException;
import org.apache.catalina.cluster.io.XByteBuffer;
public class ObjectReader
{
    private SocketChannel channel;
    private Selector selector;
    private ListenCallback callback;
    private XByteBuffer buffer;

    public ObjectReader( SocketChannel channel,
                         Selector selector,
                         ListenCallback callback )  {
        this.channel = channel;
        this.selector = selector;
        this.callback = callback;
        this.buffer = new XByteBuffer();
    }


    public SocketChannel getChannel()  {
        return this.channel;
    }

    public boolean append(byte[] data,int off,int len) {
        boolean result = false;
        buffer.append(data,off,len);
        if ( buffer.doesPackageExist() ) {
            byte[] b = buffer.extractPackage(true);
            callback.messageDataReceived(b);
            result = true;
        }//end if
        return result;
    }

    public boolean execute() {
        boolean result = false;
        if ( buffer.doesPackageExist() ) {
            byte[] data = buffer.extractPackage(true);
            callback.messageDataReceived(data);
            result = true;
        }//end if
        return result;
    }

    public int write(ByteBuffer buf)
       throws java.io.IOException {
        return getChannel().write(buf);
    }




}