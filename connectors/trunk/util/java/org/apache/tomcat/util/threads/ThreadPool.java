/*
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

package org.apache.tomcat.util.threads;

import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A thread pool that is trying to copy the apache process management.
 *
 * @author Gal Shachor
 */
public class ThreadPool  {
    static Log log = LogFactory.getLog(ThreadPool.class);

    /*
     * Default values ...
     */
    public static final int MAX_THREADS = 200;
    public static final int MAX_SPARE_THREADS = 50;
    public static final int MIN_SPARE_THREADS = 4;
    public static final int WORK_WAIT_TIMEOUT = 60*1000;

    /*
     * Where the threads are held.
     */
    protected ControlRunnable[] pool = null;

    /*
     * A monitor thread that monitors the pool for idel threads.
     */
    protected MonitorRunnable monitor;


    /*
     * Max number of threads that you can open in the pool.
     */
    protected int maxThreads;

    /*
     * Min number of idel threads that you can leave in the pool.
     */
    protected int minSpareThreads;

    /*
     * Max number of idel threads that you can leave in the pool.
     */
    protected int maxSpareThreads;

    /*
     * Number of threads in the pool.
     */
    protected int currentThreadCount;

    /*
     * Number of busy threads in the pool.
     */
    protected int currentThreadsBusy;

    /*
     * Flag that the pool should terminate all the threads and stop.
     */
    protected boolean stopThePool;

    /* Flag to control if the main thread is 'daemon' */
    protected boolean isDaemon=true;
    
    static int debug=0;

    /** The threads that are part of the pool.
     * Key is Thread, value is the ControlRunnable
     */
    protected Hashtable threads=new Hashtable();

    protected Vector listeners=new Vector();

    /**
     * Helper object for logging
     **/
    //Log loghelper = Log.getLog("tc/ThreadPool", "ThreadPool");
    
    public ThreadPool() {
        maxThreads      = MAX_THREADS;
        maxSpareThreads = MAX_SPARE_THREADS;
        minSpareThreads = MIN_SPARE_THREADS;
        currentThreadCount  = 0;
        currentThreadsBusy  = 0;
        stopThePool = false;
    }

    public synchronized void start() {
	stopThePool=false;
        currentThreadCount  = 0;
        currentThreadsBusy  = 0;

        adjustLimits();

        pool = new ControlRunnable[maxThreads];

        openThreads(minSpareThreads);
        monitor = new MonitorRunnable(this);
    }

    public MonitorRunnable getMonitor() {
        return monitor;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMinSpareThreads(int minSpareThreads) {
        this.minSpareThreads = minSpareThreads;
    }

    public int getMinSpareThreads() {
        return minSpareThreads;
    }

    public void setMaxSpareThreads(int maxSpareThreads) {
        this.maxSpareThreads = maxSpareThreads;
    }

    public int getMaxSpareThreads() {
        return maxSpareThreads;
    }

    public int getCurrentThreadCount() {
        return currentThreadCount;
    }

    public int getCurrentThreadsBusy() {
        return currentThreadsBusy;
    }

    public boolean isDaemon() {
        return isDaemon;
    }

    public static int getDebug() {
        return debug;
    }

    /** The default is true - the created threads will be
     *  in daemon mode. If set to false, the control thread
     *  will not be daemon - and will keep the process alive.
     */
    public void setDaemon( boolean b ) {
        isDaemon=b;
    }
    
    public boolean getDaemon() {
        return isDaemon;
    }

    public void addThread( Thread t, ControlRunnable cr ) {
        threads.put( t, cr );
        for( int i=0; i<listeners.size(); i++ ) {
            ThreadPoolListener tpl=(ThreadPoolListener)listeners.elementAt(i);
            tpl.threadStart(this, t);
        }
    }

    public void removeThread( Thread t ) {
        threads.remove(t);
        for( int i=0; i<listeners.size(); i++ ) {
            ThreadPoolListener tpl=(ThreadPoolListener)listeners.elementAt(i);
            tpl.threadEnd(this, t);
        }
    }

    public void addThreadPoolListener( ThreadPoolListener tpl ) {
        listeners.addElement( tpl );
    }

    public Enumeration getThreads(){
        return threads.keys();
    }

    //
    // You may wonder what you see here ... basically I am trying
    // to maintain a stack of threads. This way locality in time
    // is kept and there is a better chance to find residues of the
    // thread in memory next time it runs.
    //

    /**
     * Executes a given Runnable on a thread in the pool, block if needed.
     */
    public void runIt(ThreadPoolRunnable r) {

        if(null == r) {
            throw new NullPointerException();
        }

        if(0 == currentThreadCount || stopThePool) {
            throw new IllegalStateException();
        }

        ControlRunnable c = null;

        // Obtain a free thread from the pool.
        synchronized(this) {
            if(currentThreadsBusy == currentThreadCount) {
                 // All threads are busy
                if(currentThreadCount < maxThreads) {
                    // Not all threads were open,
                    // Open new threads up to the max number of idel threads
                    int toOpen = currentThreadCount + minSpareThreads;
                    openThreads(toOpen);
                } else {
		    logFull(log, currentThreadCount, maxThreads);
                    // Wait for a thread to become idel.
                    while(currentThreadsBusy == currentThreadCount) {
                        try {
                            this.wait();
                        }
			// was just catch Throwable -- but no other
			// exceptions can be thrown by wait, right?
			// So we catch and ignore this one, since
			// it'll never actually happen, since nowhere
			// do we say pool.interrupt().
			catch(InterruptedException e) {
			    log.error("Unexpected exception", e);
                        }

                        // Pool was stopped. Get away of the pool.
                        if(0 == currentThreadCount || stopThePool) {
                            throw new IllegalStateException();
                        }
                    }
                }
            }

            // If we are here it means that there is a free thred. Take it.
            c = pool[currentThreadCount - currentThreadsBusy - 1];
            currentThreadsBusy++;
        }
        c.runIt(r);
    }

    static boolean logfull=true;
    public static void logFull(Log loghelper, int currentThreadCount, int maxThreads) {
	if( logfull ) {
            log.error("All threads are busy, waiting. Please " +
                    "increase maxThreads or check the servlet" +
                    " status" + currentThreadCount + " " +
                    maxThreads  );
	    logfull=false;
	} 
    }

    /**
     * Stop the thread pool
     */
    public synchronized void shutdown() {
        if(!stopThePool) {
            stopThePool = true;
            monitor.terminate();
            monitor = null;
            for(int i = 0 ; i < (currentThreadCount - currentThreadsBusy - 1) ; i++) {
                try {
                    pool[i].terminate();
                } catch(Throwable t) {
                    /*
		     * Do nothing... The show must go on, we are shutting
		     * down the pool and nothing should stop that.
		     */
		    log.error("Ignored exception while shutting down thread pool", t);
                }
            }
            currentThreadsBusy = currentThreadCount = 0;
            pool = null;
            notifyAll();
        }
    }

    /**
     * Called by the monitor thread to harvest idle threads.
     */
    protected synchronized void checkSpareControllers() {

        if(stopThePool) {
            return;
        }
        if((currentThreadCount - currentThreadsBusy) > maxSpareThreads) {
            int toFree = currentThreadCount -
                         currentThreadsBusy -
                         maxSpareThreads;

            for(int i = 0 ; i < toFree ; i++) {
                ControlRunnable c = pool[currentThreadCount - currentThreadsBusy - 1];
                c.terminate();
                pool[currentThreadCount - currentThreadsBusy - 1] = null;
                currentThreadCount --;
            }
        }
    }

    /**
     * Returns the thread to the pool.
     * Called by threads as they are becoming idel.
     */
    protected synchronized void returnController(ControlRunnable c) {

        if(0 == currentThreadCount || stopThePool) {
            c.terminate();
            return;
        }

        currentThreadsBusy--;
        pool[currentThreadCount - currentThreadsBusy - 1] = c;
        notify();
    }

    /**
     * Inform the pool that the specific thread finish.
     *
     * Called by the ControlRunnable.run() when the runnable
     * throws an exception.
     */
    protected synchronized void notifyThreadEnd(ControlRunnable c) {
        currentThreadsBusy--;
        currentThreadCount --;
        notify();
    }


    /*
     * Checks for problematic configuration and fix it.
     * The fix provides reasonable settings for a single CPU
     * with medium load.
     */
    protected void adjustLimits() {
        if(maxThreads <= 0) {
            maxThreads = MAX_THREADS;
        }

        if(maxSpareThreads >= maxThreads) {
            maxSpareThreads = maxThreads;
        }

        if(maxSpareThreads <= 0) {
            if(1 == maxThreads) {
                maxSpareThreads = 1;
            } else {
                maxSpareThreads = maxThreads/2;
            }
        }

        if(minSpareThreads >  maxSpareThreads) {
            minSpareThreads =  maxSpareThreads;
        }

        if(minSpareThreads <= 0) {
            if(1 == maxSpareThreads) {
                minSpareThreads = 1;
            } else {
                minSpareThreads = maxSpareThreads/2;
            }
        }
    }

    protected void openThreads(int toOpen) {

        if(toOpen > maxThreads) {
            toOpen = maxThreads;
        }

        for(int i = currentThreadCount ; i < toOpen ; i++) {
            pool[i - currentThreadsBusy] = new ControlRunnable(this);
        }

        currentThreadCount = toOpen;
    }

    /** @deprecated */
    void log( String s ) {
	log.info(s);
	//loghelper.flush();
    }
    
    /** 
     * Periodically execute an action - cleanup in this case
     */
    public static class MonitorRunnable implements Runnable {
        ThreadPool p;
        Thread     t;
        int interval=WORK_WAIT_TIMEOUT;
        boolean    shouldTerminate;

        MonitorRunnable(ThreadPool p) {
            this.p=p;
            this.start();
        }

        public void start() {
            shouldTerminate = false;
            t = new Thread(this);
            t.setDaemon(p.getDaemon() );
	    t.setName( "MonitorRunnable" );
            t.start();
        }

        public void setInterval(int i ) {
            this.interval=i;
        }

        public void run() {
            while(true) {
                try {
                    // Sleep for a while.
                    synchronized(this) {
                        this.wait(WORK_WAIT_TIMEOUT);
                    }

                    // Check if should terminate.
                    // termination happens when the pool is shutting down.
                    if(shouldTerminate) {
                        break;
                    }

                    // Harvest idle threads.
                    p.checkSpareControllers();

                } catch(Throwable t) {
		    ThreadPool.log.error("Unexpected exception", t);
                }
            }
        }

        public void stop() {
            this.terminate();
        }

	/** Stop the monitor
	 */
        public synchronized void terminate() {
            shouldTerminate = true;
            this.notify();
        }
    }

    /**
     * A Thread object that executes various actions ( ThreadPoolRunnable )
     *  under control of ThreadPool
     */
    public static class ControlRunnable implements Runnable {
        /**
	 * ThreadPool where this thread will be returned
	 */
        ThreadPool p;

	/**
	 * The thread that executes the actions
	 */
        Thread     t;

	/**
	 * The method that is executed in this thread
	 */
        ThreadPoolRunnable   toRun;

	/**
	 * Stop this thread
	 */
	boolean    shouldTerminate;

	/**
	 * Activate the execution of the action
	 */
        boolean    shouldRun;

	/**
	 * Per thread data - can be used only if all actions are
	 *  of the same type.
	 *  A better mechanism is possible ( that would allow association of
	 *  thread data with action type ), but right now it's enough.
	 */
	boolean noThData;
	Object thData[]=null;

	/**
	 * Start a new thread, with no method in it
	 */
        ControlRunnable(ThreadPool p) {
            toRun = null;
            shouldTerminate = false;
            shouldRun = false;
            this.p = p;
            t = new ThreadWithAttributes(p, this);
            t.setDaemon(true);
            t.start();
            p.addThread( t, this );
	    noThData=true;
	    thData=null;
        }

        public void run() {

            while(true) {
                try {
		            /* Wait for work. */
                    synchronized(this) {
                        if(!shouldRun && !shouldTerminate) {
                            this.wait();
                        }
                    }
                    if(toRun == null ) {
                            if( p.log.isDebugEnabled())
                                p.log.debug( "No toRun ???");
                    }

                    if( shouldTerminate ) {
                            if( p.log.isDebugEnabled())
                                p.log.debug( "Terminate");
                            break;
                    }

                    /* Check if should execute a runnable.  */
                    try {
                        if(noThData) {
                            if(p.log.isDebugEnabled())
                                p.log.debug( "Getting new thread data");
                            thData=toRun.getInitData();
                            noThData = false;
                        }

                        if(shouldRun) {
			    toRun.runIt(thData);
                        }
                    } catch(Throwable t) {
			p.log.error("Caught exception executing " + toRun.toString() + ", terminating thread", t);
                        /*
                        * The runnable throw an exception (can be even a ThreadDeath),
                        * signalling that the thread die.
                        *
			* The meaning is that we should release the thread from
			* the pool.
			*/
                        shouldTerminate = true;
                        shouldRun = false;
                        p.notifyThreadEnd(this);
                    } finally {
                        if(shouldRun) {
                            shouldRun = false;
                            /*
			                * Notify the pool that the thread is now idle.
                            */
                            p.returnController(this);
                        }
                    }

                    /*
		     * Check if should terminate.
		     * termination happens when the pool is shutting down.
		     */
                    if(shouldTerminate) {
                        break;
                    }
                } catch(InterruptedException ie) { /* for the wait operation */
		    // can never happen, since we don't call interrupt
    		    p.log.error("Unexpected exception", ie);
                }
            }
        }

        /** Run a task
         *
         * @param toRun
         */
        public synchronized void runIt(ThreadPoolRunnable toRun) {
	    if( toRun == null ) {
		throw new NullPointerException("No Runnable");
	    }
            this.toRun = toRun;
	    // Do not re-init, the whole idea is to run init only once per
	    // thread - the pool is supposed to run a single task, that is
	    // initialized once.
            // noThData = true;
            shouldRun = true;
            this.notify();
        }

        public void stop() {
            this.terminate();
        }

        public void kill() {
            t.stop();
        }

        public synchronized void terminate() {
            shouldTerminate = true;
            this.notify();
        }
    }

    /** Special thread that allows storing of attributes and notes.
     *  A guard is used to prevent untrusted code from accessing the
     *  attributes.
     *
     *  This avoids hash lookups and provide something very similar
     * with ThreadLocal ( but compatible with JDK1.1 and faster on
     * JDK < 1.4 ).
     *
     * The main use is to store 'state' for monitoring ( like "processing
     * request 'GET /' ").
     */
    public static class ThreadWithAttributes extends Thread {
        private Object control;
        public static int MAX_NOTES=16;
        private Object notes[]=new Object[MAX_NOTES];
        private Hashtable attributes=new Hashtable();
        private String currentStage;

        public ThreadWithAttributes(Object control, Runnable r) {
            super(r);
            this.control=control;
        }

        public void setNote( Object control, int id, Object value ) {
            if( this.control != control ) return;
            notes[id]=value;
        }

        public String getCurrentStage() {
            return currentStage;
        }

        public void setCurrentStage(String currentStage) {
            this.currentStage = currentStage;
        }

        public Object getNote(Object control, int id ) {
            if( this.control != control ) return null;
            return notes[id];
        }

        public Hashtable getAttributes(Object control) {
            return attributes;
        }
    }

    /** Interface to allow applications to be notified when
     * a threads are created and stopped.
     */
    public static interface ThreadPoolListener {
        public void threadStart( ThreadPool tp, Thread t);

        public void threadEnd( ThreadPool tp, Thread t);
    }
}
