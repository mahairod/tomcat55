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

package org.apache.catalina.cluster.tcp;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
import org.apache.catalina.cluster.io.XByteBuffer;


public class ReplicationTransmitter
{
    private static org.apache.commons.logging.Log log =
        org.apache.commons.logging.LogFactory.getLog( SimpleTcpCluster.class );

    private java.util.HashMap map = new java.util.HashMap();
    public ReplicationTransmitter(IDataSender[] senders)
    {
        for ( int i=0; i<senders.length; i++)
            map.put(senders[i].getAddress().getHostAddress()+":"+senders[i].getPort(),senders[i]);
    }
    public synchronized void add(IDataSender sender)
    {
        String key = sender.getAddress().getHostAddress()+":"+sender.getPort();
        if ( !map.containsKey(key) )
            map.put(sender.getAddress().getHostAddress()+":"+sender.getPort(),sender);
    }//add

    public synchronized void remove(java.net.InetAddress addr,int port)
    {
        String key = addr.getHostAddress()+":"+port;
        IDataSender sender = (IDataSender)map.get(key);
        if ( sender == null ) return;
        if ( sender.isConnected() ) sender.disconnect();
        map.remove(key);
    }

    public void start() throws java.io.IOException
    {
        //don't have to do shit, we connect on demand
    }

    public synchronized void stop()
    {
        java.util.Iterator i = map.entrySet().iterator();
        while ( i.hasNext() )
        {
            IDataSender sender = (IDataSender)((java.util.HashMap.Entry)i.next()).getValue();
            if ( sender.isConnected() )
            {
                try { sender.disconnect(); } catch ( Exception x ){}
            }//end if
        }//while
    }//stop

    public synchronized IDataSender[] getSenders()
    {
        java.util.Iterator i = map.entrySet().iterator();
        java.util.Vector v = new java.util.Vector();
        while ( i.hasNext() )
        {
            IDataSender sender = (IDataSender)((java.util.HashMap.Entry)i.next()).getValue();
            if ( sender!=null) v.addElement(sender);
        }
        IDataSender[] result = new IDataSender[v.size()];
        return result;
    }

    public void sendMessage(byte[] indata, java.net.InetAddress addr, int port) throws java.io.IOException
    {
        byte[] data = XByteBuffer.createDataPackage(indata);
        String key = addr.getHostAddress()+":"+port;
        IDataSender sender = (IDataSender)map.get(key);
        if ( sender == null ) throw new java.io.IOException("Sender not available. Make sure sender information is available to the ReplicationTransmitter.");
        if ( !sender.isConnected() ) sender.connect();
        sender.sendMessage(data);
    }

    public void sendMessage(byte[] indata) throws java.io.IOException
    {
        java.util.Iterator i = map.entrySet().iterator();
        java.util.Vector v = new java.util.Vector();
        byte[] data = XByteBuffer.createDataPackage(indata);
        while ( i.hasNext() )
        {
            IDataSender sender = (IDataSender)((java.util.HashMap.Entry)i.next()).getValue();
            try
            {
                if (!sender.isConnected())
                    sender.connect();
                sender.sendMessage(data);
            }catch ( Exception x)
            {
                log.warn("Unable to send replicated message, is server down?",x);
            }

        }//while
    }



}