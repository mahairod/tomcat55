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
package org.apache.coyote.ajp;

import org.apache.coyote.AbstractProtocol;
import org.apache.coyote.Processor;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.AprEndpoint;
import org.apache.tomcat.util.net.AprEndpoint.Handler;
import org.apache.tomcat.util.net.SocketWrapperBase;


/**
 * This the APR/native based protocol handler implementation for AJP.
 */
public class AjpAprProtocol extends AbstractAjpProtocol<Long> {


    private static final Log log = LogFactory.getLog(AjpAprProtocol.class);

    @Override
    protected Log getLog() { return log; }


    @Override
    protected AbstractEndpoint.Handler getHandler() {
        return cHandler;
    }


    @Override
    public boolean isAprRequired() {
        // Override since this protocol implementation requires the APR/native
        // library
        return true;
    }


    // ------------------------------------------------------------ Constructor

    public AjpAprProtocol() {
        super(new AprEndpoint());
        cHandler = new AjpConnectionHandler(this);
        ((AprEndpoint) getEndpoint()).setHandler(cHandler);
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Connection handler for AJP.
     */
    private final AjpConnectionHandler cHandler;


    // --------------------------------------------------------- Public Methods


    public int getPollTime() { return ((AprEndpoint)getEndpoint()).getPollTime(); }
    public void setPollTime(int pollTime) { ((AprEndpoint)getEndpoint()).setPollTime(pollTime); }

    // pollerSize is now a synonym for maxConnections
    public void setPollerSize(int pollerSize) { getEndpoint().setMaxConnections(pollerSize); }
    public int getPollerSize() { return getEndpoint().getMaxConnections(); }


    // ----------------------------------------------------- JMX related methods

    @Override
    protected String getNamePrefix() {
        return ("ajp-apr");
    }


    // --------------------------------------  AjpConnectionHandler Inner Class


    protected static class AjpConnectionHandler
            extends AbstractAjpConnectionHandler<Long>
            implements Handler {

        protected final AjpAprProtocol proto;

        public AjpConnectionHandler(AjpAprProtocol proto) {
            this.proto = proto;
        }

        @Override
        protected AbstractProtocol<Long> getProtocol() {
            return proto;
        }

        @Override
        protected Log getLog() {
            return log;
        }

        /**
         * Expected to be used by the handler once the processor is no longer
         * required.
         */
        @Override
        public void release(SocketWrapperBase<Long> socket,
                Processor<Long> processor, boolean isSocketClosing,
                boolean addToPoller) {
            processor.recycle(isSocketClosing);
            recycledProcessors.push(processor);
            if (addToPoller) {
                ((AprEndpoint)proto.getEndpoint()).getPoller().add(
                        socket.getSocket().longValue(),
                        proto.getEndpoint().getKeepAliveTimeout(), true, false);
            }
        }


        @Override
        protected AjpProcessor<Long> createProcessor() {
            AjpProcessor<Long> processor =
                    new AjpProcessor<>(proto.getPacketSize(), proto.getEndpoint());
            proto.configureProcessor(processor);
            register(processor);
            return processor;
        }
    }
}
