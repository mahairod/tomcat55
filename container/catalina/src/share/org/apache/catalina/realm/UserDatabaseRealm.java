/*
 * Copyright 2002,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.catalina.realm;


import java.security.Principal;
import java.util.Iterator;

import javax.naming.Context;

import org.apache.catalina.Group;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Role;
import org.apache.catalina.ServerFactory;
import org.apache.catalina.User;
import org.apache.catalina.UserDatabase;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.util.StringManager;


/**
 * <p>Implementation of {@link org.apache.catalina.Realm} that is based on an implementation of
 * {@link UserDatabase} made available through the global JNDI resources
 * configured for this instance of Catalina.  Set the <code>resourceName</code>
 * parameter to the global JNDI resources name for the configured instance
 * of <code>UserDatabase</code> that we should consult.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 * @since 4.1
 */

public class UserDatabaseRealm
    extends RealmBase {


    // ----------------------------------------------------- Instance Variables


    /**
     * The <code>UserDatabase</code> we will use to authenticate users
     * and identify associated roles.
     */
    protected UserDatabase database = null;


    /**
     * Descriptive information about this Realm implementation.
     */
    protected final String info =
        "org.apache.catalina.realm.UserDatabaseRealm/1.0";


    /**
     * Descriptive information about this Realm implementation.
     */
    protected static final String name = "UserDatabaseRealm";


    /**
     * The global JNDI name of the <code>UserDatabase</code> resource
     * we will be utilizing.
     */
    protected String resourceName = "UserDatabase";


    /**
     * The string manager for this package.
     */
    private static StringManager sm =
        StringManager.getManager(Constants.Package);


    // ------------------------------------------------------------- Properties


    /**
     * Return descriptive information about this Realm implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

        return info;

    }


    /**
     * Return the global JNDI name of the <code>UserDatabase</code> resource
     * we will be using.
     */
    public String getResourceName() {

        return resourceName;

    }


    /**
     * Set the global JNDI name of the <code>UserDatabase</code> resource
     * we will be using.
     *
     * @param resourceName The new global JNDI name
     */
    public void setResourceName(String resourceName) {

        this.resourceName = resourceName;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Return <code>true</code> if the specified Principal has the specified
     * security role, within the context of this Realm; otherwise return
     * <code>false</code>. This implementation returns <code>true</code>
     * if the <code>User</code> has the role, or if any <code>Group</code>
     * that the <code>User</code> is a member of has the role. 
     *
     * @param principal Principal for whom the role is to be checked
     * @param role Security role to be checked
     */
    public boolean hasRole(Principal principal, String role) {
        if(! (principal instanceof User) ) {
            //Play nice with SSO and mixed Realms
            return super.hasRole(principal, role);
        }
        if("*".equals(role)) {
            return true;
        } else if(role == null) {
            return false;
        }
        User user = (User)principal;
        Role dbrole = database.findRole(role);
        if(dbrole == null) {
            return false; 
        }
        if(user.isInRole(dbrole)) {
            return true;
        }
        Iterator groups = user.getGroups();
        while(groups.hasNext()) {
            Group group = (Group)groups.next();
            if(group.isInRole(dbrole)) {
                return true;
            }
        }
        return false;
    }
		
    // ------------------------------------------------------ Protected Methods


    /**
     * Return a short name for this Realm implementation.
     */
    protected String getName() {

        return (name);

    }


    /**
     * Return the password associated with the given principal's user name.
     */
    protected String getPassword(String username) {

        User user = database.findUser(username);

        if (user == null) {
            return null;
        } 

        return (user.getPassword());

    }


    /**
     * Return the Principal associated with the given user name.
     */
    protected Principal getPrincipal(String username) {

        return (database.findUser(username));

    }


    // ------------------------------------------------------ Lifecycle Methods


    /**
     * Prepare for active use of the public methods of this Component.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents it from being started
     */
    public synchronized void start() throws LifecycleException {

        try {
            StandardServer server = (StandardServer) ServerFactory.getServer();
            Context context = server.getGlobalNamingContext();
            database = (UserDatabase) context.lookup(resourceName);
        } catch (Throwable e) {
            container.getLogger().error(sm.getString("userDatabaseRealm.lookup", resourceName), e);
            database = null;
        }
        if (database == null) {
            throw new LifecycleException
                (sm.getString("userDatabaseRealm.noDatabase", resourceName));
        }

        // Perform normal superclass initialization
        super.start();

    }


    /**
     * Gracefully shut down active use of the public methods of this Component.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that needs to be reported
     */
    public synchronized void stop() throws LifecycleException {

        // Perform normal superclass finalization
        super.stop();

        // Release reference to our user database
        database = null;

    }


}
