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

import java.nio.ByteBuffer;

import javax.servlet.http.HttpUpgradeHandler;

import org.apache.coyote.AbstractProtocol;
import org.apache.coyote.Processor;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.SocketWrapperBase;
import org.apache.tomcat.util.res.StringManager;

/**
 * The is the base implementation for the AJP protocol handlers. Implementations
 * typically extend this base class rather than implement {@link
 * org.apache.coyote.ProtocolHandler}. All of the implementations that ship with
 * Tomcat are implemented this way.
 *
 * @param <S> The type of socket used by the implementation
 */
public abstract class AbstractAjpProtocol<S> extends AbstractProtocol<S> {

    /**
     * The string manager for this package.
     */
    protected static final StringManager sm = StringManager.getManager(AbstractAjpProtocol.class);


    public AbstractAjpProtocol(AbstractEndpoint<S> endpoint) {
        super(endpoint);
    }


    @Override
    protected String getProtocolName() {
        return "Ajp";
    }


    // ------------------------------------------------- AJP specific properties
    // ------------------------------------------ managed in the ProtocolHandler

    /**
     * Should authentication be done in the native webserver layer,
     * or in the Servlet container ?
     */
    private boolean tomcatAuthentication = true;
    public boolean getTomcatAuthentication() { return tomcatAuthentication; }
    public void setTomcatAuthentication(boolean tomcatAuthentication) {
        this.tomcatAuthentication = tomcatAuthentication;
    }


    /**
     * Required secret.
     */
    private String requiredSecret = null;
    public void setRequiredSecret(String requiredSecret) {
        this.requiredSecret = requiredSecret;
    }


    /**
     * AJP packet size.
     */
    private int packetSize = Constants.MAX_PACKET_SIZE;
    public int getPacketSize() { return packetSize; }
    public void setPacketSize(int packetSize) {
        if(packetSize < Constants.MAX_PACKET_SIZE) {
            this.packetSize = Constants.MAX_PACKET_SIZE;
        } else {
            this.packetSize = packetSize;
        }
    }

    protected void configureProcessor(AjpProcessor<S> processor) {
        processor.setAdapter(getAdapter());
        processor.setTomcatAuthentication(getTomcatAuthentication());
        processor.setRequiredSecret(requiredSecret);
        processor.setKeepAliveTimeout(getKeepAliveTimeout());
        processor.setClientCertProvider(getClientCertProvider());
    }

    protected abstract static class AbstractAjpConnectionHandler<S>
            extends AbstractConnectionHandler<S,AjpProcessor<S>> {

        @Override
        protected void initSsl(SocketWrapperBase<S> socket, Processor<S> processor) {
            // NOOP for AJP
        }

        @Override
        protected void longPoll(SocketWrapperBase<S> socket,
                Processor<S> processor) {
            // Same requirements for all AJP connectors
            socket.setAsync(true);
        }

        @Override
        protected AjpProcessor<S> createUpgradeProcessor(SocketWrapperBase<S> socket,
                ByteBuffer leftoverInput, HttpUpgradeHandler httpUpgradeHandler) {
            // TODO should fail - throw IOE
            return null;
        }
    }
}
