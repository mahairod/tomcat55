/*
 * Copyright 1999,2004 The Apache Software Foundation.
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
package org.apache.jasper.compiler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ResourceBundle;
import java.util.Vector;
import java.net.MalformedURLException;

import org.apache.jasper.JasperException;
import org.xml.sax.SAXException;

/**
 * Class responsible for dispatching JSP parse and javac compilation errors
 * to the configured error handler.
 *
 * This class is also responsible for localizing any error codes before they
 * are passed on to the configured error handler.
 * 
 * In the case of a Java compilation error, the compiler error message is
 * parsed into an array of JavacErrorDetail instances, which is passed on to 
 * the configured error handler.
 *
 * @author Jan Luehe
 * @author Kin-man Chung
 */
public class ErrorDispatcher {

    private static final ResourceBundle bundle = ResourceBundle.getBundle(
        "org.apache.jasper.resources.messages");

    // Custom error handler
    private ErrorHandler errHandler;

    // Indicates whether the compilation was initiated by JspServlet or JspC
    private boolean jspcMode = false;


    /*
     * Constructor.
     *
     * @param jspcMode true if compilation has been initiated by JspC, false
     * otherwise
     */
    public ErrorDispatcher(boolean jspcMode) {
	// XXX check web.xml for custom error handler
	errHandler = new DefaultErrorHandler();
        this.jspcMode = jspcMode;
    }

    /*
     * Dispatches the given JSP parse error to the configured error handler.
     *
     * The given error code is localized. If it is not found in the
     * resource bundle for localized error messages, it is used as the error
     * message.
     *
     * @param errCode Error code
     */
    public void jspError(String errCode) throws JasperException {
	dispatch(null, errCode, null, null);
    }

    /*
     * Dispatches the given JSP parse error to the configured error handler.
     *
     * The given error code is localized. If it is not found in the
     * resource bundle for localized error messages, it is used as the error
     * message.
     *
     * @param where Error location
     * @param errCode Error code
     */
    public void jspError(Mark where, String errCode) throws JasperException {
	dispatch(where, errCode, null, null);
    }

    /*
     * Dispatches the given JSP parse error to the configured error handler.
     *
     * The given error code is localized. If it is not found in the
     * resource bundle for localized error messages, it is used as the error
     * message.
     *
     * @param n Node that caused the error
     * @param errCode Error code
     */
    public void jspError(Node n, String errCode) throws JasperException {
	dispatch(n.getStart(), errCode, null, null);
    }

    /*
     * Dispatches the given JSP parse error to the configured error handler.
     *
     * The given error code is localized. If it is not found in the
     * resource bundle for localized error messages, it is used as the error
     * message.
     *
     * @param errCode Error code
     * @param arg Argument for parametric replacement
     */
    public void jspError(String errCode, String arg) throws JasperException {
	dispatch(null, errCode, new Object[] {arg}, null);
    }

    /*
     * Dispatches the given JSP parse error to the configured error handler.
     *
     * The given error code is localized. If it is not found in the
     * resource bundle for localized error messages, it is used as the error
     * message.
     *
     * @param where Error location
     * @param errCode Error code
     * @param arg Argument for parametric replacement
     */
    public void jspError(Mark where, String errCode, String arg)
	        throws JasperException {
	dispatch(where, errCode, new Object[] {arg}, null);
    }

    /*
     * Dispatches the given JSP parse error to the configured error handler.
     *
     * The given error code is localized. If it is not found in the
     * resource bundle for localized error messages, it is used as the error
     * message.
     *
     * @param n Node that caused the error
     * @param errCode Error code
     * @param arg Argument for parametric replacement
     */
    public void jspError(Node n, String errCode, String arg)
	        throws JasperException {
	dispatch(n.getStart(), errCode, new Object[] {arg}, null);
    }

    /*
     * Dispatches the given JSP parse error to the configured error handler.
     *
     * The given error code is localized. If it is not found in the
     * resource bundle for localized error messages, it is used as the error
     * message.
     *
     * @param errCode Error code
     * @param arg1 First argument for parametric replacement
     * @param arg2 Second argument for parametric replacement
     */
    public void jspError(String errCode, String arg1, String arg2)
	        throws JasperException {
	dispatch(null, errCode, new Object[] {arg1, arg2}, null);
    }

    /*
     * Dispatches the given JSP parse error to the configured error handler.
     *
     * The given error code is localized. If it is not found in the
     * resource bundle for localized error messages, it is used as the error
     * message.
     *
     * @param errCode Error code
     * @param arg1 First argument for parametric replacement
     * @param arg2 Second argument for parametric replacement
     * @param arg3 Third argument for parametric replacement
     */
    public void jspError(String errCode, String arg1, String arg2, String arg3)
	        throws JasperException {
	dispatch(null, errCode, new Object[] {arg1, arg2, arg3}, null);
    }

    /*
     * Dispatches the given JSP parse error to the configured error handler.
     *
     * The given error code is localized. If it is not found in the
     * resource bundle for localized error messages, it is used as the error
     * message.
     *
     * @param where Error location
     * @param errCode Error code
     * @param arg1 First argument for parametric replacement
     * @param arg2 Second argument for parametric replacement
     */
    public void jspError(Mark where, String errCode, String arg1, String arg2)
	        throws JasperException {
	dispatch(where, errCode, new Object[] {arg1, arg2}, null);
    }

    /*
     * Dispatches the given JSP parse error to the configured error handler.
     *
     * The given error code is localized. If it is not found in the
     * resource bundle for localized error messages, it is used as the error
     * message.
     *
     * @param where Error location
     * @param errCode Error code
     * @param arg1 First argument for parametric replacement
     * @param arg2 Second argument for parametric replacement
     * @param arg3 Third argument for parametric replacement
     */

    public void jspError(Mark where, String errCode, String arg1, String arg2,
                         String arg3)
                throws JasperException {
        dispatch(where, errCode, new Object[] {arg1, arg2, arg3}, null);
    }

    /*
     * Dispatches the given JSP parse error to the configured error handler.
     *
     * The given error code is localized. If it is not found in the
     * resource bundle for localized error messages, it is used as the error
     * message.
     *
     * @param n Node that caused the error
     * @param errCode Error code
     * @param arg1 First argument for parametric replacement
     * @param arg2 Second argument for parametric replacement
     */

    public void jspError(Node n, String errCode, String arg1, String arg2)
	        throws JasperException {
	dispatch(n.getStart(), errCode, new Object[] {arg1, arg2}, null);
    }

    /*
     * Dispatches the given JSP parse error to the configured error handler.
     *
     * The given error code is localized. If it is not found in the
     * resource bundle for localized error messages, it is used as the error
     * message.
     *
     * @param n Node that caused the error
     * @param errCode Error code
     * @param arg1 First argument for parametric replacement
     * @param arg2 Second argument for parametric replacement
     * @param arg3 Third argument for parametric replacement
     */

    public void jspError(Node n, String errCode, String arg1, String arg2,
                         String arg3)
	        throws JasperException {
	dispatch(n.getStart(), errCode, new Object[] {arg1, arg2, arg3}, null);
    }

    /*
     * Dispatches the given parsing exception to the configured error handler.
     *
     * @param e Parsing exception
     */
    public void jspError(Exception e) throws JasperException {
	dispatch(null, null, null, e);
    }

    /*
     * Dispatches the given JSP parse error to the configured error handler.
     *
     * The given error code is localized. If it is not found in the
     * resource bundle for localized error messages, it is used as the error
     * message.
     *
     * @param errCode Error code
     * @param arg Argument for parametric replacement
     * @param e Parsing exception
     */
    public void jspError(String errCode, String arg, Exception e)
	        throws JasperException {
	dispatch(null, errCode, new Object[] {arg}, e);
    }

    /*
     * Dispatches the given JSP parse error to the configured error handler.
     *
     * The given error code is localized. If it is not found in the
     * resource bundle for localized error messages, it is used as the error
     * message.
     *
     * @param n Node that caused the error
     * @param errCode Error code
     * @param arg Argument for parametric replacement
     * @param e Parsing exception
     */
    public void jspError(Node n, String errCode, String arg, Exception e)
	        throws JasperException {
	dispatch(n.getStart(), errCode, new Object[] {arg}, e);
    }

    /*
     * Dispatches the given compilation error to the configured error handler.
     *
     * @param errMsg Compilation error message that was generated by the
     * javac compiler
     * @param fname Name of Java source file whose compilation failed
     * @param page Node representation of JSP page from which the Java source
     * file was generated
     */
    public void javacError(String errMsg, String fname, Node.Nodes page)
	        throws JasperException, IOException {
	JavacErrorDetail[] errDetails = parseJavacMessage(errMsg, fname, page);
	errHandler.javacError(errDetails);
    }


    //*********************************************************************
    // Private utility methods

    /*
     * Dispatches the given JSP parse error to the configured error handler.
     *
     * The given error code is localized. If it is not found in the
     * resource bundle for localized error messages, it is used as the error
     * message.
     *
     * @param where Error location
     * @param errCode Error code
     * @param args Arguments for parametric replacement
     * @param e Parsing exception
     */
    private void dispatch(Mark where, String errCode, Object[] args,
			  Exception e) throws JasperException {
	String file = null;
	String errMsg = null;
	int line = -1;
	int column = -1;
	boolean hasLocation = false;

	// Localize
	if (errCode != null) {
	    errMsg = Localizer.getMessage(errCode, args);
	} else if (e != null) {
	    // give a hint about what's wrong
	    errMsg = e.getMessage();
	}

	// Get error location
	if (where != null) {
            if (jspcMode) {
                // Get the full URL of the resource that caused the error
                try {
                    file = where.getURL().toString();
                } catch (MalformedURLException me) {
                    file = where.getFile();
                }
            } else {
                // Get the context-relative resource path
                file = where.getFile();
            }
	    line = where.getLineNumber();
	    column = where.getColumnNumber();
	    hasLocation = true;
	}

	// Get nested exception
	Exception nestedEx = e;
	if ((e instanceof SAXException)
	        && (((SAXException) e).getException() != null)) {
	    nestedEx = ((SAXException) e).getException();
	}

	if (hasLocation) {
	    errHandler.jspError(file, line, column, errMsg, nestedEx);
	} else {
	    errHandler.jspError(errMsg, nestedEx);
	}
    }

    /*
     * Parses the given Java compilation error message, which may contain one
     * or more compilation errors, into an array of JavacErrorDetail instances.
     *
     * Each JavacErrorDetail instance contains the information about a single
     * compilation error.
     *
     * @param errMsg Compilation error message that was generated by the
     * javac compiler
     * @param fname Name of Java source file whose compilation failed
     * @param page Node representation of JSP page from which the Java source
     * file was generated
     *
     * @return Array of JavacErrorDetail instances corresponding to the
     * compilation errors
     */
    private JavacErrorDetail[] parseJavacMessage(String errMsg, String fname,
						 Node.Nodes page)
	        throws IOException, JasperException {

	Vector errVec = new Vector();
	StringBuffer partialErrMsg = new StringBuffer();
	int lineNum = -1;
	Node errNode = null;

        BufferedReader reader = new BufferedReader(new StringReader(errMsg));

        while (true) {
            String line = reader.readLine();
            if (line == null) {
		break;
	    }

            /*
	     * Error line number is delimited by set of colons.
	     * (Ignore colon following drive letter on Windows.)
	     * XXX Handle deprecation warnings that don't have line info
	     */
            int beginColon = line.indexOf(':', 2); 
            int endColon = line.indexOf(':', beginColon + 1);
            if ((beginColon >= 0) && (endColon >= 0)) {
		if (errNode != null) {
		    // add previous error to error vector
		    errVec.add(new JavacErrorDetail(
		        fname,
			lineNum,
			errNode.getStart().getFile(),
			errNode.getStart().getLineNumber(),
			partialErrMsg.toString()));
		    partialErrMsg = new StringBuffer();
		}
		String lineNumStr = line.substring(beginColon + 1, endColon);
                try {
                    lineNum = Integer.parseInt(lineNumStr);
                } catch (NumberFormatException e) {
                    // XXX
                }

		// Map servlet line number to corresponding node in JSP page
		ErrorVisitor errVisitor = new ErrorVisitor(lineNum);
		page.visit(errVisitor);
		errNode = errVisitor.getJspSourceNode();
                /* XXX Supress map exception to display the original error
		if (errNode == null) {
		    jspError("jsp.error.source.map", lineNumStr);
		}
                */
            }
	    partialErrMsg.append(line);
	    partialErrMsg.append('\n');
        }

        reader.close();

	// add last error to error vector
	String pageFile = null;
	int pageLine = -1;
	if (errNode != null) {
	    pageFile = errNode.getStart().getFile();
	    pageLine = errNode.getStart().getLineNumber();
	}
	errVec.add(new JavacErrorDetail(fname,
					lineNum,
					pageFile,
					pageLine,
					partialErrMsg.toString()));

	JavacErrorDetail[] errDetails = null;
	if (errVec.size() > 0) {
	    errDetails = new JavacErrorDetail[errVec.size()];
	    errVec.copyInto(errDetails);
	}

	return errDetails;
    }


    /*
     * Visitor responsible for mapping a line number in the generated servlet
     * source code to the corresponding JSP node.
     */
    static class ErrorVisitor extends Node.Visitor {

	// Java source line number to be mapped
	private int lineNum;

	/*
	 * JSP node whose Java source code range in the generated servlet
	 * contains the Java source line number to be mapped
	 */
	Node found;

	/*
	 * Constructor.
	 *
	 * @param lineNum Source line number in the generated servlet code
	 */
	public ErrorVisitor(int lineNum) {
	    this.lineNum = lineNum;
	}

	public void doVisit(Node n) throws JasperException {
	    if ((lineNum >= n.getBeginJavaLine())
		    && (lineNum < n.getEndJavaLine())) {
		found = n;
	    }
        }

	/*
	 * Gets the JSP node to which the source line number in the generated
	 * servlet code was mapped.
	 *
	 * @return JSP node to which the source line number in the generated
	 * servlet code was mapped
	 */
	public Node getJspSourceNode() {
	    return found;
	}
    }
}
