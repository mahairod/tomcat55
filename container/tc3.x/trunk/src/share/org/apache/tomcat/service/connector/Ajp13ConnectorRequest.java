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

package org.apache.tomcat.service.connector;

import java.io.*;
import java.net.*;
import java.util.*;
import org.apache.tomcat.core.*;
import org.apache.tomcat.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class Ajp13ConnectorRequest extends RequestImpl 
{
    public static final byte SC_A_CONTEXT      = 1;
    public static final byte SC_A_SERVLET_PATH = 2;
    public static final byte SC_A_REMOTE_USER  = 3;
    public static final byte SC_A_AUTH_TYPE    = 4;
    public static final byte SC_A_QUERY_STRING = 5;
    public static final byte SC_A_JVM_ROUTE    = 6;
    public static final byte SC_A_SSL_CERT     = 7;
    public static final byte SC_A_SSL_CIPHER   = 8;
    public static final byte SC_A_SSL_SESSION  = 9;
    public static final byte SC_A_ARE_DONE     = (byte)0xFF;

    public static final String []methodTransArray = {
        "OPTIONS",
        "GET",
        "HEAD",
        "POST",
        "PUT",
        "DELETE",
        "TRACE"
    };
    
    public static final String []headerTransArray = {
        "accept",
        "accept-charset",
        "accept-encoding",
        "accept-language",
        "authorization",
        "connection",
        "content-type",
        "content-length",
        "cookie",
        "cookie2",
        "host",
        "pragma",
        "referer",
        "user-agent"
    };

    MsgConnector con;
    Hashtable env_vars;

    private InputStream in;
    byte bodyBuff[];
    int blen;
    int pos;

    public Ajp13ConnectorRequest() 
    {
        super();
    }
    
    protected int decodeRequest(MsgBuffer msg) throws IOException 
    {
        boolean isSSL = false;
        byte bsc;
        int  hCount = 0;

        /*
         * Read the method and translate it to a String
         */
        bsc        = msg.getByte();
        method     = methodTransArray[(int)bsc - 1];
        protocol   = msg.getString();
        requestURI = msg.getString();
        remoteAddr = msg.getString();
        remoteHost = msg.getString();
        serverName = msg.getString();
        serverPort = msg.getInt();
        bsc        = msg.getByte();
        if(bsc != 0) {
            isSSL = true;
        }
        hCount     = msg.getInt();
        for(int i = 0 ; i < hCount ; i++) {
            String hName = null;

            int isc = msg.peekInt();
            int hId = isc & 0x000000FF;

            isc &= 0x0000FF00;
            if(0x0000A000 == isc) {
                msg.getInt();               
                hName = headerTransArray[hId - 1];
            } else {
                hName = msg.getString().toLowerCase();
            }

            String hValue = msg.getString();
            headers.putHeader( hName , hValue );
            //System.out.println( "Head: " + hName + "=" + hValue);
        }

        for(bsc = msg.getByte() ;
            bsc != SC_A_ARE_DONE ;
            bsc = msg.getByte()) {
            switch(bsc) {
                case SC_A_CONTEXT      :
                    contextPath = msg.getString();
                break;
                                
                case SC_A_SERVLET_PATH :
                    System.out.println("SC_A_SERVLET_PATH not in use " + msg.getString());
                break;
                                
                case SC_A_REMOTE_USER  :
                    remoteUser = msg.getString();
                break;
                                
                case SC_A_AUTH_TYPE    :
                    authType = msg.getString();
                break;
                                
                case SC_A_QUERY_STRING :
                    queryString = msg.getString();
                break;
                                
                case SC_A_JVM_ROUTE    :
                    jvmRoute = msg.getString();
                break;
                                
                case SC_A_SSL_CERT     :
                    System.out.println("SC_A_SSL_CERT not in use " + msg.getString());
                break;
                                
                case SC_A_SSL_CIPHER   :
                    System.out.println("SC_A_SSL_CIPHER not in use " + msg.getString());
                break;
                                
                case SC_A_SSL_SESSION  :
                    System.out.println("SC_A_SSL_SESSION not in use " + msg.getString());
                break;
                
                default:
                    return -1;                
            }
        }

        contentLength = headers.getIntHeader("content-length");
        contentType = headers.getHeader("content-type");
    
        return 0;
    }
    
    public int doRead() throws IOException 
    {
        if(pos > blen) {
            System.out.println("Read after end " + pos + " " + blen );
            return  -1;
        }
        return bodyBuff[pos++];
    }
    
    public int doRead(byte[] b, int off, int len) throws IOException 
    {
        // XXXXXX Stupid, but the whole thing must be rewriten ( see super()! )
        for(int i = off ; i < len+off ; i++) {
            int a=doRead();
            if(a==-1) {
                System.out.println("Y");
                return i-off;
            }
            b[i]=(byte)a;
        }
        System.out.println("doRead " + off + " " + len );
        return len;
    }
    
    public void recycle() 
    {
        super.recycle();
        pos=0;
    }
    
    public void setConnector(MsgConnector con) 
    {
        this.con = con;
        pos = 0;
        this.in = new BufferedServletInputStream(this);
    }   
}
