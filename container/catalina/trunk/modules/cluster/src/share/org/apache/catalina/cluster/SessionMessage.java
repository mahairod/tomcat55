/*
 * Copyright 1999,2004 The Apache Software Foundation.
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
package org.apache.catalina.cluster;

/**
 * Title:        Tomcat Session Replication for Tomcat 4.0 <BR>
 * Description:  A very simple straight forward implementation of
 *               session replication of servers in a cluster.<BR>
 *               This session replication is implemented "live". By live
 *               I mean, when a session attribute is added into a session on Node A
 *               a message is broadcasted to other messages and setAttribute is called on the replicated
 *               sessions.<BR>
 *               A full description of this implementation can be found under
 *               <href="http://www.filip.net/tomcat/">Filip's Tomcat Page</a><BR>
 *
 * Copyright:    See apache license
 * Company:      www.filip.net
 * @author  <a href="mailto:mail@filip.net">Filip Hanik</a>
 * @version 1.0 for 4.0
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
 * InMemoryReplicationManager
 *
 * @see InMemoryReplicationManager
 */
 import java.security.Principal;
 import org.apache.catalina.cluster.Member;
public class SessionMessage
    //implements serializable,
    implements java.io.Serializable
{

    /**
     * Event type used when a session has been created on a node
     */
    public static final int EVT_SESSION_CREATED = 1;
    /**
     * Event type used when a session has expired
     */
    public static final int EVT_SESSION_EXPIRED = 2;

    /**
     * Event type used when a session has been accessed (ie, last access time
     * has been updated. This is used so that the replicated sessions will not expire
     * on the network
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
    public static final int EVT_SESSION_DELTA  = 13;

    /**
     * When a session state is transferred, this is the event.
     */
    public static final int EVT_ALL_SESSION_DATA = 12;


    /*

     * Private serializable variables to keep the messages state
     */
    private int mEvtType = -1;
    private byte[] mSession;
    private String mSessionID;
    private Member mSrc;
    private String mContextName;
    private long serializationTimestamp;


    /**
     * Creates a session message. Depending on what event type you want this
     * message to represent, you populate the different parameters in the constructor<BR>
     * The following rules apply dependent on what event type argument you use:<BR>
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
     *    The parameters: sessionID, attrName, attrValue
     * @param eventtype - one of the 8 event type defined in this class
     * @param session - the serialized byte array of the session itself
     * @param sessionID - the id that identifies this session
     * @param attrName - the name of the attribute added/removed
     * @param attrValue - the value of the attribute added

     */
    public SessionMessage( String contextName,
                           int eventtype,
                           byte[] session,
                           String sessionID)
    {
        mEvtType = eventtype;
        mSession = session;
        mSessionID = sessionID;
        mContextName = contextName;
    }

    /**
     * returns the event type
     * @return one of the event types EVT_XXXX
     */
    public int getEventType() { return mEvtType; }
    /**
     * @return the serialized data for the session
     */
    public byte[] getSession() { return mSession;}
    /**
     * @return the session ID for the session
     */
    public String getSessionID(){ return mSessionID; }
    /**
     * @return the name of the attribute
     */
//    public String getAttributeName() { return mAttributeName; }
    /**
     * the value of the attribute
     */
//    public Object getAttributeValue() {return mAttributeValue; }

//    public SerializablePrincipal getPrincipal() { return mPrincipal;}

    public void setTimestamp(long time) {serializationTimestamp=time;}
    public long getTimestamp() { return serializationTimestamp;}
    /**
     * @return the event type in a string representating, useful for debugging
     */
    public String getEventTypeString()
    {
        switch (mEvtType)
        {
            case EVT_SESSION_CREATED : return "SESSION-MODIFIED";
            case EVT_SESSION_EXPIRED : return "SESSION-EXPIRED";
            case EVT_SESSION_ACCESSED : return "SESSION-ACCESSED";
            case EVT_GET_ALL_SESSIONS : return "SESSION-GET-ALL";
            case EVT_SESSION_DELTA : return "SESSION-DELTA";
            case EVT_ALL_SESSION_DATA : return "ALL-SESSION-DATA";
            default : return "UNKNOWN-EVENT-TYPE";
        }
    }

    /**
     * Get the address that this message originated from.  This would be set
     * if the message was being relayed from a host other than the one
     * that originally sent it.
     */
    public Member getAddress()
    {
        return this.mSrc;
    }

    /**
     * Use this method to set the address that this message originated from.
     * This can be used when re-sending the EVT_GET_ALL_SESSIONS message to
     * another machine in the group.
     */
    public void setAddress(Member src)
    {
        this.mSrc = src;
    }

    public String getContextName() {
       return mContextName;
    }
}//SessionMessage
