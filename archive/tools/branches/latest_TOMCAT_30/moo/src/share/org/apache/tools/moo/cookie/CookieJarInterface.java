/*
 * $Header$
 * $Date$
 * $Revision$
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
 * @author	Ramesh Mandava [rameshm@eng.sun.com]
 */

package org.apache.tools.moo.cookie;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.beans.*;


/**
 * This is the interface to the CookieJar.  It is broken
 * out to make use of another bean later simpler and to reduce dependancies
 * at build time.
 *
 */

public interface CookieJarInterface {
    
    /*
     * Adds the an array of HttpCookies to the current in memory
     * cookies
     */
    public void addToCookieJar(HttpCookie[] cookieList);

    /*
     * Adds the cookie to current in-memory cookies 
     */
    public void addToCookieJar(String cookieString, URL docURL);

    /*
     * Discards all the current cookies in memory
     */
    public void discardAllCookies();

    /*
     * Reads in cookies from file specified by filename,
     * adds the cookies to the in-memory cookies
     */
    public void loadCookieJarFromFile(String filename);

    /* 
     * Saves in-memory cookies to file specified by filename
     * also purges any expired cookies from the in memory list
     */
    public void saveCookieJarToFile(String filename);

    /* 
     * Returns all in-memory cookies in an array 
     */
    public HttpCookie[] getAllCookies();

    /*
     * Returns all cookies that apply for the given url
     */
    public HttpCookie[] getCookiesForURL(URL url);

    /*
     * Allows users to set if wants cookies disabled/enabled
     */
    public void setCookieDisable(boolean isDisabled);

    /* 
     * purges any expired cookies in the Cookie hashtable.
     */
    public void purgeExpiredCookies();

    public void recordAnyCookies(Vector rcvVectorOfCookies, URL url);

    public void recordCookie(URL url, String cookieValue);

    public void recordCookie(HttpCookie cookie);

    public String applyRelevantCookies(URL url);



    
    /*************
     * Listener Methods.
     *************/
    
    public void addVetoableChangeListener(VetoableChangeListener l);

    public void removeVetoableChangeListener(VetoableChangeListener l);

}
