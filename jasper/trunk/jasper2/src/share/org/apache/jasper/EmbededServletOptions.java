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
 */ 

package org.apache.jasper;

import java.io.File;
import java.util.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.apache.jasper.compiler.TldLocationsCache;
import org.apache.jasper.compiler.JspConfig;
import org.apache.jasper.compiler.TagPluginManager;
import org.apache.jasper.compiler.Localizer;
import org.apache.jasper.xmlparser.ParserUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A class to hold all init parameters specific to the JSP engine. 
 *
 * @author Anil K. Vijendran
 * @author Hans Bergsten
 * @author Pierre Delisle
 */
public final class EmbededServletOptions implements Options {

    // Logger
    private static Log log = LogFactory.getLog(EmbededServletOptions.class);

    private Properties settings = new Properties();
    
    /**
     * Is Jasper being used in development mode?
     */
    private boolean development = true;

    /**
     * Should Ant fork its java compiles of JSP pages.
     */
    public boolean fork = true;

    /**
     * Do you want to keep the generated Java files around?
     */
    private boolean keepGenerated = true;

    /**
     * Do you want support for "large" files? What this essentially
     * means is that we generated code so that the HTML data in a JSP
     * file is stored separately as opposed to those constant string
     * data being used literally in the generated servlet. 
     */
    private boolean largeFile = false;

    /**
     * Determines whether tag handler pooling is enabled.
     */
    private boolean isPoolingEnabled = true;

     /**
      * Tag handler pool size.
      */
    private int tagPoolSize;
    
    /**
     * Do you want support for "mapped" files? This will generate
     * servlet that has a print statement per line of the JSP file.
     * This seems like a really nice feature to have for debugging.
     */
    private boolean mappedFile = false;
    
    /**
     * Do you want stack traces and such displayed in the client's
     * browser? If this is false, such messages go to the standard
     * error or a log file if the standard error is redirected. 
     */
    private boolean sendErrorToClient = false;

    /**
     * Do we want to include debugging information in the class file?
     */
    private boolean classDebugInfo = true;

    /**
     * Background compile thread check interval in seconds.
     */
    private int checkInterval = 300;

    /**
     * JSP reloading check ?
     */
    private boolean reloading = true;

    /**
     * Is the generation of SMAP info for JSR45 debuggin suppressed?
     */
    private boolean suppressSmap = false;

    /**
     * I want to see my generated servlets. Which directory are they
     * in?
     */
    private File scratchDir;
    
    /**
     * Need to have this as is for versions 4 and 5 of IE. Can be set from
     * the initParams so if it changes in the future all that is needed is
     * to have a jsp initParam of type ieClassId="<value>"
     */
    private String ieClassId = "clsid:8AD9C840-044E-11D1-B3E9-00805F499D93";

    /**
     * What classpath should I use while compiling generated servlets?
     */
    private String classpath = null;
    
    /**
     * Compiler to use.
     */
    private String compiler = null;

    /**
     * Cache for the TLD locations
     */
    private TldLocationsCache tldLocationsCache = null;

    /**
     * Jsp config information
     */
    private JspConfig jspConfig = null;

    /**
     * TagPluginManager
     */
    private TagPluginManager tagPluginManager = null;

    /**
     * Java platform encoding to generate the JSP
     * page servlet.
     */
    private String javaEncoding = "UTF8";

    public String getProperty(String name ) {
        return settings.getProperty( name );
    }

    public void setProperty(String name, String value ) {
        settings.setProperty( name, value );
    }
    
    /**
     * Are we keeping generated code around?
     */
    public boolean getKeepGenerated() {
        return keepGenerated;
    }
    
    /**
     * Are we supporting large files?
     */
    public boolean getLargeFile() {
        return largeFile;
    }
    
    public boolean isPoolingEnabled() {
	return isPoolingEnabled;
    }

    /**
     * Returns the tag handler pool size.
     */
    public int getTagPoolSize() {
	return tagPoolSize;
    }

    /**
     * Are we supporting HTML mapped servlets?
     */
    public boolean getMappedFile() {
        return mappedFile;
    }
    
    /**
     * Should errors be sent to client or thrown into stderr?
     */
    public boolean getSendErrorToClient() {
        return sendErrorToClient;
    }
 
    /**
     * Should class files be compiled with debug information?
     */
    public boolean getClassDebugInfo() {
        return classDebugInfo;
    }

    /**
     * Background JSP compile thread check intervall
     */
    public int getCheckInterval() {
        return checkInterval;
    }

    /**
     * Is Jasper being used in development mode?
     */
    public boolean getDevelopment() {
        return development;
    }

    /**
     * JSP reloading check ?
     */
    public boolean getReloading() {
        return reloading;
    }

    /**
     * Is the generation of SMAP info for JSR45 debuggin suppressed?
     */
    public boolean suppressSmap() {
        return suppressSmap;
    }

    /**
     * Class ID for use in the plugin tag when the browser is IE. 
     */
    public String getIeClassId() {
        return ieClassId;
    }
    
    /**
     * What is my scratch dir?
     */
    public File getScratchDir() {
        return scratchDir;
    }

    /**
     * What classpath should I use while compiling the servlets
     * generated from JSP files?
     */
    public String getClassPath() {
        return classpath;
    }

    /**
     * Compiler to use.
     */
    public String getCompiler() {
        return compiler;
    }


    public TldLocationsCache getTldLocationsCache() {
	return tldLocationsCache;
    }

    public void setTldLocationsCache( TldLocationsCache tldC ) {
        tldLocationsCache = tldC;
    }

    public String getJavaEncoding() {
	return javaEncoding;
    }

    public boolean getFork() {
        return fork;
    }

    public JspConfig getJspConfig() {
	return jspConfig;
    }

    public TagPluginManager getTagPluginManager() {
	return tagPluginManager;
    }

    /**
     * Create an EmbededServletOptions object using data available from
     * ServletConfig and ServletContext. 
     */
    public EmbededServletOptions(ServletConfig config,
				 ServletContext context) {

	this.tagPoolSize = Constants.MAX_POOL_SIZE;

        Enumeration enum=config.getInitParameterNames();
        while( enum.hasMoreElements() ) {
            String k=(String)enum.nextElement();
            String v=config.getInitParameter( k );
            setProperty( k, v);
        }

        // quick hack
        String validating=config.getInitParameter( "validating");
        if( "false".equals( validating )) ParserUtils.validating=false;
        
        String keepgen = config.getInitParameter("keepgenerated");
        if (keepgen != null) {
            if (keepgen.equalsIgnoreCase("true")) {
                this.keepGenerated = true;
            } else if (keepgen.equalsIgnoreCase("false")) {
                this.keepGenerated = false;
            } else {
		if (log.isWarnEnabled()) {
		    log.warn(Localizer.getMessage("jsp.warning.keepgen"));
		}
	    }
        }
            

        String largeFile = config.getInitParameter("largefile"); 
        if (largeFile != null) {
            if (largeFile.equalsIgnoreCase("true")) {
                this.largeFile = true;
            } else if (largeFile.equalsIgnoreCase("false")) {
                this.largeFile = false;
            } else {
		if (log.isWarnEnabled()) {
		    log.warn(Localizer.getMessage("jsp.warning.largeFile"));
		}
	    }
        }
	
	this.isPoolingEnabled = true;
        String poolingEnabledParam
	    = config.getInitParameter("enablePooling"); 
        if (poolingEnabledParam != null
  	        && !poolingEnabledParam.equalsIgnoreCase("true")) {
            if (poolingEnabledParam.equalsIgnoreCase("false")) {
                this.isPoolingEnabled = false;
            } else {
		if (log.isWarnEnabled()) {
		    log.warn(Localizer.getMessage("jsp.warning.enablePooling"));
		}		       	   
	    }
        }

        String tagPoolSizeParam = config.getInitParameter("tagPoolSize");
        if (tagPoolSizeParam != null) {
            try {
                this.tagPoolSize = Integer.parseInt(tagPoolSizeParam);
                if (this.tagPoolSize <= 0) {
                    this.tagPoolSize = Constants.MAX_POOL_SIZE;
		    if (log.isWarnEnabled()) {
			log.warn(Localizer.getMessage("jsp.warning.invalidTagPoolSize",
						      Integer.toString(Constants.MAX_POOL_SIZE)));
		    }
                }
            } catch(NumberFormatException ex) {
		if (log.isWarnEnabled()) {
		    log.warn(Localizer.getMessage("jsp.warning.invalidTagPoolSize",
						  Integer.toString(Constants.MAX_POOL_SIZE)));
		}
            }
        }

        String mapFile = config.getInitParameter("mappedfile"); 
        if (mapFile != null) {
            if (mapFile.equalsIgnoreCase("true")) {
                this.mappedFile = true;
            } else if (mapFile.equalsIgnoreCase("false")) {
                this.mappedFile = false;
            } else {
		if (log.isWarnEnabled()) {
		    log.warn(Localizer.getMessage("jsp.warning.mappedFile"));
		}
	    }
        }
	
        String senderr = config.getInitParameter("sendErrToClient");
        if (senderr != null) {
            if (senderr.equalsIgnoreCase("true")) {
                this.sendErrorToClient = true;
            } else if (senderr.equalsIgnoreCase("false")) {
                this.sendErrorToClient = false;
            } else {
		if (log.isWarnEnabled()) {
		    log.warn(Localizer.getMessage("jsp.warning.sendErrToClient"));
		}
	    }
        }

        String debugInfo = config.getInitParameter("classdebuginfo");
        if (debugInfo != null) {
            if (debugInfo.equalsIgnoreCase("true")) {
                this.classDebugInfo  = true;
            } else if (debugInfo.equalsIgnoreCase("false")) {
                this.classDebugInfo  = false;
            } else {
		if (log.isWarnEnabled()) {
		    log.warn(Localizer.getMessage("jsp.warning.classDebugInfo"));
		}
	    }
        }

        String checkInterval = config.getInitParameter("checkInterval");
        if (checkInterval != null) {
            try {
		this.checkInterval = Integer.parseInt(checkInterval);
                if (this.checkInterval == 0) {
                    this.checkInterval = 300;
		    if (log.isWarnEnabled()) {
			log.warn(Localizer.getMessage("jsp.warning.checkInterval"));
		    }
                }
            } catch(NumberFormatException ex) {
		if (log.isWarnEnabled()) {
		    log.warn(Localizer.getMessage("jsp.warning.checkInterval"));
		}
            }
        }

        String development = config.getInitParameter("development");
        if (development != null) {
            if (development.equalsIgnoreCase("true")) {
                this.development = true;
            } else if (development.equalsIgnoreCase("false")) {
                this.development = false;
            } else {
		if (log.isWarnEnabled()) {
		    log.warn(Localizer.getMessage("jsp.warning.development"));
		}
	    }
        }

        String reloading = config.getInitParameter("reloading");
        if (reloading != null) {
            if (reloading.equalsIgnoreCase("true")) {
                this.reloading = true;
            } else if (reloading.equalsIgnoreCase("false")) {
                this.reloading = false;
            } else {
		if (log.isWarnEnabled()) {
		    log.warn(Localizer.getMessage("jsp.warning.reloading"));
		}
	    }
        }

        String suppressSmap = config.getInitParameter("suppressSmap");
        if (suppressSmap != null) {
            if (suppressSmap.equalsIgnoreCase("true")) {
                this.suppressSmap = true;
            } else if (suppressSmap.equalsIgnoreCase("false")) {
                this.suppressSmap = false;
            } else {
                if (log.isWarnEnabled()) {
                    log.warn(Localizer.getMessage("jsp.warning.suppressSmap"));
                }
            }
        }

        String ieClassId = config.getInitParameter("ieClassId");
        if (ieClassId != null)
            this.ieClassId = ieClassId;

        String classpath = config.getInitParameter("classpath");
        if (classpath != null)
            this.classpath = classpath;

	/*
	 * scratchdir
	 */
        String dir = config.getInitParameter("scratchdir"); 
        if (dir != null) {
            scratchDir = new File(dir);
        } else {
            // First try the Servlet 2.2 javax.servlet.context.tempdir property
            scratchDir = (File) context.getAttribute(Constants.TMP_DIR);
            if (scratchDir == null) {
                // Not running in a Servlet 2.2 container.
                // Try to get the JDK 1.2 java.io.tmpdir property
                dir = System.getProperty("java.io.tmpdir");
                if (dir != null)
                    scratchDir = new File(dir);
            }
        }      
        if (this.scratchDir == null) {
            log.fatal(Localizer.getMessage("jsp.error.no.scratch.dir"));
            return;
        }
            
        if (!(scratchDir.exists() && scratchDir.canRead() &&
              scratchDir.canWrite() && scratchDir.isDirectory()))
            log.fatal(Localizer.getMessage("jsp.error.bad.scratch.dir",
					   scratchDir.getAbsolutePath()));
                                  
        this.compiler = config.getInitParameter("compiler");

        String javaEncoding = config.getInitParameter("javaEncoding");
        if (javaEncoding != null) {
            this.javaEncoding = javaEncoding;
        }

        String fork = config.getInitParameter("fork");
        if (fork != null) {
            if (fork.equalsIgnoreCase("true")) {
                this.fork = true;
            } else if (fork.equalsIgnoreCase("false")) {
                this.fork = false;
            } else {
		if (log.isWarnEnabled()) {
		    log.warn(Localizer.getMessage("jsp.warning.fork"));
		}
	    }
        }

	// Setup the global Tag Libraries location cache for this
	// web-application.
	tldLocationsCache = new TldLocationsCache(context);

	// Setup the jsp config info for this web app.
	jspConfig = new JspConfig(context);

	// Create a Tag plugin instance
	tagPluginManager = new TagPluginManager(context);
    }

}

