/*
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

package org.apache.tomcat.modules.config;

import org.apache.tomcat.core.*;
import org.apache.tomcat.util.log.*;
import org.apache.tomcat.util.FileUtil;
import java.io.*;
import java.net.*;
import java.util.*;

/**
  Add a Logger.
  
  Logging in Tomcat is quite flexible; we can either have a log
  file per module (example: ContextManager) or we can have one
  for Servlets and one for Jasper, or we can just have one
  tomcat.log for both Servlet and Jasper.  Right now there are
  three standard log streams, "tc_log", "servlet_log", and
  "JASPER_LOG".  
  
  Path: 
  
  The file to which to output this log, relative to
  TOMCAT_HOME.  If you omit a "path" value, then stderr or
  stdout will be used.
  
  Verbosity: 
  
  Threshold for which types of messages are displayed in the
  log.  Levels are inclusive; that is, "WARNING" level displays
  any log message marked as warning, error, or fatal.  Default
  level is WARNING.  Note: servlet_log must be level
  INFORMATION in order to see normal servlet log messages.
  
  verbosityLevel values can be: 
  FATAL
  ERROR
  WARNING 
  INFORMATION
  DEBUG

  Timestamps:
  
  By default, logs print a timestamp in the form "yyyy-MM-dd
  hh:mm:ss" in front of each message.  To disable timestamps
  completely, set 'timestamp="no"'. To use the raw
  msec-since-epoch, which is more efficient, set
  'timestampFormat="msec"'.  If you want a custom format, you
  can use 'timestampFormat="hh:mm:ss"' following the syntax of
  java.text.SimpleDateFormat (see Javadoc API).  For a
  production environment, we recommend turning timestamps off,
  or setting the format to "msec".
  
  Custom Output:
  
  "Custom" means "normal looking".  "Non-custom" means
  "surrounded with funny xml tags".  In preparation for
  possibly disposing of "custom" altogether, now the default is
  'custom="yes"' (i.e. no tags)
  
  Per-component Debugging:
  
  Some components accept a "debug" attribute.  This further
  enhances log output.  If you set the "debug" level for a
  component, it may output extra debugging information.

 */
public class LogSetter extends  BaseInterceptor {
    String name;
    String path;
    String verbosityLevel;
    boolean servletLogger=false;
    
    public LogSetter() {
    }

    /** Set the name of the logger.
     *  Predefined names are: tc_log, servlet_log, JASPER_LOG.
     */
    public void setName( String s ) {
	name=s;
    }

    public void setPath( String s ) {
	path=s;
    }

    public void setVerbosityLevel( String s ) {
	verbosityLevel=s;
    }

    /** This logger will be used for servlet's log.
     *  ( if not set, the logger will output tomcat messages )
     */
    public void setServletLogger( boolean b ) {
	servletLogger=b;
    }
    
    /**
     *  The log will be added and opened as soon as the module is
     *  added to the server
     */
    public void addInterceptor(ContextManager cm, Context ctx,
			       BaseInterceptor module)
	throws TomcatException
    {
	if( module!=this ) return;

	if( ! FileUtil.isAbsolute( path ) ) {
	    File wd= new File(cm.getHome(), path);
	    path= wd.getAbsolutePath();
	}

	QueueLogger ql=new QueueLogger();
	if( name==null )
	    throw new TomcatException( "Invalid name for logger " );
	ql.setName(name);

	if( path!=null )
	    ql.setPath(path);
	if( verbosityLevel!= null )
	    ql.setVerbosityLevel(verbosityLevel);

	ql.open();

	cm.addLogger( ql );

	if( ctx!=null ) {
	    if( servletLogger ) {
		ctx.setServletLogger( ql );
	    } else {
		ctx.setLogger( ql );
	    }
	}  

    }
}
