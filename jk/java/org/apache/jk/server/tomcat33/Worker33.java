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
 * [Additional notices, if required by prior licensing conditions]
 *
 */

package org.apache.jk.server.tomcat33;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.jk.*;
import org.apache.jk.core.*;
import org.apache.jk.common.*;
import org.apache.tomcat.modules.server.PoolTcpConnector;

import org.apache.tomcat.core.*;

import org.apache.tomcat.util.net.*;
import org.apache.tomcat.util.buf.*;
import org.apache.tomcat.util.log.*;
import org.apache.tomcat.util.http.*;

import org.apache.jk.core.*;

/** Tomcat 33 worker
 *
 */
public class Worker33 extends Worker
{
    ContextManager cm;
    
    public Worker33()
    {
        super();
    }

    private int reqNote;
    
    public void init() throws IOException {
        reqNote=we.getNoteId( WorkerEnv.REQUEST_NOTE, "tomcat33Request" );
    }

    public void setContextManager( ContextManager cm ) {
        this.cm=cm;
    }
    
    public void service( BaseRequest req, Channel ch, Endpoint ep )
        throws IOException
    {
        d("Incoming request " );
        JkRequest33 treq=(JkRequest33)req.getNote( reqNote );
        JkResponse33 tres;
        if( treq==null ) {
            treq=new JkRequest33(req);
            req.setNote( reqNote, treq );
            tres=new JkResponse33(we);
            treq.setResponse( tres );
            cm.initRequest( treq, tres );
        }
        tres=(JkResponse33)treq.getResponse();
        treq.setEndpoint( ch, ep );
        tres.setEndpoint( ch, ep );

        try {
            cm.service( treq, tres );
        } catch(Exception ex ) {
            ex.printStackTrace();
        }
    }

    private static final int dL=0;
    private static void d(String s ) {
        System.err.println( "Worker33: " + s );
    }

    
}
