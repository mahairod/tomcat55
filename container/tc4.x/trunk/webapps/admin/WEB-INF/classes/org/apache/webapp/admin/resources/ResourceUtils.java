/*
 * $Header$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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
 * 3. The end-resource documentation included with the redistribution, if
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
 *    permission of the Apache Datasource.
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
 */


package org.apache.webapp.admin.resources;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ObjectInstance;

/**
 * <p>Shared utility methods for the resource administration module.</p>
 *
 * @author Manveen Kaur
 * @version $Revision$ $Date$
 * @since 4.1
 */

public class ResourceUtils {

    public final static String ENVIRONMENT_TYPE = "Catalina:type=Environment";
    public final static String RESOURCE_TYPE = "Catalina:type=Resource";
    public final static String RESOURCELINK_TYPE = "Catalina:type=ResourceLink";
    public final static String NAMINGRESOURCES_TYPE = "Catalina:type=NamingResources";
    
    // resource class names
    public final static String USERDB_CLASS = "org.apache.catalina.UserDatabase";
    public final static String DATASOURCE_CLASS = "javax.sql.DataSource";

    // --------------------------------------------------------- Public Methods

    /**
     * Construct and return a ResourcesForm identifying all currently defined
     * resources in the specified resource database.
     *
     * @param mserver MBeanServer to be consulted
     * @param databaseName MBean Name of the resource database to be consulted
     *
     * @exception Exception if an error occurs
     */
    public static EnvEntriesForm getEnvEntriesForm(MBeanServer mserver, 
                        String parent, String parentType) throws Exception {
                           
        ObjectName ename = null;
        if ((parent == null) || (parentType == null)) {
            ename = new ObjectName( ENVIRONMENT_TYPE + ",*");
        } else {
            ename = new ObjectName( ENVIRONMENT_TYPE +
                                        ","+parentType+"=" + parent);
        }
               
        Iterator iterator = (mserver.queryMBeans(ename, null).iterator());
        
        ArrayList results = new ArrayList();        
        while (iterator.hasNext()) {
            ObjectInstance instance = (ObjectInstance) iterator.next(); 
            results.add(instance.getObjectName().toString());
        }

        Collections.sort(results);        
        
        EnvEntriesForm envEntriesForm = new EnvEntriesForm();
        envEntriesForm.setEnvEntries((String[]) 
                        results.toArray(new String[results.size()])); 
        envEntriesForm.setParentName(parent);
        envEntriesForm.setParentType(parentType);
        
        return (envEntriesForm);

    }

    /**
     * Construct and return a DataSourcesForm identifying all currently defined
     * datasources in the specified resource database.
     *
     * @param mserver MBeanServer to be consulted
     * @param databaseName MBean Name of the resource database to be consulted
     *
     * @exception Exception if an error occurs
     */
    public static DataSourcesForm getDataSourcesForm(MBeanServer mserver, 
                        String parent, String parentType) throws Exception {
                            
        ObjectName rname = new ObjectName( RESOURCE_TYPE + 
                            ",class=" + DATASOURCE_CLASS + ",*");
                                 
        Iterator iterator = (mserver.queryMBeans(rname, null).iterator());
        
        ArrayList results = new ArrayList();        
        while (iterator.hasNext()) {
            ObjectInstance instance = (ObjectInstance) iterator.next(); 
            results.add(instance.getObjectName().toString());
        }

        Collections.sort(results);        
        DataSourcesForm dataSourcesForm = new DataSourcesForm();
        dataSourcesForm.setDataSources((String[]) 
                        results.toArray(new String[results.size()]));        
        return (dataSourcesForm);

    }

    /**
     * Construct and return a ResourceLinksForm identifying all currently defined
     * resourcelinks in the specified resource database.
     *
     * @param mserver MBeanServer to be consulted
     * @param name MBean Name of the resource link to be consulted
     *
     * @exception Exception if an error occurs
     */
    public static ResourceLinksForm getResourceLinksForm(MBeanServer mserver, 
                        String parent, String parentType) throws Exception {

        ObjectName rname = new ObjectName( RESOURCELINK_TYPE + ",*");
                                 
        Iterator iterator = (mserver.queryMBeans(rname, null).iterator());
        
        ArrayList results = new ArrayList();        
        while (iterator.hasNext()) {
            ObjectInstance instance = (ObjectInstance) iterator.next(); 
            results.add(instance.getObjectName().toString());
        }

        Collections.sort(results);        
        ResourceLinksForm resourceLinksForm = new ResourceLinksForm();
        resourceLinksForm.setResourceLinks((String[]) 
                        results.toArray(new String[results.size()]));        
        return (resourceLinksForm);

    }
    
    /**
     * Construct and return a UserDatabaseForm identifying all currently defined
     * user databases in the specified resource database.
     *
     * @param mserver MBeanServer to be consulted
     * @param databaseName MBean Name of the resource database to be consulted
     *
     * @exception Exception if an error occurs
     */
    public static UserDatabasesForm getUserDatabasesForm(MBeanServer mserver)
        throws Exception {

        ObjectName rname = new ObjectName( RESOURCE_TYPE + 
                            ",class=" + USERDB_CLASS + ",*");
        
        Iterator iterator = (mserver.queryMBeans(rname, null).iterator());
        
        ArrayList results = new ArrayList();        
        while (iterator.hasNext()) {
            ObjectInstance instance = (ObjectInstance) iterator.next(); 
            results.add(instance.getObjectName().toString());
        }

        Collections.sort(results);

        UserDatabasesForm userDatabasesForm = new UserDatabasesForm();
        userDatabasesForm.setUserDatabases((String[]) 
                        results.toArray(new String[results.size()]));  
        return (userDatabasesForm);

    }
    
}
