/*
 * Copyright (c) 1998-99 The Java Apache Project.  All rights reserved.
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
 * 3. All advertising materials mentioning features or use of this
 *    software must display the following acknowledgment:
 *    "This product includes software developed by the Java Apache
 *    Project (http://java.apache.org/)."
 *
 * 4. The names "Apache JMeter" and "Java Apache Project" must
 *    not be used to endorse or promote products derived from this software
 *    without prior written permission.
 *
 * 5. Products derived from this software may not be called "Apache JMeter"
 *    nor may "Java Apache Project" appear in their names without
 *    prior written permission of the Java Apache Project.
 *
 * 6. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the Java Apache
 *    Project (http://java.apache.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JAVA APACHE PROJECT "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JAVA APACHE PROJECT OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Java Apache Project. For more information
 * on the Java Apache Project please see <http://java.apache.org/>.
 *
 */

package org.apache.tools.moo.cookie;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * This class provides an interface to the netscape cookies file to
 * pass cookies along with a request.
 *
 * @author  <a href="mailto:sdowd@arcmail.com">Sean Dowd</a>
 * @version $Revision$ $Date$
 */
public class CookieManager implements Cloneable {

    private Vector cookies = new Vector();
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd-MMM-yyyy HH:mm:ss zzz");

    public CookieManager (String cookieFile) {

        File file = new File(cookieFile);
        if (!file.isAbsolute()) file = new File(System.getProperty("user.dir") + File.separator + cookieFile);
        try {
        BufferedReader reader = new BufferedReader(new FileReader(file));

        String line;
        while ((line = reader.readLine()) != null) {
            try {
                if (line.startsWith("#") || line.trim().length() == 0) continue;
                StringTokenizer st = new StringTokenizer(line, "\t");
                String domain = st.nextToken();
                String foo = st.nextToken();
                String path = st.nextToken();
                boolean secure = new Boolean(st.nextToken()).booleanValue();
                long expires = new Long(st.nextToken()).longValue();
                String name = st.nextToken();
                String value = st.nextToken();
                Cookie cookie = new Cookie(name, value, domain, path, secure, expires);
                cookies.addElement(cookie);
            } catch (Exception e) {
                System.out.println("Error parsing cookie line\n\t'" + line + "'\n\t" + e);
                e.printStackTrace(System.out);
            }
        }

        reader.close();

        } catch (IOException ioe) {
            //ioe.printStackTrace(System.out);
        }

    }

    public String getCookieHeaderForURL(URL url) {
        if (!url.getProtocol().toUpperCase().equals("HTTP")) return null;

        StringBuffer header = new StringBuffer();
        for (Enumeration enum = cookies.elements(); enum.hasMoreElements();) {
            Cookie cookie = (Cookie) enum.nextElement();
            if (url.getHost().endsWith(cookie.getDomain()) &&
                    url.getFile().startsWith(cookie.getPath()) &&
                    (System.currentTimeMillis() / 1000) <= cookie.getExpires()) {
                if (header.length() > 0) {
                    header.append("; ");
                }
                header.append(cookie.getName()).append("=").append(cookie.getValue());
            }
        }
        
        if (header.length() != 0) {
            return header.toString();
        } else {
            return null;
        }
    }

    public void addCookieFromHeader(String cookieHeader, URL url) {
        StringTokenizer st = new StringTokenizer(cookieHeader, ";");
        String nvp;

        // first n=v is name=value
        nvp = st.nextToken();
        StringTokenizer nvpTokenizer = new StringTokenizer(nvp, "=");
        String name = nvpTokenizer.nextToken();
        String value = nvpTokenizer.nextToken();
        String domain = url.getHost();
        String path = url.getFile();

        Cookie newCookie = new Cookie(name, value, domain, path, false,
                System.currentTimeMillis() + 1000 * 60 * 60 * 24);

        // check the rest of the headers
        while (st.hasMoreTokens()) {
            nvp = st.nextToken();
            nvp = nvp.trim();
            nvpTokenizer = new StringTokenizer(nvp, "=");
            String key = nvpTokenizer.nextToken();
            if (key.equalsIgnoreCase("expires")) {
                try {
                    String expires = nvpTokenizer.nextToken();
                    Date date = dateFormat.parse(expires);
                    newCookie.setExpires(date.getTime());
                } catch (ParseException pe) {}
            } else if (name.equalsIgnoreCase("domain")) {
                newCookie.setDomain(nvpTokenizer.nextToken());
            } else if (name.equalsIgnoreCase("path")) {
                newCookie.setPath(nvpTokenizer.nextToken());
            } else if (name.equalsIgnoreCase("secure")) {
                newCookie.setSecure(true);
            }
        }

        Vector removeIndices = new Vector();
        for (int i = cookies.size() - 1; i > 0; i--) {
            Cookie cookie = (Cookie) cookies.elementAt(i);
            if (cookie == null)
                continue;
            if (cookie.getPath().equals(newCookie.getPath()) &&
                    cookie.getDomain().equals(newCookie.getDomain()) &&
                    cookie.getName().equals(newCookie.getName())) {
                removeIndices.addElement(new Integer(i));
            }
        }
        
        for (Enumeration e = removeIndices.elements(); e.hasMoreElements();) {
            int index = ((Integer) e.nextElement()).intValue();
            cookies.removeElementAt(index);
        }

        if (newCookie.getExpires() >= System.currentTimeMillis())
            cookies.addElement(newCookie);
    }

    public void removeCookieNamed(String name) {
        Vector removeIndices = new Vector();
        for (int i = cookies.size() - 1; i > 0; i--) {
            Cookie cookie = (Cookie) cookies.elementAt(i);
            if (cookie == null)
                continue;
            if (cookie.getName().equals(name))
                removeIndices.addElement(new Integer(i));
        }
        
        for (Enumeration e = removeIndices.elements(); e.hasMoreElements();) {
            cookies.removeElementAt(((Integer) e.nextElement()).intValue());
        }

    }
}

/**
 * This class is a Cookie encapsulator.
 *
 * @author  <a href="mailto:sdowd@arcmail.com">Sean Dowd</a>
 */
class Cookie implements Cloneable {

    private String name;
    private String value;
    private String domain;
    private long expires;
    private boolean secure;
    private String path;
        
    /**
     * create the coookie
     */
    Cookie(String name, String value, String domain, String path, boolean secure, long expires) {
        this.name = name;
        this.value = value;
        this.domain = domain;
        this.path = path;
        this.secure = secure;
        this.expires = expires;
    }

    /**
     * get the name for this object.
     */
    public String getName() {
        return this.name;
    }

    /**
     * set the name for this object.
     */
    public synchronized void setName(String name) {
        this.name = name;
    }

    /**
     * get the value for this object.
     */
    public String getValue() {
        return this.value;
    }

    /**
     * set the value for this object.
     */
    public synchronized void setValue(String value) {
        this.value = value;
    }

    /**
     * get the domain for this object.
     */
    public String getDomain() {
        return this.domain;
    }

    /**
     * set the domain for this object.
     */
    public synchronized void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * get the expires for this object.
     */
    public long getExpires() {
        return this.expires;
    }

    /**
     * set the expires for this object.
     */
    public synchronized void setExpires(long expires) {
        this.expires = expires;
    }

    /**
     * get the secure for this object.
     */
    public boolean getSecure() {
        return this.secure;
    }

    /**
     * set the secure for this object.
     */
    public synchronized void setSecure(boolean secure) {
        this.secure = secure;
    }

    /**
     * get the path for this object.
     */
    public String getPath() {
        return this.path;
    }

    /**
     * set the path for this object.
     */
    public synchronized void setPath(String path) {
        this.path = path;
    }

    /**
     * creates a string representation of this cookie
     */
    public String toString() {
        return "Cookie [ " + domain + ", " + path + ", " + name + ", " + value + " ]";
    }
}
