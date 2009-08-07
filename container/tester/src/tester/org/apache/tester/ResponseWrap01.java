/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.tester;


import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Basis for testing wrapped responses with combinations of forwarding to
 * or including both servlets and JSP pages.
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public class ResponseWrap01 extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {

        // Acquire request parameters
        String type = request.getParameter("type");
        String page = request.getParameter("page");

        // Prepare this response
        response.setContentType("text/plain");
        //	PrintWriter writer = response.getWriter();

        // Forward or include as requested
        RequestDispatcher rd =
            getServletContext().getRequestDispatcher(page);
        if (rd == null) {
            PrintWriter writer = response.getWriter();
            writer.println("ResponseWrap01 FAILED - No request dispatcher" +
                           " for " + page);
        } else if ("F".equals(type)) {
            HttpServletResponseWrapper wrapper =
                new CharArrayResponse(response);
            rd.forward(request, wrapper);
            wrapper.flushBuffer();
        } else {
            HttpServletResponseWrapper wrapper =
                new CharArrayResponse(response);
            rd.include(request, wrapper);
            wrapper.flushBuffer();
        }

        // No filter wrapping for this test series
        StaticLogger.reset();

    }

}
