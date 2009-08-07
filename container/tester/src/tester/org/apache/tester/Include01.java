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
import java.security.Principal;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Positive test for including a static resource.
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public class Include01 extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {

        response.setContentType("text/plain");
        RequestDispatcher rd =
            getServletContext().getRequestDispatcher("/Include01.txt");
        if (rd == null) {
            PrintWriter writer = response.getWriter();
            writer.println("Include01 FAILED - No RequestDispatcher returned");
            return;
        }
        rd.include(request, response);
        StaticLogger.reset();

    }

}
