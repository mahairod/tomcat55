/*
 * $Header$
 * $Revision$
 * $Date$
 *
 *   
 *  Copyright 1999-2004 The Apache Sofware Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.tomcat.modules.aaa;

import java.security.Principal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import org.apache.tomcat.core.Context;
import org.apache.tomcat.core.TomcatException;
import org.apache.tomcat.util.aaa.SimplePrincipal;

/**
 * Implmentation of <b>Realm</b> that works with any JDBC supported database.
 * See the JDBCRealm.howto for more details on how to set up the database and
 * for configuration options.
 *
 * @author Craig R. McClanahan
 * @author Carson McDonald
 * @author Ignacio J. Ortega
 * @author Bip Thelin
 */
public class JDBCRealm extends RealmBase {
    // ----------------------------------------------------- Instance Variables

    private boolean started=false;
    /** The connection to the database. */
    private Connection dbConnection = null;

    /** The PreparedStatement to use for authenticating users. */
    private PreparedStatement preparedAuthenticate = null;

    /** The PreparedStatement to use for identifying the roles for a specified user. */
    private PreparedStatement preparedRoles = null;

    /** The connection URL to use when trying to connect to the databse */
    protected String connectionURL = null;

    /** The connection URL to use when trying to connect to the databse */
    protected String connectionName = null;

    /** The connection URL to use when trying to connect to the databse */
    protected String connectionPassword = null;

    /** The table that holds user data. */
    protected String userTable = null;

    /** The column in the user table that holds the user's name */
    protected String userNameCol = null;

    /** The column in the user table that holds the user's credintials */
    protected String userCredCol = null;

    /** The table that holds the relation between user's and roles */
    protected String userRoleTable = null;

    /** The column in the user role table that names a role */
    protected String roleNameCol = null;

    /** The JDBC driver to use. */
    protected String driverName = null;

    /** Has the JDBC connection been started? */
    protected boolean JDBCStarted = false;
    boolean connectOnInit = false;
    // ------------------------------------------------------------- Properties

    /**
     * Set the JDBC driver that will be used.
     * @param driverName The driver name
     */
    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    /**
     * Set the URL to use to connect to the database.
     * @param connectionURL The new connection URL
     */
    public void setConnectionURL(String connectionURL) {
        this.connectionURL = connectionURL;
    }

    /**
     * Set the name to use to connect to the database.
     * @param connectionName User name
     */
    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }

    /**
     * Return status of connectionName
     */
    public boolean isConnectionNameSet() {
        return (connectionName != null);
    }

    /**
     * Set the password to use to connect to the database.
     * @param connectionPassword User password
     */
    public void setConnectionPassword(String connectionPassword) {
        this.connectionPassword = connectionPassword;
    }

    /**
     * Return status of connectionPassword
     */
    public boolean isConnectionPasswordSet() {
        return (connectionPassword != null);
    }

    /**
     * Set the table that holds user data.
     * @param userTable The table name
     */
    public void setUserTable(String userTable) {
        this.userTable = userTable;
    }

    /**
     * Set the column in the user table that holds the user's name
     * @param userNameCol The column name
     */
    public void setUserNameCol(String userNameCol) {
        this.userNameCol = userNameCol;
    }

    /**
     * Set the column in the user table that holds the user's credintials
     * @param userCredCol The column name
     */
    public void setUserCredCol(String userCredCol) {
        this.userCredCol = userCredCol;
    }

    /**
     * Set the table that holds the relation between user's and roles
     * @param userRoleTable The table name
     */
    public void setUserRoleTable(String userRoleTable) {
        this.userRoleTable = userRoleTable;
    }

    /**
     * Set the column in the user role table that names a role
     * @param roleNameCol The column name
     */
    public void setRoleNameCol(String roleNameCol) {
        this.roleNameCol = roleNameCol;
    }

    /**
     * When connectOnInit is true the JDBC connection is started at tomcat init
     * if false the connection is started the first times it is needed.
     * @param b
     */
    public void setConnectOnInit(boolean b) {
        connectOnInit = b;
    }

    /**
     * If there are any errors with the JDBC connection, executing
     * the query or anything we return false (don't authenticate). This event
     * is also logged.
     * If there is some SQL exception the connection is set to null.
     * This will allow a retry on the next auth attempt. This might not
     * be the best thing to do but it will keep tomcat from needing a restart
     * if the database goes down.
     *
     * @param username Username of the Principal to look up
     * @param credentials Password or other credentials to use in authenticating this username
     */
    public synchronized String getCredentials(String username){
        try {
            if (!checkConnection())
                return null;
            // Create the authentication search prepared statement if necessary
            if (preparedAuthenticate == null) {
                preparedAuthenticate=getPreparedAuthenticate(dbConnection);
            }
            // Perform the authentication search
            preparedAuthenticate.setString(1, username);
            ResultSet rs1 = preparedAuthenticate.executeQuery();
            if (rs1.next()) {
                return rs1.getString(1).trim();
            }
            rs1.close();
            return null;
        } catch (SQLException ex) {
            // Log the problem for posterity
            log(sm.getString("jdbcRealm.getCredentialsSQLException", username), ex);
            // Clean up the JDBC objects so that they get recreated next time
            // Return "not authenticated" for this request
            close();
            return null;
        }
    }

    protected PreparedStatement getPreparedAuthenticate(Connection conn) throws SQLException {
        String sql = "SELECT " + userCredCol
            + " FROM " + userTable
            + " WHERE " + userNameCol + " = ?";
        if (debug >= 1)
            log("JDBCRealm.authenticate: " + sql);
        return conn.prepareStatement(sql);
    }

    protected PreparedStatement getPreparedRoles(Connection conn) throws SQLException {
        String sql = "SELECT " + roleNameCol + " FROM " + userRoleTable
                   + " WHERE " + userNameCol + " = ?";
        if (debug >= 1)
            log("JDBCRealm.roles: " + sql);
        return conn.prepareStatement(sql);
    }


    private boolean checkConnection() {
        try {
            if ((dbConnection == null) || dbConnection.isClosed()) {
                Class.forName(driverName);
                if( JDBCStarted )
                        log(sm.getString("jdbcRealm.checkConnectionDBClosed"));
                if ((connectionName == null || connectionName.equals("")) ||
                    (connectionPassword == null || connectionPassword.equals(""))) {
                        dbConnection = DriverManager.getConnection(connectionURL);
                } else {
                    dbConnection = DriverManager.getConnection(connectionURL,
                        connectionName, connectionPassword);
                }
                JDBCStarted=true;
                if (dbConnection == null || dbConnection.isClosed()) {
                    log(sm.getString("jdbcRealm.checkConnectionDBReOpenFail"));
                    return false;
                }
            }
            return true;
        } catch (SQLException ex) {
            log(sm.getString("jdbcRealm.checkConnectionSQLException"), ex);
            close();
            return false;
        }
        catch (ClassNotFoundException ex) {
            throw new RuntimeException("JDBCRealm.checkConnection: " + ex);
        }
    }

/**
 * returns all the roles for a given user.
 *
 * @param username the user name
 * @return the roles array
 */
    public synchronized String[] getUserRoles(String username) {
        try {
            if (!checkConnection())
                return null;
            if (preparedRoles == null) {
                preparedRoles=getPreparedRoles(dbConnection);
            }
            preparedRoles.clearParameters();
            preparedRoles.setString(1, username);
            ResultSet rs = preparedRoles.executeQuery();
            // Next we convert the resultset into a String[]
            Vector vrol = new Vector();
            while (rs.next()) {
                vrol.addElement(rs.getString(1).trim());
            }
            String[] res = new String[vrol.size() > 0 ? vrol.size() : 1];
            // no roles case
            if (vrol.size() == 0) {
                res[0] = "";
                return res;
            }
            for (int i = 0; i < vrol.size(); i++)
                res[i] = (String)vrol.elementAt(i);
            return res;
        }
        catch (SQLException ex) {
            // Set the connection to null.
            // Next time we will try to get a new connection.
            log(sm.getString("jdbcRealm.getUserRolesSQLException", username));
            close();
        }
        return null;
    }

    // Nothing - except carry on the class name information
    public static class JdbcPrincipal extends SimplePrincipal {
        private String name;

        JdbcPrincipal(String name) {
            super(name);
        }
              
    }

    private void close() {
        if (preparedRoles != null) {
            try {
                preparedRoles.close();
            } catch (Throwable t) {
                ;
            }
            preparedRoles = null;
        }
        if (preparedAuthenticate != null) {
            try {
                preparedAuthenticate.close();
            } catch (Throwable t) {
                ;
            }
            preparedAuthenticate = null;
        }
        if (dbConnection != null) {
            try {
                dbConnection.close();
            } catch (Throwable t) {
                ;
            }
            dbConnection = null;
        }
    }

    public void contextShutdown(Context ctx) throws TomcatException {
        if (started && JDBCStarted) close();
    }

    public void contextInit(Context ctx) throws TomcatException {
        if (!started) {
            if (connectOnInit && !checkConnection()) {
                throw new RuntimeException("JDBCRealm cannot be started");
            }
            started=true;
        }
    }

    /**
     * getPrincipal
     * @param username
     * @return java.security.Principal
     */
    protected Principal getPrincipal(String username) {
        return new JdbcPrincipal( username );
    }

}

