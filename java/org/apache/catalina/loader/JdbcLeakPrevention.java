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


package org.apache.catalina.loader;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * This class is loaded by the {@link WebappClassLoader} to enable it to
 * deregister JDBC drivers forgotten by the web application. There are some
 * classloading hacks involved - see {@link WebappClassLoader#clearReferences()}
 * for details - but the short version is do not just create a new instance of
 * this class with the new keyword.
 * 
 * Since this class is loaded by {@link WebappClassLoader}, it can not refer to
 * any internal Tomcat classes as that will cause the security manager to
 * complain.
 */
public class JdbcLeakPrevention {

    /* 
     * This driver is visible to all classloaders but is loaded by the system
     * class loader so there is no need to unload it.
     */
    private static final String JDBC_ODBC_BRIDGE_DRIVER =
        "sun.jdbc.odbc.JdbcOdbcDriver";
    
    public List<String> clearJdbcDriverRegistrations() throws SQLException {
        List<String> driverNames = new ArrayList<String>();
        
        // Unregister any JDBC drivers loaded by the class loader that loaded
        // this class - ie the webapp class loader
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (JDBC_ODBC_BRIDGE_DRIVER.equals(
                    driver.getClass().getCanonicalName())) {
                continue;
            }
            driverNames.add(driver.getClass().getCanonicalName());
            DriverManager.deregisterDriver(driver);
        }
        return driverNames;
    }
}
