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
 * Representation of an EJB resource reference for a web application, as
 * represented in a <code>&lt;ejb-ref&gt;</code> element in the
 * deployment descriptor.
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public final class ContextEjb {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new EJB resource reference with the specified properties
     *
     * @param name EJB resource reference name
     * @param description EJB resource reference description
     * @param type Java class name of the EJB bean implementation class
     * @param home Java class name of the EJB home implementation class
     * @param remote Java class name of the EJB remote implementation class
     * @param link Optional link to a J2EE EJB definition 
     * @param runAs Optional security role for transactions performed
     *  to this Enterprise JavaBean resource reference
     */
    public ContextEjb(String name, String description,
		      String type, String home,
		      String remote, String link, String runAs) {

        super();        
	this.name = name;
	this.description = description;
	this.type = type;
	this.home = home;
	this.remote = remote;
	this.link = link;
	this.runAs = runAs;

    }


    // ----------------------------------------------------- Instance Variables


    private String description = null;
    private String home = null;
    private String link = null;
    private String name = null;
    private String remote = null;
    private String runAs = null;
    private String type = null;


    // ------------------------------------------------------------- Properties


    public String getDescription() {
	return (this.description);
    }

    public String getHome() {
	return (this.home);
    }

    public String getLink() {
	return (this.link);
    }

    public String getName() {
	return (this.name);
    }

    public String getRemote() {
	return (this.remote);
    }

    public String getRunAs() {
	return (this.runAs);
    }

    public String getType() {
	return (this.type);
    }


}
