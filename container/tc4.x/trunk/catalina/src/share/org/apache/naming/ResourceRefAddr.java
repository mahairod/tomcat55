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


package org.apache.naming;

import java.util.Hashtable;
import javax.naming.RefAddr;

/**
 * Represents a reference address to a resource.
 *
 * @author Remy Maucherat
 * @version $Revision$ $Date$
 */

public class ResourceRefAddr
    extends RefAddr {


    // -------------------------------------------------------------- Constants


    /**
     * Ref address type.
     */
    public static final String TYPE = "resource-ref";


    // ----------------------------------------------------------- Constructors


    /**
     * Resource Reference.
     * 
     * @param resourceClass Resource class
     * @param scope Resource scope
     * @param auth Resource authetication
     */
    public ResourceRefAddr(String resourceClass, String description, 
                           String scope, String auth) {
        super(TYPE);
        this.resourceClass = resourceClass;
        this.description = description;
        this.scope = scope;
        this.auth = auth;
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Resource class.
     */
    protected String resourceClass;


    /**
     * Description.
     */
    protected String description;


    /**
     * Scope.
     */
    protected String scope;


    /**
     * Authentication type.
     */
    protected String auth;


    // -------------------------------------------------------- RefAddr Methods


    /**
     * Returns the contents of the address.
     */
    public Object getContent() {
        return this.resourceClass;
    }


    // ------------------------------------------------------------- Properties


    /**
     * Resource class accessor.
     */
    public String getResourceClass() {
	return (this.resourceClass);
    }


    /**
     * Description accessor.
     */
    public String getDescription() {
	return (this.description);
    }


    /**
     * Scope accessor.
     */
    public String getScope() {
	return (this.scope);
    }


    /**
     * Auth accessor.
     */
    public String getAuth() {
	return (this.auth);
    }


}
