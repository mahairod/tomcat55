/* $Id$
 *
 */

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import util.HTMLFilter;

/**
 * Example servlet showing request headers
 *
 * @author James Duncan Davidson <duncan@eng.sun.com>
 */

public class RequestParamExample extends HttpServlet {


    ResourceBundle rb = ResourceBundle.getBundle("LocalStrings");
    
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException
    {
        response.setContentType("text/html");

        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<body>");
        out.println("<head>");

        String title = rb.getString("requestparams.title");
        out.println("<title>" + title + "</title>");
        out.println("</head>");
        out.println("<body bgcolor=\"white\">");

        // Can't use relative links as the addition of pathinfo to the URI
        // breaks relative links. Therefore use absolute links but don't hard
        // code the context name so webapp can be deployed as different context
        // and will still work.
        String context = request.getContextPath();
        
        out.println("<a href=\"" + context + "/servlets/reqparams.html\">");
        out.println("<img src=\"" + context + "/images/code.gif\" height=24 " +
                    "width=24 align=right border=0 alt=\"view code\"></a>");
        out.println("<a href=\"" + context + "/servlets/index.html\">");
        out.println("<img src=\"" + context + "/images/return.gif\" " +
                    "height=24 width=24 align=right border=0 alt=\"return\">" +
                    "</a>");

        out.println("<h3>" + title + "</h3>");
        String firstName = request.getParameter("firstname");
        String lastName = request.getParameter("lastname");
        out.println(rb.getString("requestparams.params-in-req") + "<br>");
        if (firstName != null || lastName != null) {
            out.println(rb.getString("requestparams.firstname"));
            out.println(" = " + HTMLFilter.filter(firstName) + "<br>");
            out.println(rb.getString("requestparams.lastname"));
            out.println(" = " + HTMLFilter.filter(lastName));
        } else {
            out.println(rb.getString("requestparams.no-params"));
        }
        out.println("<P>");
        out.print("<form action=\"");
        out.print("RequestParamExample\" ");
        out.println("method=POST>");
        out.println(rb.getString("requestparams.firstname"));
        out.println("<input type=text size=20 name=firstname>");
        out.println("<br>");
        out.println(rb.getString("requestparams.lastname"));
        out.println("<input type=text size=20 name=lastname>");
        out.println("<br>");
        out.println("<input type=submit>");
        out.println("</form>");

        out.println("</body>");
        out.println("</html>");
    }

    public void doPost(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException
    {
        doGet(request, response);
    }

}
