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

package pageextends;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class ExtendServlet extends HttpServlet {

  protected void doGet(HttpServletRequest req,
                       HttpServletResponse res)
            throws ServletException, IOException {

    final PrintWriter out = res.getWriter();

    out.println("<HTML><BODY>");
    out.println("<H1>page extends directive test</H1>");
    out.println("</BODY></HTML>");

  }

  public String getName() {
      return this.getClass().getName();
  }
}
