/*
 * Copyright 1999-2001,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.catalina.valves;


import java.io.IOException;
import java.io.Writer;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Globals;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.util.RequestUtil;
import org.apache.catalina.util.ServerInfo;
import org.apache.catalina.util.StringManager;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.tomcat.util.compat.JdkCompat;

/**
 * <p>Implementation of a Valve that outputs HTML error pages.</p>
 *
 * <p>This Valve should be attached at the Host level, although it will work
 * if attached to a Context.</p>
 *
 * <p>HTML code from the Cocoon 2 project.</p>
 *
 * @author Remy Maucherat
 * @author Craig R. McClanahan
 * @author <a href="mailto:nicolaken@supereva.it">Nicola Ken Barozzi</a> Aisa
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision$ $Date$
 */

public class ErrorReportValve
    extends ValveBase {


    // ----------------------------------------------------- Instance Variables


    /**
     * The descriptive information related to this implementation.
     */
    private static final String info =
        "org.apache.catalina.valves.ErrorReportValve/1.0";


    /**
     * The StringManager for this package.
     */
    protected static StringManager sm =
        StringManager.getManager(Constants.Package);


    // ------------------------------------------------------------- Properties


    /**
     * Return descriptive information about this Valve implementation.
     */
    public String getInfo() {

        return (info);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Invoke the next Valve in the sequence. When the invoke returns, check
     * the response state, and output an error report is necessary.
     *
     * @param request The servlet request to be processed
     * @param response The servlet response to be created
     * @param context The valve context used to invoke the next valve
     *  in the current processing pipeline
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    public void invoke(Request request, Response response)
        throws IOException, ServletException {

        // Perform the request
        getNext().invoke(request, response);

        ServletRequest sreq = (ServletRequest) request;
        Throwable throwable =
            (Throwable) sreq.getAttribute(Globals.EXCEPTION_ATTR);

        ServletResponse sresp = (ServletResponse) response;
        if (sresp.isCommitted()) {
            return;
        }

        if (throwable != null) {

            // The response is an error
            response.setError();

            // Reset the response (if possible)
            try {
                sresp.reset();
            } catch (IllegalStateException e) {
                ;
            }

            ServletResponse sresponse = (ServletResponse) response;
            if (sresponse instanceof HttpServletResponse)
                ((HttpServletResponse) sresponse).sendError
                    (HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        }

        response.setSuspended(false);

        try {
            report(request, response, throwable);
        } catch (Throwable tt) {
            tt.printStackTrace();
        }

    }


    /**
     * Return a String rendering of this object.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("ErrorReportValve[");
        sb.append(container.getName());
        sb.append("]");
        return (sb.toString());

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Prints out an error report.
     *
     * @param request The request being processed
     * @param response The response being generated
     * @param throwable The exception that occurred (which possibly wraps
     *  a root cause exception
     */
    protected void report(Request request, Response response,
                          Throwable throwable)
        throws IOException {

        // Do nothing on non-HTTP responses
        int statusCode = response.getStatus();

        // Do nothing on a 1xx, 2xx and 3xx status
        if (statusCode < 400)
            return;

        // FIXME: Reset part of the request
/*
        try {
            if (hresponse.isError())
                hresponse.reset(statusCode, message);
        } catch (IllegalStateException e) {
            ;
        }
*/

        Throwable rootCause = null;

        if (throwable != null) {

            if (throwable instanceof ServletException)
                rootCause = ((ServletException) throwable).getRootCause();

        }

        String message = RequestUtil.filter(response.getMessage());
        if (message == null)
            message = "";
    
        // Do nothing if there is no report for the specified status code
        String report = null;
        try {
            report = sm.getString("http." + statusCode, message);
        } catch (Throwable t) {
            ;
        }
        if (report == null)
            return;

        StringBuffer sb = new StringBuffer();

        sb.append("<html><head><title>");
        sb.append(ServerInfo.getServerInfo()).append(" - ");
        sb.append(sm.getString("errorReportValve.errorReport"));
        sb.append("</title>");
        sb.append("<style><!--");
        sb.append(org.apache.catalina.util.TomcatCSS.TOMCAT_CSS);
        sb.append("--></style> ");
        sb.append("</head><body>");
        sb.append("<h1>");
        sb.append(sm.getString("errorReportValve.statusHeader",
                               "" + statusCode, message)).append("</h1>");
        sb.append("<HR size=\"1\" noshade=\"noshade\">");
        sb.append("<p><b>type</b> ");
        if (throwable != null) {
            sb.append(sm.getString("errorReportValve.exceptionReport"));
        } else {
            sb.append(sm.getString("errorReportValve.statusReport"));
        }
        sb.append("</p>");
        sb.append("<p><b>");
        sb.append(sm.getString("errorReportValve.message"));
        sb.append("</b> <u>");
        sb.append(message).append("</u></p>");
        sb.append("<p><b>");
        sb.append(sm.getString("errorReportValve.description"));
        sb.append("</b> <u>");
        sb.append(report);
        sb.append("</u></p>");

        if (throwable != null) {

            String stackTrace = JdkCompat.getJdkCompat()
                .getPartialServletStackTrace(throwable);
            sb.append("<p><b>");
            sb.append(sm.getString("errorReportValve.exception"));
            sb.append("</b> <pre>");
            sb.append(RequestUtil.filter(stackTrace));
            sb.append("</pre></p>");

            while (rootCause != null) {
                stackTrace = JdkCompat.getJdkCompat()
                    .getPartialServletStackTrace(rootCause);
                sb.append("<p><b>");
                sb.append(sm.getString("errorReportValve.rootCause"));
                sb.append("</b> <pre>");
                sb.append(RequestUtil.filter(stackTrace));
                sb.append("</pre></p>");
                // In case root cause is somehow heavily nested
                try {
                    rootCause = (Throwable)PropertyUtils.getProperty
                                                (rootCause, "rootCause");
                } catch (ClassCastException e) {
                    rootCause = null;
                } catch (IllegalAccessException e) {
                    rootCause = null;
                } catch (NoSuchMethodException e) {
                    rootCause = null;
                } catch (java.lang.reflect.InvocationTargetException e) {
                    rootCause = null;
                }
            }

            sb.append("<p><b>");
            sb.append(sm.getString("errorReportValve.note"));
            sb.append("</b> <u>");
            sb.append(sm.getString("errorReportValve.rootCauseInLogs",
                                   ServerInfo.getServerInfo()));
            sb.append("</u></p>");

        }

        sb.append("<HR size=\"1\" noshade=\"noshade\">");
        sb.append("<h3>").append(ServerInfo.getServerInfo()).append("</h3>");
        sb.append("</body></html>");

        try {

            Writer writer = response.getReporter();

            if (writer != null) {

                Locale locale = Locale.getDefault();

                try {
                	response.setContentType("text/html");
                	response.setLocale(locale);
                } catch (Throwable t) {
                    if (container.getLogger().isDebugEnabled())
                        container.getLogger().debug("status.setContentType", t);
                }

                // If writer is null, it's an indication that the response has
                // been hard committed already, which should never happen
                writer.write(sb.toString());

            }

        } catch (IOException e) {
            ;
        } catch (IllegalStateException e) {
            ;
        }

    }


}
