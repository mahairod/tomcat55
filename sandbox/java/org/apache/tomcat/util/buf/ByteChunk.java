/*
 *  Copyright 1999-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.tomcat.util.buf;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;

/*
 * In a server it is very important to be able to operate on
 * the original byte[] without converting everything to chars.
 * Some protocols are ASCII only, and some allow different
 * non-UNICODE encodings. The encoding is not known beforehand,
 * and can even change during the execution of the protocol.
 * ( for example a multipart message may have parts with different
 *  encoding )
 *
 * For HTTP it is not very clear how the encoding of RequestURI
 * and mime values can be determined, but it is a great advantage
 * to be able to parse the request without converting to string.
 */

// TODO: This class could either extend ByteBuffer, or better a ByteBuffer inside
// this way it could provide the search/etc on ByteBuffer, as a helper.

/**
 * This class is used to represent a chunk of bytes, and
 * utilities to manipulate byte[].
 *
 * The buffer can be modified and used for both input and output.
 *
 * There are 2 modes: The chunk can be associated with a sink - ByteInputChannel or ByteOutputChannel,
 * which will be used when the buffer is empty ( on input ) or filled ( on output ).
 * For output, it can also grow. This operating mode is selected by calling setLimit() or
 * allocate(initial, limit) with limit != -1.
 *
 * Various search and append method are defined - similar with String and StringBuffer, but
 * operating on bytes.
 *
 * This is important because it allows processing the http headers directly on the received bytes,
 * without converting to chars and Strings until the strings are needed. In addition, the charset
 * is determined later, from headers or user code.
 *
 *
 * @author dac@sun.com
 * @author James Todd [gonzo@sun.com]
 * @author Costin Manolache
 * @author Remy Maucherat
 */
public final class ByteChunk implements Cloneable, Serializable {

    /** Input interface, used when the buffer is emptiy
     *
     * Same as java.nio.channel.ReadableByteChannel
     */
    public static interface ByteInputChannel {
        /** 
         * Read new bytes ( usually the internal conversion buffer ).
         * The implementation is allowed to ignore the parameters, 
         * and mutate the chunk if it wishes to implement its own buffering.
         */
        public int realReadBytes(byte cbuf[], int off, int len)
            throws IOException;
    }

    /** Same as java.nio.channel.WrittableByteChannel.
     */
    public static interface ByteOutputChannel {
        /** 
         * Send the bytes ( usually the internal conversion buffer ).
         * Expect 8k output if the buffer is full.
         */
        public void realWriteBytes(byte cbuf[], int off, int len)
            throws IOException;
    }

    // --------------------

    /** Default encoding used to convert to strings. It should be UTF8,
	as most standards seem to converge, but the servlet API requires
	8859_1, and this object is used mostly for servlets. 
    */
    public static final String DEFAULT_CHARACTER_ENCODING="ISO-8859-1";
        
    // byte[]
    //private byte[] buff;
    private ByteBuffer bb;
    
    private int start=0;
    private int end;

    private String enc;

    private boolean isSet=false; // XXX

    // How much can it grow, when data is added
    private int limit=-1;

    private ByteInputChannel in = null;
    private ByteOutputChannel out = null;

    private boolean isOutput=false;
    private boolean optimizedWrite=true;
    
    /**
     * Creates a new, uninitialized ByteChunk object.
     */
    public ByteChunk() {
    }

    public ByteChunk( int initial ) {
	allocate( initial, -1 );
    }

    //--------------------
    public ByteChunk getClone() {
	try {
	    return (ByteChunk)this.clone();
	} catch( Exception ex) {
	    return null;
	}
    }

    public boolean isNull() {
	return ! isSet; // buff==null;
    }
    
    /**
     * Resets the message buff to an uninitialized state.
     */
    public void recycle() {
	//	buff = null;
	enc=null;
	start=0;
	end=0;
	isSet=false;
    }

    public void reset() {
	bb=null;
    }

    // -------------------- Setup --------------------

    public void allocate( int initial, int limit  ) {
	isOutput=true;
	if( bb==null || bb.capacity() < initial ) {
	    //buff=new byte[initial];
            bb=ByteBuffer.allocate(initial);
	}    
	this.limit=limit;
	start=0;
	end=0;
	isSet=true;
    }

    /**
     * Sets the message bytes to the specified subarray of bytes.
     * 
     * @param b the ascii bytes
     * @param off the start offset of the bytes
     * @param len the length of the bytes
     */
    public void setBytes(byte[] b, int off, int len) {
        //buff = b;
        bb=ByteBuffer.wrap( b, off, len );
        start = off;
        end = start+ len;
        isSet=true;
    }

    public void setOptimizedWrite(boolean optimizedWrite) {
        this.optimizedWrite = optimizedWrite;
    }

    public void setEncoding( String enc ) {
	this.enc=enc;
    }
    public String getEncoding() {
        if (enc == null)
            enc=DEFAULT_CHARACTER_ENCODING;
        return enc;
    }

    /**
     * Returns the message bytes.
     */
    public byte[] getBytes() {
	return getBuffer();
    }

    /**
     * Returns the message bytes.
     */
    public byte[] getBuffer() {
	return bb.array();
    }

    /**
     * Returns the start offset of the bytes.
     * For output this is the end of the buffer.
     */
    public int getStart() {
	return start;
    }

    public int getOffset() {
	return start;
    }

    public void setOffset(int off) {
        if (end < off ) end=off;
	start=off;
    }

    /**
     * Returns the length of the bytes.
     * XXX need to clean this up
     */
    public int getLength() {
	return end-start;
    }

    /** Maximum amount of data in this buffer.
     *
     *  If -1 or not set, the buffer will grow undefinitely.
     *  Can be smaller than the current buffer size ( which will not shrink ).
     *  When the limit is reached, the buffer will be flushed ( if out is set )
     *  or throw exception.
     */
    public void setLimit(int limit) {
	this.limit=limit;
    }
    
    public int getLimit() {
	return limit;
    }

    /**
     * When the buffer is empty, read the data from the input channel.
     */
    public void setByteInputChannel(ByteInputChannel in) {
        this.in = in;
    }

    /** When the buffer is full, write the data to the output channel.
     * 	Also used when large amount of data is appended.
     *
     *  If not set, the buffer will grow to the limit.
     */
    public void setByteOutputChannel(ByteOutputChannel out) {
	this.out=out;
    }

    public int getEnd() {
	return end;
    }

    public void setEnd( int i ) {
	end=i;
    }

    // -------------------- Adding data to the buffer --------------------
    /** Append a char, by casting it to byte. This IS NOT intended for unicode.
     *
     * @param c
     * @throws IOException
     */
    public void append( char c )
	throws IOException
    {
	append( (byte)c);
    }

    public void append( byte b )
	throws IOException
    {
	makeSpace( 1 );

	// couldn't make space
	if( limit >0 && end >= limit ) {
	    flushBuffer();
	}
	//buff[end++]=b;
        bb.put(end++, b);
    }

    public void append( ByteChunk src )
	throws IOException
    {
	append( src.getBytes(), src.getStart(), src.getLength());
    }

    /** Add data to the buffer
     */
    public void append( byte src[], int off, int len )
	throws IOException
    {
	// will grow, up to limit
	makeSpace( len );

	// if we don't have limit: makeSpace can grow as it wants
	if( limit < 0 ) {
	    // assert: makeSpace made enough space
	    System.arraycopy( src, off, bb.array(), end, len );
	    end+=len;
	    return;
	}

        // Optimize on a common case.
        // If the buffer is empty and the source is going to fill up all the
        // space in buffer, may as well write it directly to the output,
        // and avoid an extra copy
        if ( optimizedWrite && len == limit && end == start) {
            out.realWriteBytes( src, off, len );
            return;
        }
	// if we have limit and we're below
	if( len <= limit - end ) {
	    // makeSpace will grow the buffer to the limit,
	    // so we have space
	    System.arraycopy( src, off, bb.array(), end, len );
	    end+=len;
	    return;
	}

	// need more space than we can afford, need to flush
	// buffer

	// the buffer is already at ( or bigger than ) limit

        // We chunk the data into slices fitting in the buffer limit, although
        // if the data is written directly if it doesn't fit

        int avail=limit-end;
        System.arraycopy(src, off, bb.array(), end, avail);
        end += avail;

        flushBuffer();

        int remain = len - avail;

        while (remain > (limit - end)) {
            out.realWriteBytes( src, (off + len) - remain, limit - end );
            remain = remain - (limit - end);
        }

        System.arraycopy(src, (off + len) - remain, bb.array(), end, remain);
        end += remain;

    }


    // -------------------- Removing data from the buffer --------------------

    public int substract()
        throws IOException {

        if ((end - start) == 0) {
            if (in == null)
                return -1;
            int n = in.realReadBytes( bb.array(), 0, bb.capacity() );
            if (n < 0)
                return -1;
        }

        return (bb.get(start++) & 0xFF);

    }

    public int substract(ByteChunk src)
        throws IOException {

        if ((end - start) == 0) {
            if (in == null)
                return -1;
            int n = in.realReadBytes( bb.array(), 0, bb.capacity() );
            if (n < 0)
                return -1;
        }

        int len = getLength();
        src.append(bb.array(), start, len);
        start = end;
        return len;

    }

    public int substract( byte src[], int off, int len )
        throws IOException {

        if ((end - start) == 0) {
            if (in == null)
                return -1;
            int n = in.realReadBytes( bb.array(), 0, bb.capacity() );
            if (n < 0)
                return -1;
        }

        int n = len;
        if (len > getLength()) {
            n = getLength();
        }
        System.arraycopy(bb.array(), start, src, off, n);
        start += n;
        return n;

    }


    /** Send the buffer to the sink. Called by append() when the limit is reached.
     *  You can also call it explicitely to force the data to be written.
     *
     * @throws IOException
     */
    public void flushBuffer()
	throws IOException
    {
	//assert out!=null
	if( out==null ) {
	    throw new IOException( "Buffer overflow, no sink " + limit + " " +
				   bb.capacity()  );
	}
	out.realWriteBytes( bb.array(), start, end-start );
	end=start;
    }

    /** Make space for len chars. If len is small, allocate
     *	a reserve space too. Never grow bigger than limit.
     */
    private void makeSpace(int count)
    {
	ByteBuffer tmp = null;

	int newSize;
	int desiredSize=end + count;

	// Can't grow above the limit
	if( limit > 0 &&
	    desiredSize > limit) {
	    desiredSize=limit;
	}

	if( bb==null ) {
	    if( desiredSize < 256 ) desiredSize=256; // take a minimum
	    bb=ByteBuffer.allocate(desiredSize);
	}
	
	// limit < buf.length ( the buffer is already big )
	// or we already have space XXX
	if( desiredSize <= bb.capacity() ) {
	    return;
	}
	// grow in larger chunks
	if( desiredSize < 2 * bb.capacity() ) {
	    newSize= bb.capacity() * 2;
	    if( limit >0 &&
		newSize > limit ) newSize=limit;
	} else {
	    newSize= bb.capacity() * 2 + count ;
	    if( limit > 0 &&
		newSize > limit ) newSize=limit;
	}
	tmp=ByteBuffer.allocate(newSize);
	
	System.arraycopy(bb.array(), start, tmp.array(), 0, end-start);
	bb = tmp;
	tmp = null;
	end=end-start;
	start=0;
    }
    
    // -------------------- Conversion and getters --------------------

    public String toString() {
        if (null == bb.array()) {
            return null;
        } else if (end-start == 0) {
            return "";
        }
        return StringCache.toString(this);
    }
    
    public String toStringInternal() {
        String strValue=null;
        try {
            if( enc==null ) enc=DEFAULT_CHARACTER_ENCODING;
            strValue = new String( bb.array(), start, end-start, enc );
            /*
             Does not improve the speed too much on most systems,
             it's safer to use the "clasical" new String().
             
             Most overhead is in creating char[] and copying,
             the internal implementation of new String() is very close to
             what we do. The decoder is nice for large buffers and if
             we don't go to String ( so we can take advantage of reduced GC)
             
             // Method is commented out, in:
              return B2CConverter.decodeString( enc );
              */
        } catch (java.io.UnsupportedEncodingException e) {
            // Use the platform encoding in that case; the usage of a bad
            // encoding will have been logged elsewhere already
            strValue = new String(bb.array(), start, end-start);
        }
        return strValue;
    }

    public int getInt()
    {
	return Ascii.parseInt(bb.array(), start,end-start);
    }

    public long getLong() {
        return Ascii.parseLong(bb.array(), start,end-start);
    }


    // -------------------- equals --------------------

    /**
     * Compares the message bytes to the specified String object.
     * @param s the String to compare
     * @return true if the comparison succeeded, false otherwise
     */
    public boolean equals(String s) {
	// XXX ENCODING - this only works if encoding is UTF8-compat
	// ( ok for tomcat, where we compare ascii - header names, etc )!!!
	
	byte[] b = bb.array();
	int blen = end-start;
	if (b == null || blen != s.length()) {
	    return false;
	}
	int boff = start;
	for (int i = 0; i < blen; i++) {
	    if (b[boff++] != s.charAt(i)) {
		return false;
	    }
	}
	return true;
    }

    /**
     * Compares the message bytes to the specified String object.
     * @param s the String to compare
     * @return true if the comparison succeeded, false otherwise
     */
    public boolean equalsIgnoreCase(String s) {
	byte[] b = bb.array();
	int blen = end-start;
	if (b == null || blen != s.length()) {
	    return false;
	}
	int boff = start;
	for (int i = 0; i < blen; i++) {
	    if (Ascii.toLower(b[boff++]) != Ascii.toLower(s.charAt(i))) {
		return false;
	    }
	}
	return true;
    }

    public boolean equals( ByteChunk bb ) {
	return equals( bb.getBytes(), bb.getStart(), bb.getLength());
    }
    
    public boolean equals( byte b2[], int off2, int len2) {
	byte b1[]=bb.array();
	if( b1==null && b2==null ) return true;

	int len=end-start;
	if ( len2 != len || b1==null || b2==null ) 
	    return false;
		
	int off1 = start;

	while ( len-- > 0) {
	    if (b1[off1++] != b2[off2++]) {
		return false;
	    }
	}
	return true;
    }

    public boolean equals( CharChunk cc ) {
	return equals( cc.getChars(), cc.getStart(), cc.getLength());
    }
    
    public boolean equals( char c2[], int off2, int len2) {
	// XXX works only for enc compatible with ASCII/UTF !!!
	byte b1[]=bb.array();
	if( c2==null && b1==null ) return true;
	
	if (b1== null || c2==null || end-start != len2 ) {
	    return false;
	}
	int off1 = start;
	int len=end-start;
	
	while ( len-- > 0) {
	    if ( (char)b1[off1++] != c2[off2++]) {
		return false;
	    }
	}
	return true;
    }

    /**
     * Returns true if the message bytes starts with the specified string.
     * @param s the string
     */
    public boolean startsWith(String s) {
	// Works only if enc==UTF
	byte[] b = bb.array();
	int blen = s.length();
	if (b == null || blen > end-start) {
	    return false;
	}
	int boff = start;
	for (int i = 0; i < blen; i++) {
	    if (b[boff++] != s.charAt(i)) {
		return false;
	    }
	}
	return true;
    }

    /* Returns true if the message bytes start with the specified byte array */
    public boolean startsWith(byte[] b2) {
        byte[] b1 = bb.array();
        if (b1 == null && b2 == null) {
            return true;
        }

        int len = end - start;
        if (b1 == null || b2 == null || b2.length > len) {
            return false;
        }
        for (int i = start, j = 0; i < end && j < b2.length; ) {
            if (b1[i++] != b2[j++]) 
                return false;
        }
        return true;
    }

    /**
     * Returns true if the message bytes starts with the specified string.
     * @param s the string
     * @param pos The position
     */
    public boolean startsWithIgnoreCase(String s, int pos) {
	byte[] b = bb.array();
	int len = s.length();
	if (b == null || len+pos > end-start) {
	    return false;
	}
	int off = start+pos;
	for (int i = 0; i < len; i++) {
	    if (Ascii.toLower( b[off++] ) != Ascii.toLower( s.charAt(i))) {
		return false;
	    }
	}
	return true;
    }

    public int indexOf( String src, int srcOff, int srcLen, int myOff ) {
	char first=src.charAt( srcOff );

	// Look for first char 
	int srcEnd = srcOff + srcLen;
        
	for( int i=myOff+start; i <= (end - srcLen); i++ ) {
	    if( bb.get(i) != first ) continue;
	    // found first char, now look for a match
            int myPos=i+1;

            // not enough chars to have a match
            if( myPos + srcLen >= end ) {
                break;
            }
            
            try {
	    for( int srcPos=srcOff + 1; srcPos< srcEnd; ) {
                if( bb.get(myPos++) != src.charAt( srcPos++ ))
		    break;
                if( srcPos==srcEnd ) return i-start; // found it
	    }
            } catch( Throwable t ) {
                t.printStackTrace();
            }
	}
	return -1;
    }

    // -------------------- Hash code  --------------------

    // normal hash. 
    public int hash() {
	return hashBytes( bb.array(), start, end-start);
    }

    // hash ignoring case
    public int hashIgnoreCase() {
	return hashBytesIC( bb.array(), start, end-start );
    }

    private static int hashBytes( byte buff[], int start, int bytesLen ) {
	int max=start+bytesLen;
	byte bb[]=buff;
	int code=0;
	for (int i = start; i < max ; i++) {
	    code = code * 37 + bb[i];
	}
	return code;
    }

    private static int hashBytesIC( byte bytes[], int start,
				    int bytesLen )
    {
	int max=start+bytesLen;
	byte bb[]=bytes;
	int code=0;
	for (int i = start; i < max ; i++) {
	    code = code * 37 + Ascii.toLower(bb[i]);
	}
	return code;
    }

    /**
     * Returns true if the message bytes starts with the specified string.
     * @param c the character
     * @param starting The start position
     */
    public int indexOf(char c, int starting) {
	int ret = indexOf( bb.array(), start+starting, end, c);
	return (ret >= start) ? ret - start : -1;
    }

    public static int  indexOf( byte bytes[], int off, int end, char qq )
    {
	// Works only for UTF 
	while( off < end ) {
	    byte b=bytes[off];
	    if( b==qq )
		return off;
	    off++;
	}
	return -1;
    }

    /** Find a character, no side effects.
     *  @return index of char if found, -1 if not
     */
    public static int findChar( byte buf[], int start, int end, char c ) {
	byte b=(byte)c;
	int offset = start;
	while (offset < end) {
	    if (buf[offset] == b) {
		return offset;
	    }
	    offset++;
	}
	return -1;
    }

    /** Find a character, no side effects.
     *  @return index of char if found, -1 if not
     */
    public static int findChars( byte buf[], int start, int end, byte c[] ) {
	int clen=c.length;
	int offset = start;
	while (offset < end) {
	    for( int i=0; i<clen; i++ ) 
		if (buf[offset] == c[i]) {
		    return offset;
		}
	    offset++;
	}
	return -1;
    }

    /** Find the first character != c 
     *  @return index of char if found, -1 if not
     */
    public static int findNotChars( byte buf[], int start, int end, byte c[] )
    {
	int clen=c.length;
	int offset = start;
	boolean found;
		
	while (offset < end) {
	    found=true;
	    for( int i=0; i<clen; i++ ) {
		if (buf[offset] == c[i]) {
		    found=false;
		    break;
		}
	    }
	    if( found ) { // buf[offset] != c[0..len]
		return offset;
	    }
	    offset++;
	}
	return -1;
    }


    /**
     * Convert specified String to a byte array. This ONLY WORKS for ascii, UTF chars will be truncated.
     * 
     * @param value to convert to byte array
     * @return the byte array value
     * @deprecated WRONG, if ascii is all you need - rename the method !
     */
    public static final byte[] convertToBytes(String value) {
        byte[] result = new byte[value.length()];
        for (int i = 0; i < value.length(); i++) {
            result[i] = (byte) value.charAt(i);
        }
        return result;
    }
    
    
}
