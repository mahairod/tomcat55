/*
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2001 The Apache Software Foundation.  All rights
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


package org.apache.catalina.startup;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URLConnection;
import java.net.URLClassLoader;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.servlet.ServletException;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.util.SchemaResolver;
import org.apache.catalina.util.StringManager;
import org.apache.commons.digester.Digester;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * Startup event listener for a <b>Context</b> that configures the properties
 * of that Context, and the associated defined servlets.
 *
 * @author Craig R. McClanahan
 * @author Jean-Francois Arcand
 * @author Costin Manolache
 */
public final class TldConfig  {

    private static org.apache.commons.logging.Log log=
        org.apache.commons.logging.LogFactory.getLog( TldConfig.class );

    private static final String FILE_URL_PREFIX = "file:";
    private static final int FILE_URL_PREFIX_LEN = FILE_URL_PREFIX.length();

    // ----------------------------------------------------- Instance Variables

    /**
     * The Context we are associated with.
     */
    private Context context = null;


    /**
     * The string resources for this package.
     */
    private static final StringManager sm =
        StringManager.getManager(Constants.Package);

    /**
     * The <code>Digester</code> we will use to process tag library
     * descriptor files.
     */
    private static Digester tldDigester = null;


    /**
     * Attribute value used to turn on/off XML validation
     */
     private static boolean xmlValidation = false;


    /**
     * Attribute value used to turn on/off XML namespace awarenes.
     */
    private static boolean xmlNamespaceAware = false;

    private boolean rescan=true;

    private ArrayList listeners=new ArrayList();

    // --------------------------------------------------------- Public Methods

    public boolean isRescan() {
        return rescan;
    }

    public void setRescan(boolean rescan) {
        this.rescan = rescan;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void addApplicationListener( String s ) {
        //if(log.isDebugEnabled())
            log.info( "Add tld listener " + s);
        listeners.add(s);
    }

    public String[] getTldListeners() {
        String result[]=new String[listeners.size()];
        listeners.toArray(result);
        return result;
    }

    /**
     * Scan for and configure all tag library descriptors found in this
     * web application.
     *
     * @exception Exception if a fatal input/output or parsing error occurs
     */
    public void execute() throws Exception {
        long t1=System.currentTimeMillis();

        File tldCache=null;

        if( context instanceof StandardContext ) {
            File workDir=(File)
                    ((StandardContext)context).getServletContext().getAttribute(Globals.WORK_DIR_ATTR);
            tldCache=new File( workDir, "tldCache.ser");
        }

        // Option to not rescan
        if( ! rescan ) {
            // find the cache
            if( tldCache!= null && tldCache.exists()) {
                // just read it...
                processCache(tldCache);
                return;
            }
        }

        /*
	 * Acquire the list of TLD resource paths, possibly embedded in JAR
	 * files, to be processed
	 */
        Set resourcePaths = tldScanResourcePaths();
	Set globalJarPaths = getGlobalJarPaths();

	// Check to see if we can use cached listeners
        if (tldCache != null && tldCache.exists()) {
            long lastModified = getLastModified(resourcePaths, globalJarPaths);
            if (lastModified < tldCache.lastModified()) {
                processCache(tldCache);
                return;
            }
        }

        // Scan each accumulated resource path for TLDs to be processed
        Iterator paths = resourcePaths.iterator();
        while (paths.hasNext()) {
            String path = (String) paths.next();
            if (path.endsWith(".jar")) {
                tldScanJar(path);
            } else {
                tldScanTld(path);
            }
        }
        paths = globalJarPaths.iterator();
        while (paths.hasNext()) {
            tldScanJar((JarURLConnection) paths.next());
        }

        String list[] = getTldListeners();

        if( tldCache!= null ) {
            log.info( "Saving tld cache: " + tldCache + " " + list.length);
            try {
                FileOutputStream out=new FileOutputStream(tldCache);
                ObjectOutputStream oos=new ObjectOutputStream( out );
                oos.writeObject( list );
                oos.close();
            } catch( IOException ex ) {
                ex.printStackTrace();
            }
        }

        if( log.isDebugEnabled() )
            log.debug( "Adding tld listeners:" + list.length);
        for( int i=0; list!=null && i<list.length; i++ ) {
            context.addApplicationListener(list[i]);
        }

        long t2=System.currentTimeMillis();
        if( context instanceof StandardContext ) {
            ((StandardContext)context).setTldScanTime(t2-t1);
        }

    }

    // -------------------------------------------------------- Private Methods

    /*
     * Returns the last modification date of the given sets of resources.
     *
     * @param resourcePaths
     * @param globalJarPaths
     *
     * @return Last modification date
     */
    private long getLastModified(Set resourcePaths, Set globalJarPaths)
            throws Exception {

	long lastModified = 0;

	Iterator paths = resourcePaths.iterator();
	while (paths.hasNext()) {
	    String path = (String) paths.next();
	    URL url = context.getServletContext().getResource(path);
	    if (url == null) {
		log.info( "Null url "+ path );
		break;
	    }
	    long lastM = url.openConnection().getLastModified();
	    if (lastM > lastModified) lastModified = lastM;
	    if (log.isDebugEnabled()) {
		log.debug( "Last modified " + path + " " + lastM);
	    }
	}

	paths = globalJarPaths.iterator();
	while (paths.hasNext()) {
	    JarURLConnection conn = (JarURLConnection) paths.next();
	    long lastM = conn.getLastModified();
	    if (lastM > lastModified) lastModified = lastM;
	    if (log.isDebugEnabled()) {
		log.debug("Last modified " + conn.getJarFileURL().toString()
			  + " " + lastM);
	    }
	}

	return lastModified;
    }

    private void processCache(File tldCache ) throws IOException {
        // read the cache and return;
        try {
            FileInputStream in=new FileInputStream(tldCache);
            ObjectInputStream ois=new ObjectInputStream( in );
            String list[]=(String [])ois.readObject();
            if( log.isDebugEnabled() )
                log.debug("Reusing tldCache " + tldCache + " " + list.length);
            for( int i=0; list!=null && i<list.length; i++ ) {
                context.addApplicationListener(list[i]);
            }
            ois.close();
        } catch( ClassNotFoundException ex ) {
            ex.printStackTrace();
        }
    }
    /**
     * Create (if necessary) and return a Digester configured to process a tag
     * library descriptor, looking for additional listener classes to be
     * registered.
     */
    private static Digester createTldDigester() {

        URL url = null;
        Digester tldDigester = new Digester();
        tldDigester.setNamespaceAware(xmlNamespaceAware);
        tldDigester.setValidating(xmlValidation);
        
        if (tldDigester.getFactory().getClass().getName().indexOf("xerces")!=-1) {
            tldDigester = patchXerces(tldDigester);
        }
        // Set the schemaLocation
        url = TldConfig.class.getResource(Constants.TldSchemaResourcePath_20);
        SchemaResolver tldEntityResolver = new SchemaResolver(url.toString(), 
                                                              tldDigester);
        if( xmlValidation ) {
            tldDigester.setSchema(url.toString());
        }
        
        url = TldConfig.class.getResource(Constants.TldDtdResourcePath_11);
        tldEntityResolver.register(Constants.TldDtdPublicId_11,
                                   url.toString());
        
        url = TldConfig.class.getResource(Constants.TldDtdResourcePath_12);
        tldEntityResolver.register(Constants.TldDtdPublicId_12,
                                   url.toString());
        
        tldEntityResolver = registerLocalSchema(tldEntityResolver);
        
        tldDigester.setEntityResolver(tldEntityResolver);
        tldDigester.addRuleSet(new TldRuleSet());
        return (tldDigester);

    }

    
    private static Digester patchXerces(Digester digester){
        // This feature is needed for backward compatibility with old DDs
        // which used Java encoding names such as ISO8859_1 etc.
        // with Crimson (bug 4701993). By default, Xerces does not
        // support ISO8859_1.
        try{
            digester.setFeature(
                "http://apache.org/xml/features/allow-java-encodings", true);
        } catch(ParserConfigurationException e){
                // log("contextConfig.registerLocalSchema", e);
        } catch(SAXNotRecognizedException e){
                // log("contextConfig.registerLocalSchema", e);
        } catch(SAXNotSupportedException e){
                // log("contextConfig.registerLocalSchema", e);
        }
        return digester;
    }
    

    /**
     * Utilities used to force the parser to use local schema, when available,
     * instead of the <code>schemaLocation</code> XML element.
     * @param entityResolver The instance on which properties are set.
     * @return an instance ready to parse XML schema.
     */
    protected static SchemaResolver registerLocalSchema(SchemaResolver entityResolver){

        URL url = TldConfig.class.getResource(Constants.J2eeSchemaResourcePath_14);
        entityResolver.register(Constants.J2eeSchemaPublicId_14,
                                url.toString());

        url = TldConfig.class.getResource(Constants.W3cSchemaResourcePath_10);
        entityResolver.register(Constants.W3cSchemaPublicId_10,
                                url.toString());

        url = TldConfig.class.getResource(Constants.JspSchemaResourcePath_20);
        entityResolver.register(Constants.JspSchemaPublicId_20,
                                url.toString());

        url = TldConfig.class.getResource(Constants.TldSchemaResourcePath_20);
        entityResolver.register(Constants.TldSchemaPublicId_20,
                                url.toString());
        
        url = TldConfig.class.getResource(Constants.WebSchemaResourcePath_24);
        entityResolver.register(Constants.WebSchemaPublicId_24,
                                url.toString());
        
        url = TldConfig.class.getResource(Constants.J2eeWebServiceSchemaResourcePath_11);
        entityResolver.register(Constants.J2eeWebServiceSchemaPublicId_11,
                                url.toString());

        url = TldConfig.class.getResource(Constants.J2eeWebServiceClientSchemaResourcePath_11);
        entityResolver.register(Constants.J2eeWebServiceClientSchemaPublicId_11,
                                url.toString());
        
        return entityResolver;
    }



    /**
     * Scan the JAR file at the specified resource path for TLDs in the
     * <code>META-INF</code> subdirectory, and scan them for application
     * event listeners that need to be registered.
     *
     * @param resourcePath Resource path of the JAR file to scan
     *
     * @exception Exception if an exception occurs while scanning this JAR
     */
    private void tldScanJar(String resourcePath) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug(" Scanning JAR at resource path '" + resourcePath + "'");
        }

	URL url = context.getServletContext().getResource(resourcePath);
	if (url == null) {
	    throw new IllegalArgumentException
		(sm.getString("contextConfig.tldResourcePath",
			      resourcePath));
	}
	url = new URL("jar:" + url.toString() + "!/");
	JarURLConnection conn = (JarURLConnection) url.openConnection();
	conn.setUseCaches(false);
	tldScanJar(conn);
    }

    /*
     * Scans all TLD entries in the given JAR for application listeners.
     *
     * @param conn URLConnection to the JAR file whose TLD entries are
     * scanned for application listeners
     */
    private void tldScanJar(JarURLConnection conn) throws Exception {

        JarFile jarFile = null;
        String name = null;
        InputStream inputStream = null;

	String jarPath = conn.getJarFileURL().toString();

	try {
	    jarFile = conn.getJarFile();
            Enumeration entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = (JarEntry) entries.nextElement();
                name = entry.getName();
                if (!name.startsWith("META-INF/")) {
                    continue;
                }
                if (!name.endsWith(".tld")) {
                    continue;
                }
                if (log.isTraceEnabled()) {
                    log.trace("  Processing TLD at '" + name + "'");
                }
                inputStream = jarFile.getInputStream(entry);
                tldScanStream(inputStream);
                inputStream.close();
                inputStream = null;
                name = null;
            }
            // FIXME - Closing the JAR file messes up the class loader???
            //            jarFile.close();
        } catch (Exception e) {
            // XXX Why do we wrap it ? The signature is 'throws Exception'
            if (name == null) {
                    log.error(sm.getString("contextConfig.tldJarException",
					   jarPath, context.getPath()),
			      e);
            } else {
                    log.error(sm.getString("contextConfig.tldEntryException",
					   name, jarPath,
					   context.getPath()),
			      e);
            }
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Throwable t) {
                    ;
                }
                inputStream = null;
            }
            if (jarFile != null) {
            // FIXME - Closing the JAR file messes up the class loader???
            //                try {
            //                    jarFile.close();
            //                } catch (Throwable t) {
            //                    ;
            //                }
                jarFile = null;
            }
        }

    }

    /**
     * Scan the TLD contents in the specified input stream, and register
     * any application event listeners found there.  <b>NOTE</b> - It is
     * the responsibility of the caller to close the InputStream after this
     * method returns.
     *
     * @param resourceStream InputStream containing a tag library descriptor
     *
     * @exception Exception if an exception occurs while scanning this TLD
     */
    private void tldScanStream(InputStream resourceStream)
        throws Exception {

        if (tldDigester == null){
            tldDigester = createTldDigester();
        }
        
        synchronized (tldDigester) {
            tldDigester.clear();
            tldDigester.push(this);
            tldDigester.parse(resourceStream);
        }

    }

    /**
     * Scan the TLD contents at the specified resource path, and register
     * any application event listeners found there.
     *
     * @param resourcePath Resource path being scanned
     *
     * @exception Exception if an exception occurs while scanning this TLD
     */
    private void tldScanTld(String resourcePath) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug(" Scanning TLD at resource path '" + resourcePath + "'");
        }

        InputStream inputStream = null;
        try {
            inputStream =
                context.getServletContext().getResourceAsStream(resourcePath);
            if (inputStream == null) {
                throw new IllegalArgumentException
                    (sm.getString("contextConfig.tldResourcePath",
                                  resourcePath));
            }
            tldScanStream(inputStream);
            inputStream.close();
            inputStream = null;
        } catch (Exception e) {
             throw new ServletException
                 (sm.getString("contextConfig.tldFileException", resourcePath, context.getPath()),
                  e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Throwable t) {
                    ;
                }
                inputStream = null;
            }
        }

    }

    /**
     * Accumulate and return a Set of resource paths to be analyzed for
     * tag library descriptors.  Each element of the returned set will be
     * the context-relative path to either a tag library descriptor file,
     * or to a JAR file that may contain tag library descriptors in its
     * <code>META-INF</code> subdirectory.
     *
     * @exception IOException if an input/output error occurs while
     *  accumulating the list of resource paths
     */
    private Set tldScanResourcePaths() throws IOException {
        if (log.isDebugEnabled()) {
            log.debug(" Accumulating TLD resource paths");
        }
        Set resourcePaths = new HashSet();

        // Accumulate resource paths explicitly listed in the web application
        // deployment descriptor
        if (log.isTraceEnabled()) {
            log.trace("  Scanning <taglib> elements in web.xml");
        }
        String taglibs[] = context.findTaglibs();
        for (int i = 0; i < taglibs.length; i++) {
            String resourcePath = context.findTaglib(taglibs[i]);
            // FIXME - Servlet 2.4 DTD implies that the location MUST be
            // a context-relative path starting with '/'?
            if (!resourcePath.startsWith("/")) {
                resourcePath = "/WEB-INF/" + resourcePath;
            }
            if (log.isTraceEnabled()) {
                log.trace("   Adding path '" + resourcePath +
                    "' for URI '" + taglibs[i] + "'");
            }
            resourcePaths.add(resourcePath);
        }

        DirContext resources = context.getResources();
	if (resources != null) {
	    tldScanResourcePathsWebInf(resources, resourcePaths);
	    tldScanResourcePathsWebInfLibJars(resources, resourcePaths);
	}

        // Return the completed set
        return (resourcePaths);

    }

    /*
     * Scans TLDs in the /WEB-INF subdirectory of the web application.
     *
     * @param resources The web application's resources
     * @param resourcePaths The set of resource paths to add to
     */
    private void tldScanResourcePathsWebInf(DirContext resources,
					    Set resourcePaths) 
            throws IOException {

        if (log.isTraceEnabled()) {
            log.trace("  Scanning TLDs in /WEB-INF subdirectory");
        }

	try {
	    NamingEnumeration items = resources.list("/WEB-INF");
	    while (items.hasMoreElements()) {
		NameClassPair item = (NameClassPair) items.nextElement();
		String resourcePath = "/WEB-INF/" + item.getName();
		// FIXME - JSP 2.0 is not explicit about whether we should
		// scan subdirectories of /WEB-INF for TLDs also
		if (!resourcePath.endsWith(".tld")) {
		    continue;
		}
		if (log.isTraceEnabled()) {
		    log.trace("   Adding path '" + resourcePath + "'");
		}
		resourcePaths.add(resourcePath);
	    }
	} catch (NamingException e) {
	    ; // Silent catch: it's valid that no /WEB-INF directory exists
	}
    }

    /*
     * Adds any JARs in the /WEB-INF/lib subdirectory of the web application
     * to the given set of resource paths.
     *
     * @param resources The web application's resources
     * @param resourcePaths The set of resource paths to add to
     */
    private void tldScanResourcePathsWebInfLibJars(DirContext resources,
						   Set resourcePaths)
            throws IOException {

        if (log.isTraceEnabled()) {
            log.trace("  Scanning JARs in /WEB-INF/lib subdirectory");
        }

        try {
            NamingEnumeration items = resources.list("/WEB-INF/lib");
            while (items.hasMoreElements()) {
                NameClassPair item = (NameClassPair) items.nextElement();
                String resourcePath = "/WEB-INF/lib/" + item.getName();
                if (!resourcePath.endsWith(".jar")) {
                    continue;
                }
                if (log.isTraceEnabled()) {
                    log.trace("   Adding path '" + resourcePath + "'");
                }
                resourcePaths.add(resourcePath);
            }
        } catch (NamingException e) {
            ; // Silent catch: it's valid that no /WEB-INF/lib directory exists
        }
    }

    /*
     * Returns the paths to all JAR files accessible to all parent
     * classloaders of the web application class loader.
     *
     * This is a Tomcat-specific extension to the TLD search order defined in
     * the JSP spec, which will allow tag libraries packaged as JAR
     * files to be shared by web applications by simply dropping them in a 
     * location that all web applications have access to (e.g.,
     * <CATALINA_HOME>/common/lib).
     *
     * @return Set of paths to all JAR files accessible to all parent class
     * loaders of the web application class loader
     */
    private Set getGlobalJarPaths() throws IOException {

        Set globalJarPaths = new HashSet();

	ClassLoader loader = Thread.currentThread().getContextClassLoader();
	while (loader != null) {
	    if (loader instanceof URLClassLoader) {
		URL[] urls = ((URLClassLoader) loader).getURLs();
		for (int i=0; i<urls.length; i++) {
		    URLConnection conn = urls[i].openConnection();
		    if (conn instanceof JarURLConnection) {
			conn.setUseCaches(false);			
			globalJarPaths.add((JarURLConnection) conn);
		    } else {
			String urlStr = urls[i].toString();
			if (urlStr.startsWith("file:")
			        && urlStr.endsWith(".jar")) {
			    URL jarURL = new URL("jar:" + urlStr + "!/");
			    JarURLConnection jarConn = (JarURLConnection)
				jarURL.openConnection();
			    jarConn.setUseCaches(false);
			    globalJarPaths.add(jarConn);
			}
		    }
		}
	    }
	    loader = loader.getParent();
	}

	return globalJarPaths;
    }
}
