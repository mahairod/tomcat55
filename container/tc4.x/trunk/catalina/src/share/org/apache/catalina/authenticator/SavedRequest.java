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


package org.apache.catalina.authenticator;


import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;
import javax.servlet.http.Cookie;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.Session;


/**
 * Object that saves the critical information from a request so that
 * form-based authentication can reproduce it once the user has been
 * authenticated.
 * <p>
 * <b>FIXME</b> - Currently, this object has no mechanism to save or
 * restore the data content of the request, so it will not support a
 * POST request triggering the authentication.
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public final class SavedRequest {


    /**
     * The set of Cookies associated with this Request.
     */
    private Vector cookies = new Vector();

    public void addCookie(Cookie cookie) {
	cookies.addElement(cookie);
    }

    public Cookie[] getCookies() {
	Cookie results[] = new Cookie[cookies.size()];
	cookies.copyInto(results);
	return (results);
    }


    /**
     * The set of Headers associated with this Request.  Each key is a header
     * name, while the value is a Vector containing one or more actual
     * values for this header.
     */
    private Hashtable headers = new Hashtable();

    public void addHeader(String name, String value) {
	Vector values = (Vector) headers.get(name);
	if (values == null) {
	    values = new Vector();
	    headers.put(name, values);
	}
	values.addElement(value);
    }

    public String[] getHeaderNames() {
	Vector keys = new Vector();
	Enumeration enum = headers.keys();
	while (enum.hasMoreElements())
	    keys.addElement(enum.nextElement());
	String results[] = new String[keys.size()];
	keys.copyInto(results);
	return (results);
    }

    public String[] getHeaderValues(String name) {
	Vector values = (Vector) headers.get(name);
	if (values == null)
	    return (new String[0]);
	String results[] = new String[values.size()];
	values.copyInto(results);
	return (results);
    }


    /**
     * The set of Locales associated with this Request.
     */
    private Vector locales = new Vector();

    public void addLocale(Locale locale) {
	locales.addElement(locale);
    }

    public Locale[] getLocales() {
	Locale results[] = new Locale[locales.size()];
	locales.copyInto(results);
	return (results);
    }


    /**
     * The request method used on this Request.
     */
    private String method = null;

    public String getMethod() {
	return (this.method);
    }

    public void setMethod(String method) {
	this.method = method;
    }


    /**
     * The query string associated with this Request.
     */
    private String queryString = null;

    public String getQueryString() {
	return (this.queryString);
    }

    public void setQueryString(String queryString) {
	this.queryString = queryString;
    }


    /**
     * The request URI associated with this Request.
     */
    private String requestURI = null;

    public String getRequestURI() {
	return (this.requestURI);
    }

    public void setRequestURI(String requestURI) {
	this.requestURI = requestURI;
    }


}
