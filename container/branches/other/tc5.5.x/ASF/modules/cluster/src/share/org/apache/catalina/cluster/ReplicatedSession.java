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

package org.apache.catalina.cluster;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Principal;

import org.apache.catalina.Manager;
import org.apache.catalina.SessionListener;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.catalina.session.StandardSession;
import org.apache.catalina.util.StringManager;

/**
 * Title:        Tomcat Session Replication for Tomcat 4.0 <BR>
 * Description:  A very simple straight forward implementation of 
 *               session replication of servers in a cluster.<BR>
 *               This session replication is implemented "live". By live
 *               I mean, when a session attribute is added into a session on 
 *               Node A a message is broadcasted to other messages 
 *               and setAttribute is called on the replicated sessions.<BR>
 *               A full description of this implementation can be found under
 *               <href="http://www.filip.net/tomcat/">Filip's Tomcat 
 *               Page</a><BR>            
 *               
 * Description:<BR>
 * The ReplicatedSession class is a simple extension of the StandardSession 
 * class. It overrides a few methods (setAttribute, removeAttribute, expire, 
 * access) and has hooks into the JGManager to broadcast and receive events 
 * from the cluster.<BR>
 * This class inherits the readObjectData and writeObject data methods from 
 * the StandardSession and does not contain any serializable elements 
 * in addition to the inherited ones from the StandardSession.
 * 
 * @author Filip Hanik
 */
public class ReplicatedSession
    extends StandardSession {


    // ----------------------------------------------------- Instance Variables


    /**
     * The string manager for this package.
     */
    protected StringManager sm = StringManager.getManager(Constants.Package);


    private transient Manager manager = null;


    // ------------------------------------------------------------ Constructor


    public ReplicatedSession(Manager manager) {
        super(manager);
        this.manager = manager;
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Update the accessed time information for this session.  This method
     * should be called by the context when a request comes in for a particular
     * session, even if the application does not reference it.
     * 
     * @param notify - if true the other cluster nodes will be notified 
     */
    public void access(boolean notify) {

        super.access();
        // Notify javagroups that session has been accessed
        if (notify) {
            SessionMessage msg = new SessionMessage
                (manager.getContainer().getName(), 
                 SessionMessage.EVT_SESSION_ACCESSED,
                 null, this.getId(), null, null, null);
            sendMessage(msg);
        }

    }


    /**
     * Update the accessed time information for this session.  This method
     * should be called by the context when a request comes in for a particular
     * session, even if the application does not reference it.
     */
    public void access() {
        access(true);
    }


    /**
     * Perform the internal processing required to invalidate this session,
     * without triggering an exception if the session has already expired.
     * 
     * @param notify - the inherited notify from StandardSession
     * @param jgnotify - if true other nodes in the cluster will be notified
     */
    public void expire(boolean notify, boolean jgnotify) {
        
        String id = getId();
        super.expire();
        // Notify javagroups about the expiration
        if (jgnotify) {
            int event = notify ? SessionMessage.EVT_SESSION_EXPIRED_WNOTIFY 
                : SessionMessage.EVT_SESSION_EXPIRED_WONOTIFY; 
            SessionMessage msg = new SessionMessage
                (manager.getContainer().getName(), event,
                 null, id, null, null, null);
            sendMessage(msg);
        }

    }


    /**
     * Perform the internal processing required to invalidate this session,
     * without triggering an exception if the session has already expired.
     */
    public void expire() {
        expire(true,true);
    }


    /**
     * Perform the internal processing required to invalidate this session,
     * without triggering an exception if the session has already expired.
     */
    public void expire(boolean notify) {
        expire(notify,true);
    }


    /**
     * Remove the object bound with the specified name from this session.  If
     * the session does not have an object bound with this name, this method
     * does nothing.
     * <p>
     * After this method executes, and if the object implements
     * <code>HttpSessionBindingListener</code>, the container calls
     * <code>valueUnbound()</code> on the object.
     *
     * @param name Name of the object to remove from this session.
     * @param notify - notify the listeners in the session
     * @param jgnotify - notify the other nodes in the cluster  
     *
     * @exception IllegalStateException if this method is called on an
     *  invalidated session
     */
    public void removeAttribute(String name, boolean notify, 
                                boolean jgnotify) {

        super.removeAttribute(name);
        if (jgnotify) {
            SessionMessage msg = new SessionMessage
                (manager.getContainer().getName(), 
                 notify ? SessionMessage.EVT_ATTRIBUTE_REMOVED_WNOTIFY 
                 : SessionMessage.EVT_ATTRIBUTE_REMOVED_WONOTIFY,
                 null, getId(), name, null, null);
            sendMessage(msg);
        }

    }


    /**
     * See parent description,
     * plus we also notify other nodes in the cluster
     */
    public void removeAttribute(String name, boolean notify) {
        removeAttribute(name,notify,true);
    }


    /**
     * Bind an object to this session, using the specified name.  If an object
     * of the same name is already bound to this session, the object is
     * replaced.
     * <p>
     * After this method executes, and if the object implements
     * <code>HttpSessionBindingListener</code>, the container calls
     * <code>valueBound()</code> on the object.
     *
     * @param name Name to which the object is bound, cannot be null
     * @param value Object to be bound, cannot be null
     * @param notify true to notify the other nodes in the cluster
     * @exception IllegalArgumentException if an attempt is made to add a
     *  non-serializable object in an environment marked distributable.
     * @exception IllegalStateException if this method is called on an
     *  invalidated session
     * @excpetion IllegalArgumentException if the value is not serializable
     */
    public void setAttribute(String name, Object value, boolean notify) {

        if (!(value instanceof java.io.Serializable)) {
            throw new IllegalArgumentException
                (sm.getString("clusterSession.attributeNotSerializable",
                              name));
        }
        super.setAttribute(name,value);
        // Notify javagroups
        if (notify) {
            SessionMessage msg = new SessionMessage
                (manager.getContainer().getName(), 
                 SessionMessage.EVT_ATTRIBUTE_ADDED,
                 null, getId(), name, value, null);
            sendMessage(msg);
        }

    }


    /**
     * Sets an attribute and notifies the other nodes in the cluster
     */
    public void setAttribute(String name, Object value) {
        setAttribute(name, value, true);
    }


    /**
     * Sets the manager for this session
     * @param mgr - the servers JGManager 
     */
    public void setManager(JGManager mgr) {
        manager = mgr;
        super.setManager(mgr);
    }


    /**
     * Set the authenticated Principal that is associated with this Session.
     * This provides an <code>Authenticator</code> with a means to cache a
     * previously authenticated Principal, and avoid potentially expensive
     * <code>Realm.authenticate()</code> calls on every request.
     *
     * @param principal The new Principal, or <code>null</code> if none
     * @param jgnotify notify the other nodes in the cluster? (true/false)
     */
    public void setPrincipal(Principal principal) {
        setPrincipal(principal, true);
    }


    public void setPrincipal(Principal principal, boolean jgnotify) {
        super.setPrincipal(principal);
        if (jgnotify) {
            SessionMessage msg = new SessionMessage
                (manager.getContainer().getName(), 
                 SessionMessage.EVT_SET_USER_PRINCIPAL,
                 null, getId(), null, null,
                 SerializablePrincipal.createPrincipal
                 ((GenericPrincipal) principal));
            sendMessage(msg);
        }
    }


    /**
     * Remove any object bound to the specified name in the internal notes
     * for this session.
     *
     * @param name Name of the note to be removed
     */
    public void removeNote(String name, boolean jgnotify) {
        super.removeNote(name);
        if (jgnotify) {
            SessionMessage msg = new SessionMessage
                (manager.getContainer().getName(), 
                 SessionMessage.EVT_REMOVE_SESSION_NOTE,
                 null, getId(), name, null, null);
            sendMessage(msg);
        }
    }


    public void removeNote(String name) {
        // Disable replication of notes
        removeNote(name, false);
    }


    /**
     * Bind an object to a specified name in the internal notes associated
     * with this session, replacing any existing binding for this name.
     *
     * @param name Name to which the object should be bound
     * @param value Object to be bound to the specified name
     */
    public void setNote(String name, Object value, boolean jgnotify) {
        
        super.setNote(name,value);
        if (jgnotify) {
            SessionMessage msg = new SessionMessage
                (manager.getContainer().getName(), 
                 SessionMessage.EVT_SET_SESSION_NOTE,
                 null, getId(), name, value, null);
            sendMessage(msg);
        }

    }


    public void setNote(String name, Object value) {
        //for now disable replication of notes
        setNote(name, value, false);
    }


    // -------------------------------------------------------- Private Methods


    /**
     * Uses the manager to send a message to the other nodes.
     */
    private void sendMessage(SessionMessage msg) {
        if ((this.manager != null) && (this.manager instanceof JGManager)) {
            JGManager transport = (JGManager) manager;
            transport.sendSessionEvent(msg);
        } else {
            log(sm.getString("clusterSession.invalidManager"));
        }
    }


}
