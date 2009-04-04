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


package org.apache.tomcat.lite;

import java.io.IOException;

import javax.servlet.ServletOutputStream;

import org.apache.tomcat.lite.coyote.MessageWriter;

/**
 * Coyote implementation of the servlet output stream.
 * 
 * @author Costin Manolache
 * @author Remy Maucherat
 */
public class ServletOutputStreamImpl 
    extends ServletOutputStream {


    // ----------------------------------------------------- Instance Variables


    protected MessageWriter ob;


    // ----------------------------------------------------------- Constructors


    public ServletOutputStreamImpl(MessageWriter ob) {
        this.ob = ob;
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Prevent cloning the facade.
     */
    protected Object clone()
        throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }


    // -------------------------------------------------------- Package Methods


    /**
     * Clear facade.
     */
    void clear() {
        ob = null;
    }


    // --------------------------------------------------- OutputStream Methods


    public void write(int i)
        throws IOException {
        ob.writeByte(i);
    }


    public void write(byte[] b)
        throws IOException {
        write(b, 0, b.length);
    }


    public void write(byte[] b, int off, int len)
        throws IOException {
        ob.write(b, off, len);
    }


    /**
     * Will send the buffer to the client.
     */
    public void flush()
        throws IOException {
        ob.flush();
    }


    public void close()
        throws IOException {
        ob.close();
    }


    // -------------------------------------------- ServletOutputStream Methods


    public void print(String s)
        throws IOException {
        ob.write(s);
    }


}

