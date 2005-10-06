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

import org.apache.catalina.LifecycleException;
import org.apache.catalina.Session;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.catalina.session.StandardManager;
import org.apache.catalina.session.StandardSession;
import org.apache.catalina.util.CustomObjectInputStream;

import org.javagroups.stack.IpAddress;

/**
 * Title:        Tomcat Session Replication for Tomcat<BR>
 * Description:  A very simple straight forward implementation of 
 *               session replication of servers in a cluster.<BR>
 *               This session replication is implemented "live". By live
 *               I mean, when a session attribute is added into a session on 
 *               Node A a message is broadcasted to other messages 
 *               and setAttribute is called on the replicated sessions.<BR>
 *               A full description of this implementation can be found under
 *               <href="http://www.filip.net/tomcat/">Filip's Tomcat Page</a>
 *               <BR>
 * Company:      www.filip.net<br>
 * Description: The JGManager is a session manager 
 * that replicated session information in memory. It uses 
 * <a href="www.javagroups.com">JavaGroups</a> as a communication protocol 
 * to ensure guaranteed and ordered message delivery.
 * JavaGroups also provides a very flexible protocol stack to ensure that the 
 * replication can be used in any environment.
 * <BR><BR>
 * The JGManager extends the StandardManager hence it allows 
 * for us to inherit all the basic session management features like 
 * expiration, session listeners, etc.
 * <BR><BR>
 * To communicate with other nodes in the cluster, 
 * the JGManager sends out 7 different type of multicast 
 * messages all defined in the SessionMessage class.<BR>
 * When a session is replicated (not an attribute added/removed) the session 
 * is serialized into a byte array using the StandardSession.readObjectData, 
 * StandardSession.writeObjectData methods.
 * 
 * @author  <a href="mailto:mail@filip.net">Filip Hanik</a>
 */

public class JGManager 
    extends StandardManager {


    private static org.apache.commons.logging.Log log =
        org.apache.commons.logging.LogFactory.getLog( JGManager.class );


    // ----------------------------------------------------- Instance Variables


    /**
     * Associated JavaGroups cluster.
     */
    protected JGCluster cluster = null;


    // --------------------------------------------------------- Public Methods


    /**
     * Set JavaGroups cluster.
     */
    public void setCluster(JGCluster cluster) {
        if (this.cluster != null) {
            return;
        }
        this.cluster = cluster;
    }


    /**
     * Construct and return a new session object, based on the default
     * settings specified by this Manager's properties.  The session
     * id will be assigned by this method, and available via the getId()
     * method of the returned session.  If a new session cannot be created
     * for any reason, return <code>null</code>.
     *
     * @exception IllegalStateException if a new session cannot be
     *  instantiated for any reason
     */
    public Session createSession() {
        //create a session and notify the other nodes in the cluster
        Session session = createSession(true);
        add(session); // ?
        return session;
    }


    public void start() throws LifecycleException {

        super.start();

        try {
            SessionMessage msg = new SessionMessage
                (container.getName(), SessionMessage.EVT_GET_ALL_SESSIONS, 
                 null, null, null, null, null);
            sendSessionEvent(msg);
        } catch (Exception x) {
            log.error(sm.getString("jgManager.startFail"), x);
        }

    }


    public void stop() throws LifecycleException {
        super.stop();
    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Get new session class to be used in the doLoad() method.
     */
    protected StandardSession getNewSession() {
        return new ReplicatedSession(this);
    }


    /**
     * Creates a HTTP session.<br>
     * Most of the code in here is copied from the StandardManager.
     * This is not pretty, yeah I know, but it was necessary since the 
     * StandardManager had hard coded the session instantiation to the a
     * StandardSession, when we actually want to instantiate 
     * a ReplicatedSession<BR>
     * If the call comes from the Tomcat servlet engine, a SessionMessage 
     * goes out to the other nodes in the cluster that this session 
     * has been created.
     * 
     * @param notify if set to true the other nodes in the cluster will 
     * be notified. This flag is needed so that we can create a session 
     * before we deserialize a replicated one
     * @see ReplicatedSession
     */
    protected Session createSession(boolean notify) {

        Session session = super.createSession();

        if (notify) {
            log.info(sm.getString
                     ("jgManager.sessionCreated", session.getId()));
            // Notify javagroups
            SessionMessage msg = new SessionMessage
                (container.getName(), SessionMessage.EVT_SESSION_CREATED,
                 writeSession(session), session.getId(), null, null, null);
            sendSessionEvent(msg);
        }

        return (session);

    }


    /**
     * Serialize a session into a byte array.<BR>
     * This method simple calls the writeObjectData method on the session
     * and returns the byte data from that call.
     * 
     * @param session - the session to be serialized
     * @return a byte array containing the session data, null if the 
     * serialization failed 
     */
    protected byte[] writeSession(Session session) {
        ClassLoader oldCtxClassLoader =
            Thread.currentThread().getContextClassLoader();
        try {
            java.io.ByteArrayOutputStream session_data = 
                new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream session_out = 
                new java.io.ObjectOutputStream(session_data);
            ClassLoader classLoader = container.getLoader().getClassLoader();
            Thread.currentThread().setContextClassLoader(classLoader);
            ((ReplicatedSession)session).writeObjectData(session_out);
            session_out.flush();
            session_out.close();
            return session_data.toByteArray();
        } catch ( Exception x ) {
            log.warn(sm.getString("jgManager.sessionSerializationFail"), x);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCtxClassLoader);
        }
        return null;
    }


    /**
     * Reinstantiates a serialized session from the data passed in.
     * This will first call createSession() so that we get a fresh instance 
     * with all the managers set and all the transient fields validated.
     * Then it calls Session.readObjectData(byte[]) to deserialize the object.
     * 
     * @param data - a byte array containing session data
     * @return a valid Session object, null if an error occurs
     */
    protected Session readSession(byte[] data) {
        ClassLoader oldCtxClassLoader =
            Thread.currentThread().getContextClassLoader();
        try {
            java.io.ByteArrayInputStream session_data = 
                new java.io.ByteArrayInputStream(data);
            ReplicationStream session_in = new ReplicationStream
                (session_data,container.getLoader().getClassLoader());
            Session session = createSession(false);
            ClassLoader classLoader = container.getLoader().getClassLoader();
            Thread.currentThread().setContextClassLoader(classLoader);
            ((ReplicatedSession)session).readObjectData(session_in);
            return session;
        } catch (Exception x) {
            log.warn(sm.getString("jgManager.sessionDeserializationFail"), x);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCtxClassLoader);
        }
        return null;
    }


    protected void sendSessionEvent(SessionMessage msg) {
        sendSessionEvent(msg, null);
    }


    protected void sendSessionEvent( SessionMessage msg, IpAddress dest ) {
        try {
            cluster.send(msg, dest);
        } catch (Exception x) {
            log.error(sm.getString("jgManager.messageSendFail"), x);
        }
    }


    /**
     * This method is called by the received thread when a SessionMessage has 
     * been received from one of the other nodes in the cluster.
     * 
     * @param msg - the message received
     * @param sender - the sender of the message, this is used if we receive a 
     *                 EVT_GET_ALL_SESSION message, so that we only reply to 
     *                 the requesting node  
     */
    public void messageReceived(SessionMessage msg, IpAddress sender) {

        try {

            if (log.isDebugEnabled()) {
                log.debug(sm.getString("jgManager.messageType", 
                                       msg.getEventTypeString()));
            }
            
            switch (msg.getEventType()) {

                case SessionMessage.EVT_ATTRIBUTE_ADDED:
                {
                    // Add the attribute to the replicated session
                    ReplicatedSession session = 
                        (ReplicatedSession) findSession(msg.getSessionID());
                    if (session == null) {
                        log.warn(sm.getString("jgManager.unknownSessionID", 
                                              msg.getSessionID()));
                        return;
                    }
                    // FIXME
                    // How does the call below affect session binding listeners
                    session.setAttribute
                        (msg.getAttributeName(),msg.getAttributeValue(),false);
                    break;
                }
                case SessionMessage.EVT_ATTRIBUTE_REMOVED_WNOTIFY:
                case SessionMessage.EVT_ATTRIBUTE_REMOVED_WONOTIFY:
                {
                    boolean notify = 
                        (msg.getEventType() 
                         == SessionMessage.EVT_ATTRIBUTE_REMOVED_WNOTIFY); 
                    // Remove the attribute from the session
                    ReplicatedSession session = 
                        (ReplicatedSession) findSession(msg.getSessionID());
                    if (session == null) {
                        log.warn(sm.getString("jgManager.unknownSessionID", 
                                              msg.getSessionID()));
                        return;
                    }
                    // How does this affect the listeners?
                    session.removeAttribute
                        (msg.getAttributeName(), notify, false);
                    break;
                }
                case SessionMessage.EVT_REMOVE_SESSION_NOTE:
                {
                    // Remove the note from the session
                    ReplicatedSession session = 
                        (ReplicatedSession) findSession(msg.getSessionID());
                    if (session == null) {
                        log.warn(sm.getString("jgManager.unknownSessionID", 
                                              msg.getSessionID()));
                        return;
                    }
                    // How does this affect the listeners?
                    session.removeNote(msg.getAttributeName(),false);
                    break;
                }
                case SessionMessage.EVT_SET_SESSION_NOTE:
                {
                    // Add the note to the session
                    ReplicatedSession session = 
                        (ReplicatedSession) findSession(msg.getSessionID());
                    if (session == null) {
                        log.warn(sm.getString("jgManager.unknownSessionID", 
                                              msg.getSessionID()));
                        return;
                    }
                    // How does this affect the listeners?
                    session.setNote(msg.getAttributeName(), 
                                    msg.getAttributeValue(), false);
                    break;
                }
                case SessionMessage.EVT_GET_ALL_SESSIONS:
                {
                    // Get a list of all the session from this manager
                    Object[] sessions = findSessions();
                    for (int i=0; i<sessions.length; i++) {
                        // Make sure we only replicate sessions
                        // that are replicatable :)
                        if (sessions[i] instanceof ReplicatedSession) {
                            ReplicatedSession ses = 
                                (ReplicatedSession) sessions[i];
                            SessionMessage newmsg = new SessionMessage
                                (container.getName(), 
                                 SessionMessage.EVT_SESSION_CREATED,
                                 writeSession(ses), ses.getId(),
                                 null, null, null);
                            sendSessionEvent(newmsg,sender);
                            // Since the principal doesn't get serialized, 
                            // we better send it over too
                            if (ses.getPrincipal() != null) {
                                SessionMessage pmsg = new SessionMessage
                                    (container.getName(),
                                     SessionMessage.EVT_SET_USER_PRINCIPAL,
                                     null, ses.getId(), null, null,
                                     SerializablePrincipal.createPrincipal
                                     ((GenericPrincipal) ses.getPrincipal()));
                                sendSessionEvent(pmsg,sender);
                            }
                        } else {
                            log.warn(sm.getString
                                     ("jgManager.nonStandardSession", 
                                      sessions[i]));
                        }
                    }
                    break;
                }
                case SessionMessage.EVT_SESSION_ACCESSED:
                {
                    // This is so that the replicated session doesn't expire 
                    // in any other node
                    ReplicatedSession session = 
                        (ReplicatedSession) findSession(msg.getSessionID());
                    if (session == null) {
                        log.warn(sm.getString("jgManager.unknownSessionID", 
                                              msg.getSessionID()));
                        return;
                    }
                    session.access(false);
                    break;
                }
                case SessionMessage.EVT_SET_USER_PRINCIPAL:
                {
                    // Set the user principal
                    ReplicatedSession session = 
                        (ReplicatedSession) findSession(msg.getSessionID());
                    if (session == null) {
                        log.warn(sm.getString("jgManager.unknownSessionID", 
                                              msg.getSessionID()));
                        return;
                    }
                    GenericPrincipal principal = 
                        (msg.getPrincipal()).getPrincipal
                        (getContainer().getRealm());
                    session.setPrincipal(principal, false);
                    break;
                }
                case SessionMessage.EVT_SESSION_CREATED:
                {
                    Session session = this.readSession(msg.getSession());
                    session.setManager(this);
                    add(session);
                    break;
                }
                case SessionMessage.EVT_SESSION_EXPIRED_WNOTIFY:
                case SessionMessage.EVT_SESSION_EXPIRED_WONOTIFY:
                {
                    // Session has expired
                    boolean notify = 
                        (msg.getEventType() 
                         == SessionMessage.EVT_SESSION_EXPIRED_WNOTIFY); 
                    ReplicatedSession session = 
                        (ReplicatedSession) findSession(msg.getSessionID());
                    if (session == null) {
                        log.warn(sm.getString("jgManager.unknownSessionID", 
                                              msg.getSessionID()));
                        return;
                    }
                    session.expire(notify, false);
                    break;
                }
                default:
                {
                    // We didn't recognize the message type, do nothing
                    break;
                }
            }
        } catch (Exception x) {
            log.error(sm.getString("jgManager.messageReceiveFail", x));
        }
    }


}
