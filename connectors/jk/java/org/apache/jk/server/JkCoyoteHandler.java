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

package org.apache.jk.server;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.jk.*;
import org.apache.jk.core.*;
import org.apache.jk.common.*;
import org.apache.jk.util.*;

import org.apache.tomcat.util.buf.*;
import org.apache.tomcat.util.log.*;
import org.apache.tomcat.util.http.*;

import org.apache.coyote.*;

/** Plugs Jk2 into Coyote
 */
public class JkCoyoteHandler extends JkHandler implements ProtocolHandler, ActionHook
{
    Adapter adapter;
    protected JkMain jkMain=new JkMain();
    
    /** Pass config info
     */
    public void setAttribute( String name, Object value ) {
        System.out.println("Set attribute " + name + " " + value );
    }
    
    public Object getAttribute( String name ) {
        return null;
    }

    /** The adapter, used to call the connector 
     */
    public void setAdapter(Adapter adapter) {
        this.adapter=adapter;
    }

    public Adapter getAdapter() {
        return adapter;
    }

    boolean started=false;
    
    /** Start the protocol
     */
    public void init() {
        if( started ) return;

        started=true;
        jkMain.getWorkerEnv().addHandler("container", this );

        try {
            jkMain.init();
            jkMain.start();
        } catch( Exception ex ) {
            ex.printStackTrace();
        }
    }

    public void destroy() {
        //  jkMain.stop();
    }


    // Jk Handler mehod
    public int invoke( Msg msg, MsgContext ep ) 
        throws IOException
    {
        System.out.println("XXX Invoke " );
        org.apache.coyote.Request req=(org.apache.coyote.Request)ep.getRequest();
        org.apache.coyote.Response res=req.getResponse();
        res.setHook( this );
        try {
            adapter.service( req, res );
        } catch( Exception ex ) {
            ex.printStackTrace();
        }
        return OK;
    }

    public void action(ActionCode actionCode, Object param) {
        System.out.println("XXX Action " + actionCode + " " + param );
        if( actionCode==ActionCode.ACTION_COMMIT ) {
            
        }
        if( actionCode==ActionCode.ACTION_RESET ) {
            
        }
        if( actionCode==ActionCode.ACTION_CLOSE ) {
        }
        if( actionCode==ActionCode.ACTION_ACK ) {
            
        }
    }


}
