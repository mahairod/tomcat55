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

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import javax.servlet.http.Cookie;


/**
 * General purpose request parsing and encoding utility methods.
 *
 * @author Craig R. McClanahan
 * @author Tim Tye
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
        encoding = encoding.trim();
        if ((encoding.length() > 2) && (encoding.startsWith("\"")) 
            && (encoding.endsWith("\"")))
            encoding = encoding.substring(1, encoding.length() - 1);
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

        ArrayList cookies = new ArrayList();
        while (header.length() > 0) {
            int semicolon = header.indexOf(";");
            if (semicolon < 0)
                semicolon = header.length();
            if (semicolon == 0)
                break;
            String token = header.substring(0, semicolon);
            if (semicolon < header.length())
                header = header.substring(semicolon + 1);
            else
                header = "";
	    try {
		int equals = token.indexOf("=");
		if (equals > 0) {
		    String name = URLDecode(token.substring(0, equals).trim());
		    String value = URLDecode(token.substring(equals+1).trim());
		    cookies.add(new Cookie(name, value));
		}
	    } catch (Throwable e) {
		;
	    }
	}

        return ((Cookie[]) cookies.toArray(new Cookie[cookies.size()]));

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
     * @param urlParameters true if we're parsing parameters on the URL
     *
     * @exception IllegalArgumentException if the data is malformed
     */
    public static void parseParameters(Map map, String data, String encoding) 
        throws UnsupportedEncodingException {
        
        if ((data != null) && (data.length() > 0)) {
            int len = data.length();
            byte[] bytes = new byte[len];
            data.getBytes(0, len, bytes, 0);
            parseParameters(map, bytes, encoding);
        }
        
    }


    /**
     * Decode and return the specified URL-encoded String.
     * When the byte array is converted to a string, the system default 
     * character encoding is used...  This may be different than some other
     * servers.
     *
     * @param str The url-encoded string
     *
     * @exception IllegalArgumentException if a '%' character is not followed
     * by a valid 2-digit hexadecimal number
     */
    public static String URLDecode(String str) {
        
 	if (str != null) {
            int len = str.length();
            byte[] bytes = new byte[len];
            str.getBytes(0, len, bytes, 0);
            int ix = 0;
            int ox = 0;
            
            while (ix < len) {
                byte b = bytes[ix++];     // Get byte to test
                if (b == '+') {
                    b = (byte)' ';
                } else if (b == '%') {
                    b = (byte) ((convertHexDigit(bytes[ix++]) << 4)
                                + convertHexDigit(bytes[ix++]));
                } 
                bytes[ox++] = b;
            }
            return new String(bytes, 0, ox);
        }
        return null;
        
    }


    /**
     * Convert a byte character value to hexidecimal digit value.
     *
     * @param b the character value byte
     */
    private static byte convertHexDigit( byte b ) {
        if ((b >= '0') && (b <= '9')) return (byte)(b - '0');
        if ((b >= 'a') && (b <= 'f')) return (byte)(b - 'a' + 10);
        if ((b >= 'A') && (b <= 'F')) return (byte)(b - 'A' + 10);
        return 0;
    }


    /**
     * Put name value pair in map.
     *
     * @param b the character value byte
     *
     * Put name and value pair in map.  When name already exist, add value 
     * to array of values.
     */
    private static void putMapEntry( Map map, String name, String value) {
        String[] newValues = null;
        String[] oldValues = (String[]) map.get(name);
        if (oldValues == null) {
            newValues = new String[1];
            newValues[0] = value;
        } else {
            newValues = new String[oldValues.length + 1];
            System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
            newValues[oldValues.length] = value;
        }
        map.put(name, newValues);
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
     * NOTE: byte array data is modified by this method.  Caller beware.
     *
     * @param map Map that accumulates the resulting parameters
     * @param data Input string containing request parameters
     * @param urlParameters true if we're parsing parameters on the URL
     *
     * @exception UnsupportedEncodingException if the data is malformed
     */
    public static void parseParameters(Map map, byte[] data, String encoding) 
        throws UnsupportedEncodingException {

        if (data != null && data.length > 0) {
            int    pos = 0;
            int    ix = 0;
            int    ox = 0;
            String key = null;
            String value = null;
            while (ix < data.length) {
                byte c = data[ix++];
                switch (c) {
                case '&':
                    value = new String(data, 0, ox, encoding);
                    if (key != null) {
                        putMapEntry(map, key, value);
                        key = null;
                    }
                    ox = 0;
                    break;
                case '=':
                    key = new String(data, 0, ox, encoding);
                    ox = 0;
                    break;
                case '+':
                    data[ox++] = (byte)' ';
                    break;
                case '%':
                    data[ox++] = (byte)((convertHexDigit(data[ix++]) << 4)
                                    + convertHexDigit(data[ix++]));
                    break;
                default:
                    data[ox++] = c;
                }
            }
            //The last value does not end in '&'.  So save it now.
            if (key != null) {
                value = new String(data, 0, ox, encoding);
                putMapEntry(map, key, value);
            }
        }

    }



}

