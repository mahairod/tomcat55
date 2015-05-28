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

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.net.Nio2Channel;
import org.apache.tomcat.util.net.Nio2Endpoint;
import org.apache.tomcat.util.net.Nio2Endpoint.Handler;


/**
 * This the NIO2 based protocol handler implementation for AJP.
 */
public class AjpNio2Protocol extends AbstractAjpProtocol<Nio2Channel> {

    private static final Log log = LogFactory.getLog(AjpNio2Protocol.class);

    @Override
    protected Log getLog() { return log; }


    // ------------------------------------------------------------ Constructor

    public AjpNio2Protocol() {
        super(new Nio2Endpoint());
        AjpConnectionHandler cHandler = new AjpConnectionHandler(this);
        setHandler(cHandler);
        ((Nio2Endpoint) getEndpoint()).setHandler(cHandler);
    }


    // ----------------------------------------------------- JMX related methods

    @Override
    protected String getNamePrefix() {
        return ("ajp-nio2");
    }


    // --------------------------------------  AjpConnectionHandler Inner Class

    protected static class AjpConnectionHandler
            extends AbstractAjpConnectionHandler<Nio2Channel>
            implements Handler {

        public AjpConnectionHandler(AjpNio2Protocol proto) {
            super(proto);
        }


        @Override
        protected Log getLog() {
            return log;
        }


        @Override
        public void closeAll() {
            for (Nio2Channel channel : connections.keySet()) {
                ((Nio2Endpoint) getProtocol().getEndpoint()).closeSocket(channel.getSocket());
            }
        }
    }
}
