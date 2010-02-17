package org.apache.catalina.loader;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.startup.TomcatBaseTest;

public class TestWebappClassLoaderMemoryLeak extends TomcatBaseTest {

    public void testTimerThreadLeak() throws Exception {
        Tomcat tomcat = getTomcatInstance();
        
        // Must have a real docBase - just use temp
        Context ctx = 
            tomcat.addContext("/", System.getProperty("java.io.tmpdir"));
        
        Tomcat.addServlet(ctx, "taskServlet", new TaskServlet());
        ctx.addServletMapping("/", "taskServlet");
        
        tomcat.start();

        // This will trigger the timer & thread creation
        getUrl("http://localhost:" + getPort() + "/");
        
        // Stop the context
        ctx.stop();
        
        // If the thread still exists, we have a thread/memory leak
        Thread[] threads = getThreads();
        for (Thread thread : threads) {
            if (thread != null &&
                    TaskServlet.TIMER_THREAD_NAME.equals(thread.getName())) {
                fail("Timer thread still running");
            }
        }
    }
    
    /*
     * Get the set of current threads as an array.
     * Copied from WebappClassLoader
     */
    private Thread[] getThreads() {
        // Get the current thread group 
        ThreadGroup tg = Thread.currentThread( ).getThreadGroup( );
        // Find the root thread group
        while (tg.getParent() != null) {
            tg = tg.getParent();
        }
        
        int threadCountGuess = tg.activeCount() + 50;
        Thread[] threads = new Thread[threadCountGuess];
        int threadCountActual = tg.enumerate(threads);
        // Make sure we don't miss any threads
        while (threadCountActual == threadCountGuess) {
            threadCountGuess *=2;
            threads = new Thread[threadCountGuess];
            // Note tg.enumerate(Thread[]) silently ignores any threads that
            // can't fit into the array 
            threadCountActual = tg.enumerate(threads);
        }
        
        return threads;
    }

    private static final class TaskServlet extends HttpServlet {

        private static final long serialVersionUID = 1L;
        private static final String TIMER_THREAD_NAME = "leaked-thread";

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {
            Timer timer = new Timer(TIMER_THREAD_NAME);
            timer.schedule(new LocalTask(), 0, 10000);
        }
        
    }

    private static final class LocalTask extends TimerTask {

        @Override
        public void run() {
            // Doesn't actually need to do anything.
        }
        
    }
}
