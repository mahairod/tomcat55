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


package org.apache.catalina.connector;


import java.io.InputStream;
import java.io.IOException;
import javax.servlet.ServletInputStream;
import org.apache.catalina.Request;
import org.apache.catalina.util.StringManager;


/**
 * Convenience implementation of <b>ServletInputStream</b> that works with
 * the standard implementations of <b>Request</b>.  If the content length has
 * been set on our associated Request, this implementation will enforce
 * not reading more than that many bytes on the underlying stream.
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public class RequestStream
    extends ServletInputStream {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a servlet input stream associated with the specified Request.
     *
     * @param request The associated request
     */
    public RequestStream(Request request) {

	super();
	closed = false;
	count = 0;
	length = request.getRequest().getContentLength();
	stream = request.getStream();

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The default buffer size for our input buffer.
     */
    protected static final int BUFFER_SIZE = 512;


    /**
     * The input buffer for our stream.
     */
    protected byte buffer[] = new byte[BUFFER_SIZE];


    /**
     * The current number of bytes in the buffer.
     */
    protected int bufferCount = 0;


    /**
     * The current position in the buffer.
     */
    protected int bufferPosition = 0;



    /**
     * Has this stream been closed?
     */
    protected boolean closed = false;


    /**
     * The number of bytes which have already been returned by this stream.
     */
    protected int count = 0;


    /**
     * The content length past which we will not read, or -1 if there is
     * no defined content length.
     */
    protected int length = -1;


    /**
     * The localized strings for this package.
     */
    protected static StringManager sm =
	StringManager.getManager(Constants.Package);


    /**
     * The underlying input stream from which we should read data.
     */
    protected InputStream stream = null;


    // --------------------------------------------------------- Public Methods


    /**
     * Return the number of bytes that can be read (or skipped over) from this
     * input stream without blocking by the next caller of a method on this
     * input stream.
     *
     * @exception IOException if an input/output error occurs
     */
    public int available() throws IOException {

	// Has this stream been closed?
	if (closed)
	    throw new IOException(sm.getString("requestStream.read.closed"));

	// Calculate the number of bytes that are available
	int available = (bufferCount - bufferPosition) + stream.available();
	if ((length > 0) && ((count + available) > length))
	    available = length - count;
	return (available);

    }


    /**
     * Close this input stream.  No physical level I-O is performed, but
     * any further attempt to read from this stream will throw an IOException.
     * If a content length has been set but not all of the bytes have yet been
     * consumed, the remaining bytes will be swallowed.
     *
     * @exception IOException if an input/output error occurs
     */
    public void close() throws IOException {

	if (closed)
	    throw new IOException(sm.getString("requestStream.close.closed"));

	if ((length > 0) && (count < length)) {
	    long remaining = length - count;
	    while (remaining > 0) {
	        long skipped = skip(remaining);
	        if (skipped == 0L)
		    throw new IOException
		      (sm.getString("requestStream.close.skip"));
		remaining -= skipped;
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
    public int read() throws IOException {

	// Has this stream been closed?
	if (closed)
	    throw new IOException(sm.getString("requestStream.read.closed"));

	// Have we read the specified content length already?
	if ((length >= 0) && (count >= length))
	    return (-1);	// End of file indicator

	// Refill the buffer if needed
	if (bufferPosition >= bufferCount) {
	    fill();
	    if (bufferPosition >= bufferCount)
	        return (-1);
	}

	// Grab and return the next byte from the buffer
	int b = buffer[bufferPosition++];
	count++;
	return (b);

    }


    /**
     * Read some number of bytes from the input stream, and store them
     * into the buffer array b.  The number of bytes actually read is
     * returned as an integer.  This method blocks until input data is
     * available, end of file is detected, or an exception is thrown.
     *
     * @param b The buffer into which the data is read
     *
     * @exception IOException if an input/output error occurs
     */
    public int read(byte b[]) throws IOException {

	return (read(b, 0, b.length));

    }


    /**
     * Read up to <code>len</code> bytes of data from the input stream
     * into an array of bytes.  An attempt is made to read as many as
     * <code>len</code> bytes, but a smaller number may be read,
     * possibly zero.  The number of bytes actually read is returned as
     * an integer.  This method blocks until input data is available,
     * end of file is detected, or an exception is thrown.
     *
     * @param b The buffer into which the data is read
     * @param off The start offset into array <code>b</code> at which
     *  the data is written
     * @param len The maximum number of bytes to read
     *
     * @exception IOException if an input/output error occurs
     */
    public int read(byte b[], int off, int len) throws IOException {

        // Has this stream been closed?
	if (closed)
	    throw new IOException(sm.getString("requestStream.read.closed"));

	// Have we read the specified content length already?
	if ((length >= 0) && (count >= length))
	    return (-1);	// End of file indicator

	// Refill the buffer if needed
	int available = bufferCount - bufferPosition;
	if (available <= 0) {
	    fill();
	    available = bufferCount - bufferPosition;
	    if (available <= 0)
	        return (-1);
	}

	// Copy as many bytes as we can from the buffer
	if (available < len)
	    len = available;
	System.arraycopy(buffer, bufferPosition, b, off, len);
	bufferPosition += len;
	count += len;
	return (len);

    }


    /**
     * Read into an array of bytes until all requested bytes have been
     * read or a '\n' is encountered, in which case the '\n' is read into
     * the array as well.
     *
     * @param b The buffer where data is stored
     * @param off The start offset into the buffer
     * @param len The maximum number of bytes to be returned
     *
     * @return The actual number of bytes read, or -1 if the end of the
     *  stream is reached or the byte limit has been exceeded
     *
     * @exception IOException if an input/output error occurs
     */
    public int readLine(byte b[], int off, int len) throws IOException {

        // Has this stream been closed?
	if (closed)
	    throw new IOException(sm.getString("requestStream.read.closed"));

	// Have we read the specified content length already?
	if ((length >= 0) && (count >= length))
	    return (-1);	// End of file indicator

	int available;          // Bytes available in buffer
	int readlen;            // Amount to be read by copyLine()
	int remain = len;       // Amount remaining to be read
	int newlen;             // Amount read by copyLine()
        int totalread;          // Total amount read so far

	// Refill the buffer if needed
	available = bufferCount - bufferPosition;
	if (available <= 0) {
	    fill();
	    available = bufferCount - bufferPosition;
	    if (available <= 0)
	        return (-1);
	}

	// Determine how many bytes we should try to read
	if (available < len)
	    readlen = available;
	else
	    readlen = len;

	// Copy the initial portion of this line
	newlen = copyLine(buffer, bufferPosition, b, off, readlen);
	bufferPosition += newlen;
	count += newlen;
	remain -= newlen;
	totalread = newlen;
	if (totalread == 0) // Cannot happen
	    return (-1);

	// Copy additional chunks until a newline is encountered
	while ((remain > 0) && (b[off + totalread - 1] != '\n')) {
	    fill();
	    available = bufferCount - bufferPosition;
	    if (available <= 0)
	        return (totalread); // The stream is finished
	    if (available < remain)
	        readlen = available;
	    else
	        readlen = remain;
	    newlen = copyLine(buffer, bufferPosition, b, off, readlen);
	    bufferPosition += newlen;
	    count += newlen;
	    remain -= newlen;
	    totalread += newlen;
        }

	return (totalread);

    }


    /**
     * Skip the specified number of bytes of input, and return the actual
     * number of bytes skipped.
     *
     * @param n The number of bytes to be skipped
     *
     * @exception IOException if an input/output error occurs
     */
    public long skip(long n) throws IOException {

        if ((length > 0) && (count >= length))
	    return (0);

	long remaining = n;
	if (length > 0)
	    remaining = Math.min(remaining, (length - count));
	long skipped = 0;
	while (remaining > 0) {
	    int available = bufferCount - bufferPosition;
	    if (available <= 0) {
	        fill();
		available = bufferCount - bufferPosition;
		if (available <= 0)
		    return (skipped);
	    }
	    if (remaining < available)
	        available = (int) remaining;
	    skipped += available;
	    remaining -= available;
	    bufferPosition += available;
	    count += available;
	}
	return (skipped);

    }


    // ------------------------------------------------- Protected Methods


    /**
     * Copy up to a line of data from source to destination buffer.
     */
    protected int copyLine(byte source[], int srcOff,
			   byte dest[], int destOff, int len) {

        int off = srcOff;
	while ((len-- > 0) && (source[off++] != '\n'))
	    ;
	System.arraycopy(source, srcOff, dest, destOff, off - srcOff);
	return (off - srcOff);

    }


    /**
     * Refill the buffer from the underlying stream.
     *
     * @exception IOException if an input/output error occurs
     */
    protected void fill() throws IOException {

        bufferPosition = 0;
        bufferCount = 0;
        int len = buffer.length;
	if (length > 0)
	    len = Math.min(len, (length - count));
	if (len > 0) {
	    len = stream.read(buffer, 0, len);
	    if (len > 0)
		bufferCount = len;
	}

    }


}
