/*
 * $Header$
 * $Revision$
 * $Date$
 *
 * ====================================================================
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


package org.apache.catalina.users;


import java.util.ArrayList;
import java.util.Iterator;
import org.apache.catalina.Group;
import org.apache.catalina.User;


/**
 * <p>Convenience base class for {@link User} implementations.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 * @since 4.1
 */

public abstract class AbstractUser implements User {


    // ----------------------------------------------------- Instance Variables


    /**
     * The full name of this user.
     */
    protected String fullName = null;


    /**
     * The logon password of this user.
     */
    protected String password = null;


    /**
     * The set of security roles associated with this user.
     */
    protected ArrayList roles = new ArrayList();


    /**
     * The logon username of this user.
     */
    protected String username = null;


    // ------------------------------------------------------------- Properties


    /**
     * Return the full name of this user.
     */
    public String getFullName() {

        return (this.fullName);

    }


    /**
     * Set the full name of this user.
     *
     * @param fullName The new full name
     */
    public void setFullName(String fullName) {

        this.fullName = fullName;

    }


    /**
     * Return the set of {@link Group}s to which this user belongs.
     */
    public abstract Iterator getGroups();


    /**
     * Return the logon password of this user, optionally prefixed with the
     * identifier of an encoding scheme surrounded by curly braces, such as
     * <code>{md5}xxxxx</code>.
     */
    public String getPassword() {

        return (this.password);

    }


    /**
     * Set the logon password of this user, optionally prefixed with the
     * identifier of an encoding scheme surrounded by curly braces, such as
     * <code>{md5}xxxxx</code>.
     *
     * @param password The new logon password
     */
    public void setPassword(String password) {

        this.password = password;

    }


    /**
     * Return the set of security roles assigned specifically to this user,
     * as Strings.
     */
    public Iterator getRoles() {

        synchronized (roles) {
            return (roles.iterator());
        }

    }


    /**
     * Return the logon username of this user, which must be unique
     * within the scope of a {@link UserDatabase}.
     */
    public String getUsername() {

        return (this.username);

    }


    /**
     * Set the logon username of this user, which must be unique within
     * the scope of a {@link UserDatabase}.
     *
     * @param username The new logon username
     */
    public void setUsername(String username) {

        this.username = username;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Add a new {@link Group} to those this user belongs to.
     *
     * @param group The new group
     */
    public abstract void addGroup(Group group);


    /**
     * Add a new security role to those assigned specifically to this user.
     *
     * @param role The new role
     */
    public void addRole(String role) {

        synchronized (roles) {
            if (!roles.contains(role)) {
                roles.add(role);
            }
        }

    }


    /**
     * Is this user in the specified group?
     *
     * @param group The group to check
     */
    public abstract boolean isInGroup(Group group);


    /**
     * Is this user specifically assigned the specified role?  This method
     * does <strong>NOT</strong> check for roles inherited based on group
     * membership.
     *
     * @param role The role to check
     */
    public boolean isInRole(String role) {

        synchronized (roles) {
            return (roles.contains(role));
        }

    }


    /**
     * Remove a {@link Group} from those this user belongs to.
     *
     * @param group The old group
     */
    public abstract void removeGroup(Group group);


    /**
     * Remove a security role from those assigned to this user.
     *
     * @param role The old role
     */
    public void removeRole(String role) {

        synchronized (roles) {
            roles.remove(role);
        }

    }


}
