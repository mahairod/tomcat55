/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

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


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.BufferOverflowException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.servlet.ServletException;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.util.StringManager;


/**
 * <p>Implementation of the <b>Valve</b> interface that generates a web server
 * access log with the detailed line contents matching either the common or
 * combined patterns.  As an additional feature, automatic rollover of log files
 * when the date changes is also supported.</p>
 * <p>
 * Conditional logging is also supported. This can be done with the
 * <code>condition</code> property.
 * If the value returned from ServletRequest.getAttribute(condition)
 * yields a non-null value. The logging will be skipped.
 * </p>
 *
 * This us an NIO adaptation of the default AccessLogValve. 
 * 
 * @author Jean-Francois Arcand
 */

public final class ByteBufferAccessLogValve
    extends ValveBase
    implements Lifecycle {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new instance of this class with default property values.
     */
    public ByteBufferAccessLogValve() {

        super();
        setPattern("common");
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(directByteBufferSize);
        
        // setup first view buffer
        byteBuffer.position(0).limit(directByteBufferSize/2);
        viewBuffer1 = byteBuffer.slice();

        // setup 2nd view buffer
        byteBuffer.limit(byteBuffer.capacity()).position(directByteBufferSize/2);
        viewBuffer2 = byteBuffer.slice();

        currentByteBuffer = viewBuffer1;      
        writerByteBuffer = viewBuffer1;
 
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The as-of date for the currently open log file, or a zero-length
     * string if there is no open log file.
     */
    private String dateStamp = "";


    /**
     * The directory in which log files are created.
     */
    private String directory = "logs";


    /**
     * The descriptive information about this implementation.
     */
    protected static final String info =
        "org.apache.catalina.valves.ByteBufferAccessLogValve/1.0";


    /**
     * The lifecycle event support for this component.
     */
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);


    /**
     * The set of month abbreviations for log messages.
     */
    protected static final String months[] =
    { "Jan", "Feb", "Mar", "Apr", "May", "Jun",
      "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };


    /**
     * If the current log pattern is the same as the common access log
     * format pattern, then we'll set this variable to true and log in
     * a more optimal and hard-coded way.
     */
    private boolean common = false;


    /**
     * For the combined format (common, plus useragent and referer), we do
     * the same
     */
    private boolean combined = false;


    /**
     * The pattern used to format our access log lines.
     */
    private String pattern = null;


    /**
     * The prefix that is added to log file filenames.
     */
    private String prefix = "access_log.";


    /**
     * Should we rotate our log file? Default is true (like old behavior)
     */
    private boolean rotatable = true;


    /**
     * The string manager for this package.
     */
    private StringManager sm =
        StringManager.getManager(Constants.Package);


    /**
     * Has this component been started yet?
     */
    private boolean started = false;


    /**
     * The suffix that is added to log file filenames.
     */
    private String suffix = "";


    /**
     * A date formatter to format a Date into a date in the format
     * "yyyy-MM-dd".
     */
    private SimpleDateFormat dateFormatter = null;


    /**
     * A date formatter to format Dates into a day string in the format
     * "dd".
     */
    private SimpleDateFormat dayFormatter = null;


    /**
     * A date formatter to format a Date into a month string in the format
     * "MM".
     */
    private SimpleDateFormat monthFormatter = null;


    /**
     * Time taken formatter for 3 decimal places.
     */
     private DecimalFormat timeTakenFormatter = null;


    /**
     * A date formatter to format a Date into a year string in the format
     * "yyyy".
     */
    private SimpleDateFormat yearFormatter = null;


    /**
     * A date formatter to format a Date into a time in the format
     * "kk:mm:ss" (kk is a 24-hour representation of the hour).
     */
    private SimpleDateFormat timeFormatter = null;


    /**
     * The time zone relative to GMT.
     */
    private String timeZone = null;

    
    /**
     * The system time when we last updated the Date that this valve
     * uses for log lines.
     */
    private String currentDateString = null;

    
    /**
     * The instant where the date string was last updated.
     */
    private long currentDate = 0L;


    /**
     * When formatting log lines, we often use strings like this one (" ").
     */
    private String space = " ";


    /**
     * Resolve hosts.
     */
    private boolean resolveHosts = false;


    /**
     * Instant when the log daily rotation was last checked.
     */
    private long rotationLastChecked = 0L;


    /**
     * Are we doing conditional logging. default false.
     */
    private String condition = null;


    /**
     * Date format to place in log file name. Use at your own risk!
     */
    private String fileDateFormat = null;
    
    
    /**
     * The <code>FileChannel</code> used to write the access log.
     */
    protected FileChannel fileChannel;
    
    
    /**
     * The background writerThread completion semaphore.
     */
    private boolean threadDone = false;
    
    
    /**
     * The <code>ByteBuffer</code> used to store the logs.
     */
    private ByteBuffer viewBuffer1;

    
    /**
     * The <code>ByteBuffer</code> used to store the logs.
     */
    private ByteBuffer viewBuffer2;

    
    
    /**
     * The <code>ByteBuffer</code> used to store the logs.
     */
    private ByteBuffer writerByteBuffer;
    
    
    /**
     * The <code>ByteBuffer</code> used to store the logs.
     */
    private ByteBuffer currentByteBuffer;
   
    
    /**
     * The default byte buffer size. Default is 16k
     */
    protected int directByteBufferSize = 16 * 1024;
    
    
    /**
     * Per-Thread <code>StringBuffer</code> used to store the log.
     */
    static private ThreadLocal stringBufferThreadLocal = new ThreadLocal(){
          protected Object initialValue(){
              return new StringBuffer(80);
          }
    };

    
    /**
     * Per-Thread <code>StringBuffer</code> used to store the date.
     */
    static private ThreadLocal dateThreadLocal = new ThreadLocal(){
          protected Object initialValue(){
              return new StringBuffer(24);
          }
    };
    
    
    /**
     * Useful object to synchronize on.
     */
    private Object[] lock = new Object[0];

    
    /**
     * Set the direct <code>ByteBuffer</code> size
     */
    public void setBufferSize(int size){
        directByteBufferSize = size;
    }
    
    
    /**
     * Return the direct <code>ByteBuffer</code> size
     */    
    public int getBufferSize(){
        return directByteBufferSize;
    }
    
    
    // ------------------------------------------------------------- Properties


    /**
     * Return the directory in which we create log files.
     */
    public String getDirectory() {

        return (directory);

    }


    /**
     * Set the directory in which we create log files.
     *
     * @param directory The new log file directory
     */
    public void setDirectory(String directory) {

        this.directory = directory;

    }


    /**
     * Return descriptive information about this implementation.
     */
    public String getInfo() {

        return (this.info);

    }


    /**
     * Return the format pattern.
     */
    public String getPattern() {

        return (this.pattern);

    }


    /**
     * Set the format pattern, first translating any recognized alias.
     *
     * @param pattern The new pattern
     */
    public void setPattern(String pattern) {

        if (pattern == null)
            pattern = "";
        if (pattern.equals(Constants.AccessLog.COMMON_ALIAS))
            pattern = Constants.AccessLog.COMMON_PATTERN;
        if (pattern.equals(Constants.AccessLog.COMBINED_ALIAS))
            pattern = Constants.AccessLog.COMBINED_PATTERN;
        this.pattern = pattern;

        if (this.pattern.equals(Constants.AccessLog.COMMON_PATTERN))
            common = true;
        else
            common = false;

        if (this.pattern.equals(Constants.AccessLog.COMBINED_PATTERN))
            combined = true;
        else
            combined = false;

    }


    /**
     * Return the log file prefix.
     */
    public String getPrefix() {

        return (prefix);

    }


    /**
     * Set the log file prefix.
     *
     * @param prefix The new log file prefix
     */
    public void setPrefix(String prefix) {

        this.prefix = prefix;

    }


    /**
     * Should we rotate the logs
     */
    public boolean isRotatable() {

        return rotatable;

    }


    /**
     * Set the value is we should we rotate the logs
     *
     * @param rotatable true is we should rotate.
     */
    public void setRotatable(boolean rotatable) {

        this.rotatable = rotatable;

    }


    /**
     * Return the log file suffix.
     */
    public String getSuffix() {

        return (suffix);

    }


    /**
     * Set the log file suffix.
     *
     * @param suffix The new log file suffix
     */
    public void setSuffix(String suffix) {

        this.suffix = suffix;

    }


    /**
     * Set the resolve hosts flag.
     *
     * @param resolveHosts The new resolve hosts value
     */
    public void setResolveHosts(boolean resolveHosts) {

        this.resolveHosts = resolveHosts;

    }


    /**
     * Get the value of the resolve hosts flag.
     */
    public boolean isResolveHosts() {

        return resolveHosts;

    }


    /**
     * Return whether the attribute name to look for when
     * performing conditional loggging. If null, every
     * request is logged.
     */
    public String getCondition() {

        return condition;

    }


    /**
     * Set the ServletRequest.attribute to look for to perform
     * conditional logging. Set to null to log everything.
     *
     * @param condition Set to null to log everything
     */
    public void setCondition(String condition) {

        this.condition = condition;

    }

    /**
     *  Return the date format date based log rotation.
     */
    public String getFileDateFormat() {
        return fileDateFormat;
    }


    /**
     *  Set the date format date based log rotation.
     */
    public void setFileDateFormat(String fileDateFormat) {
        this.fileDateFormat =  fileDateFormat;
    }    
    
    
    // --------------------------------------------------------- Public Methods


    /**
     * Log a message summarizing the specified request and response, according
     * to the format specified by the <code>pattern</code> property.
     *
     * @param request Request being processed
     * @param response Response being processed
     * @param context The valve context used to invoke the next valve
     *  in the current processing pipeline
     *
     * @exception IOException if an input/output error has occurred
     * @exception ServletException if a servlet error has occurred
     */
   public void invoke(Request request, Response response)
            throws IOException, ServletException {
            
        // Pass this request on to the next valve in our pipeline
        getNext().invoke(request, response);
        
        if (condition!=null &&
                null!=request.getRequest().getAttribute(condition)) {
            return;
        }

        // Check to see if we should log using the "common" access log pattern
        String value = null;
        StringBuffer stringBuffer = (StringBuffer)stringBufferThreadLocal.get();       
        stringBuffer.setLength(0);                                                                                

        if (isResolveHosts())
            stringBuffer.append(request.getRemoteHost());
        else
            stringBuffer.append(request.getRemoteAddr());
        
        stringBuffer.append(" - ");
        
        value = request.getRemoteUser();
        if (value == null)
            stringBuffer.append("- ");
        else {
            stringBuffer.append(value);
            stringBuffer.append(space);
        }
        
        stringBuffer.append(getCurrentDateString());
        
        stringBuffer.append(request.getMethod());
        stringBuffer.append(space);
        stringBuffer.append(request.getRequestURI());
        if (request.getQueryString() != null) {
            stringBuffer.append('?');
            stringBuffer.append(request.getQueryString());
        }
        stringBuffer.append(space);
        stringBuffer.append(request.getProtocol());
        stringBuffer.append("\" ");
        
        stringBuffer.append(response.getStatus());
        
        stringBuffer.append(space);
        
        int length = response.getContentCount();
        
        if (length <= 0)
            value = "-";
        else
            value = "" + length;
        stringBuffer.append(value);
        
        if (combined) {
            stringBuffer.append(space);
            stringBuffer.append("\"");
            String referer = request.getHeader("referer");
            if(referer != null)
                stringBuffer.append(referer);
            else
                stringBuffer.append("-");
            stringBuffer.append("\"");
            
            stringBuffer.append(space);
            stringBuffer.append("\"");
            String ua = request.getHeader("user-agent");
            if(ua != null)
                stringBuffer.append(ua);
            else
                stringBuffer.append("-");
            stringBuffer.append("\"");
        }
        stringBuffer.append("\n");
        
        // synchronized before switching buffer
        byte[] bytesToWrite = stringBuffer.toString().getBytes();
        synchronized (lock) {
            int remaining = currentByteBuffer.remaining();
            boolean flushBuffer = false;
            if ( remaining < bytesToWrite.length){
                // The byteBuffer is full, so we need to
                // flush the buffer.
                flushBuffer = true;
            } else {
                currentByteBuffer.put(bytesToWrite);
            }


            // The buffer is full, so flush it.
            if ( flushBuffer ){
                if (currentByteBuffer == viewBuffer1) {
                    writerByteBuffer = viewBuffer1;
                } else {
                    writerByteBuffer = viewBuffer2;
                }

                try{
                    lock.notify();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                
                if (currentByteBuffer == viewBuffer1) {
                    currentByteBuffer = viewBuffer2;
                } else {
                    currentByteBuffer = viewBuffer1;
                }
                // Now we can store the results since the currentByteBuffer 
                // is empty.
                currentByteBuffer.put(bytesToWrite);
            }

        }
       
    }


    // -------------------------------------------------------- Private Methods

   
    /**
     * This method returns a Date object that is accurate to within one
     * second.  If a thread calls this method to get a Date and it's been
     * less than 1 second since a new Date was created, this method
     * simply gives out the same Date again so that the system doesn't
     * spend time creating Date objects unnecessarily.
     *
     * @return Date
     */
    private String getCurrentDateString() {
        // Only create a new Date once per second, max.
        long systime = System.currentTimeMillis();
        if ((systime - currentDate) > 1000) {
            synchronized (this) {
                // We don't care about being exact here: if an entry does get
                // logged as having happened during the previous second
                // it will not make any difference
                if ((systime - currentDate) > 1000) {

                    // Format the new date
                    Date date = new Date();
                    StringBuffer result = (StringBuffer)dateThreadLocal.get();       
                    result.setLength(0);  
                    
                    result.append("[");
                    // Day
                    result.append(dayFormatter.format(date));
                    result.append('/');
                    // Month
                    result.append(lookup(monthFormatter.format(date)));
                    result.append('/');
                    // Year
                    result.append(yearFormatter.format(date));
                    result.append(':');
                    // Time
                    result.append(timeFormatter.format(date));
                    result.append(space);
                    // Time zone
                    result.append(timeZone);
                    result.append("] \"");
                    
                    // Check for log rotation
                    if (rotatable) {
                        // Check for a change of date
                        String tsDate = dateFormatter.format(date);
                        // If the date has changed, switch log files
                        if (!dateStamp.equals(tsDate)) {
                            synchronized (this) {
                                if (!dateStamp.equals(tsDate)) {
                                    close();
                                    dateStamp = tsDate;
                                    open();
                                }
                            }
                        }
                    }
                    
                    currentDateString = result.toString();
                    currentDate = date.getTime();
                }
            }
        }
        return currentDateString;
    }
    
    
    /**
     * Close the currently open log file (if any)
     */
    private synchronized void close() {

        dateStamp = "";
        
        // Make sure everything has been writen.
        synchronized(lock){
            log();
        }
    }


    /**
     * Log the specified message to the log file, switching files if the date
     * has changed since the previous log call.
     *
     * @param message Message to be logged
     * @param date the current Date object (so this method doesn't need to
     *        create a new one)
     */
    public void log() {        
        try{
            writerByteBuffer.flip();
 
            while (writerByteBuffer.hasRemaining()){
                fileChannel.write(writerByteBuffer);
            }
            writerByteBuffer.clear();
        } catch (IOException ex){
            ;
        }

    }


    /**
     * Return the month abbreviation for the specified month, which must
     * be a two-digit String.
     *
     * @param month Month number ("01" .. "12").
     */
    private String lookup(String month) {

        int index;
        try {
            index = Integer.parseInt(month) - 1;
        } catch (Throwable t) {
            index = 0;  // Can not happen, in theory
        }
        return (months[index]);

    }


    /**
     * Open the new log file for the date specified by <code>dateStamp</code>.
     */
    private synchronized void open() {

        // Create the directory if necessary
        File dir = new File(directory);
        if (!dir.isAbsolute())
            dir = new File(System.getProperty("catalina.base"), directory);
        dir.mkdirs();

        // Open the current log file
        try {
            String pathname;
            // If no rotate - no need for dateStamp in fileName
            if (rotatable){
                pathname = dir.getAbsolutePath() + File.separator +
                            prefix + dateStamp + suffix;
            } else {
                pathname = dir.getAbsolutePath() + File.separator +
                            prefix + suffix;
            }
            
            // Open the file and then get a channel from the stream
            FileOutputStream fis = new FileOutputStream(pathname, true);
            fileChannel = fis.getChannel();

        } catch (IOException e) {
            try{
                fileChannel.close();
            } catch (IOException ex){
                ;
            }
        } 

    }


    private String calculateTimeZoneOffset(long offset) {
        StringBuffer tz = new StringBuffer();
        if ((offset<0))  {
            tz.append("-");
            offset = -offset;
        } else {
            tz.append("+");
        }

        long hourOffset = offset/(1000*60*60);
        long minuteOffset = (offset/(1000*60)) % 60;

        if (hourOffset<10)
            tz.append("0");
        tz.append(hourOffset);

        if (minuteOffset<10)
            tz.append("0");
        tz.append(minuteOffset);

        return tz.toString();
    }


    // ------------------------------------------------------ Lifecycle Methods


    /**
     * Add a lifecycle event listener to this component.
     *
     * @param listener The listener to add
     */
    public void addLifecycleListener(LifecycleListener listener) {

        lifecycle.addLifecycleListener(listener);

    }


    /**
     * Get the lifecycle listeners associated with this lifecycle. If this
     * Lifecycle has no listeners registered, a zero-length array is returned.
     */
    public LifecycleListener[] findLifecycleListeners() {

        return lifecycle.findLifecycleListeners();

    }


    /**
     * Remove a lifecycle event listener from this component.
     *
     * @param listener The listener to add
     */
    public void removeLifecycleListener(LifecycleListener listener) {

        lifecycle.removeLifecycleListener(listener);

    }


   /**
     * Prepare for the beginning of active use of the public methods of this
     * component.  This method should be called after <code>configure()</code>,
     * and before any of the public methods of the component are utilized.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    public void start() throws LifecycleException {

        // Validate and update our current component state
        if (started)
            throw new LifecycleException
                (sm.getString("accessLogValve.alreadyStarted"));
        lifecycle.fireLifecycleEvent(START_EVENT, null);
        started = true;

        // Initialize the timeZone, Date formatters, and currentDate
        TimeZone tz = TimeZone.getDefault();
        timeZone = calculateTimeZoneOffset(tz.getRawOffset());

        if (fileDateFormat==null || fileDateFormat.length()==0)
            fileDateFormat = "yyyy-MM-dd";
        dateFormatter = new SimpleDateFormat(fileDateFormat);
        dateFormatter.setTimeZone(tz);
        dayFormatter = new SimpleDateFormat("dd");
        dayFormatter.setTimeZone(tz);
        monthFormatter = new SimpleDateFormat("MM");
        monthFormatter.setTimeZone(tz);
        yearFormatter = new SimpleDateFormat("yyyy");
        yearFormatter.setTimeZone(tz);
        timeFormatter = new SimpleDateFormat("HH:mm:ss");
        timeFormatter.setTimeZone(tz);
        currentDateString = getCurrentDateString();
        dateStamp = dateFormatter.format(new Date());

        open();

    }


    /**
     * Gracefully terminate the active use of the public methods of this
     * component.  This method should be the last one called on a given
     * instance of this component.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that needs to be reported
     */
    public void stop() throws LifecycleException {

        // Validate and update our current component state
        if (!started)
            throw new LifecycleException
                (sm.getString("accessLogValve.notStarted"));
        lifecycle.fireLifecycleEvent(STOP_EVENT, null);
        started = false;

        close();
    }
    
    
    /**
     * Execute a periodic task, such as reloading, etc. This method will be
     * invoked inside the classloading context of this container. Unexpected
     * throwables will be caught and logged.
     */
    public void backgroundProcess() {
        log();
    }
}
