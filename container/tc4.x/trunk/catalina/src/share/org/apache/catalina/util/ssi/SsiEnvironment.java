/*
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

package org.apache.catalina.util.ssi;

import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Hashtable;
import java.util.TimeZone;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.util.DateTool;

/**
 *  Contains the execution environment for SSI commands.  Nested
 *  SsiEnvironment instances are created for nested requests.  In
 *  these cases, most environment access will defer to the root
 *  environment.  The construct is provided for the few cases where
 *  nested environment would not be shared.  In any case,
 *  createEnvironmentForRequest() takes care of these details.
 *
 *  @version   $Revision$, $Date$
 *  @author    Paul Speed
 */
public class SsiEnvironment {

    /**
     *  The key used when storing the environment as a request
     *  attribute.
     */
    public static final String ATTR_SSI_ENVIRONMENT
                                    = "tomcat.ssi.environment";

    /**
     *  The parent SsiEnvironment instance, or null if this is a
     *  root.
     */
    private SsiEnvironment parent;

    /**
     *  The server variables for this SSI environment.
     */
    private Hashtable serverVariables;

    /**
     *  The SSI configuration for this environment.  Includes the
     *  time format, file size format, etc.
     */
    private Hashtable ssiConfig;

    /**
     *  A place where commands can stick state information specific
     *  to this environment.
     */
    private Hashtable commandVars = new Hashtable(7);

    /**
     *  The ServletContext associated with this environment.
     */
    private ServletContext servletContext;

    /**
     *  The context path for this environment.
     */
    private String contextPath;

    /**
     *  The path of the original request that created this environment.
     */
    private String requestPath;

    /**
     *  The location of the file pointed to be requestPath.  This
     *  is the requestPath minus anything after the last /.
     */
    private String location;

    /**
     *  True if virtual paths should be resolved relative to the webapp
     *  directory.
     */
    private boolean isVirtualWebappRelative;

    /**
     *  The request object for the request that created this environment.
     */
    private HttpServletRequest request;

    /**
     *  The response object for the request that created this environment.
     */
    private HttpServletResponse response;

    /**
     *  Set to true if output is temporarily disabled.
     */
    private boolean disableOutput;

    /**
     *  Creates a new SsiEnvironment instance for the specified request.
     *  This method will create a new or nested instance as appropriate.
     *
     *  @param context  The ServletContext from which the environment will
     *                  be initialized.
     *  @param req      The HttpServletRequest from which the environment
     *                  will be initialized.
     *  @param path     The normalized path of the request.
     */
    public static SsiEnvironment createSsiEnvironment( ServletContext context,
                                                       HttpServletRequest req,
                                                       HttpServletResponse res,
                                                       String path ) {

        SsiEnvironment env = null;

        // Try to get a parent
        env = (SsiEnvironment)req.getAttribute( ATTR_SSI_ENVIRONMENT );

        if (env == null) {
            // Create a new root level environment
            env = new SsiEnvironment( context, req, res, path );

            // Only store root environments in the request
            req.setAttribute( ATTR_SSI_ENVIRONMENT, env );
        } else {
            // Create a nested environment
            env = new SsiEnvironment( env, context, req, res, path );
        }

        return env;
    }


    /**
     *  Constructs and initializes a new root-level SsiEnvironment for the
     *  specified request.
     *
     *  @param req The HttpServletRequest used to initialize the environment
     *             variables.
     *  @param path The normalized path of the request.
     */
    protected SsiEnvironment( ServletContext context,
                              HttpServletRequest req,
                              HttpServletResponse res,
                              String path ) {

        this.serverVariables = new Hashtable(17);
        this.ssiConfig = new Hashtable(3);
        this.servletContext = context;
        this.contextPath = req.getContextPath();
        this.requestPath = path;
        this.location = getDirectoryFromPath( path );
        this.request = req;
        this.response = res;

        // Set the default configuration
        setConfiguration( "errmsg",
                          "[an error occurred while processing this directive]" );
        setConfiguration( "sizefmt", "abbrev" );
        setConfiguration( "timefmt", "EEE, dd MMM yyyyy HH:mm:ss z" );


        // Set the standard server variables
        setVariable( "AUTH_TYPE", req.getAuthType() );

        int c = req.getContentLength();
        if (c <= 0)
            setVariable( "CONTENT_LENGTH", "" );
        else
            setVariable( "CONTENT_LENGTH", String.valueOf(c) );

        setVariable( "CONTENT_TYPE", req.getContentType() );
        setVariable( "GATEWAY_INTERFACE", "CGI/1.1");
        setVariable( "PATH_INFO", req.getPathInfo() );
        setVariable( "PATH_TRANSLATED ", req.getPathTranslated() );
        setVariable( "QUERY_STRING", req.getQueryString() );
        setVariable( "REMOTE_ADDR", req.getRemoteAddr() );
        setVariable( "REMOTE_HOST", req.getRemoteHost() );
        setVariable( "REMOTE_IDENT", null );
        setVariable( "REMOTE_USER", req.getRemoteUser() );
        setVariable( "REQUEST_METHOD", req.getMethod() );
        setVariable( "SCRIPT_NAME", req.getServletPath() );
        setVariable( "SERVER_NAME", req.getServerName() );
        setVariable( "SERVER_PORT", String.valueOf(req.getServerPort()) );
        setVariable( "SERVER_PROTOCOL", req.getProtocol() );
        setVariable( "SERVER_SOFTWARE", servletContext.getServerInfo() );
        setVariable( "DOCUMENT_NAME", getFileFromPath(path) );
        setVariable( "DOCUMENT_URI", path );
        setVariable( "QUERY_STRING_UNESCAPED", "" );

        applyDate();
    }

    /**
     *  Constructs a new SsiEnvironment that will use the specified parent
     *  for variable access.
     *
     *  @param parent   The parent for this nested environment.
     *  @param req      The request used to initialize some local nested
     *                  properities.
     */
    protected SsiEnvironment( SsiEnvironment parent,
                              ServletContext context,
                              HttpServletRequest req,
                              HttpServletResponse res,
                              String path ) {

        this.parent = parent;
        this.servletContext = context;
        this.requestPath = path;
        this.location = getDirectoryFromPath( path );
        this.request = req;
        this.response = res;
    }

    /**
     *  Sets whether virtual paths should be resolved relative to the webapp.
     */
    public void setIsVirtualWebappRelative( boolean flag ) {
        this.isVirtualWebappRelative = flag;
    }

    /**
     *  Returns true if virtual paths should be resolved relative to the
     *  webapp.
     */
    public boolean isVirtualWebappRelative() {
        return isVirtualWebappRelative;
    }

    /**
     *  Returns true if output is currently disabled.  Certain conditional
     *  commands will change the disabled state, but it should always
     *  be a command that sets it.
     */
    public boolean isOutputDisabled() {
        return disableOutput;
    }

    /**
     *  Set to true to disable output.  This should only be done by
     *  SSI commands.
     */
    public void setOutputDisabled( boolean flag ) {
        this.disableOutput = flag;
    }

    /**
     *  Returns the request object for this environment instance.
     */
    public HttpServletRequest getRequest() {
        return request;
    }

    /**
     *  Returns the response object for this environment instance.
     */
    public HttpServletResponse getResponse() {
        return response;
    }

    /**
     *  Returns the current context path for this environment.
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     *  Sets the value of the specified environment variable.
     */
    public void setVariable( String key, String value ) {
        if (parent != null) {
            // Delegate
            parent.setVariable( key, value );
            return;
        }

        if (value == null) {
            // Remove it
            serverVariables.remove( key );
            return;
        }

        // Set the value
        serverVariables.put( key, value );
    }

    /**
     *  Returns the value for the specified environment variable.
     */
    public String getVariable( String key ) {
        if (parent != null)
            return parent.getVariable(key);
        return (String)serverVariables.get( key );
    }

    /**
     *  Sets the value of the specified configuration variable.
     */
    public void setConfiguration( String key, String value ) {
        if (parent != null) {
            // Delegate
            parent.setConfiguration( key, value );
            return;
        }

        if (value == null) {
            // Remove it
            ssiConfig.remove( key );
            return;
        }

        // Set the value
        ssiConfig.put( key, value );
    }

    /**
     *  Returns the value for the specified configuration variable.
     */
    public String getConfiguration( String key ) {
        if (parent != null)
            return parent.getConfiguration(key);
        return (String)ssiConfig.get( key );
    }

    /**
     *  Sets the specified command variable.
     */
    public void setCommandVariable( String key, Object value ) {
        commandVars.put( key, value );
    }

    /**
     *  Returns the specified command variable.  Command variables
     *  can be used by commands to store state information that needs
     *  to be associated with a specific request and used across
     *  multiple command executions.  Note: it may be better to
     *  change get/setConfiguration to take Object values and use
     *  it instead.
     */
    public Object getCommandVariable( String key ) {
        return commandVars.get( key );
    }

    /**
     *  Sets the current data and time to the appropriate environment
     *  variables.
     */
    public void applyDate() {
        setVariable( "DATE_LOCAL", formatDate(new Date()) );
        setVariable( "DATE_GMT", formatDate(new Date(), DateTool.GMT_ZONE) );
    }

    /**
     *  Sets the last modified variable to the last modification time
     *  of the current document.
     */
    public void applyLastModTime() {
        setVariable( "LAST_MODIFIED", getLastModified(requestPath) );
    }

    /**
     *  Applies variable substitution to the specified String and
     *  returns the new resolved string.
     */
    public String substituteVariables( String val ) {

        // If it has no variable references then no work
        // need to be done
        if (val.indexOf( '$' ) < 0)
            return val;

        StringBuffer sb = new StringBuffer( val );
        for (int i = 0; i < sb.length();) {

            // Find the next $
            for (; i < sb.length(); i++) {
                if (sb.charAt(i) == '$') {
                    i++;
                    break;
                }
            }

            if (i == sb.length())
                break;

            // Check to see if the $ is escaped
            if (i > 1 && sb.charAt(i-2) == '\\')
                continue;

            int nameStart = i;
            int start = i - 1;
            int end = -1;
            int nameEnd = -1;
            char endChar = ' ';

            // Check for {} wrapped var
            if (sb.charAt(i) == '{') {
                nameStart++;
                endChar = '}';
            }

            // Find the end of the var reference
            for (; i < sb.length(); i++) {
                if (sb.charAt(i) == endChar)
                    break;
            }
            end = i;
            nameEnd = end;

            if (endChar == '}')
                end++;

            // We should now have enough to extract the var name
            String varName = sb.substring( nameStart, nameEnd );

            String value = getVariable( varName );
            if (value == null)
                value = "";

            // Replace the var name with its value
            sb.replace( start, end, value );

            // Start searching for the next $ after the value
            // that was just substituted.
            i = start + value.length();
        }

        return sb.toString();
    }

    /**
     *  Format the Date using the current time format settings.
     */
    public String formatDate( Date date ) {

        String pattern = getConfiguration( "timefmt" );
        DateFormat dateFormat = new SimpleDateFormat( pattern,
                                                      DateTool.LOCALE_US );
        return dateFormat.format(date);
    }

    /**
     *  Format the Date using the current time format settings and the
     *  specified TimeZone.
     */
    public String formatDate( Date date, TimeZone zone ) {

        String pattern = getConfiguration( "timefmt" );
        DateFormat dateFormat = new SimpleDateFormat( pattern,
                                                      DateTool.LOCALE_US );
        dateFormat.setTimeZone(DateTool.GMT_ZONE);
        return dateFormat.format(date);
    }

    /**
     *  Returns a FileReference object that can be used to lookup
     *  resources and request dispatchers.
     *
     *  @param path     The path of the file relative to the current
     *                  location.
     *  @param virtual  True if the path should be treated as if it were a
     *                  "virtual" argument in the SSI include directive.
     */
    public FileReference getFileReference( String path, boolean virtual ) {

        if (path == null)
            return null;

        // Create a place for the normalized path
        String normalized = path;

        // Flip the backslashes
        if (normalized.indexOf('\\') >= 0)
            normalized = normalized.replace( '\\', '/' );

        // Only virtual allows ".."
        if (!virtual && path.indexOf( ".." ) > 0)
            return null;

        if (!normalized.startsWith("/")) {
            // Make the full relative path otherwise assume absolute
            normalized = location.concat( normalized );
        } else if (!virtual) {
            // Only virtual paths can be absolute
            return null;
        }

        // Resolve occurrences of "//" in the normalized path
        int index;
        while ((index = normalized.indexOf("//")) >= 0) {
            normalized = normalized.substring( 0, index )
                        + normalized.substring( index + 1 );
        }

        // Resololve occurrences of "/./" in the normalized path
        while ((index = normalized.indexOf("/./")) >= 0)  {
            normalized = normalized.substring( 0, index )
                        + normalized.substring( index + 2 );
        }

        // Resolve occurrences of "/../" in the normalized path
        while ((index = normalized.indexOf("/../")) >= 0) {
            if (index == 0) // Trying to go outside our context
                return null;

            // Find the start of the parent dir
            int index2 = normalized.lastIndexOf( '/', index - 1 );

            // Take it out along with the /..
            normalized = normalized.substring( 0, index2 )
                        + normalized.substring( index + 3 );
        }

        // Default the file context to the current environment context.
        ServletContext context = servletContext;

        // Only virtual paths can be anything other than webapp relative
        // If the path should be interpretted as server root relative
        // then the appropriate path and servlet context still need to
        // be resolved.
        if (virtual && !isVirtualWebappRelative) {

            // case of virtual="file.txt", "./file.txt", or dir/file.txt
            if ((!path.startsWith("/")) || (path.startsWith("./"))) {
                // handle as file in the current directory with original servletContext
                // No special processing necessary
            } else if (path.indexOf('/', 1)==-1) {
                // root context
                context = servletContext.getContext("/");
            } else if (!contextPath.equals("") &&
                       (normalized !=null) && (normalized.startsWith(contextPath))) {
                // starts with the context path of this webapp
                // strip off the context path
                context = servletContext.getContext(contextPath);
                normalized = normalized.substring(contextPath.length());
            } else if (normalized != null) {
                // find which context is the right one to handle
                String contextName = normalized.substring(0, path.indexOf('/', 1));
                ServletContext sc = servletContext.getContext(contextName);
                if (sc != null) {
                    context = sc;
                    normalized = normalized.substring(contextName.length());
                }
            }
        }

        // Return the resolved file reference object
        return new FileReference( normalized, context );
    }


    /**
     *  Returns the formatted last modification time of the specified path.
     */
    public String getLastModified( String path ) {

        // Get the file reference for the path
        FileReference ref = getFileReference( path, true );

        return getLastModified( ref );
    }

    /**
     *  Returns the formatted last modification time of the specified
     *  FileReference.
     */
    public String getLastModified( FileReference ref ) {

        try {
            URL u = ref.getResource();
            long lastModified = u.openConnection().getLastModified();
            return formatDate(new Date(lastModified));
        } catch (MalformedURLException e) {
            return null;
        } catch (IOException e) {
            return null;
        } catch (NullPointerException e) {
            return null;
        }
    }

    /**
     *  Returns just the file portion of the specified path.
     */
    private String getFileFromPath( String path ) {

        int split = path.lastIndexOf( '/' );
        if (split < 0)
            return path;
        return path.substring( split + 1 );
    }

    /**
     *  Returns just the directory portion of the specified path.
     */
    private String getDirectoryFromPath( String path ) {

        int split = path.lastIndexOf( '/' );
        if (split < 0)
            return path;
        return path.substring( 0, split + 1 );
    }

    /**
     *  Helper method to convert null values to "".
     */
    private String nullToString( String value ) {
        return (value==null)?"":value;
    }
}
