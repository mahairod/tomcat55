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

import java.security.Principal;

/**
 * Title:        Tomcat Session Replication for Tomcat 4.0 <BR>
 * Description:  A very simple straight forward implementation of 
 *               session replication of servers in a cluster.<BR>
 *               This session replication is implemented "live". By live
 *               I mean, when a session attribute is added into a session 
 *               on Node A a message is broadcasted to other messages 
 *               and setAttribute is called on the replicated sessions.<BR>
 *               A full description of this implementation can be found under
 *               <href="http://www.filip.net/tomcat/">Filip's Tomcat 
 *               Page</a><BR>            
 *               
 * <B>Class Description:</B><BR>
 * The SessionMessage class is a class that is used when a session has been 
 * created, modified, expired in a Tomcat cluster node.<BR>
 * 
 * The following events are currently available:
 * <ul>
 *   <li><pre>public static final int EVT_SESSION_CREATED</pre><li>
 *   <li><pre>public static final int EVT_SESSION_ACCESSED</pre><li>
 *   <li><pre>public static final int EVT_ATTRIBUTE_ADDED</pre><li>
 *   <li><pre>public static final int EVT_ATTRIBUTE_REMOVED</pre><li>
 *   <li><pre>public static final int EVT_SESSION_EXPIRED_WONOTIFY</pre><li>
 *   <li><pre>public static final int EVT_SESSION_EXPIRED_WNOTIFY</pre><li>
 *   <li><pre>public static final int EVT_GET_ALL_SESSIONS</pre><li>
 *   <li><pre>public static final int EVT_SET_USER_PRINCIPAL</pre><li>
 *   <li><pre>public static final int EVT_SET_SESSION_NOTE</pre><li>
 *   <li><pre>public static final int EVT_REMOVE_SESSION_NOTE</pre><li>
 * </ul>
 * 
 * These message are being sent and received from and to the
 * JGManager
 * 
 * @author Filip Hanik
 * @see JGManager
 */
public class SessionMessage
    implements java.io.Serializable {


    // -------------------------------------------------------------- Constants


    /**
     * Event type used when a session has been created on a node
     */
    public static final int EVT_SESSION_CREATED = 1;


    /**
     * Event type used when a session has expired, but we don't 
     * want to notify the session listeners
     */
    public static final int EVT_SESSION_EXPIRED_WONOTIFY = 2;


    /**
     * Event type used when a session has expired, and we do 
     * want to notify the session listeners
     */
    public static final int EVT_SESSION_EXPIRED_WNOTIFY = 7;


    /**
     * Event type used when a session has been accessed (ie, last access time
     * has been updated. This is used so that the replicated sessions will 
     * not expire on the network
     */
    public static final int EVT_SESSION_ACCESSED = 3;


    /**
     * Event type used when a server comes online for the first time.
     * The first thing the newly started server wants to do is to grab the 
     * all the sessions from one of the nodes and keep the same state in there
     */
    public static final int EVT_GET_ALL_SESSIONS = 4;


    /**
     * Event type used when an attribute has been added to a session,
     * the attribute will be sent to all the other nodes in the cluster
     */
    public static final int EVT_ATTRIBUTE_ADDED  = 5;


    /**
     * Event type used when an attribute has been removed from a session,
     * the attribute will be sent to all the other nodes in the cluster
     */    
    public static final int EVT_ATTRIBUTE_REMOVED_WONOTIFY = 6;


    /**
     * Event type used when an attribute has been removed from a session,
     * the attribute will be sent to all the other nodes in the cluster
     */    
    public static final int EVT_ATTRIBUTE_REMOVED_WNOTIFY = 8;


    /**
     * Event type used when a user principal is being cached in the session
     */    
    public static final int EVT_SET_USER_PRINCIPAL = 9;


    /**
     * Event type used when a user principal is being cached in the session
     */    
    public static final int EVT_REMOVE_SESSION_NOTE = 10;
    
    
    /**
     * Event type used when a user principal is being cached in the session
     */    
    public static final int EVT_SET_SESSION_NOTE = 11;


    // ----------------------------------------------------- Instance Variables


    /**
     * Private serializable variables to keep the messages state
     */
    private String webapp;
    private int eventType = -1;
    private byte[] session;
    private String sessionID;
    private String attrName;
    private Object attrValue;
    private SerializablePrincipal principal;


    // ------------------------------------------------------------ Constructor


    /**
     * Creates a session message. Depending on what event type you want this
     * message to represent, you populate the different parameters in 
     * the constructor<BR>
     * The following rules apply dependent on what event type argument 
     * you use:<BR>
     * <B>EVT_SESSION_CREATED</B><BR>
     *    The parameters: session, sessionID must be set.<BR>
     * <B>EVT_SESSION_EXPIRED</B><BR>
     *    The parameters: sessionID must be set.<BR>
     * <B>EVT_SESSION_ACCESSED</B><BR>
     *    The parameters: sessionID must be set.<BR>
     * <B>EVT_SESSION_EXPIRED_XXXX</B><BR>
     *    The parameters: sessionID must be set.<BR>
     * <B>EVT_ATTRIBUTE_ADDED</B><BR>
     *    The parameters: sessionID, attrName, attrValue must be set.<BR>
     * <B>EVT_ATTRIBUTE_REMOVED</B><BR>
     *    The parameters: sessionID, attrName must be set.<BR>
     * <B>EVT_SET_USER_PRINCIPAL</B><BR>
     *    The parameters: sessionID, principal<BR>
     * <B>EVT_REMOVE_SESSION_NOTE</B><BR>
     *    The parameters: sessionID, attrName<
     * <B>EVT_SET_SESSION_NOTE</B><BR>
     *    The parameters: sessionID, attrName, attrValue<
     * 
     * @param eventtype - one of the 8 event type defined in this class
     * @param session - the serialized byte array of the session itself
     * @param sessionID - the id that identifies this session
     * @param attrName - the name of the attribute added/removed
     * @param attrValue - the value of the attribute added
     */
    public SessionMessage(String webapp,
                          int eventType,
                          byte[] session,
                          String sessionID,
                          String attrName,
                          Object attrValue,
                          SerializablePrincipal principal) {
        this.webapp = webapp;
        this.eventType = eventType;
        this.session = session;
        this.sessionID = sessionID;
        this.attrName = attrName;
        this.attrValue = attrValue;
        this.principal = principal;
    }


    // --------------------------------------------------------- Public Methods


    public String getWebapp() {
        return (this.webapp);
    }


    /**
     * returns the event type
     * @return one of the event types EVT_XXXX
     */
    public int getEventType() {
        return eventType;
    }


    /**
     * @return the serialized data for the session
     */
    public byte[] getSession() {
        return session;
    }


    /**
     * @return the session ID for the session
     */
    public String getSessionID() {
        return sessionID;
    }


    /**
     * @return the name of the attribute 
     */
    public String getAttributeName() {
        return attrName;
    }


    /**
     * the value of the attribute
     */
    public Object getAttributeValue() {
        return attrValue;
    }


    public SerializablePrincipal getPrincipal() {
        return principal;
    }


    /**
     * @return the event type in a string representating, useful for debugging
     */
    public String getEventTypeString() {
        switch (eventType) {
        case EVT_SESSION_CREATED : 
            return "SESSION-CREATED";
        case EVT_SESSION_EXPIRED_WNOTIFY : 
            return "SESSION-EXPIRED-WITH-NOTIFY";
        case EVT_SESSION_EXPIRED_WONOTIFY : 
            return "SESSION-EXPIRED-WITHOUT-NOTIFY";
        case EVT_ATTRIBUTE_ADDED : 
            return "SESSION-ATTRIBUTE-ADDED";
        case EVT_ATTRIBUTE_REMOVED_WNOTIFY : 
            return "SESSION-ATTRIBUTE-REMOVED-WITH-NOTIFY";
        case EVT_ATTRIBUTE_REMOVED_WONOTIFY: 
            return "SESSION-ATTRIBUTE-REMOVED-WITHOUT-NOTIFY";
        case EVT_SESSION_ACCESSED : 
            return "SESSION-ACCESSED";
        case EVT_GET_ALL_SESSIONS : 
            return "SESSION-GET-ALL";
        case EVT_SET_SESSION_NOTE: 
            return "SET-SESSION-NOTE";
        case EVT_SET_USER_PRINCIPAL : 
            return "SET-USER-PRINCIPAL";
        case EVT_REMOVE_SESSION_NOTE : 
            return "REMOVE-SESSION-NOTE";
        default : 
            return "UNKNOWN-EVENT-TYPE";
        }
    }


}
