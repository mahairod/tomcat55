/*
 * $Id$
 */

package dispatch;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class SimpleRedirectServlet extends HttpServlet {

    private ServletContext context;
    
    public void init() {
	context = getServletConfig().getServletContext();
    }
    
    public void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        String qs = req.getQueryString();

        res.sendRedirect(qs);
    }
}
