/*
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


package org.apache.catalina.realm;


import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.security.Principal;
import java.io.File;
import org.apache.catalina.Container;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Logger;
import org.apache.catalina.Realm;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.util.StringManager;
//import org.apache.catalina.util.xml.SaxContext;
//import org.apache.catalina.util.xml.XmlAction;
//import org.apache.catalina.util.xml.XmlMapper;
import org.xml.sax.AttributeList;
import org.apache.catalina.util.Base64;
import org.apache.catalina.util.HexUtils;

import java.security.*;
import java.sql.*;


/**
 *
 * Implmentation of <b>Realm</b> that works with any JDBC supported database.
 * See the JDBCRealm.howto for more details on how to set up the database and
 * for configuration options.
 *
 * TODO:
 *    - Work on authentication with non-plaintext passwords
 *    - Make sure no bad chars can get in and trick the auth and hasrole
 *
 * @author Craig R. McClanahan
 * @author Carson McDonald
 */

public class JDBCRealm 
    extends RealmBase {


    // ----------------------------------------------------- Instance Variables


    /**
     * The connection URL to use when trying to connect to the databse
     */
    private String connectionURL = null;


    /**
     * The Container with which this Realm is associated.
     */
    private Container container = null;


    /**
     * The connection to the database.
     */
    private Connection dbConnection = null;


    /**
     * The debugging detail level for this component.
     */
    private int debug = 0;


    /**
     * The JDBC driver to use.
     */
    private String driverName = null;


    /**
     * Descriptive information about this Realm implementation.
     */
    private static final String info = "org.apache.catalina.realm.JDBCRealm/1.0";


    /**
     * The lifecycle event support for this component.
     */
    private LifecycleSupport lifecycle = new LifecycleSupport(this);


    /**
     * The PreparedStatement to use for authenticating users.
     */
    private PreparedStatement preparedAuthenticate = null;


    /**
     * The PreparedStatement to use for identifying the roles for
     * a specified user.
     */
    private PreparedStatement preparedRoles = null;


    /**
     * The column in the user role table that names a role
     */
    private String roleNameCol = null;


    /**
     * The string manager for this package.
     */
    private static final StringManager sm =
	StringManager.getManager(Constants.Package);


    /**
     * Has this component been started?
     */
    private boolean started = false;


    /**
     * The property change support for this component.
     */
    private PropertyChangeSupport support = new PropertyChangeSupport(this);


    /**
     * The column in the user table that holds the user's credintials
     */
    private String userCredCol = null;


    /**
     * The column in the user table that holds the user's name
     */
    private String userNameCol = null;


    /**
     * The table that holds the relation between user's and roles
     */
    private String userRoleTable = null;


    /**
     * The table that holds user data.
     */
    private String userTable = null;

    /**
     * The connection URL to use when trying to connect to the databse
     */
    private String connectionName = null;

    /**
     * The connection URL to use when trying to connect to the databse
     */
    private String connectionPassword = null;

     /**
     *
     * Digest algorithm used in passwords thit is same values
     * accepted by MessageDigest  for algorithm
     * plus "No" ( no encode ) that is the default
     *
     */

    private String digest="";

   // ------------------------------------------------------------- Properties


    /**
     * Set the URL to use to connect to the database.
     *
     * @param connectionURL The new connection URL
     */
    public void setConnectionURL( String connectionURL ) {
      this.connectionURL = connectionURL;
    }


    /**
     * Return the Container with which this Realm has been associated.
     */
    public Container getContainer() {
	return (container);
    }


    /**
     * Set the Container with which this Realm has been associated.
     *
     * @param container The associated Container
     */
    public void setContainer(Container container) {
	Container oldContainer = this.container;
	this.container = container;
	support.firePropertyChange("container", oldContainer, this.container);
    }


    /**
     * Return the debugging detail level for this component.
     */
    public int getDebug() {
	return (this.debug);
    }


    /**
     * Set the debugging detail level for this component.
     *
     * @param debug The new debugging detail level
     */
    public void setDebug(int debug) {
	this.debug = debug;
    }


    /**
     * Set the JDBC driver that will be used.
     *
     * @param driverName The driver name
     */
    public void setDriverName( String driverName ) {
      this.driverName = driverName;
    }


    /**
     * Return descriptive information about this Realm implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {
	return (info);
    }


    /**
     * Set the column in the user role table that names a role
     *
     * @param userRoleNameCol The column name
     */
    public void setRoleNameCol( String roleNameCol ) {
        this.roleNameCol = roleNameCol;
    }


    /**
     * Set the column in the user table that holds the user's credintials
     *
     * @param userCredCol The column name
     */
    public void setUserCredCol( String userCredCol ) {
       this.userCredCol = userCredCol;
    }


    /**
     * Set the column in the user table that holds the user's name
     *
     * @param userNameCol The column name
     */
    public void setUserNameCol( String userNameCol ) {
       this.userNameCol = userNameCol;
    }


    /**
     * Set the table that holds the relation between user's and roles
     *
     * @param userRoleTable The table name
     */
    public void setUserRoleTable( String userRoleTable ) {
        this.userRoleTable = userRoleTable;
    }


    /**
     * Set the table that holds user data.
     *
     * @param userTable The table name
     */
    public void setUserTable( String userTable ) {
      this.userTable = userTable;
    }

    /**
     * Set the name to use to connect to the database.
     *
     * @param connectionName User name
     */
    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }

    /**
     * Set the password to use to connect to the database.
     *
     * @param connectionPassword User password
     */
    public void setConnectionPassword(String connectionPassword) {
        this.connectionPassword = connectionPassword;
    }


    /**
     * Gets the digest algorithm  used for credentials in the database
     * could be the same that MessageDigest accepts vor algorithm
     * and "No" that is the Default
     *
     */

    public String getDigest() {
        return digest;
    }

    /**
     * Gets the digest algorithm  used for credentials in the database
     * could be the same that MessageDigest accepts vor algorithm
     * and "No" that is the Default
     *
     * @param algorithm the Encode type
     */

    public void setDigest(String algorithm) {
        digest = algorithm;
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Add a property change listener to this component.
     *
     * @param listener The listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
	support.addPropertyChangeListener(listener);
    }


    /**
     *
     * Return the Principal associated with the specified username and
     * credentials, if there is one; otherwise return <code>null</code>.
     *
     * If there are any errors with the JDBC connection, executing 
     * the query or anything we return null (don't authenticate). This
     * event is also logged. 
     *
     * If there is some SQL exception the connection is set to null. 
     * This will allow a retry on the next auth attempt. This might not
     * be the best thing to do but it will keep Catalina from needing a
     * restart if the database goes down.
     *
     * @param username Username of the Principal to look up
     * @param credentials Password or other credentials to use in
     *  authenticating this username
     */
    public synchronized Principal authenticate(String username,
					       String credentials) {

        try {

	    // Establish the database connection if necessary
	    if ((dbConnection == null) || dbConnection.isClosed()) {
		log(sm.getString("jdbcRealm.authDBClosed"));
                if ((connectionName == null || connectionName.equals("")) &&
                    (connectionPassword == null || connectionPassword.equals(""))) {
                    dbConnection = DriverManager.getConnection(connectionURL);
                } else {
                    dbConnection = DriverManager.getConnection(connectionURL,
                                                               connectionName,
                                                               connectionPassword);
                }
		if( (dbConnection == null) || dbConnection.isClosed() ) {
		    log(sm.getString("jdbcRealm.authDBReOpenFail"));
		    return null;
		}
// XXX Commented it gives problems on Oracle 8i Drivers
//		dbConnection.setReadOnly(true);
	    }

	    // Create the authentication search prepared statement if necessary
	    if (preparedAuthenticate == null) {
		String sql = "SELECT " + userCredCol + " FROM " + userTable +
		    " WHERE " + userNameCol + " = ?";
		if (debug >= 1)
		    log("JDBCRealm.authenticate: " + sql);
		preparedAuthenticate = dbConnection.prepareStatement(sql);
	    }

	    // Create the roles search prepared statement if necessary
	    if (preparedRoles == null) {
		String sql = "SELECT " + roleNameCol + " FROM " +
		    userRoleTable + " WHERE " + userNameCol + " = ?";
		if (debug >= 1)
		    log("JDBCRealm.roles: " + sql);
		preparedRoles = dbConnection.prepareStatement(sql);
	    }

	    // Perform the authentication search
	    preparedAuthenticate.setString(1, username);
	    ResultSet rs1 = preparedAuthenticate.executeQuery();
	    boolean found = false;
	    if (rs1.next()) {
                if( digest.equals("") || digest.equalsIgnoreCase("No")){
                    if(credentials.equals(rs1.getString(1))) {
                        if(debug >= 2)
                            log(sm.getString("jdbcRealm.authenticateSuccess",
                                             username));
                        found = true;
                    }else if (credentials.equals(
                                Digest(rs1.getString(1),digest))) {
                        if (debug >= 2)
                            log(sm.getString("jdbcRealm.authenticateSuccess",
                                     username));
                        found = true;
                    }
                }
	    }
	    rs1.close();
	    if (!found) {
		if (debug >= 2)
		    log(sm.getString("jdbcRealm.authenticateFailure",
				     username));
		return (null);
	    }

	    // Prepare and return a suitable Principal to be returned
	    JDBCRealmPrincipal principal =
		new JDBCRealmPrincipal(username, credentials);
	    preparedRoles.setString(1, username);
	    ResultSet rs2 = preparedRoles.executeQuery();
	    while (rs2.next()) {
		principal.addRole(rs2.getString(1));
	    }
	    rs2.close();
	    return (principal);

	} catch( SQLException ex ) {

	    // Log the problem for posterity
	    log("JDBCRealm.authenticate", ex);

	    // Clean up the JDBC objects so that they get recreated next time
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

	    // Return "not authenticated" for this request
	    return (null);

	}

    }


    /**
     *
     * Return the Principal associated with the specified username and
     * credentials, if there is one; otherwise return <code>null</code>.
     *
     * See other authenticate for more details.
     * 
     * @param username Username of the Principal to look up
     * @param credentials Password or other credentials to use in
     *  authenticating this username
     */
    public Principal authenticate(String username, byte[] credentials) {
	return (authenticate(username, credentials.toString()));
    }


    /**
     *
     * Return <code>true</code> if the specified Principal has the specified
     * security role, within the context of this Realm; otherwise return
     * <code>false</code>.
     *
     * If there are any errors with the JDBC connection, executing 
     * the query or anything we return false (not in role set). This
     * event is also logged. 
     *
     * If there is some SQL exception the connection is set to null. 
     * This will allow a retry on the next auth attempt. This might not
     * be the best thing to do but it will keep Catalina from needing a
     * restart if the database goes down.
     *
     * @param principal Principal for whom the role is to be checked
     * @param role Security role to be checked
     */
    public boolean hasRole(Principal principal, String role) {
        String username = principal.getName();
     
	// Is the specified Principal one that we created?
	if (!(principal instanceof JDBCRealmPrincipal))
	    return (false);

	// Ask this Principal for the answer
	return (((JDBCRealmPrincipal) principal).hasRole(role));

    }


    /**
     * Remove a property change listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
	support.removePropertyChangeListener(listener);
    }


    // -------------------------------------------------------- Package Methods


    // ------------------------------------------------------ Protected Methods


    /**
     * Return the password associated with the given principal's user name.
     */
    protected String getPassword(String username) {
        return (null);
    }


    /**
     * Return the Principal associated with the given user name.
     */
    protected Principal getPrincipal(String username) {
	return (null);
    }


    // -------------------------------------------------------- Private Methods


    /**
     * Log a message on the Logger associated with our Container (if any)
     *
     * @param message Message to be logged
     */
    private void log(String message) {
	Logger logger = null;

	if (container != null) {
	    logger = container.getLogger();
        }

	if (logger != null) {
	    logger.log("JDBCRealm[" + container.getName() + "]: " + message);
        } else {
	    String containerName = null;
	    if (container != null) {
		containerName = container.getName();
            }
	    System.out.println("JDBCRealm[" + containerName + "]: " + message);
	}
    }


    /**
     * Log a message on the Logger associated with our Container (if any)
     *
     * @param message Message to be logged
     * @param throwable Associated exception
     */
    private void log(String message, Throwable throwable) {
	Logger logger = null;

	if (container != null) {
	    logger = container.getLogger();
        }

	if (logger != null) {
	    logger.log("JDBCRealm[" + container.getName() + "] " + message, throwable);
        } else {
	    String containerName = null;
	    if (container != null) {
		containerName = container.getName();
            }
	    System.out.println("JDBCRealm[" + containerName + "]: " + message);
	    System.out.println("" + throwable);
	    throwable.printStackTrace(System.out);
	}
    }


    // ------------------------------------------------------ Lifecycle Methods


    /**
     * Add a lifecycle event listener to this component.
     *
     * @param listener The listener to add
     */
    public void addLifecycleListener(LifecycleListener listener) {
	lifecycle.addLifecycleListener(listener);
    }


    /**
     * Remove a lifecycle event listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removeLifecycleListener(LifecycleListener listener) {
	lifecycle.removeLifecycleListener(listener);
    }


    /**
     *
     * Prepare for active use of the public methods of this Component.
     *
     * The DriverManager is initiated here. The initial database connection
     * is also formed.
     *
     * @exception IllegalStateException if this component has already been
     *  started
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents it from being started
     */
    public synchronized void start() throws LifecycleException {
	// Validate and update our current component state
	if (started) {
	    throw new LifecycleException (sm.getString("jdbcRealm.alreadyStarted"));
        }

	lifecycle.fireLifecycleEvent(START_EVENT, null);
	started = true;

        try {
            Class.forName(driverName);
            if ((connectionName == null || connectionName.equals("")) &&
                (connectionPassword == null || connectionPassword.equals(""))){
                        dbConnection = DriverManager.getConnection(connectionURL);
            } else {
                        dbConnection =DriverManager.getConnection(connectionURL,
                            connectionName,
                            connectionPassword);
            }

        } catch( ClassNotFoundException ex ) {
	  throw new LifecycleException("JDBCRealm.start.readXml: " + ex, ex);
        }
        catch( SQLException ex ) {
	  throw new LifecycleException("JDBCRealm.start.readXml: " + ex, ex);
        }
    }


    /**
     *
     * Gracefully shut down active use of the public methods of this Component.
     *
     * If there is a connection it is closed.
     *
     * @exception IllegalStateException if this component has not been started
     * @exception LifecycleException if this component detects a fatal error
     *  that needs to be reported
     */
    public synchronized void stop() throws LifecycleException {
	// Validate and update our current component state
	if (!started) {
	    throw new LifecycleException (sm.getString("jdbcRealm.notStarted"));
        }
	lifecycle.fireLifecycleEvent(STOP_EVENT, null);
	started = false;

	// Close any open DB connection
        if( dbConnection != null ) {
          try {
            dbConnection.close();
          }
          catch( SQLException ex ) {
            // XXX: Don't know if this is the best thing to do. Maybe just ignore.
	    throw new LifecycleException (sm.getString("jdbcRealm.notStarted"));
          }
        }
    }

    /**
     * Digest password using the algorithm especificied and
     * convert the result to a corresponding hex string.
     * If exception, the plain credentials string is returned
     *
     * @param credentials Password or other credentials to use in
     *  authenticating this username
     *
     * @param algorithm Algorithm used to do th digest
     *
     */
    final public static String Digest(String credentials,String algorithm) {
        try {
            // Obtain a new message digest with "digest" encryption
            MessageDigest md = (MessageDigest)MessageDigest.getInstance(algorithm).clone();
            // encode the credentials
            md.update( credentials.getBytes() );
            // obtain the byte array from the digest
            byte[] dig = md.digest();
            // convert the byte array to hex string
            Base64 enc=new Base64();
            return new String(enc.encode(HexUtils.convert(dig).getBytes()));
//            return HexUtils.convert(dig);

        } catch( Exception ex ) {
                ex.printStackTrace();
                return credentials;
        }
    }

    public static void main(String args[] ) {
        if (args.length >= 2) {
            if( args[0].equalsIgnoreCase("-a")){
                for( int i=2; i < args.length ; i++){
                    System.out.print(args[i]+":");
                    System.out.println(Digest(args[i],args[1]));
                }
            }
        }

    }
}


/**
 * Private class representing an individual user's Principal object.
 */
final class JDBCRealmPrincipal implements Principal {


    /**
     * The password for this Principal.
     */
    private String password = null;


    /**
     * The role names possessed by this Principal.
     */
    private String roles[] = new String[0];


    /**
     * The username for this Principal.
     */
    private String username = null;


    /**
     * Construct a new JDBCRealmPrincipal instance.
     *
     * @param username The username for this Principal
     * @param password The password for this Principal
     */
    public JDBCRealmPrincipal(String username, String password) {
	this.username = username;
	this.password = password;
    }


    /**
     * Add a new role assigned to this Principal.
     * @param role The new role to be assigned
     */
    void addRole(String role) {
	if (role == null)
	    return;
	for (int i = 0; i < roles.length; i++) {
	    if (role.equals(roles[i]))
		return;
	}
	String results[] = new String[roles.length + 1];
	for (int i = 0; i < roles.length; i++)
	    results[i] = roles[i];
	results[roles.length] = role;
        roles = results;
    }


    /**
     * Return the name of this Principal.
     */
    public String getName() {
	return (username);
    }


    /**
     * Return the password of this Principal.
     */
    public String getPassword() {
	return (password);
    }


    /**
     * Does this user have the specified role assigned?
     * @param role The role to be checked
     */
    boolean hasRole(String role) {
	if (role == null)
	    return (false);
	for (int i = 0; i < roles.length; i++) {
	    if (role.equals(roles[i]))
		return (true);
	}
	return (false);
    }

}
