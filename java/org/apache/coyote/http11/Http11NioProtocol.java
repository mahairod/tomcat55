/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import org.apache.coyote.AbstractProtocol;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.NioChannel;
import org.apache.tomcat.util.net.NioEndpoint;
import org.apache.tomcat.util.net.NioEndpoint.Handler;
import org.apache.tomcat.util.net.NioEndpoint.KeyAttachment;
import org.apache.tomcat.util.net.SSLImplementation;
import org.apache.tomcat.util.net.SecureNioChannel;
import org.apache.tomcat.util.net.SocketWrapper;


/**
 * Abstract the protocol implementation, including threading, etc.
 * Processor is single threaded and specific to stream-based protocols,
 * will not fit Jk protocols like JNI.
 *
 * @author Remy Maucherat
 * @author Costin Manolache
 * @author Filip Hanik
 */
public class Http11NioProtocol extends AbstractHttp11JsseProtocol {
    
    private static final Log log = LogFactory.getLog(Http11NioProtocol.class);


    @Override
    protected Log getLog() { return log; }
    

    @Override
    protected AbstractEndpoint.Handler getHandler() {
        return cHandler;
    }


    public Http11NioProtocol() {
        endpoint=new NioEndpoint();
        cHandler = new Http11ConnectionHandler(this);
        ((NioEndpoint) endpoint).setHandler(cHandler);
        setSoLinger(Constants.DEFAULT_CONNECTION_LINGER);
        setSoTimeout(Constants.DEFAULT_CONNECTION_TIMEOUT);
        setTcpNoDelay(Constants.DEFAULT_TCP_NO_DELAY);
    }


    public NioEndpoint getEndpoint() {
        return ((NioEndpoint)endpoint);
    }


    // -------------------- Properties--------------------
    
    private Http11ConnectionHandler cHandler;

    // -------------------- Pool setup --------------------

    public void setPollerThreadCount(int count) {
        ((NioEndpoint)endpoint).setPollerThreadCount(count);
    }
    
    public int getPollerThreadCount() {
        return ((NioEndpoint)endpoint).getPollerThreadCount();
    }
    
    public void setSelectorTimeout(long timeout) {
        ((NioEndpoint)endpoint).setSelectorTimeout(timeout);
    }
    
    public long getSelectorTimeout() {
        return ((NioEndpoint)endpoint).getSelectorTimeout();
    }
    
    public void setAcceptorThreadPriority(int threadPriority) {
        ((NioEndpoint)endpoint).setAcceptorThreadPriority(threadPriority);
    }

    public void setPollerThreadPriority(int threadPriority) {
        ((NioEndpoint)endpoint).setPollerThreadPriority(threadPriority);
    }

    public int getAcceptorThreadPriority() {
      return ((NioEndpoint)endpoint).getAcceptorThreadPriority();
    }
    
    public int getPollerThreadPriority() {
      return ((NioEndpoint)endpoint).getThreadPriority();
    }
    
    
    public boolean getUseSendfile() {
        return ((NioEndpoint)endpoint).getUseSendfile();
    }

    public void setUseSendfile(boolean useSendfile) {
        ((NioEndpoint)endpoint).setUseSendfile(useSendfile);
    }
    
    // -------------------- Tcp setup --------------------
    public void setOomParachute(int oomParachute) {
        ((NioEndpoint)endpoint).setOomParachute(oomParachute);
    }

    // ----------------------------------------------------- JMX related methods

    @Override
    protected String getNamePrefix() {
        return ("http-nio");
    }


    // --------------------  Connection handler --------------------

    protected static class Http11ConnectionHandler
            extends AbstractConnectionHandler<NioChannel,Http11NioProcessor>
            implements Handler {

        protected Http11NioProtocol proto;

        Http11ConnectionHandler(Http11NioProtocol proto) {
            this.proto = proto;
        }
        
        @Override
        protected AbstractProtocol getProtocol() {
            return proto;
        }

        @Override
        protected Log getLog() {
            return log;
        }
        
        
        @Override
        public SSLImplementation getSslImplementation() {
            return proto.sslImplementation;
        }

        /**
         * Expected to be used by the Poller to release resources on socket
         * close, errors etc.
         */
        @Override
        public void release(SocketChannel socket) {
            if (log.isDebugEnabled()) 
                log.debug("Iterating through our connections to release a socket channel:"+socket);
            boolean released = false;
            Iterator<java.util.Map.Entry<NioChannel, Http11NioProcessor>> it = connections.entrySet().iterator();
            while (it.hasNext()) {
                java.util.Map.Entry<NioChannel, Http11NioProcessor> entry = it.next();
                if (entry.getKey().getIOChannel()==socket) {
                    it.remove();
                    Http11NioProcessor result = entry.getValue();
                    result.recycle();
                    unregister(result);
                    released = true;
                    break;
                }
            }
            if (log.isDebugEnabled()) 
                log.debug("Done iterating through our connections to release a socket channel:"+socket +" released:"+released);
        }
        
        /**
         * Expected to be used by the Poller to release resources on socket
         * close, errors etc.
         */
        @Override
        public void release(SocketWrapper<NioChannel> socket) {
            Http11NioProcessor processor =
                connections.remove(socket.getSocket());
            if (processor != null) {
                processor.recycle();
                recycledProcessors.offer(processor);
            }
        }


        /**
         * Expected to be used by the handler once the processor is no longer
         * required.
         * 
         * @param socket
         * @param processor
         * @param isSocketClosing   Not used in HTTP
         * @param addToPoller
         */
        @Override
        public void release(SocketWrapper<NioChannel> socket,
                Http11NioProcessor processor, boolean isSocketClosing,
                boolean addToPoller) {
            processor.recycle();
            recycledProcessors.offer(processor);
            if (addToPoller) {
                socket.getSocket().getPoller().add(socket.getSocket());
            }
        }


        @Override
        protected void initSsl(SocketWrapper<NioChannel> socket,
                Http11NioProcessor processor) {
            if (proto.isSSLEnabled() &&
                    (proto.sslImplementation != null)
                    && (socket.getSocket() instanceof SecureNioChannel)) {
                SecureNioChannel ch = (SecureNioChannel)socket.getSocket();
                processor.setSslSupport(
                        proto.sslImplementation.getSSLSupport(
                                ch.getSslEngine().getSession()));
            } else {
                processor.setSslSupport(null);
            }

        }

        @Override
        protected void longPoll(SocketWrapper<NioChannel> socket,
                Http11NioProcessor processor) {
            connections.put(socket.getSocket(), processor);
            
            if (processor.isAsync()) {
                socket.setAsync(true);
            } else {
                // Either:
                //  - this is comet request
                //  - the request line/headers have not been completely
                //    read
                SelectionKey key = socket.getSocket().getIOChannel().keyFor(
                        socket.getSocket().getPoller().getSelector());
                key.interestOps(SelectionKey.OP_READ);
                ((KeyAttachment) socket).interestOps(
                        SelectionKey.OP_READ);
            }
        }

        @Override
        public Http11NioProcessor createProcessor() {
            Http11NioProcessor processor = new Http11NioProcessor(
                    proto.getMaxHttpHeaderSize(), (NioEndpoint)proto.endpoint,
                    proto.getMaxTrailerSize());
            processor.setAdapter(proto.adapter);
            processor.setMaxKeepAliveRequests(proto.getMaxKeepAliveRequests());
            processor.setKeepAliveTimeout(proto.getKeepAliveTimeout());
            processor.setConnectionUploadTimeout(
                    proto.getConnectionUploadTimeout());
            processor.setDisableUploadTimeout(proto.getDisableUploadTimeout());
            processor.setCompressionMinSize(proto.getCompressionMinSize());
            processor.setCompression(proto.getCompression());
            processor.setNoCompressionUserAgents(proto.getNoCompressionUserAgents());
            processor.setCompressableMimeTypes(proto.getCompressableMimeTypes());
            processor.setRestrictedUserAgents(proto.getRestrictedUserAgents());
            processor.setSocketBuffer(proto.getSocketBuffer());
            processor.setMaxSavePostSize(proto.getMaxSavePostSize());
            processor.setServer(proto.getServer());
            register(processor);
            return processor;
        }
    }
}
