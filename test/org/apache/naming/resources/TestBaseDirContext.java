package org.apache.naming.resources;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.startup.TomcatBaseTest;
import org.apache.tomcat.util.buf.ByteChunk;

public class TestBaseDirContext extends TomcatBaseTest {

    public void testDirContextAliases() throws Exception {
        Tomcat tomcat = getTomcatInstance();

        // Must have a real docBase - just use temp
        StandardContext ctx = (StandardContext) 
            tomcat.addContext("/", System.getProperty("java.io.tmpdir"));
        
        File lib = new File("webapps/examples/WEB-INF/lib");
        ctx.setAliases("/WEB-INF/lib=" + lib.getCanonicalPath());
        
        Tomcat.addServlet(ctx, "test", new TestServlet());
        ctx.addServletMapping("/", "test");
        
        tomcat.start();

        ByteChunk res = getUrl("http://localhost:" + getPort() + "/");

        String result = res.toString();
        
        assertTrue(result.indexOf("00-PASS") > -1);
        assertTrue(result.indexOf("01-PASS") > -1);
        assertTrue(result.indexOf("02-PASS") > -1);
    }


    /**
     * Looks for the JSTL JARs in WEB-INF/lib.
     */
    public static class TestServlet extends HttpServlet {

        private static final long serialVersionUID = 1L;

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {
            
            resp.setContentType("text/plain");
            
            ServletContext context = getServletContext();
            
            // Check resources individually
            URL url = context.getResource("/WEB-INF/lib/jstl.jar");
            if (url != null) {
                resp.getWriter().write("00-PASS\n");
            }
            
            url = context.getResource("/WEB-INF/lib/standard.jar");
            if (url != null) {
                resp.getWriter().write("01-PASS\n");
            }
            
            // Check a directory listing
            Set<String> libs = context.getResourcePaths("/WEB-INF/lib");
            if (libs == null) {
                return;
            }
            
            if (!libs.contains("/WEB-INF/lib/jstl.jar")) {
                return;
            }
            if (!libs.contains("/WEB-INF/lib/standard.jar")) {
                return;
            }

            resp.getWriter().write("02-PASS\n");
        }
        
    }
}
