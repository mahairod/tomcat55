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
 * [Additional notices, if required by prior licensing conditions]
 *
 */ 

package org.apache.tomcat.util;

import java.util.zip.*;
import java.net.*;
import java.util.*;
import java.io.*;

/**
 * The correct name for this class should be URLClassLoader.
 * But there is already a class by that name in JDK1.2.
 *
 * I have had quite a few problems with URLClassLoader in
 * past, so I ended up writing this ClassLoader. I found that
 * the Java 2's URLClassLoader, does not close the Jar file once
 * opened. It is a pretty good optimization step, but if you
 * modify the class in the jar file, it does not pick it up. Some
 * operating systems may not let you modify the jar file while it is
 * still open. IMHO, it does make sense to close the jar file
 * after you are done reading the class data. But this approach may not
 * get you the performance of the URLClassLoader, but it works in all
 * cases and also runs on JDK1.1
 *
 * @author Harish Prabandham
 */
public class NetworkClassLoader extends ClassLoader {
    private ClassLoader parent = null; // parent classloader
    private Hashtable classCache = new Hashtable();
    private Vector urlset = new Vector();

    /**
     * Creates a new instance of the class loader.
     * @param delegate/parent class loader.
     */
    public NetworkClassLoader(ClassLoader parent) {
        setParent(parent);
    }

    /**
     * Sets the parent/delegate class loader.
     * @param delegate/parent class loader.
     */
    protected final void setParent(ClassLoader parent) {
        this.parent = parent;
    }

    /**
     * Adds the given URL to this class loader. If the URL
     * ends with "/", then it is assumed to be a directory
     * otherwise, it is assumed to be a zip/jar file.
     * @param URL where to look for the classes.
     */
    public void addURL(URL url) {
        System.out.println("Adding url: " + url);
        if(!urlset.contains(url)) {
            urlset.addElement(url);
        }
    }

    /**
     * @return The URLs where this class loader looks for classes.
     */
    public URL[] getURLs() {
        URL[] urls = new URL[urlset.size()];
        urlset.copyInto(urls);
        return urls;
    }

    private byte[] loadResource(URL url, String resourceName)
        throws MalformedURLException, IOException {
        byte[] bytes = null;

        if(url.getFile().endsWith("/")) {
            // It is a directory 
            URL realURL = new URL(url.getProtocol(), url.getHost(),
                                  url.getFile() + resourceName);

            InputStream istream = realURL.openStream();
            bytes = getBytes(istream);
            try{istream.close();}catch(Exception e){}
            
        } else {
            // It is a zip/jar file.
            InputStream istream = url.openStream();
            ZipInputStream zstream = new ZipInputStream(istream);
            ZipEntry entry = null;

            while( (entry = zstream.getNextEntry()) != null) {
                System.out.println("Entry=" + entry.getName());
                if(!entry.isDirectory() &&
                   entry.getName().equals(resourceName)) {
                    bytes = getBytes(zstream);
                    zstream.close();
                    break;
                }

                zstream.closeEntry();
            }

            try{istream.close();}catch(Exception e){}
        }

        return bytes;
    }

    private byte[] loadResource(String resource) {
        byte[] barray = null;
        for(Enumeration e = urlset.elements(); e.hasMoreElements();) {
            URL url = (URL) e.nextElement();

            try {
                barray = loadResource(url, resource);
            } catch(Exception ex) {
            } finally {
                if(barray != null)
                    break;
            }
        }

        return barray;
    }

    private byte[] getBytes(InputStream istream) throws IOException {
        byte[] buf = new byte[1024];
        int num = 0;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        while( (num = istream.read(buf)) != -1) {
            bout.write(buf, 0, num);
        }

        return bout.toByteArray();
    }

    private byte[] loadClassData(String classname) {
        String resourceName = classname.replace('.', '/') + ".class";
        return loadResource(resourceName);
    }

    /**
     * @return The resource as the input stream if such a resource
     * exists, otherwise returns null.
     */
    public InputStream getResourceAsStream(String name) {
        InputStream istream = null;
        
        // Algorithm:
        //
        // 1. first check the system path for the resource
        // 2. next  check the  delegate/parent class loader for the resource
        // 3. then attempt to get the resource from the url set.
        //

        // Lets check the system path for the resource.
        istream = getSystemResourceAsStream(name);
        if(istream != null)
            return istream;

        // Lets check the parent/delegate class loader for the resource.
        if(parent != null) {
            istream = parent.getResourceAsStream(name);
            if(istream != null)
                return istream;
        }

        // Lets load it ourselves.
        byte[] data = loadResource(name);
        if(data != null) {
            istream = new ByteArrayInputStream(data);
        }

        return istream;
    }

    /**
     * java.lang.ClassLoader's defineClass method is final, so the
     * its subclasses cannot override this method. But, this class
     * instead calls this method in the loadClass() instead.
     * @param The name of the class without ".class" extension.
     * @param The class data bytes.
     * @return The class object.
     */
    protected Class defineClass(String classname, byte[] classdata) {
        return defineClass(classname, classdata, 0, classdata.length);
    }

    protected synchronized Class loadClass(String name, boolean resolve)
        throws ClassNotFoundException {
        Class c = null;

        // Algorithm: (Please do not change the order; unless you
        // have a good reason to do so).
        //
        // 1. first check the system class loader.
        // 2. next  check the  delegate/parent class loader.
        // 3. next  check the class cache
        // 4. then attempt to load classes from the URL set.
        //
        
        // Lets see if the class is in system class loader.
        try {
            c = findSystemClass(name);
        }catch(ClassNotFoundException cnfe) {
        }finally {
            if(c != null)
                return c;
        }

        // Lets see if the class is in parent class loader.
        try {
            if(parent != null)
                c = parent.loadClass(name);
        }catch(ClassNotFoundException cnfe) {
        }finally {
            if(c != null)
                return c;
        }

        // Lets see if the class is in the cache..
        c = (Class) classCache.get(name);

        if(c != null)
            return c;


        // Lets see if we find the class all by ourselves.
        byte[] data = loadClassData(name);

        if(data != null) {
            // we did !!
            c = defineClass(name, data);
            classCache.put(name, c);
            if(resolve)
                resolveClass(c);
        } else {
            // We are out of luck at this point...
            throw new ClassNotFoundException(name);
        }

        return c;
    }

    /**
     * This method resets this ClassLoader's state. It completely
     * removes all the URLs and classes in this class loader cache. 
     */
    protected final void clear() {
        urlset.removeAllElements();
        classCache.clear();
    }

    /**
     * This method resets this ClassLoader's state and resets the
     * references for garbage collection.
     */
    protected void finalize() throws Throwable {
        // Cleanup real well. Otherwise, this can be
        // a major source of memory leaks...

        // remove all the urls & class entries.
        clear();
        
        parent = null;
        urlset = null;
        classCache = null;
    }
}












