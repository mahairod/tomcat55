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


package org.apache.tomcat.util;

import java.io.*;
import java.util.*;
import java.text.*;

/* XXX XXX XXX Need a major rewrite  !!!!
 */

/**
 * This class is used to contain standard internet message headers,
 * used for SMTP (RFC822) and HTTP (RFC2068) messages as well as for
 * MIME (RFC 2045) applications such as transferring typed data and
 * grouping related items in multipart message bodies.
 *
 * <P> Message headers, as specified in RFC822, include a field name
 * and a field body.  Order has no semantic significance, and several
 * fields with the same name may exist.  However, most fields do not
 * (and should not) exist more than once in a header.
 *
 * <P> Many kinds of field body must conform to a specified syntax,
 * including the standard parenthesized comment syntax.  This class
 * supports only two simple syntaxes, for dates and integers.
 *
 * <P> When processing headers, care must be taken to handle the case of
 * multiple same-name fields correctly.  The values of such fields are
 * only available as strings.  They may be accessed by index (treating
 * the header as an array of fields), or by name (returning an array
 * of string values).
 */

/**
 *  Memory-efficient repository for Mime Headers. When the object is recycled, it
 *  will keep the allocated headers[] and all the MimeHeaderField - no GC is generated.
 *
 *  For input headers it is possible to use the MessageByte for Fileds - so no GC
 *  will be generated.
 *
 *  The only garbage is generated when using the String for header names/values -
 *  this can't be avoided when the servlet calls header methods, but is easy
 *  to avoid inside tomcat. The goal is to use _only_ MessageByte-based Fields,
 *  and reduce to 0 the memory overhead of tomcat.
 *
 *  TODO:
 *  XXX one-buffer parsing - for http ( other protocols don't need that )
 *  XXX remove unused methods
 *  XXX External enumerations, with 0 GC.
 *  XXX use HeaderName ID
 *  
 * 
 * @author dac@eng.sun.com
 * @author James Todd [gonzo@eng.sun.com]
 * @author Costin Manolache
 */
public class MimeHeaders {
    /** Initial size - should be == average number of headers per request
     *  XXX  make it configurable ( fine-tuning of web-apps )
     */
    public static final int DEFAULT_HEADER_SIZE=8;
    
    /**
     * The header fields.
     */
    private MimeHeaderField[] headers = new MimeHeaderField[DEFAULT_HEADER_SIZE];

    /**
     * The current number of header fields.
     */
    private int count;

    /**
     * Creates a new MimeHeaders object using a default buffer size.
     */
    public MimeHeaders() {
    }

    /**
     * Clears all header fields.
     */
    public void clear() {
	for (int i = 0; i < count; i++) {
	    headers[i].recycle();
	}
	count = 0;
    }

    /**
     * Returns the current number of header fields.
     */
    public int size() {
	return count;
    }

    /**
     * Returns the Nth header name, or null if there is no such header.
     * This may be used to iterate through all header fields.
     */
    public MessageBytes getName(int n) {
	return n >= 0 && n < count ? headers[n].getName() : null;
    }

    /**
     * Returns the Nth header value, or null if there is no such header.
     * This may be used to iterate through all header fields.
     */
    public MessageBytes getValue(int n) {
	return n >= 0 && n < count ? headers[n].getValue() : null;
    }

    /**
     * Finds and returns a header field with the given name.  If no such
     * field exists, null is returned.  If more than one such field is
     * in the header, an arbitrary one is returned.
     */
    private MimeHeaderField find(String name) {
        for (int i = 0; i < count; i++) {
	    if (headers[i].getName().equalsIgnoreCase(name)) {
                return headers[i];
            }
        }
        return null;
    }

    /**
     * Adds a partially constructed field to the header.  This
     * field has not had its name or value initialized.
     */
    private MimeHeaderField createHeader() {
	MimeHeaderField mh;
	int len = headers.length;
	if (count >= len) {
	    // expand header list array
	    MimeHeaderField tmp[] = new MimeHeaderField[count * 2];
	    System.arraycopy(headers, 0, tmp, 0, len);
	    headers = tmp;
	}
	if ((mh = headers[count]) == null) {
	    headers[count] = mh = new MimeHeaderField();
	}
	count++;
	return mh;
    }

    /**
     * Returns an enumeration of strings representing the header field names.
     * Field names may appear multiple times in this enumeration, indicating
     * that multiple fields with that name exist in this header.
     */
    public Enumeration names() {
	return new MimeHeadersEnumerator(this);
    }

    // NOTE:  All of these put/get "Header" calls should
    // be renamed to put/get "field" !!!  This object is
    // the header, and its components are called fields.

    public void addBytesHeader(byte b[], int startN, int endN,
			       int startV, int endV)
    {
	MimeHeaderField mhf=createHeader();
	mhf.getName().setBytes(b, startN, endN);
	mhf.getValue().setBytes(b, startV, endV);
    }

    /**
     * Creates a new header field whose value is the specified string.
     * @param name the header name
     * @param s the header field string value
     */
    public void setHeader(String name, String s) {
	MimeHeaderField headerF= find( name );
	if( headerF == null )
	    headerF=addHeader( name );
	headerF.getValue().setString(s);
    }

    public void addHeader(String name, String s) {
        addHeader(name).getValue().setString(s);
    }

    /**
     * Creates a new header field whose value is the specified integer.
     * @param name the header name
     * @param i the header field integer value
     */
    public void setIntHeader(String name, int i) {
	MimeHeaderField headerF= find( name );
	if( headerF == null )
	    headerF=addHeader( name );
	headerF.getValue().setInt(i);
    }

    public void addIntHeader(String name, int i) {
        addHeader(name).getValue().setInt(i);
    }

    /**
     * Creates a new header field whose value is the specified time.
     * The encoding uses RFC 822 date format, as updated by RFC 1123.
     * @param name the header name
     * @param t the time in number of milliseconds since the epoch
     */
    public void setDateHeader(String name, long t) {
	MimeHeaderField headerF= find( name );
	if( headerF == null )
	    headerF=addHeader( name );
	headerF.getValue().setTime(t);
    }

    public void addDateHeader(String name, long t) {
        addHeader(name).getValue().setTime(t);
    }

    /**
     * Returns the string value of one of the headers with the
     * specified name.
     * @see getHeaders
     * @param name the header field name
     * @return the string value of the field, or null if none found
     */
    public String getHeader(String name) {
	MimeHeaderField mh = find(name);

	return mh != null ? mh.getValue().toString() : null;
    }

    /**
     * Returns the string value of all of the headers with the
     * specified name.
     * @see getHeader
     * @param name the header field name
     * @return array values of the fields, or null if none found
     */
    public String[] getHeaders(String name) {
	// XXX XXX XXX XXX XXX XXX
	Vector values = getHeadersVector(name);

	if (values.size() > 0) {
	    String retval[] = new String[values.size()];

	    for (int i = 0; i < retval.length; i++)
		retval[i] = (String)values.elementAt(i);
	    return retval;
	}
	return null;
    }

    // XXX XXX XXX XXX XXX XXX 
    /** Same as getHeaders, return a Vector - avoid Vector-[]-Vector conversion
     */
    public Vector getHeadersVector(String name) {
	Vector values = new Vector();

	for (int i = 0; i < count; i++) {
	    if (headers[i].getName().equalsIgnoreCase(name))
		values.addElement(headers[i].getValue().toString());
	}

	return values;
    }

    /**
     * Returns the integer value of a header with the specified name.
     * @param name the header field name
     * @return the integer value of the header field, or -1 if the header
     *	       was not found
     * @exception NumberFormatException if the integer format was invalid
     */

    public int getIntHeader(String name) throws NumberFormatException {
	MimeHeaderField mh = find(name);

	return mh != null ? mh.getValue().getInt() : -1;
    }

    /**
     * Returns the date value of a header with the specified name.
     * @param name the header field name
     * @return the date value of the header field in number of milliseconds
     *	       since the epoch, or -1 if the header was not found
     * @exception IllegalArgumentException if the date format was invalid
     */
    public long getDateHeader(String name) throws IllegalArgumentException {
	MimeHeaderField mh = find(name);

	return mh != null ? mh.getValue().getTime() : -1;
    }

    /**
     * Returns the name of the nth header field where n >= 0. Returns null
     * if there were fewer than (n + 1) fields. This can be used to iterate
     * through all the fields in the header.
     */
    public String getHeaderName(int n) {
	return n >= 0 && n < count ? headers[n].getName().toString()
	    : null;
    }

    /**
     * Returns the body of the nth header field where n >= 0. Returns null
     * if there were fewer than (n + 1) fields. This can be used along
     * with getHeaderName to iterate through all the fields in the header.
     */
    public String getHeader(int n) {
	return n >= 0 && n < count ? headers[n].getValue().toString()
	    : null;
    }

    /**
     * Returns the number of fields using a given field name.
     */
    public int getFieldCount (String name) {
	int retval = 0;

	for (int i = 0; i < count; i++)
	    if (headers [i].getName().equalsIgnoreCase(name))
		retval++;

	return retval;
    }

    /**
     * Removes a header field with the specified name.  Does nothing
     * if such a field could not be found.
     * @param name the name of the header field to be removed
     */
    public void removeHeader(String name) {
        // XXX
        // warning: rather sticky code; heavily tuned

        for (int i = 0; i < count; i++) {
	    if (headers[i].getName().equalsIgnoreCase(name)) {
	        // reset and swap with last header
	        MimeHeaderField mh = headers[i];

		mh.recycle();
		headers[i] = headers[count - 1];
		headers[count - 1] = mh;

		count--;
		i--;
	    }
	}
    }

    /**
     * Returns true if the specified field is contained in the header,
     * otherwise returns false.
     * @param name the field name
     */
    public boolean containsHeader(String name) {
	return find(name) != null;
    }


    protected MimeHeaderField addHeader(String name) {
 	MimeHeaderField mh = createHeader();

	mh.getName().setString(name);

	return mh;
    }
    
    /**
     * Returns a lengthly string representation of the current header fields.
     */
    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append("{");
	for (int i = 0; i < count; i++) {
	    sb.append("{");
	    sb.append(headers[i].toString());
	    sb.append("}");

	    if (i < count - 1) {
		sb.append(",");
	    }
	}
	sb.append("}");

	return sb.toString();
    }
}

// XXX XXX XXX XXX Must be rewritten !!!
class MimeHeadersEnumerator implements Enumeration {
    private Hashtable hash;
    private Enumeration delegate;

    MimeHeadersEnumerator(MimeHeaders headers) {
        // Store header names in a Hashtable to guarantee uniqueness
        // This has the side benefit of letting us use Hashtable's enumerator
        hash = new Hashtable();
        int size = headers.size();
        for (int i = 0; i < size; i++) {
            hash.put(headers.getHeaderName(i), "");
        }
        delegate = hash.keys();
    }

    public boolean hasMoreElements() {
	return delegate.hasMoreElements();
    }

    public Object nextElement() {
	return delegate.nextElement();
    }
}

class MimeHeaderField {
    // multiple headers with same name - a linked list will
    // speed up name enumerations and search ( both cpu and
    // GC)
    MimeHeaderField next; 
    
    protected final MessageBytes nameB = new MessageBytes();
    protected final MessageBytes valueB = new MessageBytes();

    /**
     * Creates a new, uninitialized header field.
     */
    public MimeHeaderField() {
    }

    public void recycle() {
	nameB.recycle();
	valueB.recycle();
	next=null;
    }

    public MessageBytes getName() {
	return nameB;
    }

    public MessageBytes getValue() {
	return valueB;
    }
}
