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


package org.apache.tomcat.core;

/** 
 * This is the adapter between the servlet world and the
 * protocol implementation.
 *
 *  Tomcat.core will receive a Request/Response object - it
 *  can be either a normal HTTP request or any other protocol that
 *  can be mapped to a Request/Response model - including AJPxx,
 *  JNI, etc. If tomcat is integrated with a web server it will
 *  probably use a optimized binary protocol or JNI to communicate with
 *  the server, and in this case tomcat will be in fact a AJP or
 *  JNI server. 
 * 
 *  The Request and Response can be created and should be reused by the connector.
 *
 *  The connector must implement the get/set pattern for configuration, and
 *  may use setProperty( name, value ) pattern, but this is not
 *  under the control of the servlet engine ( and it's not part of this
 *  interface!).
 */
public interface ServerConnector {

    /** Start the adapter. This is done by the context manager when the server
     * is ready to accept requests.
     */
    public void start() throws Exception;

    /** Stop the connector. Will be called by the context manager when the
     *  server is shut-down.
     */
    public void stop() throws Exception;

    /** Set the entry point to tomcat. This object will be used by the
     *  protocol implementation. 
     */
    public void setServer( Object cm );

    /** Set a config property
     */
    public void setAttribute( String s, Object value );

    /** Get the value for the config property that was passed
     */
    public Object getAttribute( String s );

    /** Set a org.apache.tomcat.logging.Logger explicitly
     */
    public void setLogger( org.apache.tomcat.logging.Logger logger );
}
