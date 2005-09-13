package tests.javax_servlet.Servlet;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 *
 * @author Ankur Chandra [ankurc@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 */

public abstract class CoreServletTest
implements Servlet {

	ServletConfig config;
	
	private boolean sawInit=false;
	private boolean sawDestroy=false;
	private boolean sawService=false;

    // for life cycle test
    public boolean isInit() {
	return sawInit;
    }

    // for life cycle test
    public boolean isDestroyed() {
	return sawDestroy;
    }

    public void init(ServletConfig config) throws ServletException {
	//set sawInit to true
	sawInit=true;
	this.config=config;
    }

    public void service (ServletRequest request,ServletResponse response) throws ServletException,IOException {
	sawService=true;
    }


    public String getServletInfo() {
	return "Servlet Info";
    }

    public ServletConfig getServletConfig() {
	return config;
    }

    public void destroy() {
	//set sawDestroy to true
	sawDestroy=true;
    }
}
