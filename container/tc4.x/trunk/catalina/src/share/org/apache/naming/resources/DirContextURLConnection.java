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

package org.apache.naming.resources;

import java.net.URL;
import java.net.URLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.FilePermission;
import java.security.Permission;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import javax.naming.NamingException;
import javax.naming.NamingEnumeration;
import javax.naming.NameClassPair;
import javax.naming.directory.DirContext;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import org.apache.naming.resources.Resource;
import org.apache.naming.resources.ResourceAttributes;

/**
 * Connection to a JNDI directory context.
 * <p/>
 * Note: All the object attribute names are the WebDAV names, not the HTTP 
 * names, so this class overrides some methods from URLConnection to do the
 * queries using the right names. Content handler is also not used; the 
 * content is directly returned.
 * 
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @version $Revision$
 */
public class DirContextURLConnection 
    extends URLConnection {
    
    
    // ----------------------------------------------------------- Constructors
    
    
    public DirContextURLConnection(DirContext context, URL url) {
        super(url);
        if (context == null)
            throw new IllegalArgumentException
                ("Directory context can't be null");
        this.permission = new FilePermission(url.toString(),"read");
        this.context = context;
    }
    
    
    // ----------------------------------------------------- Instance Variables
    
    
    /**
     * Directory context.
     */
    protected DirContext context;
    
    
    /**
     * Associated resource.
     */
    protected Resource resource;
    
    
    /**
     * Associated DirContext.
     */
    protected DirContext collection;
    
    
    /**
     * Other unknown object.
     */
    protected Object object;
    
    
    /**
     * Attributes.
     */
    protected Attributes attributes;
    
    
    /**
     * Date.
     */
    protected long date;
    
    
    /**
     * Permission
     */
    protected Permission permission;


    // ------------------------------------------------------------- Properties
    
    
    /**
     * Connect to the DirContext, and retrive the bound object, as well as
     * its attributes. If no object is bound with the name specified in the
     * URL, then an IOException is thrown.
     * 
     * @throws IOException Object not found
     */
    public void connect()
        throws IOException {
        
        if (!connected) {
            
            try {
                date = System.currentTimeMillis();
                object = context.lookup(getURL().getFile());
                attributes = context.getAttributes(getURL().getFile());
                if (object instanceof Resource)
                    resource = (Resource) object;
                if (object instanceof DirContext)
                    collection = (DirContext) object;
            } catch (NamingException e) {
                // Object not found
            }
            
            connected = true;
            
        }
        
    }
    
    
    /**
     * Return the content length value.
     */
    public int getContentLength() {
        return getHeaderFieldInt(ResourceAttributes.CONTENT_LENGTH, -1);
    }
    
    
    /**
     * Return the content type value.
     */
    public String getContentType() {
        return getHeaderField(ResourceAttributes.CONTENT_TYPE);
    }
    
    
    /**
     * Return the last modified date.
     */
    public long getDate() {
        return date;
    }
    
    
    /**
     * Return the last modified date.
     */
    public long getLastModified() {
        if (!connected) {
            // Try to connect (silently)
            try {
                connect();
            } catch (IOException e) {
            }
        }
        Attribute lastModified = 
            attributes.get(ResourceAttributes.LAST_MODIFIED);
        if (lastModified != null) {
            try {
                Date lmDate = (Date) lastModified.get();
                return lmDate.getTime();
            } catch (Exception e) {
            }
        }
        return 0;
    }
    
    
    /**
     * Returns the name of the specified header field.
     */
    public String getHeaderField(String name) {
        if (!connected) {
            // Try to connect (silently)
            try {
                connect();
            } catch (IOException e) {
            }
        }
        
        Attribute attribute = attributes.get(name);
        try {
            return attribute.get().toString();
        } catch (Exception e) {
            // Shouldn't happen, unless the attribute has no value
        }
        return (null);
        
    }
    
    
    /**
     * Get object content.
     */
    public Object getContent()
        throws IOException {
        
        if (!connected)
            connect();
        
        if (resource != null)
            return getInputStream();
        if (collection != null)
            return collection;
        if (object != null)
            return object;
        
        throw new FileNotFoundException();
        
    }
    
    
    /**
     * Get object content.
     */
    public Object getContent(Class[] classes)
        throws IOException {
        
        Object object = getContent();
        
        for (int i = 0; i < classes.length; i++) {
            if (classes[i].isInstance(object))
                return object;
        }
        
        return null;
        
    }
    
    
    /**
     * Get input stream.
     */
    public InputStream getInputStream() 
        throws IOException {
        
        if (!connected)
            connect();
        
        if (resource == null) {
            throw new FileNotFoundException();
        } else {
            // Reopen resource
            try {
                resource = (Resource) context.lookup(getURL().getFile());
            } catch (NamingException e) {
            }
        }
        
        return (resource.streamContent());
        
    }
    
    
    /**
     * Get the Permission for this URL
     */
    public Permission getPermission() {

        return permission;
    }


    // --------------------------------------------------------- Public Methods
    
    
    /**
     * List children of this collection. The names given are relative to this
     * URI's path. The full uri of the children is then : path + "/" + name.
     */
    public Enumeration list()
        throws IOException {
        
        if (!connected) {
            connect();
        }
        
        if ((resource == null) && (collection == null)) {
            throw new FileNotFoundException();
        }
        
        Vector result = new Vector();
        
        if (collection != null) {
            try {
                NamingEnumeration enum = context.list(getURL().getFile());
                while (enum.hasMoreElements()) {
                    NameClassPair ncp = (NameClassPair) enum.nextElement();
                    result.addElement(ncp.getName());
                }
            } catch (NamingException e) {
                // Unexpected exception
                throw new FileNotFoundException();
            }
        }
        
        return result.elements();
        
    }
    
    
}
