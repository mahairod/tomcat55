/*
 * $Id$
 */

/**
 * Test FORWARD with a query string
 *
 * @author Arun Jamwal [arunj@eng.sun.com]
 */

package dispatch;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class ForwardWithQueryServlet extends HttpServlet {

    public void init() {
	context = getServletConfig().getServletContext();
    }
    
    public void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
	res.setContentType(ContentType);
	RequestDispatcher rd = context.getRequestDispatcher(TargetServlet);
	rd.forward(req, res);
    }


    private ServletContext context;
    private static final String ContentType = "text/foobar";
    private static final String TargetServlet = 
	"/servlet/dispatch.ForwardWithQueryTarget";
}



