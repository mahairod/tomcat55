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

package org.apache.coyote;


/**
 * Structure holding the Request and Response objects. It also holds statistical
 * informations about request processing and provide management informations
 * about the requests beeing processed.
 *
 * Each thread uses a Request/Response pair that is recycled on each request.
 * This object provides a place to collect global low-level statistics - without
 * having to deal with synchronization ( since each thread will have it's own
 * RequestProcessorMX ).
 *
 * TODO: Request notifications will be registered here.
 *
 * @author Costin Manolache
 */
public class RequestInfo  {
    RequestGroupInfo global=null;

    // ----------------------------------------------------------- Constructors

    public RequestInfo( Request req) {
        this.req=req;
    }

    public RequestGroupInfo getGlobalProcessor() {
        return global;
    }
    
    public void setGlobalProcessor(RequestGroupInfo global) {
        if( global != null) {
            this.global=global;
            global.addRequestProcessor( this );
        } else {
        	if (this.global != null) {
                this.global.removeRequestProcessor( this ); 
                this.global = null;
            }
        }
    }


    // ----------------------------------------------------- Instance Variables
    Request req;
    Response res;
    int stage = Constants.STAGE_NEW;

    // -------------------- Information about the current request  -----------
    // This is usefull for long-running requests only

    public String getMethod() {
        return req.method().toString();
    }

    public String getCurrentUri() {
        return req.requestURI().toString();
    }

    public String getCurrentQueryString() {
        return req.queryString().toString();
    }

    public String getProtocol() {
        return req.protocol().toString();
    }

    public String getVirtualHost() {
        return req.serverName().toString();
    }

    public int getServerPort() {
        return req.getServerPort();
    }

    public String getRemoteAddr() {
        req.action(ActionCode.ACTION_REQ_HOST_ADDR_ATTRIBUTE, null);
        return req.remoteAddr().toString();
    }

    public int getContentLength() {
        return req.getContentLength();
    }

    public long getRequestBytesReceived() {
        return req.getBytesRead();
    }

    public long getRequestBytesSent() {
        return req.getResponse().getBytesWritten();
    }

    public long getRequestProcessingTime() {
        return (System.currentTimeMillis() - req.getStartTime());
    }

    // -------------------- Statistical data  --------------------
    // Collected at the end of each request.
    private long bytesSent;
    private long bytesReceived;

    // Total time = divide by requestCount to get average.
    private long processingTime;
    // The longest response time for a request
    private long maxTime;
    // URI of the request that took maxTime
    private String maxRequestUri;

    private int requestCount;
    // number of response codes >= 400
    private int errorCount;


    /** Called by the processor before recycling the request. It'll collect
     * statistic information.
     */
    void updateCounters() {
        bytesReceived+=req.getBytesRead();
        bytesSent+=req.getResponse().getBytesWritten();

        requestCount++;
        if( req.getResponse().getStatus() >=400 )
            errorCount++;
        long t0=req.getStartTime();
        long t1=System.currentTimeMillis();
        long time=t1-t0;
        processingTime+=time;
        if( maxTime < time ) {
            maxTime=time;
            maxRequestUri=req.requestURI().toString();
        }
    }

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    public long getBytesSent() {
        return bytesSent;
    }

    public void setBytesSent(long bytesSent) {
        this.bytesSent = bytesSent;
    }

    public long getBytesReceived() {
        return bytesReceived;
    }

    public void setBytesReceived(long bytesReceived) {
        this.bytesReceived = bytesReceived;
    }

    public long getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(long processingTime) {
        this.processingTime = processingTime;
    }

    public long getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(long maxTime) {
        this.maxTime = maxTime;
    }

    public String getMaxRequestUri() {
        return maxRequestUri;
    }

    public void setMaxRequestUri(String maxRequestUri) {
        this.maxRequestUri = maxRequestUri;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }


}
