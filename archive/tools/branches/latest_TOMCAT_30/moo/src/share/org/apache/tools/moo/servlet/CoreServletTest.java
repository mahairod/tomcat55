package org.apache.tools.moo.servlet;

import org.apache.tools.moo.servlet.Constants;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletConfig;
import java.util.Properties;
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
	
	private boolean sawInit;
	private boolean sawDestroy;

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

    public void
    service (ServletRequest request,ServletResponse response) {
        Properties props = new Properties();
	ServletOutputStream sos = null;

        try {
            props = doTest(request,response);

	   /* response.setStatus(HttpServletResponse.SC_OK); //OK*/

	    sos = response.getOutputStream();

        } catch (ServletException se) {

/*	  response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);*/
	  props.put(Constants.Response.Exception,
			    se.getMessage());
        } catch (IOException ioe) {
/*	  response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);*/
	  props.put(Constants.Response.Exception,
			    ioe.getMessage());
        } catch (RuntimeException e) { //servlet crash?
	 /* response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);*/
	  props.put(Constants.Response.Exception,
			    e.getMessage());
	}

	props.put(Constants.Response.TestClass,

	    this.getClass().getName());

	//try {
	//this used to be props.store, but that was not JDK 1.1 compliant
	    props.save(sos, this.getClass().getName());
	    //} catch (IOException ioe) {
	    //System.out.println(this.getClass().getName() +
	    //   " exception: " + ioe);
	    //}
    }

  /**
   * a short title to identify the test being run
   */
    public abstract String
    getTitle ();

  /**
   * A more-verbose description of the server-side test than the title
   */
    public abstract String
    getDescription ();

  /**
   * This method returns a properties list whose keys are a subset of the
   * Constants.Response.* list.
   *
   * I don't believe that this method should affect the response variable
   * as this needs to be a properties file which will be written by service()
   */
    public abstract Properties
    doTest (ServletRequest request, ServletResponse response)
    throws ServletException, IOException;


    public String getServletInfo() {

	return "";
    }

    public ServletConfig getServletConfig() {

	return config;

	
    }

    public void destroy() {

	//set sawDestroy to true

	sawDestroy=true;

    }
}
