/*
 * $Id$
 */

package dispatch;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class Target1 extends HttpServlet {

    private ServletContext context;
    
    public void init() {
	context = getServletConfig().getServletContext();
    }
    
    public void doGet(HttpServletRequest req, HttpServletResponse res)
        throws IOException
    {
	res.setContentType("text/funky");
	PrintWriter pwo = res.getWriter();
	pwo.println("TARGET1");
    }
}



