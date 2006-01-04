/*
 *  Copyright 1999-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.coyote.http11;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.commons.modeler.Registry;
import org.apache.coyote.ActionCode;
import org.apache.coyote.ActionHook;
import org.apache.coyote.Adapter;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.RequestGroupInfo;
import org.apache.coyote.RequestInfo;
import org.apache.tomcat.util.net.PoolTcpEndpoint;
import org.apache.tomcat.util.net.TcpConnection;
import org.apache.tomcat.util.net.TcpConnectionHandler;
import org.apache.tomcat.util.net.javaio.SSLImplementation;
import org.apache.tomcat.util.net.javaio.SSLSupport;
import org.apache.tomcat.util.net.javaio.ServerSocketFactory;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.threads.ThreadPool;
import org.apache.tomcat.util.threads.ThreadWithAttributes;


/**
 * Abstract the protocol implementation, including threading, etc.
 * Processor is single threaded and specific to stream-based protocols,
 * will not fit Jk protocols like JNI.
 *
 * @author Remy Maucherat
 * @author Costin Manolache
 */
public class Http11Protocol extends Http11BaseProtocol implements MBeanRegistration
{
    public Http11Protocol() {
    }
    
    protected Http11ConnectionHandler createConnectionHandler() {
        Http11ConnectionHandler cHandler = new JmxHttp11ConnectionHandler( this );
        setSoLinger(Constants.DEFAULT_CONNECTION_LINGER);
        setSoTimeout(Constants.DEFAULT_CONNECTION_TIMEOUT);
        setServerSoTimeout(Constants.DEFAULT_SERVER_SOCKET_TIMEOUT);
        setTcpNoDelay(Constants.DEFAULT_TCP_NO_DELAY);
        return cHandler ;
    }

    ObjectName tpOname;
    ObjectName rgOname;

    public void start() throws Exception {
        if( this.domain != null ) {
            try {
                // XXX We should be able to configure it separately
                // XXX It should be possible to use a single TP
                tpOname=new ObjectName
                    (domain + ":" + "type=ThreadPool,name=" + getName());
                Registry.getRegistry(null, null).registerComponent(ep, tpOname, null );
                ep.setName(getName());
                ep.setDaemon(false);
                ep.addEndpointListener(new MXPoolListener(this, ep));
            } catch (Exception e) {
                log.error("Can't register threadpool" );
            }
            rgOname=new ObjectName
                (domain + ":type=GlobalRequestProcessor,name=" + getName());
            Registry.getRegistry(null, null).registerComponent
                ( cHandler.global, rgOname, null );
        }

        super.start();
    }

    public void destroy() throws Exception {
        super.destroy();
        if( tpOname!=null )
            Registry.getRegistry(null, null).unregisterComponent(tpOname);
        if( rgOname != null )
            Registry.getRegistry(null, null).unregisterComponent(rgOname);
    }

    // --------------------  Connection handler --------------------

    static class MXPoolListener implements PoolTcpEndpoint.EndpointListener {
        MXPoolListener( Http11Protocol proto, PoolTcpEndpoint control ) {

        }

        public void threadStart(PoolTcpEndpoint tp, Thread t) {
        }

        public void threadEnd(PoolTcpEndpoint tp, Thread t) {
            // Register our associated processor
            // TP uses only TWA
            ThreadWithAttributes ta=(ThreadWithAttributes)t;
            Object tpData[]=ta.getThreadData(tp);
            if( tpData==null ) return;
            // Weird artifact - it should be cleaned up, but that may break something
            // and it won't gain us too much
            if( tpData[1] instanceof Object[] ) {
                tpData=(Object [])tpData[1];
            }
            ObjectName oname=(ObjectName)tpData[Http11BaseProtocol.THREAD_DATA_OBJECT_NAME];
            if( oname==null ) return;
            Registry.getRegistry(null, null).unregisterComponent(oname);
            Http11Processor processor =
                (Http11Processor) tpData[Http11Protocol.THREAD_DATA_PROCESSOR];
            RequestInfo rp=processor.getRequest().getRequestProcessor();
            rp.setGlobalProcessor(null);
        }
    }

    static class JmxHttp11ConnectionHandler extends Http11ConnectionHandler  {
        Http11Protocol proto;
        static int count=0;
        RequestGroupInfo global=new RequestGroupInfo();

        JmxHttp11ConnectionHandler( Http11Protocol proto ) {
            super(proto);
            this.proto = proto ;
        }

        public void setAttribute( String name, Object value ) {
        }

        public void setServer( Object o ) {
        }

        public Object[] init() {

            Object thData[]=super.init();

            // was set up by supper
            Http11Processor  processor = (Http11Processor)
                    thData[ Http11BaseProtocol.THREAD_DATA_PROCESSOR];

            if( proto.getDomain() != null ) {
                try {
                    RequestInfo rp=processor.getRequest().getRequestProcessor();
                    rp.setGlobalProcessor(global);
                    ObjectName rpName=new ObjectName
                        (proto.getDomain() + ":type=RequestProcessor,worker="
                         + proto.getName() +",name=HttpRequest" + count++ );
                    Registry.getRegistry(null, null).registerComponent( rp, rpName, null);
                    thData[Http11BaseProtocol.THREAD_DATA_OBJECT_NAME]=rpName;
                } catch( Exception ex ) {
                    log.warn("Error registering request");
                }
            }

            return  thData;
        }
    }

    // -------------------- Various implementation classes --------------------


    protected String domain;
    protected ObjectName oname;
    protected MBeanServer mserver;

    public ObjectName getObjectName() {
        return oname;
    }

    public String getDomain() {
        return domain;
    }

    public ObjectName preRegister(MBeanServer server,
                                  ObjectName name) throws Exception {
        oname=name;
        mserver=server;
        domain=name.getDomain();
        return name;
    }

    public void postRegister(Boolean registrationDone) {
    }

    public void preDeregister() throws Exception {
    }

    public void postDeregister() {
    }

}
