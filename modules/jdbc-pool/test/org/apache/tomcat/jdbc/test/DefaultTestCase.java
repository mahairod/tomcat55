/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomcat.jdbc.test;

import java.lang.reflect.Method;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;
import org.apache.tomcat.dbcp.dbcp.BasicDataSourceFactory;

import junit.framework.TestCase;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.apache.tomcat.jdbc.pool.DataSourceProxy;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.log.MLevel;
import com.mchange.v2.log.MLog;

/**
 * @author Filip Hanik
 * @version 1.0
 */
public class DefaultTestCase extends TestCase {
    protected org.apache.tomcat.jdbc.pool.DataSource datasource;
    protected BasicDataSource tDatasource;
    protected ComboPooledDataSource c3p0Datasource;
    protected int threadcount = 10;
    protected int iterations = 100000;
    public DefaultTestCase(String name) {
        super(name);
    }

    public org.apache.tomcat.jdbc.pool.DataSource createDefaultDataSource() {
        org.apache.tomcat.jdbc.pool.DataSource datasource = null;
        PoolProperties p = new DefaultProperties();
        p.setJmxEnabled(false);
        p.setTestWhileIdle(false);
        p.setTestOnBorrow(false);
        p.setTestOnReturn(false);
        p.setValidationInterval(30000);
        p.setTimeBetweenEvictionRunsMillis(30000);
        p.setMaxActive(threadcount);
        p.setInitialSize(threadcount);
        p.setMaxWait(10000);
        p.setRemoveAbandonedTimeout(10);
        p.setMinEvictableIdleTimeMillis(10000);
        p.setMinIdle(threadcount);
        p.setLogAbandoned(false);
        p.setRemoveAbandoned(false);
        datasource = new org.apache.tomcat.jdbc.pool.DataSource();
        datasource.setPoolProperties(p);
        return datasource;
    }
    
    protected void init() throws Exception {
        this.datasource = createDefaultDataSource();
    }

    protected void transferProperties() {
        try {
            BasicDataSourceFactory factory = new BasicDataSourceFactory();
            Properties p = new Properties();

            for (int i=0; i< ALL_PROPERTIES.length; i++) {
                String name = "get" + Character.toUpperCase(ALL_PROPERTIES[i].charAt(0)) + ALL_PROPERTIES[i].substring(1);
                String bname = "is" + name.substring(3);
                Method get = null;
                try {
                    get = PoolProperties.class.getMethod(name, new Class[0]);
                }catch (NoSuchMethodException x) {
                    try {
                    get = PoolProperties.class.getMethod(bname, new Class[0]);
                    }catch (NoSuchMethodException x2) {
                        System.err.println(x2.getMessage());
                    }
                }
                   if (get!=null) {
                       Object value = get.invoke(datasource.getPoolProperties(), new Object[0]);
                       if (value!=null) {
                           p.setProperty(ALL_PROPERTIES[i], value.toString());
                       }
                }
            }
            tDatasource = (BasicDataSource) BasicDataSourceFactory.createDataSource(p);
        }catch (Exception x) {
            x.printStackTrace();
        }
    }
    
    protected void transferPropertiesToC3P0() throws Exception {
        System.setProperty("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "WARNING");
        MLog.getLogger().setLevel(MLevel.WARNING);
        MLog.getLogger("com").setLevel(MLevel.WARNING);
        //http://www.mchange.com/projects/c3p0/index.html#automaticTestTable
        ComboPooledDataSource c3p0 = new ComboPooledDataSource();  
        c3p0.setAcquireIncrement(1);
        c3p0.setAcquireRetryAttempts(2);
        c3p0.setAcquireRetryDelay(datasource.getPoolProperties().getMaxWait());
        c3p0.setCheckoutTimeout(datasource.getPoolProperties().getMaxWait());
        c3p0.setDebugUnreturnedConnectionStackTraces(datasource.getPoolProperties().isLogAbandoned());
        c3p0.setIdleConnectionTestPeriod(datasource.getPoolProperties().getTimeBetweenEvictionRunsMillis()/1000);
        c3p0.setInitialPoolSize(datasource.getPoolProperties().getInitialSize());
        c3p0.setMaxIdleTime(datasource.getPoolProperties().getMinEvictableIdleTimeMillis()/1000);
        c3p0.setMaxIdleTimeExcessConnections(datasource.getPoolProperties().getMaxIdle());
        c3p0.setMaxPoolSize(datasource.getPoolProperties().getMaxActive());
        c3p0.setMinPoolSize(datasource.getPoolProperties().getMinIdle());
        c3p0.setPassword(datasource.getPoolProperties().getPassword());
        c3p0.setPreferredTestQuery(datasource.getPoolProperties().getValidationQuery());
        c3p0.setTestConnectionOnCheckin(datasource.getPoolProperties().isTestOnReturn());
        c3p0.setTestConnectionOnCheckout(datasource.getPoolProperties().isTestOnBorrow());
        c3p0.setUnreturnedConnectionTimeout(datasource.getPoolProperties().getRemoveAbandonedTimeout());
        c3p0.setUser(datasource.getPoolProperties().getUsername());
        c3p0.setUsesTraditionalReflectiveProxies(true);
        c3p0.setJdbcUrl(datasource.getPoolProperties().getUrl());
        c3p0.setDriverClass(datasource.getPoolProperties().getDriverClassName());
        this.c3p0Datasource = c3p0;
        
      /**
        acquireIncrement
        acquireRetryAttempts
        acquireRetryDelay
        autoCommitOnClose
        automaticTestTable
        breakAfterAcquireFailure
        checkoutTimeout
        connectionCustomizerClassName
        connectionTesterClassName
        debugUnreturnedConnectionStackTraces
        factoryClassLocation
        forceIgnoreUnresolvedTransactions
        idleConnectionTestPeriod
        initialPoolSize
        maxAdministrativeTaskTime
        maxConnectionAge
        maxIdleTime
        maxIdleTimeExcessConnections
        maxPoolSize
        maxStatements
        maxStatementsPerConnection
        minPoolSize
        numHelperThreads
        overrideDefaultUser
        overrideDefaultPassword
        password
        preferredTestQuery
        propertyCycle
        testConnectionOnCheckin
        testConnectionOnCheckout
        unreturnedConnectionTimeout
        user
        usesTraditionalReflectiveProxies
        */
    }


    protected void tearDown() throws Exception {
        try {datasource.close();}catch(Exception ignore){}
        try {tDatasource.close();}catch(Exception ignore){}
        try {((ComboPooledDataSource)c3p0Datasource).close(true);}catch(Exception ignore){}
        datasource = null;
        tDatasource = null;
        c3p0Datasource = null;
        System.gc();
    }

    private final static String PROP_DEFAULTAUTOCOMMIT = "defaultAutoCommit";
    private final static String PROP_DEFAULTREADONLY = "defaultReadOnly";
    private final static String PROP_DEFAULTTRANSACTIONISOLATION = "defaultTransactionIsolation";
    private final static String PROP_DEFAULTCATALOG = "defaultCatalog";
    private final static String PROP_DRIVERCLASSNAME = "driverClassName";
    private final static String PROP_MAXACTIVE = "maxActive";
    private final static String PROP_MAXIDLE = "maxIdle";
    private final static String PROP_MINIDLE = "minIdle";
    private final static String PROP_INITIALSIZE = "initialSize";
    private final static String PROP_MAXWAIT = "maxWait";
    private final static String PROP_TESTONBORROW = "testOnBorrow";
    private final static String PROP_TESTONRETURN = "testOnReturn";
    private final static String PROP_TIMEBETWEENEVICTIONRUNSMILLIS = "timeBetweenEvictionRunsMillis";
    private final static String PROP_NUMTESTSPEREVICTIONRUN = "numTestsPerEvictionRun";
    private final static String PROP_MINEVICTABLEIDLETIMEMILLIS = "minEvictableIdleTimeMillis";
    private final static String PROP_TESTWHILEIDLE = "testWhileIdle";
    private final static String PROP_PASSWORD = "password";
    private final static String PROP_URL = "url";
    private final static String PROP_USERNAME = "username";
    private final static String PROP_VALIDATIONQUERY = "validationQuery";
    private final static String PROP_ACCESSTOUNDERLYINGCONNECTIONALLOWED = "accessToUnderlyingConnectionAllowed";
    private final static String PROP_REMOVEABANDONED = "removeAbandoned";
    private final static String PROP_REMOVEABANDONEDTIMEOUT = "removeAbandonedTimeout";
    private final static String PROP_LOGABANDONED = "logAbandoned";
    private final static String PROP_POOLPREPAREDSTATEMENTS = "poolPreparedStatements";
    private final static String PROP_MAXOPENPREPAREDSTATEMENTS = "maxOpenPreparedStatements";
    private final static String PROP_CONNECTIONPROPERTIES = "connectionProperties";

    private final static String[] ALL_PROPERTIES = {
        PROP_DEFAULTAUTOCOMMIT,
        PROP_DEFAULTREADONLY,
        PROP_DEFAULTTRANSACTIONISOLATION,
        PROP_DEFAULTCATALOG,
        PROP_DRIVERCLASSNAME,
        PROP_MAXACTIVE,
        PROP_MAXIDLE,
        PROP_MINIDLE,
        PROP_INITIALSIZE,
        PROP_MAXWAIT,
        PROP_TESTONBORROW,
        PROP_TESTONRETURN,
        PROP_TIMEBETWEENEVICTIONRUNSMILLIS,
        PROP_NUMTESTSPEREVICTIONRUN,
        PROP_MINEVICTABLEIDLETIMEMILLIS,
        PROP_TESTWHILEIDLE,
        PROP_PASSWORD,
        PROP_URL,
        PROP_USERNAME,
        PROP_VALIDATIONQUERY,
        PROP_ACCESSTOUNDERLYINGCONNECTIONALLOWED,
        PROP_REMOVEABANDONED,
        PROP_REMOVEABANDONEDTIMEOUT,
        PROP_LOGABANDONED,
        PROP_CONNECTIONPROPERTIES
    };



}
