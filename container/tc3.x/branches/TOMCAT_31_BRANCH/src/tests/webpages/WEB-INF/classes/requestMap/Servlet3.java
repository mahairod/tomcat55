/*
 * $Id$
 */

package requestMap;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class Servlet3 extends HttpServlet {
    private String servletInfo = "Servlet3";

    private ServletContext context;
    
    public void init() {
	context = getServletConfig().getServletContext();
    }

    public String getServletInfo() {
        return this.servletInfo;
    }
    
    public void doGet(HttpServletRequest request,
        HttpServletResponse response)
    throws IOException {
	PrintWriter pw = response.getWriter();

	pw.println("Servlet: " + getServletInfo());
    }
}
