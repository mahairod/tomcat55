/*
 * SsiInvokerServlet.java
 * $Header$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */

package org.apache.catalina.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.TimeZone;
import java.util.Locale;
import java.util.StringTokenizer;
import java.text.SimpleDateFormat;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.naming.directory.DirContext;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.NamingException;
import javax.naming.InitialContext;
import org.apache.catalina.Globals;
import org.apache.catalina.util.RequestUtil;
import org.apache.catalina.util.ssi.SsiCommand;
import org.apache.catalina.util.ssi.SsiMediator;
import org.apache.catalina.util.ssi.ServletOutputStreamWrapper;
import org.apache.naming.resources.Resource;
import org.apache.naming.resources.ResourceAttributes;

/**
 * Servlet to process SSI requests within a webpage.
 * Mapped to a path from within web.xml.
 *
 * @author Bip Thelin
 * @version $Revision$, $Date$
 */
public final class SsiInvokerServlet extends HttpServlet {
    /** Debug level for this servlet. */
    private int debug = 0;

    /** Should the output be buffered. */
    private boolean buffered = false;

    /** Expiration time in seconds for the doc. */
    private Long expires = null;

    /** The Mediator object for the SsiCommands. */
    private static SsiMediator ssiMediator = null;

    /** JNDI resources name. */
    protected static final String RESOURCES_JNDI_NAME = "java:/comp/Resources";

    /** The set of SimpleDateFormat formats to use in getDateHeader(). */
    protected static final SimpleDateFormat[] formats = {
	new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US),
	new SimpleDateFormat("EEEEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US),
	new SimpleDateFormat("EEE MMMM d HH:mm:ss yyyy", Locale.US)
    };
    protected final static TimeZone gmtZone = TimeZone.getTimeZone("GMT");

    /** The start pattern */
    private final static byte[] bStart = {
	(byte)'<',(byte)'!',(byte)'-',(byte)'-',(byte)'#'
    };

    /** The end pattern */
    private final static byte[] bEnd = {
	(byte)'-',(byte)'-',(byte)'>'
    };

    static {
        formats[0].setTimeZone(gmtZone);
        formats[1].setTimeZone(gmtZone);
        formats[2].setTimeZone(gmtZone);
    }
    //----------------- Public methods.

    /**
     * Initialize this servlet.
     * @exception ServletException if an error occurs
     */
    public void init() throws ServletException {
        String value = null;
        try {
            value = getServletConfig().getInitParameter("debug");
            debug = Integer.parseInt(value);
        } catch (Throwable t) {
            ;
        }
        try {
            // adapted from JSSI
            value = getServletConfig().getInitParameter("expires");
            expires = Long.valueOf(value);
        } catch (NumberFormatException e) {
            expires = null;
            log("Invalid format for expires initParam; expected integer (seconds)");
        } catch (Throwable t) {
            ;
        }
        try {
            value = getServletConfig().getInitParameter("buffered");
            buffered = Integer.parseInt(value) > 0 ? true : false;
        } catch (Throwable t) {
            ;
        }
        if (debug > 0)
            log("SsiInvokerServlet.init() SSI invoker started with 'debug'="
		+ debug);
    }

    /**
     * Process and forward the GET request
     * to our <code>requestHandler()</code>.
     *
     * @param req a value of type 'HttpServletRequest'
     * @param res a value of type 'HttpServletResponse'
     * @exception IOException if an error occurs
     * @exception ServletException if an error occurs
     */
    public void doGet(HttpServletRequest req, HttpServletResponse res)
	throws IOException, ServletException {

        if (debug > 0)
            log("SsiInvokerServlet.doGet()");
        requestHandler(req, res);
    }

    /**
     * Process and forward the POST request
     * to our <code>requestHandler()</code>.
     *
     * @param req a value of type 'HttpServletRequest'
     * @param res a value of type 'HttpServletResponse'
     * @exception IOException if an error occurs
     * @exception ServletException if an error occurs
     */
    public void doPost(HttpServletRequest req, HttpServletResponse res)
	throws IOException, ServletException {

        if (debug > 0)
            log("SsiInvokerServlet.doPut()");
        requestHandler(req, res);
    }

    //----------------- Private methods.

    /**
     * Process our request and locate right SSI command.
     * @param req a value of type 'HttpServletRequest'
     * @param res a value of type 'HttpServletResponse'
     */
    private void requestHandler(HttpServletRequest req,
				HttpServletResponse res)
	throws IOException, ServletException {

        DirContext resources = getResources();
        ServletContext servletContext = getServletContext();
        String path = getRelativePath(req);
        ResourceInfo resourceInfo = new ResourceInfo(path, resources);

        if (debug > 0)
            log("SsiInvokerServlet.requestHandler()\n" +
		"Serving " + (buffered ? "buffered " : "unbuffered ") +
                "resource '" + path + "'");

        // Exclude any resource in the /WEB-INF and /META-INF subdirectories
        // (the "toUpperCase()" avoids problems on Windows systems)
        if ((path == null)
	    || path.toUpperCase().startsWith("/WEB-INF")
	    || path.toUpperCase().startsWith("/META-INF")) {

            res.sendError(res.SC_NOT_FOUND, path);
            return;
        }

	if (!resourceInfo.exists) {
	    res.sendError(res.SC_NOT_FOUND, path);
	    return;
	}

        if (expires != null) {
            res.setDateHeader("Expires", (
                new java.util.Date()).getTime() + expires.longValue() * 1000);
        }

        OutputStream out = null;
        InputStream resourceInputStream =
	    servletContext.getResourceAsStream(path);

        InputStream in = new BufferedInputStream(resourceInputStream, 4096);
        ByteArrayOutputStream soonOut =
	    new ByteArrayOutputStream((int)resourceInfo.length);

        StringBuffer command = new StringBuffer();
        byte buf[] = new byte[4096];
        int len = 0, bIdx = 0;
        char ch;
        boolean inside = false;
        SsiCommand ssiCommand = null;
        String strCmd;
        String[] strParamType;
        String[] strParam;

        if (buffered)
            out = (OutputStream)new ServletOutputStreamWrapper();
        else
            out = res.getOutputStream();

        if (ssiMediator == null)
            ssiMediator =
		new SsiMediator(req, res, out, servletContext, debug, path);
        else
            ssiMediator.flush(req, res, out, servletContext, path);

        while ((len = in.read(buf)) != -1)
            soonOut.write(buf, 0, len);

        soonOut.close();
        byte[] unparsed = soonOut.toByteArray();
        soonOut = null; buf = null;
        while (bIdx < unparsed.length) {
            if (!inside) {
                if (unparsed[bIdx] == bStart[0]&&
		    byteCmp(unparsed, bIdx, bStart)) {
                    inside = true;
                    bIdx += bStart.length;
                    command.delete(0, command.length());
                    continue;
                }
                out.write(unparsed[bIdx]);
                bIdx++;
            } else {
                if (unparsed[bIdx] == bEnd[0]&&
		    byteCmp(unparsed, bIdx, bEnd)) {
                    inside = false;
                    bIdx += bEnd.length;
                    strCmd = parseCmd(command);
                    strParamType = parseParamType(command, strCmd.length());
                    strParam = parseParam(command, strCmd.length());

		    if(debug > 0)
			log("Serving SSI resource: " + strCmd);

                    ssiCommand = ssiMediator.getCommand(strCmd);
                    if (ssiCommand != null&&
			strParamType.length==strParam.length&&
			strParamType.length>0) {
                        if (ssiCommand.isPrintable())
                            out.write((ssiCommand.getStream(strParamType,
							    strParam)).getBytes());
                        else
                            ssiCommand.process(strParamType, strParam);
                    } else {
                        out.write(ssiMediator.getError());
                    }
                    continue;
                }
                command.append((char)unparsed[bIdx]);
                bIdx++;
            }
        }
        if (buffered)
            ((ServletOutputStreamWrapper)out).writeTo(res.getOutputStream());

        out = null;
    }

    /**
     * Parse a StringBuffer and take out the command token.
     * Called from <code>requestHandler</code>
     * @param cmd a value of type 'StringBuffer'
     * @return a value of type 'String'
     */
    private String parseCmd(StringBuffer cmd)
	throws IndexOutOfBoundsException {

        String modString = ((cmd.toString()).trim()).toLowerCase();
        return modString.substring(0, modString.indexOf(" "));
    }

    /**
     * Parse a StringBuffer and take out the param type token.
     * Called from <code>requestHandler</code>
     * @param cmd a value of type 'StringBuffer'
     * @return a value of type 'String[]'
     */
    private String[] parseParamType(StringBuffer cmd, int start) {
	int bIdx = start;
	int i = 0;
	int quotes = 0;
	boolean inside = false;
	StringBuffer retBuf = new StringBuffer();

	while(bIdx < cmd.length()) {
	    if(!inside) {
		while(bIdx < cmd.length()&&isSpace(cmd.charAt(bIdx)))
		    bIdx++;

		if(bIdx>=cmd.length())
		    break;

		inside=!inside;
	    } else {
		while(bIdx < cmd.length()&&cmd.charAt(bIdx)!='=') {
		    retBuf.append(cmd.charAt(bIdx));
		    bIdx++;
		}

		retBuf.append('"');
		inside=!inside;
		quotes=0;

		while(bIdx < cmd.length()&&quotes!=2) {
		    if(cmd.charAt(bIdx)=='"'||
		       cmd.charAt(bIdx)=='\'')
			quotes++;

		    bIdx++;
		}
	    }
	}

	StringTokenizer str = new StringTokenizer(retBuf.toString(), "\"");
	String[] retString = new String[str.countTokens()];

	while(str.hasMoreTokens()) {
	    retString[i++] = str.nextToken().trim();
	}

	return retString;
    }

    /**
     * Parse a StringBuffer and take out the param token.
     * Called from <code>requestHandler</code>
     * @param cmd a value of type 'StringBuffer'
     * @return a value of type 'String[]'
     */
    private String[] parseParam(StringBuffer cmd, int start) {
	int bIdx = start;
	int i = 0;
	int quotes = 0;
	boolean inside = false;
	StringBuffer retBuf = new StringBuffer();

	while(bIdx < cmd.length()) {
	    if(!inside) {
		while(bIdx < cmd.length()&&
		      cmd.charAt(bIdx)!='"'&&
		      cmd.charAt(bIdx)!='\'')
		    bIdx++;

		if(bIdx>=cmd.length())
		    break;

		inside=!inside;
	    } else {
		while(bIdx < cmd.length()&&
		      cmd.charAt(bIdx)!='"'&&
		      cmd.charAt(bIdx)!='\'') {
		    retBuf.append(cmd.charAt(bIdx));
		    bIdx++;
		}

		retBuf.append('"');
		inside=!inside;
	    }

	    bIdx++;
	}

	StringTokenizer str = new StringTokenizer(retBuf.toString(), "\"");
	String[] retString = new String[str.countTokens()];

	while(str.hasMoreTokens()) {
	    retString[i++] = str.nextToken();
	}

	return retString;
    }

    /**
     * Check the input param 'buf' if it matches with the
     * pattern 'pattern'. If it does return true.
     * @param buf a value of type 'byte[]'
     * @param bIdx a value of type 'int'
     * @param pattern a value of type 'byte[]'
     * @return a value of type 'boolean'
     */
    private boolean byteCmp(byte[] buf, int bIdx, byte[] pattern) {
        for (int i = 0; i < pattern.length; i++)
            if (buf[bIdx + i] != pattern[i])
                return false;
        return true;
    }

    private boolean isSpace(char c) {
	return c==' '||c=='\n'||c=='\t'||c=='\r';
    }

    //----------------- Taken from DefaultServlet.java

    /**
     * Return the relative path associated with this servlet.
     * @param request The servlet request we are processing
     */
    private String getRelativePath(HttpServletRequest request) {
        // Are we being processed by a RequestDispatcher.include()?
        if (request.getAttribute("javax.servlet.include.request_uri") != null) {
            String result =
		(String)request.getAttribute("javax.servlet.include.path_info");
            if (result == null)
                result =
		    (String)request.getAttribute("javax.servlet.include.servlet_path");
            if ((result == null) || (result.equals("")))
                result = "/";
            return (result);
        }
        // No, extract the desired path directly from the request
        String result = request.getPathInfo();
        if (result == null) {
            result = request.getServletPath();
        }
        if ((result == null) || (result.equals(""))) {
            result = "/";
        }
        return normalize(result);
    }

    /**
     * Return a context-relative path, beginning with a "/", that represents
     * the canonical version of the specified path after ".." and "." elements
     * are resolved out.  If the specified path attempts to go outside the
     * boundaries of the current context (i.e. too many ".."
     * path elements are present), return <code>null</code> instead.
     * @param path Path to be normalized
     */
    private String normalize(String path) {
        if (path == null)
            return null;
        // Resolve encoded characters in the normalized path,
        // which also handles encoded spaces so we can skip that later.
        // Placed at the beginning of the chain so that encoded
        // bad stuff(tm) can be caught by the later checks
        String normalized = path;
        if (normalized.indexOf('%') >= 0)
            normalized = RequestUtil.URLDecode(normalized, "UTF8");
        if (normalized == null)
            return (null);
        // Normalize the slashes and add leading slash if necessary
        if (normalized.indexOf('\\') >= 0)
            normalized = normalized.replace('\\', '/');
        if (!normalized.startsWith("/"))
            normalized = "/" + normalized;
        // Resolve occurrences of "//" in the normalized path
        while (true) {
            int index = normalized.indexOf("//");
            if (index < 0)
                break;
            normalized = normalized.substring(0, index) +
		normalized.substring(index + 1);
        }
        // Resolve occurrences of "/./" in the normalized path
        while (true) {
            int index = normalized.indexOf("/./");
            if (index < 0)
                break;
            normalized = normalized.substring(0, index) +
		normalized.substring(index + 2);
        }
        // Resolve occurrences of "/../" in the normalized path
        while (true) {
            int index = normalized.indexOf("/../");
            if (index < 0)
                break;
            if (index == 0)
                return (null); // Trying to go outside our context
            int index2 = normalized.lastIndexOf('/', index - 1);
            normalized = normalized.substring(0, index2) +
		normalized.substring(index + 3);
        }
        // Return the normalized path that we have completed
        return (normalized);
    }

    /**
     * Get resources. This method will try to retrieve the resources through
     * JNDI first, then in the servlet context if JNDI has failed
     * (it could be disabled). It will return null.
     * @return A JNDI DirContext, or null.
     */
    private DirContext getResources() {
        // First : try JNDI
        try {
            return (DirContext)new InitialContext().lookup(RESOURCES_JNDI_NAME);
        } catch (NamingException e) {
            // Failed
        } catch (ClassCastException e) {
            // Failed : Not the right type
        }
        // If it has failed, try the servlet context
        try {
            return (DirContext)getServletContext().getAttribute(Globals.RESOURCES_ATTR);
        } catch (ClassCastException e) {
            // Failed : Not the right type
        }
        return null;
    }

    // ----------------------------------------------  ResourceInfo Inner Class
    protected class ResourceInfo {
        /**
         * Constructor.
         * @param pathname Path name of the file
         */
        public ResourceInfo(String path, DirContext resources) {
            set(path, resources);
        }

        public Object object;
        public DirContext directory;
        public Resource file;
        public Attributes attributes;
        public String path;
        public long creationDate;
        public String httpDate;
        public long date;
        public long length;
        public boolean collection;
        public boolean exists;
        public DirContext resources;
        protected InputStream is;

        public void recycle() {
            object = null;
            directory = null;
            file = null;
            attributes = null;
            path = null;
            creationDate = 0;
            httpDate = null;
            date = 0;
            length = -1;
            collection = true;
            exists = false;
            resources = null;
            is = null;
        }

        public void set(String path, DirContext resources) {
            recycle();
            this.path = path;
            this.resources = resources;
            exists = true;
            try {
                object = resources.lookup(path);
                if (object instanceof Resource) {
                    file = (Resource)object;
                    collection = false;
                } else if (object instanceof DirContext) {
                    directory = (DirContext)object;
                    collection = true;
                } else {
                    // Don't know how to serve another object type
                    exists = false;
                }
            } catch (NamingException e) {
                exists = false;
            }
            if (exists) {
                try {
                    attributes = resources.getAttributes(path);
                    if (attributes instanceof ResourceAttributes) {
                        ResourceAttributes tempAttrs = (ResourceAttributes)attributes;
                        Date tempDate = tempAttrs.getCreationDate();
                        if (tempDate != null)
                            creationDate = tempDate.getTime();
                        tempDate = tempAttrs.getLastModified();
                        if (tempDate != null) {
                            date = tempDate.getTime();
                            httpDate = formats[0].format(tempDate);
                        } else {
                            httpDate = formats[0].format(
                                new Date());
                        }
                        length = tempAttrs.getContentLength();
                    }
                } catch (NamingException e) {
                    // Shouldn't happen, the implementation of the DirContext
                    // is probably broken
                    exists = false;
                }
            }
        }

        /** Test if the associated resource exists. */
        public boolean exists() {
            return exists;
        }

        /** String representation. */
        public String toString() {
            return path;
        }

        /** Set IS. */
        public void setStream(InputStream is) {
            this.is = is;
        }

        /** Get IS from resource. */
        public InputStream getStream() throws IOException {
            if (is != null)
                return is;
            if (file != null)
                return (file.streamContent());
            else
                return null;
        }
    }
}
