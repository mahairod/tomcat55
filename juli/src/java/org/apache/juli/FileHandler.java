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


package org.apache.juli;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.logging.ErrorManager;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * Implementation of <b>Handler</b> that appends log messages to a file
 * named {prefix}.{date}.{suffix} in a configured directory, with an
 * optional preceding timestamp.
 *
 * @version $Revision$ $Date$
 */

public class FileHandler
    extends Handler {


    // ------------------------------------------------------------ Constructor

    
    public FileHandler() {
        configure();
        open();
    }
    

    // ----------------------------------------------------- Instance Variables


    /**
     * The as-of date for the currently open log file, or a zero-length
     * string if there is no open log file.
     */
    private String date = "";


    /**
     * The directory in which log files are created.
     */
    private String directory = null;


    /**
     * The prefix that is added to log file filenames.
     */
    private String prefix = null;


    /**
     * The suffix that is added to log file filenames.
     */
    private String suffix = null;


    /**
     * The PrintWriter to which we are currently logging, if any.
     */
    private PrintWriter writer = null;


    // ------------------------------------------------------------- Properties


    /**
     * Return the directory in which we create log files.
    public String getDirectory() {
        return (directory);
    }
     */


    /**
     * Set the directory in which we create log files.
     *
     * @param directory The new log file directory
    public void setDirectory(String directory) {
        this.directory = directory;
    }
     */


    /**
     * Return the log file prefix.
    public String getPrefix() {
        return (prefix);
    }
     */


    /**
     * Set the log file prefix.
     *
     * @param prefix The new log file prefix
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
     */


    /**
     * Return the log file suffix.
    public String getSuffix() {
        return (suffix);
    }
     */


    /**
     * Set the log file suffix.
     *
     * @param suffix The new log file suffix
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
     */


    // --------------------------------------------------------- Public Methods


    /**
     * Format and publish a <tt>LogRecord</tt>.
     *
     * @param  record  description of the log event
     */
    public void publish(LogRecord record) {

        if (!isLoggable(record)) {
            return;
        }

        // Construct the timestamp we will use, if requested
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        String tsString = ts.toString().substring(0, 19);
        String tsDate = tsString.substring(0, 10);

        // If the date has changed, switch log files
        if (!date.equals(tsDate)) {
            synchronized (this) {
                if (!date.equals(tsDate)) {
                    close();
                    date = tsDate;
                    open();
                }
            }
        }

        String result = null;
        try {
            result = getFormatter().format(record);
        } catch (Exception e) {
            reportError(null, e, ErrorManager.FORMAT_FAILURE);
            return;
        }
        
        try {
            writer.write(result);
            writer.flush();
        } catch (Exception e) {
            reportError(null, e, ErrorManager.WRITE_FAILURE);
            return;
        }
        
    }
    
    
    // -------------------------------------------------------- Private Methods


    /**
     * Close the currently open log file (if any)
     */
    public void close() {
        
        try {
            if (writer == null)
                return;
            writer.write(getFormatter().getTail(this));
            writer.flush();
            writer.close();
            writer = null;
            date = "";
        } catch (Exception e) {
            reportError(null, e, ErrorManager.CLOSE_FAILURE);
        }
        
    }


    /**
     * FLush
     */
    public void flush() {

        try {
            writer.flush();
        } catch (Exception e) {
            reportError(null, e, ErrorManager.FLUSH_FAILURE);
        }
        
    }
    
    
    /**
     * Configure from <code>LogManager</code> properties.
     */
    private void configure() {

        Timestamp ts = new Timestamp(System.currentTimeMillis());
        String tsString = ts.toString().substring(0, 19);
        date = tsString.substring(0, 10);

        LogManager manager = LogManager.getLogManager();
        String className = FileHandler.class.getName();
        
        // Retrieve configuration of logging file name
        directory = getProperty(className + ".directory", "logs");
        prefix = getProperty(className + ".prefix", "juli.");
        suffix = getProperty(className + ".suffix", ".log");

        // FIXME: Add filter configuration in LogManager ?
        //setFilter(manager.getFilterProperty(className + ".filter", null));
        // FIXME: Add formatter configuration in LogManager ?
        //setFormatter(manager.getFormatterProperty(className + ".formatter", new SimpleFormatter()));
        // Hardcode for now a SimpleFormatter
        setFormatter(new SimpleFormatter());
        // FIXME: Add encoding configuration in LogManager ?
        try {
            setEncoding(manager.getProperty(className + ".encoding"));
        } catch (UnsupportedEncodingException e) {
            try {
                setEncoding(null);
            } catch (Exception ex) {
            }
        }
        
        setErrorManager(new ErrorManager());
        
    }

    
    private String getProperty(String name, String defaultValue) {
        String value = LogManager.getLogManager().getProperty(name);
        if (value == null) {
            value = defaultValue;
        }
        return value.trim();
    }
    
    
    /**
     * Open the new log file for the date specified by <code>date</code>.
     */
    private void open() {

        // Create the directory if necessary
        File dir = new File(directory);
        dir.mkdirs();

        // Open the current log file
        try {
            String pathname = dir.getAbsolutePath() + File.separator +
                prefix + date + suffix;
            writer = new PrintWriter(new FileWriter(pathname, true), true);
            writer.write(getFormatter().getHead(this));
        } catch (Exception e) {
            reportError(null, e, ErrorManager.OPEN_FAILURE);
            writer = null;
        }

    }


}
