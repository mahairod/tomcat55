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


package org.apache.catalina.deploy;


/**
 * Representation of a login configuration element for a web application,
 * as represented in a <code>&lt;login-config&gt;</code> element in the
 * deployment descriptor.
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public final class LoginConfig {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new login configuration with the specified properties
     *
     * @param authMethod Authentication method to use, if any
     * @param realmName Realm name to use in security challenges
     * @param loginPage Context-relative URI of the login page
     * @param errorPage Context-relative URI of the error page
     */
    public LoginConfig(String authMethod, String realmName,
		       String loginPage, String errorPage) {

	super();
	if (authMethod != null)
	    this.authMethod = authMethod;
	if (realmName != null)
	    this.realmName = realmName;
	if (loginPage != null)
	    this.loginPage = loginPage;
	if (errorPage != null)
	    this.errorPage = errorPage;

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The authentication method to use for application login.  Must be
     * BASIC, DIGEST, FORM, or CLIENT-CERT.
     */
    private String authMethod = null;


    /**
     * The context-relative URI of the error page for form login.
     */
    private String errorPage = null;


    /**
     * The context-relative URI of the login page for form login.
     */
    private String loginPage = null;


    /**
     * The realm name used when challenging the user for authentication
     * credentials.
     */
    private String realmName = null;


    // ------------------------------------------------------------- Properties


    /**
     * Return the authentication method for this web application.
     */
    public String getAuthMethod() {

	return (this.authMethod);

    }


    /**
     * Return the error page URI for form login for this web application.
     */
    public String getErrorPage() {

	return (this.errorPage);

    }


    /**
     * Return the login page URI for form login for this web application.
     */
    public String getLoginPage() {

	return (this.loginPage);

    }


    /**
     * Return the realm name for this web application.
     */
    public String getRealmName() {

	return (this.realmName);

    }


}
