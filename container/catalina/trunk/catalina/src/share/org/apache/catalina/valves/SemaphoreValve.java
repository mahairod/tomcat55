/*
 * Copyright 1999-2001,2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.catalina.valves;


import java.io.IOException;
import java.util.concurrent.Semaphore;

import javax.servlet.ServletException;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;


/**
 * <p>Implementation of a Valve that limits concurrency.</p>
 *
 * <p>This Valve may be attached to any Container, depending on the granularity
 * of the concurrency control you wish to perform.</p>
 *
 * @author Remy Maucherat
 * @version $Revision$ $Date$
 */

public class SemaphoreValve
    extends ValveBase {


    // ------------------------------------------------------------ Constructor


    /**
     * Create a new StandardHost component with the default basic Valve.
     */
    public SemaphoreValve() {
        semaphore = new Semaphore(concurrency, fairness);
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The descriptive information related to this implementation.
     */
    private static final String info =
        "org.apache.catalina.valves.SemaphoreValve/1.0";


    /**
     * Semaphore.
     */
    protected Semaphore semaphore = null;
    

    // ------------------------------------------------------------- Properties

    
    /**
     * Concurrency level of the semaphore.
     */
    protected int concurrency = 10;
    public int getConcurrency() { return concurrency; }
    public void setConcurrency(int concurrency) { this.concurrency = concurrency; }
    

    /**
     * Fairness of the semaphore.
     */
    protected boolean fairness = false;
    public boolean getFairness() { return fairness; }
    public void setFairness(boolean fairness) { this.fairness = fairness; }
    

    // --------------------------------------------------------- Public Methods


    /**
     * Return descriptive information about this Valve implementation.
     */
    public String getInfo() {
        return (info);
    }


    /**
     * Do concurrency control on the request using the semaphore.
     *
     * @param request The servlet request to be processed
     * @param response The servlet response to be created
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    public void invoke(Request request, Response response)
        throws IOException, ServletException {

        try {
            semaphore.acquireUninterruptibly();
            // Perform the request
            getNext().invoke(request, response);
        } finally {
            semaphore.release();
        }

    }


}
