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
 * The XByteBuffer provides a dual functionality.
 * One, it stores message bytes and automatically extends the byte buffer if needed.<BR>
 * Two, it can encode and decode packages so that they can be defined and identified
 * as they come in on a socket.
 *
 * @author Filip Hanik
 * @version $Revision$, $Date$
 */


public class XByteBuffer
{
    /**
     * This is a package header, 7 bytes
     */
    public static final byte[] START_DATA = {70,76,84,50,48,48,50};
    /**
     * This is the package footer, 7 bytes
     */
    public static final byte[] END_DATA = {84,76,70,50,48,48,51};
    //A package looks like, always.
    /**
     * START_DATA - 7 bytes
     * SIZE       - 4 bytes - size of the data package
     * DATA       - should be as many bytes as the prev SIZE
     * END_DATA   - 7 bytes
     */

    /**
     * Default size on the initial byte buffer
     */
    static final int DEF_SIZE = 1024;
    /**
     * Default size to extend the buffer with
     */
    static final int DEF_EXT  = 1024;
    /**
     * Variable to hold the data
     */
    protected byte[] buf = null;
    /**
     * Current length of data in the buffer
     */
    protected int bufSize = 0;

    /**
     * Constructs a new XByteBuffer
     * @param size - the initial size of the byte buffer
     */
    public XByteBuffer(int size) {
        buf = new byte[size];
    }//XByteBuffer

    /**
     * Constructs a new XByteBuffer with an initial size of 1024 bytes
     */
    public XByteBuffer()  {
        this(DEF_SIZE);
    }//XByteBuffer

    /**
     * Returns the bytes in the buffer, in its exact length
     * @return
     */
    public synchronized byte[] getBytes() {
        byte[] b = new byte[bufSize];
        System.arraycopy(buf,0,b,0,bufSize);
        return b;
    }//getBytes

    /**
     * Resets the buffer
     */
    public synchronized void clear() {
        bufSize = 0;
    }

    /**
     * Appends the data to the buffer. If the data is incorrectly formatted, ie, the data should always start with the
     * header, false will be returned and the data will be discarded.
     * @param b - bytes to be appended
     * @param off - the offset to extract data from
     * @param len - the number of bytes to append.
     * @return true if the data was appended correctly. Returns false if the package is incorrect, ie missing header or something, or the length of data is 0
     */
    public synchronized boolean append(byte[] b, int off, int len) {
        if ((off < 0) || (off > b.length) || (len < 0) ||
            ((off + len) > b.length) || ((off + len) < 0))  {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return false;
        }//end if

        int newcount = bufSize + len;
        if (newcount > buf.length) {
            byte newbuf[] = new byte[Math.max(buf.length << 1, newcount)];
            System.arraycopy(buf, 0, newbuf, 0, bufSize);
            buf = newbuf;
        }
        System.arraycopy(b, off, buf, bufSize, len);
        bufSize = newcount;

        if (bufSize > START_DATA.length && (firstIndexOf(buf,0,START_DATA)==-1)){
            bufSize = 0;
            System.out.println("Discarded the package, invalid header");
            return false;
        }
        return true;
    }//append


    /**
     * Internal mechanism to make a check if a complete package exists
     * within the buffer
     * @return - true if a complete package (header,size,data,footer) exists within the buffer
     */
    protected synchronized int packageExists()
    {
        int pos = START_DATA.length;
        //first check start header
        int index = this.firstIndexOf(buf,0,START_DATA);
        //if the header (START_DATA) isn't the first thing or
        //the buffer isn't even 10 bytes
        if ( index != 0 || (bufSize<10) ) return 0;
        //then get the size 4 bytes
        int size = toInt(buf,pos);
        //now the total buffer has to be long enough to hold
        //START_DATA.length+4+size+END_DATA.length
        pos = START_DATA.length+4+size;
        if ( (pos+END_DATA.length) > bufSize ) return 0;
        //and finally check the footer of the package END_DATA
        int newpos = firstIndexOf(buf,pos,END_DATA);
        if ( newpos != pos ) return 0;
        return size;
    }//getSize

    /**
     * Method to check if a package exists in this byte buffer.
     * @return - true if a complete package (header,size,data,footer) exists within the buffer
     */
    public synchronized boolean doesPackageExist()  {
        return (packageExists()>0);
    }//doesPackageExist

    /**
     * Extracts the message bytes from a package.
     * If no package exists, a IllegalStateException will be thrown.
     * @param clearFromBuffer - if true, the package will be removed from the byte buffer
     * @return - returns the actual message bytes (header, size and footer not included).
     */
    public synchronized byte[] extractPackage(boolean clearFromBuffer) {
        int size = packageExists();
        if ( size == 0 ) throw new java.lang.IllegalStateException("No package exists in XByteBuffer");
        byte[] result = new byte[size];
        System.arraycopy(buf,START_DATA.length+4,result,0,size);
        if ( clearFromBuffer ) {
            int totalsize = START_DATA.length + 4 + size + END_DATA.length;
            bufSize = bufSize - totalsize;
            System.arraycopy(buf, totalsize, buf, 0, bufSize);
        }
        return result;
    }//extractPackage

    /**
     * Convert four bytes to an int
     * @param b - the byte array containing the four bytes
     * @param off - the offset
     * @return the integer value constructed from the four bytes
     * @exception java.lang.ArrayOutOfBoundsException
     */
    public static int toInt(byte[] b,int off){
        return ( ( (int) b[off+3]) & 0xFF) +
            ( ( ( (int) b[off+2]) & 0xFF) << 8) +
            ( ( ( (int) b[off+1]) & 0xFF) << 16) +
            ( ( ( (int) b[off+0]) & 0xFF) << 24);
    }//toInt

    /**
     * Converts an integer to four bytes
     * @param n - the integer
     * @return - four bytes in an array
     */
    public static byte[] toBytes(int n) {
        byte[] b = new byte[4];
        b[3] = (byte) (n);
        n >>>= 8;
        b[2] = (byte) (n);
        n >>>= 8;
        b[1] = (byte) (n);
        n >>>= 8;
        b[0] = (byte) (n);
        return b;
    } //toBytes

    /**
     * Similar to a String.IndexOf, but uses pure bytes
     * @param src - the source bytes to be searched
     * @param srcOff - offset on the source buffer
     * @param find - the string to be found within src
     * @return - the index of the first matching byte. -1 if the find array is not found
     */
    public static int firstIndexOf(byte[] src, int srcOff, byte[] find){
        int result = -1;
        if (find.length > src.length) return result;
        if (find.length == 0 || src.length == 0) return result;
        if (srcOff >= src.length ) throw new java.lang.ArrayIndexOutOfBoundsException();
        boolean found = false;
        int srclen = src.length;
        int findlen = find.length;
        byte first = find[0];
        int pos = srcOff;
        while (!found) {
            //find the first byte
            while (pos < srclen){
                if (first == src[pos])
                    break;
                pos++;
            } //while
            if (pos >= srclen)
                return -1;

            //we found the first character
            //match the rest of the bytes - they have to match
            if ( (srclen - pos) < findlen)
                return -1;
            //assume it does exist
            found = true;
            for (int i = 1; ( (i < findlen) && found); i++)
                found = found && (find[i] == src[pos + i]);
            if (found)
                result = pos;
            else if ( (srclen - pos) < findlen)
                return -1; //no more matches possible
            else
                pos++;
        } //while
        return result;
    } //firstIndexOf

    /**
     * Creates a complete data package
     * @param data - the message data to be contained within the package
     * @return - a full package (header,size,data,footer)
     */
    public static byte[] createDataPackage(byte[] data)  {
        byte[] result = new byte[START_DATA.length+4+data.length+END_DATA.length];
        System.arraycopy(START_DATA,0,result,0,START_DATA.length);
        System.arraycopy(toBytes(data.length),0,result,START_DATA.length,4);
        System.arraycopy(data,0,result,START_DATA.length+4,data.length);
        System.arraycopy(END_DATA,0,result,START_DATA.length+4+data.length,END_DATA.length);
        return result;
    }//createDataPackage

}//class