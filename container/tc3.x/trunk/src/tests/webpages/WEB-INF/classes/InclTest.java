/*
 *  Copyright 1999-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *  Simplest possible test of problem configuration:
 *  servlet forwards request to JSP, JSP includes another.
 */
public class InclTest
    extends HttpServlet
{
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws ServletException, IOException
    {
        RequestDispatcher rd =
 	    this.getServletContext().getRequestDispatcher("/Outer.jsp");
        rd.forward(request, response);
    }
}

