/*
 * $Header$ 
 * $Date$ 
 * $Revision$
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
package org.apache.tools.moo;

import java.io.OutputStream;
import java.io.IOException;

/**
 * A Logger is a singleton class used to log within the moo harness
 * The only way to get an instance of the logger class is by the static
 * getLogger class.  Only one logger exists in a given classloader.
 * The logger writes messages to the output stream (defaults to System.out)
 * via the log method
 */
public class Logger {

    //static members
    
    private static Logger logger;
    
    //static initialization block to set up logger
    static {
	logger = new Logger();
	logger.setOutputStream(System.out);
    }
    
    public static Logger getLogger() {
	return logger;
    }
    
    //object members
    private OutputStream out;
    
    //private constructor
    private Logger() {    
    }
    
    /**
     * sets the output stream of the logger to out
     * note: the logger will continue to use this new output stream
     *       until this method is called again or the virtual machine is shut
     *       down.
     *
     * @param out   the new output stream to which to log
     */
    public void setOutputStream(OutputStream out) {
	this.out = out;
    }
    
    /**
     * returns the output stream to which this Logger is logging
     */
    public OutputStream getOutputStream() {
	return out;
    }
    
    /**
     * logs the message to the given output stream
     *
     * @param msg   the message to be written to the log
     *
     */
    public void print(String msg) 
	throws IOException 
    {
	out.write(msg.getBytes());
    }
    
    /**
     * writes a line of input to the log
     *
     * @param msg   the message to be written to the log
     *
     */
    public void println(String msg) 
	throws IOException
    {
	print(msg + "\n");
    }
    
    public void println() throws IOException
    {
	println("");
    }
    
}
