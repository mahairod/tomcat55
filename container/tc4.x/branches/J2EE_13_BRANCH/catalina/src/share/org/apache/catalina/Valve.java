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


package org.apache.catalina;


import java.io.IOException;
import javax.servlet.ServletException;


/**
 * A <b>Valve</b> is a request processing component associated with a
 * particular Container.  A series of Valves may be associated with the
 * same Container, creating a request processing pipeline.
 * <p>
 * <b>HISTORICAL NOTE</b>:  The "Valve" name was assigned to this concept
 * because a valve is what you use in a real world pipeline to control and/or
 * modify flows through it.
 *
 * @author Craig R. McClanahan
 * @author Gunnar Rjnning
 * @version $Revision$ $Date$
 */

public interface Valve {


    //-------------------------------------------------------------- Properties


    /**
     * Return the Container with which this Valve is associated, if any.
     */
    public Container getContainer();


    /**
     * Set the Container with which this Valve is associated.
     *
     * @param container The newly associated Container
     *
     * @exception IllegalArgumentException if this Valve refused to be
     *  associated with the specified Container
     * @exception IllegalStateException if this Valve is already
     *  associated a different Container.
     */
    public void setContainer(Container container);


    /**
     * Return descriptive information about this Valve implementation.
     */
    public String getInfo();


    /**
     * Return the next Valve in the pipeline containing this Valve, if any.
     */
    public Valve getNext();


    /**
     * Set the next Valve in the pipeline containing this Valve.
     *
     * @param valve The new next valve, or <code>null</code> if none
     */
    public void setNext(Valve valve);


    /**
     * Return the previous Valve in the pipeline containing this Valve, if any.
     */
    public Valve getPrevious();


    /**
     * Set the previous Valve in the pipeline containing this Valve.
     *
     * @param valve The new previous valve, or <code>null</code> if none
     */
    public void setPrevious(Valve valve);


    //---------------------------------------------------------- Public Methods


    /**
     * Perform request processing as required by this Valve.
     * <p>
     * An individual Valve <b>MAY</b> perform the following actions, in
     * the specified order:
     * <ul>
     * <li>Examine and/or modify the properties of the specified Request and
     *     Response.
     * <li>Examine the properties of the specified Request, completely generate
     *     the corresponding Response, and return control to the caller.
     * <li>Examine the properties of the specified Request and Response, wrap
     *     either or both of these objects to supplement their functionality,
     *     and pass them on.
     * <li>If the corresponding Response was not generated (and control was not
     *     returned, call the next Valve in the pipeline (if there is one) by
     *     executing <code>getNext().invoke()</code>.
     * <li>Examine, but not modify, the properties of the resulting Response
     *     (which was created by a subsequently invoked Valve or Container).
     * </ul>
     * <p>
     * A Valve <b>MUST NOT</b> do any of the following things:
     * <ul>
     * <li>Change request properties that have already been used to direct
     *     the flow of processing control for this request (for instance,
     *     trying to change the virtual host to which a Request should be
     *     sent from a pipeline attached to a Host or Context in the
     *     standard implementation).
     * <li>Create a completed Response <strong>AND</strong> pass this
     *     Request and Response on to the next Valve in the pipeline.
     * <li>Consume bytes from the input stream associated with the Request,
     *     unless it is completely generating the response, or wrapping the
     *     request before passing it on.
     * <li>Modify the HTTP headers included with the Response after the
     *     next <code>invoke()</code> has returned.
     * <li>Perform any actions on the output stream associated with the
     *     specified Response after the next <code>invoke()</code> has
     *     returned.
     * </ul>
     * <p>
     *
     * @param request The servlet request to be processed
     * @param response The servlet response to be created
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    public void invoke(Request request, Response response)
	throws IOException, ServletException;


}
