/*
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
 * ====================================================================
 *
 * This source code implements specifications defined by the Java
 * Community Process. In order to remain compliant with the specification
 * DO NOT add / change / or delete method signatures!
 */ 


package javax.servlet.http;

import javax.servlet.ServletInputStream;
import java.util.Hashtable;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.io.IOException;

/**
 * @deprecated		As of Java(tm) Servlet API 2.3. 
 *			These methods were only useful
 *			with the default encoding and have been moved
 *			to the request interfaces.
 *
*/


public class HttpUtils {

    private static final String LSTRING_FILE =
	"javax.servlet.http.LocalStrings";
    private static ResourceBundle lStrings =
	ResourceBundle.getBundle(LSTRING_FILE);
        
    
    
    /**
     * Constructs an empty <code>HttpUtils</code> object.
     *
     */

    public HttpUtils() {}
    
    
    
    

    /**
     *
     * Parses a query string passed from the client to the
     * server and builds a <code>HashTable</code> object
     * with key-value pairs. 
     * The query string should be in the form of a string
     * packaged by the GET or POST method, that is, it
     * should have key-value pairs in the form <i>key=value</i>,
     * with each pair separated from the next by a & character.
     *
     * <p>A key can appear more than once in the query string
     * with different values. However, the key appears only once in 
     * the hashtable, with its value being
     * an array of strings containing the multiple values sent
     * by the query string.
     * 
     * <p>The keys and values in the hashtable are stored in their
     * decoded form, so
     * any + characters are converted to spaces, and characters
     * sent in hexadecimal notation (like <i>%xx</i>) are
     * converted to ASCII characters.
     *
     * @param s		a string containing the query to be parsed
     *
     * @return		a <code>HashTable</code> object built
     * 			from the parsed key-value pairs
     *
     * @exception IllegalArgumentException	if the query string 
     *						is invalid
     *
     */

    static public Hashtable parseQueryString(String s) {

	String valArray[] = null;
	
	if (s == null) {
	    throw new IllegalArgumentException();
	}
	Hashtable ht = new Hashtable();
	StringBuffer sb = new StringBuffer();
	StringTokenizer st = new StringTokenizer(s, "&");
	while (st.hasMoreTokens()) {
	    String pair = (String)st.nextToken();
	    int pos = pair.indexOf('=');
	    if (pos == -1) {
		// XXX
		// should give more detail about the illegal argument
		throw new IllegalArgumentException();
	    }
	    String key = parseName(pair.substring(0, pos), sb);
	    String val = parseName(pair.substring(pos+1, pair.length()), sb);
	    if (ht.containsKey(key)) {
		String oldVals[] = (String []) ht.get(key);
		valArray = new String[oldVals.length + 1];
		for (int i = 0; i < oldVals.length; i++) 
		    valArray[i] = oldVals[i];
		valArray[oldVals.length] = val;
	    } else {
		valArray = new String[1];
		valArray[0] = val;
	    }
	    ht.put(key, valArray);
	}
	return ht;
    }




    /**
     *
     * Parses data from an HTML form that the client sends to 
     * the server using the HTTP POST method and the 
     * <i>application/x-www-form-urlencoded</i> MIME type.
     *
     * <p>The data sent by the POST method contains key-value
     * pairs. A key can appear more than once in the POST data
     * with different values. However, the key appears only once in 
     * the hashtable, with its value being
     * an array of strings containing the multiple values sent
     * by the POST method.
     *
     * <p>The keys and values in the hashtable are stored in their
     * decoded form, so
     * any + characters are converted to spaces, and characters
     * sent in hexadecimal notation (like <i>%xx</i>) are
     * converted to ASCII characters.
     *
     *
     *
     * @param len	an integer specifying the length,
     *			in characters, of the 
     *			<code>ServletInputStream</code>
     *			object that is also passed to this
     *			method
     *
     * @param in	the <code>ServletInputStream</code>
     *			object that contains the data sent
     *			from the client
     * 
     * @return		a <code>HashTable</code> object built
     *			from the parsed key-value pairs
     *
     *
     * @exception IllegalArgumentException	if the data
     *			sent by the POST method is invalid
     *
     */
     

    static public Hashtable parsePostData(int len, 
					  ServletInputStream in)
    {
	// XXX
	// should a length of 0 be an IllegalArgumentException
	
	if (len <=0)
	    return new Hashtable(); // cheap hack to return an empty hash

	if (in == null) {
	    throw new IllegalArgumentException();
	}
	
	//
	// Make sure we read the entire POSTed body.
	//
        byte[] postedBytes = new byte [len];
        try {
            int offset = 0;
       
	    do {
		int inputLen = in.read (postedBytes, offset, len - offset);
		if (inputLen <= 0) {
		    String msg = lStrings.getString("err.io.short_read");
		    throw new IllegalArgumentException (msg);
		}
		offset += inputLen;
	    } while ((len - offset) > 0);

	} catch (IOException e) {
	    throw new IllegalArgumentException(e.getMessage());
	}

        // XXX we shouldn't assume that the only kind of POST body
        // is FORM data encoded using ASCII or ISO Latin/1 ... or
        // that the body should always be treated as FORM data.
        //

        try {
            String postedBody = new String(postedBytes, 0, len, "8859_1");
            return parseQueryString(postedBody);
        } catch (java.io.UnsupportedEncodingException e) {
            // XXX function should accept an encoding parameter & throw this
            // exception.  Otherwise throw something expected.
            throw new IllegalArgumentException(e.getMessage());
        }
    }




    /*
     * Parse a name in the query string.
     */

    static private String parseName(String s, StringBuffer sb) {
	sb.setLength(0);
	for (int i = 0; i < s.length(); i++) {
	    char c = s.charAt(i); 
	    switch (c) {
	    case '+':
		sb.append(' ');
		break;
	    case '%':
		try {
		    sb.append((char) Integer.parseInt(s.substring(i+1, i+3), 
						      16));
		    i += 2;
		} catch (NumberFormatException e) {
		    // XXX
		    // need to be more specific about illegal arg
		    throw new IllegalArgumentException();
		} catch (StringIndexOutOfBoundsException e) {
		    String rest  = s.substring(i);
		    sb.append(rest);
		    if (rest.length()==2)
			i++;
		}
		
		break;
	    default:
		sb.append(c);
		break;
	    }
	}
	return sb.toString();
    }




    /**
     *
     * Reconstructs the URL the client used to make the request,
     * using information in the <code>HttpServletRequest</code> object.
     * The returned URL contains a protocol, server name, port
     * number, and server path, but it does not include query
     * string parameters.
     * 
     * <p>Because this method returns a <code>StringBuffer</code>,
     * not a string, you can modify the URL easily, for example,
     * to append query parameters.
     *
     * <p>This method is useful for creating redirect messages
     * and for reporting errors.
     *
     * @param req	a <code>HttpServletRequest</code> object
     *			containing the client's request
     * 
     * @return		a <code>StringBuffer</code> object containing
     *			the reconstructed URL
     *
     */

    public static StringBuffer getRequestURL (HttpServletRequest req) {
	StringBuffer url = new StringBuffer ();
	String scheme = req.getScheme ();
	int port = req.getServerPort ();
	String urlPath = req.getRequestURI();
	
	//String		servletPath = req.getServletPath ();
	//String		pathInfo = req.getPathInfo ();

	url.append (scheme);		// http, https
	url.append ("://");
	url.append (req.getServerName ());
	if ((scheme.equals ("http") && port != 80)
		|| (scheme.equals ("https") && port != 443)) {
	    url.append (':');
	    url.append (req.getServerPort ());
	}
	//if (servletPath != null)
	//    url.append (servletPath);
	//if (pathInfo != null)
	//    url.append (pathInfo);
	url.append(urlPath);
	return url;
    }
}



