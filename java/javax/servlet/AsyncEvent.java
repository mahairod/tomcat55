/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package javax.servlet;

/**
 * @since Servlet 3.0
 * TODO SERVLET3 - Add comments
 */
public class AsyncEvent {
    private AsyncContext context;
    private ServletRequest request;
    private ServletResponse response;
    private Throwable throwable;
    
    public AsyncEvent(AsyncContext context) {
        this.context = context;
    }

    public AsyncEvent(AsyncContext context, ServletRequest request,
            ServletResponse response) {
        this.context = context;
        this.request = request;
        this.response = response;
    }
    
    public AsyncEvent(AsyncContext context, Throwable throwable) {
        this.context = context;
        this.throwable = throwable;
    }

    public AsyncEvent(AsyncContext context, ServletRequest request,
            ServletResponse response, Throwable throwable) {
        this.context = context;
        this.request = request;
        this.response = response;
        this.throwable = throwable;
    }
    
    public AsyncContext getAsyncContext() {
        return context;
    }

    public ServletRequest getSuppliedRequest() {
        return request;
    }
    
    public ServletResponse getSuppliedResponse() {
        return response;
    }
    
    public Throwable getThrowable() {
        return throwable;
    }
}
