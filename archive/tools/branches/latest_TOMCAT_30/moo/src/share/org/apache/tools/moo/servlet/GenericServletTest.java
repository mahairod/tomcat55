package org.apache.tools.moo.servlet;

import org.apache.tools.moo.servlet.Constants;
import javax.servlet.GenericServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.Properties;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 *
 * @author Ankur Chandra [ankurc@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 */

/**
 * All Server-side tests subclass ServletTest.
 * They need to override the getTitle and getDescription methods,
 * as well as the doTest(HttpServletRequest, HttpServletResponse) which
 * handles the test
 */
public abstract class GenericServletTest
extends GenericServlet {

  /*
   * We should probably set the status codes of the response here appropriately
   */
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
}
