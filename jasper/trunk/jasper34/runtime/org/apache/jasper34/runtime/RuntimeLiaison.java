/*
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
 */ 

package org.apache.jasper34.runtime;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.*;

// XXX This is just the first draft, to collect the likely hooks.
// The actual interface will change as we implement it ( since
// I have no idea what's going to be the best way to do this stuff )

/**
 *  Interface between jasper runtime and the container. This allows
 *  containers to hook in and provide optimized operations.
 *
 *  A default implementation is provided and can be used for 
 *  "uncooperating" containers ( for example if JspServlet is used and
 *  we have no knowledge of the container ).
 *
 *  A container that is aware of jasper and has a Liaison will register
 *  it with jasper, and can provide special services and shortcut
 *  some layers.
 *
 *  @author Costin Manolache
 *
 */
public abstract class RuntimeLiaison  {
    
    public RuntimeLiaison() {
    }

    /**
     *  Register a dependency between a servlet and a file. The
     *  default implementation will do nothing ( the checks will be
     *  done in checkExpire ).
     *
     *  A smarter implementation will use some depend manager that
     *  avoids multiple checks ( probably using the container dependency
     *  manager ).
     */
    public abstract void registerDependency( Servlet servlet, String file );

    /** Verify if the servlet is not expired
     */
    public abstract boolean checkExpire( Servlet servlet, String file[] );
    
    /** Send a chunk. The chunk must be registered.
     *  The default implementation will just do a
     *   out.println( chunks[id] ).
     *
     *  A smart implementation can pre-convert the String to bytes
     *  ( avoiding per/request overhead ), maintain a cache on the
     *  connector side ( avoiding communication overhead with the server),
     *
     *  XXX The interface will probably change, there are few other
     *  big things that need to be considered - for large chunks we
     *  could use a different API and a static file.
     */
    public abstract void sendChunk( Writer out, String chunks[], int id )
	throws IOException;
}
