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


package org.apache.catalina.util;


import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;
import javax.servlet.http.Cookie;


/**
 * General purpose request parsing and encoding utility methods.
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public final class RequestUtil {


    /**
     * The DateFormat to use for generating readable dates in cookies.
     */
    private static SimpleDateFormat format =
        new SimpleDateFormat(" EEEE, dd-MMM-yy kk:mm:ss zz");

    static {
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
    }


    /**
     * Encode a cookie as per RFC 2109.  The resulting string can be used
     * as the value for a <code>Set-Cookie</code> header.
     *
     * @param cookie The cookie to encode.
     * @return A string following RFC 2109.
     */
    public static String encodeCookie(Cookie cookie) {

        StringBuffer buf = new StringBuffer( cookie.getName() );
        buf.append("=");
        buf.append(cookie.getValue());

	if (cookie.getComment() != null) {
	    buf.append("; Comment=\"");
	    buf.append(cookie.getComment());
	    buf.append("\"");
	}

        if (cookie.getDomain() != null) {
            buf.append("; Domain=\"");
            buf.append(cookie.getDomain());
	    buf.append("\"");
        }

        long age = cookie.getMaxAge();
	if (cookie.getMaxAge() >= 0) {
	    buf.append("; Max-Age=\"");
	    buf.append(cookie.getMaxAge());
	    buf.append("\"");
	}

        if (cookie.getPath() != null) {
            buf.append("; Path=\"");
            buf.append(cookie.getPath());
	    buf.append("\"");
        }

        if (cookie.getSecure()) {
            buf.append("; Secure");
        }

	if (cookie.getVersion() > 0) {
	    buf.append("; Version=\"");
	    buf.append(cookie.getVersion());
	    buf.append("\"");
	}

        return (buf.toString());
    }


    /**
     * Parse the character encoding from the specified content type header.
     * If the content type is null, or there is no explicit character encoding,
     * <code>null</code> is returned.
     *
     * @param contentType a content type header
     */
    public static String parseCharacterEncoding(String contentType) {

	if (contentType == null)
	    return (null);
	int start = contentType.indexOf("charset=");
	if (start < 0)
	    return (null);
	String encoding = contentType.substring(start + 8);
	int end = encoding.indexOf(";");
	if (end >= 0)
	    encoding = encoding.substring(0, end);
	return (encoding.trim());

    }


    /**
     * Parse a cookie header into an array of cookies according to RFC 2109.
     *
     * @param header Value of an HTTP "Cookie" header
     */
    public static Cookie[] parseCookieHeader(String header) {

	if ((header == null) || (header.length() < 1))
	    return (new Cookie[0]);

	Vector cookieJar = new Vector();
	StringTokenizer tokens = new StringTokenizer(header, ";");
	while (tokens.hasMoreTokens()) {
	    try {
		String token = tokens.nextToken();
		int equals = token.indexOf("=");
		if (equals > 0) {
		    String name = URLDecode(token.substring(0, equals).trim());
		    String value = URLDecode(token.substring(equals+1).trim());
		    cookieJar.addElement(new Cookie(name, value));
		}
	    } catch (Throwable e) {
		;
	    }
	}

	Cookie[] cookies = new Cookie[cookieJar.size()];
	cookieJar.copyInto(cookies);
	return (cookies);

    }


    /**
     * Append request parameters from the specified String to the specified
     * Map.  It is presumed that the specified Map is not accessed from any
     * other thread, so no synchronization is performed.
     * <p>
     * <strong>IMPLEMENTATION NOTE</strong>:  URL decoding is performed
     * individually on the parsed name and value elements, rather than on
     * the entire query string ahead of time, to properly deal with the case
     * where the name or value includes an encoded "=" or "&" character
     * that would otherwise be interpreted as a delimiter.
     *
     * @param map Map that accumulates the resulting parameters
     * @param data Input string containing request parameters
     *
     * @exception IllegalArgumentException if the data is malformed
     */
    public static void parseParameters(Map map, String data) {

        if ((data == null) || (data.length() < 1))
	    return;

	// Initialize the variables we will require
	StringParser parser = new StringParser(data);
	boolean first = true;
	int nameStart = 0;
	int nameEnd = 0;
	int valueStart = 0;
	int valueEnd = 0;
	String name = null;
	String value = null;
	String oldValues[] = null;
	String newValues[] = null;

	// Loop through the "name=value" entries in the input data
	while (true) {

	    // Extract the name and value components
	    if (first)
	        first = false;
	    else
	        parser.advance();
	    nameStart = parser.getIndex();
	    nameEnd = parser.findChar('=');
	    parser.advance();
	    valueStart = parser.getIndex();
	    valueEnd = parser.findChar('&');
	    name = parser.extract(nameStart, nameEnd);
	    value = parser.extract(valueStart, valueEnd);

	    // A zero-length name means we are done
	    if (name.length() < 1)
	        break;

            // Decode the name and value if required
            if ((name.indexOf('%') >= 0) || (name.indexOf('+') >= 0)) {
                name = URLDecoder.decode(name);
            }
            if ((value.indexOf('%') >= 0) || (value.indexOf('+') >= 0)) {
                value = URLDecoder.decode(value);
            }

	    // Create or update the array of values for this name
	    oldValues = (String[]) map.get(name);
	    if (oldValues == null)
	        oldValues = new String[0];
	    newValues = new String[oldValues.length + 1];
	    System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
	    newValues[oldValues.length] = value;
	    map.put(name, newValues);

	}

    }


    /**
     * Decode and return the specified URL-encoded String.
     *
     * @param str The url-encoded string
     *
     * @exception IllegalArgumentException if a '%' character is not followed
     * by a valid 2-digit hexadecimal number
     */
    public static String URLDecode(String str)
	throws IllegalArgumentException {

	if (str == null)
	    return (null);

	StringBuffer dec = new StringBuffer();
	int pos = 0;
	int len = str.length();
	dec.ensureCapacity(str.length());

	while (pos < len) {
	    int lookahead;	// Look-ahead position

	    // Look ahead to the next URLencoded metacharacter, if any
	    for (lookahead = pos; lookahead < len; lookahead++) {
		char ch = str.charAt(lookahead);
		if ((ch == '+') || (ch == '%'))
		    break;
	    }

	    // If there were non-metacharacters, copy them as a block
	    if (lookahead > pos) {
		dec.append(str.substring(pos, lookahead));
		pos = lookahead;
	    }

	    // Shortcut out if we are at the end of the string
	    if (pos >= len)
		break;

	    // Process the next metacharacter
	    char meta = str.charAt(pos);
	    if (meta == '+') {
		dec.append(' ');
		pos++;
	    } else if (meta == '%') {
		try {
		    dec.append((char) Integer.parseInt
			       (str.substring(pos+1, pos+3), 16));
		} catch (NumberFormatException e) {
		    throw new IllegalArgumentException
			("Invalid hexadecimal '" + str.substring(pos+1, pos+3)
			 + " in URLencoded string");
		} catch (StringIndexOutOfBoundsException e) {
		    throw new IllegalArgumentException
			("Invalid unescaped '%' in URLcoded string");
		}
		pos += 3;
	    }
	}
	return (dec.toString());

    }

}
