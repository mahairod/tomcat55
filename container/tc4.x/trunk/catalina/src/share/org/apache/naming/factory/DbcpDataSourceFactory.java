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


package org.apache.naming.factory;

import java.util.Hashtable;
import java.sql.Driver;
import java.sql.DriverManager;
import javax.naming.Name;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.RefAddr;
import javax.naming.spi.ObjectFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.naming.ResourceRef;


/**
 * <p>Object factory for DataSources based on the <code>commons-dbcp</code>
 * connection pool. See
 * <a href="http://jakarta.apache.org/commons/">http://jakarta.apache.org/commons</a>
 * for more information.</p>
 *
 * <p>This factory is configured based on the following properties:</p>
 * <ul>
 * <li><strong>driverClassName</strong> - Fully qualified Java class name of
 *     the JDBC driver to be used.</li>
 * <li><strong>driverName</strong> - Connection URL to be passed to our JDBC
 *     driver.  <em>DEPRECATED - use the <strong>url</strong> property to
 *     initialize the connection URL.</li>
 * <li><strong>password</strong> - Database password to be passed to our
 *     JDBC driver.</li>
 * <li><strong>url</strong> - Connection URL to be passed to our
 *     JDBC driver.</li>
 * <li><strong>user</strong> - Database username to be passed to our
 *     JDBC driver.</li>
 * </ul>
 * 
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public class DbcpDataSourceFactory
    implements ObjectFactory {


    // ----------------------------------------------------------- Constructors


    // ----------------------------------------------------- Instance Variables


    // --------------------------------------------------------- Public Methods


    // -------------------------------------------------- ObjectFactory Methods


    /**
     * Create a new DataSource instance.
     * 
     * @param obj The reference object describing the DataSource
     */
    public Object getObjectInstance(Object obj, Name name, Context nameCtx,
                                    Hashtable environment)
        throws NamingException {
        
        // Can we process this request?
        if (!(obj instanceof ResourceRef))
            return (null);
        Reference ref = (Reference) obj;
        if (!"javax.sql.DataSource".equals(ref.getClassName()))
            return (null);

        // Extract configuration parameters for our new data source
        RefAddr currentRefAddr = null;

        String driverClassName = null;
        currentRefAddr = ref.get("driverClassName");
        if (currentRefAddr != null)
            driverClassName = currentRefAddr.getContent().toString();

        String password = null;
        currentRefAddr = ref.get("password");
        if (currentRefAddr != null)
            password = currentRefAddr.getContent().toString();

        String url = null;
        currentRefAddr = ref.get("url");
        if (currentRefAddr == null)
            currentRefAddr = ref.get("driverName");
        if (currentRefAddr != null)
            url = currentRefAddr.getContent().toString();

        String user = null;
        currentRefAddr = ref.get("user");
        if (currentRefAddr != null)
            user = currentRefAddr.getContent().toString();

        // Validate our configuration parameters
        if (driverClassName == null)
            throw new NamingException
                ("DbcpDataSourceFactory:  driverClassName is required");
        if (password == null)
            throw new NamingException
                ("DbcpDataSourceFactory:  password is required");
        if (url == null)
            throw new NamingException
                ("DbcpDataSourceFactory:  url is required");
        if (user == null)
            throw new NamingException
                ("DbcpDataSourceFactory:  username is required");
        log("driverClassName=" + driverClassName + ", password=" + password +
            ", url=" + url + ", user=" + user);

        // Load and register the JDBC driver
        Class driverClass = null;
        try {
            driverClass = Class.forName(driverClassName);
        } catch (Throwable t) {
            log("Cannot load JDBC driver " + driverClassName, t);
            throw new NamingException("Cannot load JDBC driver " +
                                      driverClassName);
        }
        Driver driver = null;
        try {
            driver = (Driver) driverClass.newInstance();
        } catch (Throwable t) {
            log("Cannot create JDBC driver " + driverClassName +
                " instance", t);
            throw new NamingException("Cannot create JDBC driver " +
                                      driverClassName + " instance");
        }
        try {
            DriverManager.registerDriver(driver);
        } catch (Throwable t) {
            log("Cannot register JDBC driver " + driverClassName +
                " instance", t);
            throw new NamingException("Cannot register JDBC driver " +
                                      driverClassName + " instance");
        }

        // Create a new data source instance
        // FIXME - Cache this for later reuse???
        GenericObjectPool connectionPool = new GenericObjectPool(null);
        DriverManagerConnectionFactory connectionFactory =
            new DriverManagerConnectionFactory(url, user, password);
        PoolableConnectionFactory poolableConnectionFactory =
            new PoolableConnectionFactory(connectionFactory, connectionPool,
                                          null, null,
                                          false, true);
        PoolingDataSource dataSource =
            new PoolingDataSource(connectionPool);
        return (dataSource);

        
    }


    // -------------------------------------------------------- Private Methods


    private void log(String message) {
        System.out.print("DbcpDataSourceFactory:  ");
        System.out.println(message);
    }


    private void log(String message, Throwable exception) {
        log(message);
        exception.printStackTrace(System.out);
    }

}
