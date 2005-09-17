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


package org.apache.catalina.valves;


import java.io.IOException;
import javax.servlet.ServletException;
import org.apache.catalina.Container;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.Valve;
import org.apache.catalina.util.StringManager;


/**
 * Convenience base class for implementations of the <b>Valve</b> interface.
 * A subclass <strong>MUST</strong> implement an <code>invoke()</code>
 * method to provide the required functionality, and <strong>MAY</strong>
 * implement the <code>Lifecycle</code> interface to provide configuration
 * management and lifecycle support.
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public abstract class ValveBase
    implements Valve {


    //------------------------------------------------------ Instance Variables


    /**
     * The Container whose pipeline this Valve is a component of.
     */
    protected Container container = null;


    /**
     * Descriptive information about this Valve implementation.  This value
     * should be overridden by subclasses.
     */
    protected static String info =
	"org.apache.catalina.core.ValveBase/1.0";


    /**
     * The next Valve in the pipeline this Valve is a component of.
     */
    protected Valve next = null;


    /**
     * The previous Valve in the pipeline this Valve is a component of.
     */
    protected Valve previous = null;


    /**
     * The string manager for this package.
     */
    protected final static StringManager sm =
	StringManager.getManager(Constants.Package);


    //-------------------------------------------------------------- Properties


    /**
     * Return the Container with which this Valve is associated, if any.
     */
    public Container getContainer() {

	return (container);

    }


    /**
     * Set the Container with which this Valve is associated, if any.
     *
     * @param container The new associated container
     */
    public void setContainer(Container container) {

	this.container = container;

    }


    /**
     * Return descriptive information about this Valve implementation.
     */
    public String getInfo() {

	return (info);

    }


    /**
     * Return the next Valve in this pipeline, or <code>null</code> if this
     * is the last Valve in the pipeline.
     */
    public Valve getNext() {

	return (next);

    }


    /**
     * Set the Valve that follows this one in the pipeline it is part of.
     *
     * @param valve The new next valve
     */
    public void setNext(Valve valve) {

	this.next = valve;

    }


    /**
     * Return the previous Valve in this pipeline, or <code>null</code> if
     * this is the first Valve in the pipeline.
     */
    public Valve getPrevious() {

	return (previous);

    }


    /**
     * Set the Valve that preceeds this one in the pipeline it is part of.
     *
     * @param valve The previous valve
     */
    public void setPrevious(Valve valve) {

	this.previous = valve;

    }


    //---------------------------------------------------------- Public Methods


    /**
     * The implementation-specific logic represented by this Valve.  See the
     * Valve description for the normal design patterns for this method.
     * <p>
     * This method <strong>MUST</strong> be provided by a subclass.
     *
     * @param request The servlet request to be processed
     * @param response The servlet response to be created
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    public abstract void invoke(Request request, Response response)
	throws IOException, ServletException;


    /**
     * Invoke the next Valve in our pipeline, or complain if there is no such
     * Valve remaining.
     *
     * @param request The servlet request to be processed
     * @param response The servlet response to be created
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    public void invokeNext(Request request, Response response)
	throws IOException, ServletException {

	if (getNext() != null)
	    getNext().invoke(request, response);
	else
	    throw new IllegalStateException
		(sm.getString("valveBase.noNext"));

    }


}
