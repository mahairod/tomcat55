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
 */ 
package org.apache.tomcat.logging;

import java.io.Writer;
import java.io.StringWriter;
import java.io.PrintWriter;

import java.util.Date;

import org.apache.tomcat.util.Queue;

/**
 * A real implementation of the Logger abstraction. 
 *
 * @author Anil V (akv@eng.sun.com)
 * @since  Tomcat 3.1
 */
public class TomcatLogger extends Logger {

    /**
     * This is an entry that is created in response to every
     * Logger.log(...) call.
     */
    class LogEntry {
	String logName;
	long date;
	String message;
	Throwable t;
	
	LogEntry(String message, Throwable t) {
	    this.date = System.currentTimeMillis();
	    this.message = message;
	    this.t = t;
	}

	/**
	 * Get the writer into which this log entry needs to be
	 * written into. 
	 */
	Writer getWriter() {
	    return TomcatLogger.this.sink;
	}

	/**
	 * Format the log message nicely into a string.
	 */
	public String toString() {
	    StringWriter sw = new StringWriter();
	    PrintWriter w = new PrintWriter(sw);

	    w.print("<"+TomcatLogger.this.getName()+"> ");
	    w.print(new Date(date).toString());
	    w.print(' ');

	    if (message != null)
		w.println(message);
	    
	    if (t != null)
		t.printStackTrace(w);

	    return sw.toString();
	}
    }


    /**
     * Just one daemon and one queue for all Logger instances.. 
     */
    static LogDaemon logDaemon = null;
    static Queue     logQueue  = null;

    public TomcatLogger() {
	if (logDaemon == null || logQueue == null) {
	    logQueue = new Queue();
	    logDaemon = new LogDaemon(logQueue);
	    logDaemon.start();
	}
    }
    
    /**
     * Adds a log message to the queue and returns immediately. The
     * logger daemon thread will pick it up later and actually print
     * it out.
     * 
     * @param	message		the message to log.
     */
    protected void realLog(String message) {
	logQueue.put(new LogEntry(message, null));
    }
    
    /**
     * Adds a log message and stack trace to the queue and returns
     * immediately. The logger daemon thread will pick it up later and
     * actually print it out. 
     *
     * @param	message		the message to log. 
     * @param	t		the exception that was thrown.
     */
    protected void realLog(String message, Throwable t) {
	logQueue.put(new LogEntry(message, t));
    }
    
    /**
     * Flush the log. 
     */
    public void flush() {
	logDaemon.flush();
    }
}

/**
 * The daemon thread that looks in a queue and if it is not empty
 * writes out everything in the queue to the sink.
 */
class LogDaemon extends Thread {
    LogDaemon(Queue logQueue) {
	this.logQueue = logQueue;
	setDaemon(true);
    }

    Runnable flusher = new Runnable() {
	    public void run() {
		do {
		    TomcatLogger.LogEntry logEntry = (TomcatLogger.LogEntry) logQueue.pull();
		    Writer writer = logEntry.getWriter();
		    if (writer != null)
			try {
			    writer.write(logEntry.toString());
			    writer.flush();
			} catch (Exception ex) { // IOException
			    ex.printStackTrace();
			}
		} while (!logQueue.isEmpty());
	    }
    };

    public void run() {
	while (true)
	    flusher.run();
    }

    public void flush() {
	Thread workerThread = new Thread(flusher);
	workerThread.start();
    }

    private Queue logQueue;
}
