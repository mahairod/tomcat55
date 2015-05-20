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
package org.apache.coyote.http2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.http.HttpUpgradeHandler;

import org.apache.coyote.AbstractProcessor;
import org.apache.coyote.ActionCode;
import org.apache.coyote.Adapter;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.net.AbstractEndpoint.Handler.SocketState;
import org.apache.tomcat.util.net.SSLSupport;
import org.apache.tomcat.util.net.SocketStatus;
import org.apache.tomcat.util.net.SocketWrapperBase;
import org.apache.tomcat.util.res.StringManager;

public class StreamProcessor extends AbstractProcessor implements Runnable {

    private static final Log log = LogFactory.getLog(StreamProcessor.class);
    private static final StringManager sm = StringManager.getManager(StreamProcessor.class);

    private final Stream stream;


    public StreamProcessor(Stream stream, Adapter adapter, SocketWrapperBase<?> socketWrapper) {
        super(stream.getCoyoteRequest(), stream.getCoyoteResponse());
        this.stream = stream;
        setAdapter(adapter);
        setSocketWrapper(socketWrapper);
    }


    @Override
    public void run() {
        try {
            adapter.service(request, response);
            // Ensure the response is complete
            response.action(ActionCode.CLIENT_FLUSH, null);
        } catch (Exception e) {
            // TODO
            e.printStackTrace();
        }
    }


    @Override
    public SocketState process(SocketWrapperBase<?> socket) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public SocketState dispatch(SocketStatus status) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public void action(ActionCode actionCode, Object param) {
        switch (actionCode) {
        case REQ_HOST_ATTRIBUTE: {
            request.remoteHost().setString(socketWrapper.getRemoteHost());
            break;
        }
        case IS_ERROR: {
            ((AtomicBoolean) param).set(getErrorState().isError());
            break;
        }
        case CLIENT_FLUSH: {
            action(ActionCode.COMMIT, null);
            stream.flushData();
            break;
        }
        case COMMIT: {
            if (!response.isCommitted()) {
                response.setCommitted(true);
                stream.writeHeaders();
            }
            break;
        }
        default:
            // TODO
            log.debug("TODO: Action: " + actionCode);
        }
    }


    @Override
    public void recycle() {
        // TODO Auto-generated method stub

    }


    @Override
    public void setSslSupport(SSLSupport sslSupport) {
        // TODO Auto-generated method stub

    }


    @Override
    public boolean isUpgrade() {
        return false;
    }


    @Override
    protected Log getLog() {
        return log;
    }


    @Override
    public HttpUpgradeHandler getHttpUpgradeHandler() {
        // Should never happen
        throw new IllegalStateException(sm.getString("streamProcessor.httpupgrade.notsupported"));
    }


    @Override
    public ByteBuffer getLeftoverInput() {
        // Should never happen
        throw new IllegalStateException(sm.getString("streamProcessor.httpupgrade.notsupported"));
    }
}
