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


package org.apache.catalina.connector.http;

import java.io.IOException;
import org.apache.catalina.Request;
import org.apache.catalina.connector.RequestStream;

/**
 * 
 *
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 */
public class HttpRequestStream extends RequestStream {
    
    
    // ----------------------------------------------------------- Constructors


    /**
     * Construct a servlet input stream associated with the specified Request.
     *
     * @param request The associated request
     * @param response The associated response
     */
    public HttpRequestStream(HttpRequestImpl request, 
                             HttpResponseImpl response) {

	super(request);
        String transferEncoding = request.getHeader("Transfer-Encoding");
        
        chunk = ((transferEncoding != null) 
                 && (transferEncoding.indexOf("chunked") != -1));
        
        if ((!chunk) && (length == -1)) {
            // Ask for connection close
            response.addHeader("Connection", "close");
        }

        http11 = request.getProtocol().equals("HTTP/1.1");
        
        System.out.println("HTTP/1.1: " + http11);
        
    }
    
    
    // ----------------------------------------------------- Instance Variables
    
    
    /**
     * Use chunking ?
     */
    protected boolean chunk = false;
    
    
    /**
     * True if the final chunk was found.
     */
    protected boolean endChunk = false;
    
    
    /**
     * Chunk buffer.
     */
    protected byte[] chunkBuffer = null;
    
    
    /**
     * Chunk length.
     */
    protected int chunkLength = 0;
    
    
    /**
     * Chunk buffer position.
     */
    protected int chunkPos = 0;
    
    
    /**
     * HTTP/1.1 flag.
     */
    protected boolean http11 = false;
    
    
    // --------------------------------------------------------- Public Methods
    
    
    /**
     * Close this input stream.  No physical level I-O is performed, but
     * any further attempt to read from this stream will throw an IOException.
     * If a content length has been set but not all of the bytes have yet been
     * consumed, the remaining bytes will be swallowed.
     */
    public void close()
        throws IOException {
        
	if (closed)
	    throw new IOException(sm.getString("requestStream.close.closed"));
        
        if (chunk) {
            
            while (!endChunk) {
                int b = read();
                if (b < 0)
                    break;
            }
            
        } else {
            
            if (http11 && (length > 0)) {
                while (count < length) {
                    System.out.println("Consuming bytes");
                    int b = read();
                    if (b < 0)
                        break;
                }
            }
            
        }
        
	closed = true;
        
    }
    
    
    /**
     * Read and return a single byte from this input stream, or -1 if end of
     * file has been encountered.
     *
     * @exception IOException if an input/output error occurs
     */
    public int read()
        throws IOException {
        
        // Has this stream been closed?
        if (closed)
            throw new IOException(sm.getString("requestStream.read.closed"));
        
        if (chunk) {
            
            if (endChunk)
                return (-1);
            
            if ((chunkBuffer == null)
                || (chunkPos == chunkLength)) {
                
                chunkPos = 0;
                
                try {
                    chunkLength = Integer.parseInt(readLine(), 16);
                } catch (NumberFormatException e) {
                    // Critical error, unable to parse the chunk length
                    chunkLength = 0;
                    chunk = false;
                    close();
                    return -1;
                }
                
                if (chunkLength == 0) {
                    
                    // Skipping trailing headers, if any
                    String trailingLine = readLine();
                    while (!trailingLine.equals(""))
                        trailingLine = readLine();
                    endChunk = true;
                    return (-1);
                    // TODO : Should the stream be automatically closed ?
                    
                } else {
                    
                    if ((chunkBuffer == null)
                        || (chunkLength > chunkBuffer.length))
                        chunkBuffer = new byte[chunkLength];
                    
                    // Now read the whole chunk into the buffer
                    
                    int nbRead = 0;
                    int currentRead = 0;
                    
                    while (nbRead < chunkLength) {
                        currentRead = 
                            super.read(chunkBuffer, nbRead, 
                                       chunkLength - nbRead);
                        if (currentRead == -1)
                            throw new IOException
                                (sm.getString("requestStream.read.error"));
                        nbRead += currentRead;
                    }
                    
                    // Skipping the CRLF
                    super.read();
                    super.read();
                    
                }
                
            }
            
            return (chunkBuffer[chunkPos++]);
            
        } else {
            
            return (super.read());
            
        }
        
    }


    // -------------------------------------------------------- Private Methods
    

    /**
     * Reads the input stream, one line at a time. Reads bytes into an array, 
     * until it reads a certain number of bytes or reaches a newline character,
     * which it reads into the array as well.
     * 
     * @param input Input stream on which the bytes are read
     * @return The line that was read, or <code>null</code> if end-of-file
     *  was encountered
     * @exception IOException	if an input or output exception has occurred
     */
    private String readLine() 
        throws IOException {
        
	StringBuffer sb = new StringBuffer();
	while (true) {
	    int ch = super.read();
	    if (ch < 0) {
		if (sb.length() == 0) {
		    return (null);
		} else {
		    break;
		}
	    } else if (ch == '\r') {
		continue;
	    } else if (ch == '\n') {
		break;
	    }
	    sb.append((char) ch);
	}
	return (sb.toString());
        
    }
    
    
}
