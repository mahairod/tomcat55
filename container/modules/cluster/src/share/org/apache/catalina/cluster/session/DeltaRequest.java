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


package org.apache.catalina.cluster.session;

/**
 * This class is used to track the series of actions that happens when
 * a request is executed. These actions will then
 * @author <a href="mailto:fhanik@apache.org">Filip Hanik</a>
 * @version 1.0
 */

import java.util.Vector;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.security.Principal;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.catalina.cluster.ClusterSession;


public class DeltaRequest implements Serializable {

    public static final int TYPE_ATTRIBUTE = 0;
    public static final int TYPE_PRINCIPAL = 1;
    public static final int TYPE_ISNEW = 2;
    public static final int TYPE_MAXINTERVAL = 3;

    public static final int ACTION_SET = 0;
    public static final int ACTION_REMOVE = 1;

    public static final String NAME_PRINCIPAL = "__SET__PRINCIPAL__";
    public static final String NAME_MAXINTERVAL = "__SET__MAXINTERVAL__";
    public static final String NAME_ISNEW = "__SET__ISNEW__";

    private String sessionId;
    private Vector actions = new Vector();
    private boolean recordAllActions = false;

    public DeltaRequest(String sessionId, boolean recordAllActions) {
        this.sessionId = sessionId;
        this.recordAllActions = recordAllActions;
    }


    public void setAttribute(String name, Object value) {
        int action = (value==null)?ACTION_REMOVE:ACTION_SET;
        addAction(TYPE_ATTRIBUTE,action,name,value);
    }

    public void removeAttribute(String name) {
        int action = ACTION_REMOVE;
        addAction(TYPE_ATTRIBUTE,action,name,null);
    }

    public void setMaxInactiveInterval(int interval) {
        int action = ACTION_SET;
        addAction(TYPE_MAXINTERVAL,action,NAME_MAXINTERVAL,new Integer(interval));
    }

    public void setPrincipal(Principal p) {
        int action = (p==null)?ACTION_REMOVE:ACTION_SET;
        SerializablePrincipal sp = null;
        if ( p != null ) {
            sp = SerializablePrincipal.createPrincipal((GenericPrincipal)p);
        }
        addAction(TYPE_PRINCIPAL,action,NAME_PRINCIPAL,sp);
    }

    public void setNew(boolean n) {
        int action = ACTION_SET;
        addAction(TYPE_ISNEW,action,NAME_ISNEW,new Boolean(n));
    }

    protected void addAction(int type,
                             int action,
                             String name,
                             Object value) {
        AttributeInfo info = new AttributeInfo(type,action,name,value);
        //if we have already done something to this attribute, make sure
        //we don't send multiple actions across the wire
        if ( !recordAllActions) actions.remove(info);
        //add the action
        actions.addElement(info);
    }

    public void execute(ClusterSession session) {
        if ( !this.sessionId.equals( session.getId() ) )
            throw new java.lang.IllegalArgumentException("Session id mismatch, not executing the delta request");
        for ( int i=0; i<actions.size(); i++ ) {
            AttributeInfo info = (AttributeInfo)actions.get(i);
            switch ( info.getType() ) {
                case TYPE_ATTRIBUTE: {
                    if ( info.getAction() == ACTION_SET )
                        session.setAttribute(info.getName(),info.getValue());
                    else
                        session.removeAttribute(info.getName());
                    break;
                }//case
                case TYPE_ISNEW: {
                    session.setNew(((Boolean)info.getValue()).booleanValue());
                    break;
                }//case
                case TYPE_MAXINTERVAL: {
                    session.setMaxInactiveInterval(((Integer)info.getValue()).intValue());
                    break;
                }//case
                case TYPE_PRINCIPAL: {
                    Principal p = null;
                    if ( info.getAction() == ACTION_SET ) {
                        SerializablePrincipal sp = (SerializablePrincipal)info.getValue();
                        p = (Principal)sp.getPrincipal(session.getManager().getContainer().getRealm());
                    }
                    session.setPrincipal(p);
                    break;
                }//case
                default : throw new java.lang.IllegalArgumentException("Invalid attribute info type="+info);
            }//switch
        }//for
    }


    public static class AttributeInfo implements java.io.Serializable {
        private String name = null;
        private Object value = null;
        private int action;
        private int type;

        public AttributeInfo(int type,
                             int action,
                             String name,
                             Object value) {
            this.name = name;
            this.value = value;
            this.action = action;
            this.type = type;
        }

        public int getType() {
            return type;
        }

        public int getAction() {
            return action;
        }

        public Object getValue() {
            return value;
        }
        public int hashCode() {
            return name.hashCode();
        }

        public String getName() {
            return name;
        }

        public boolean equals(Object o) {
            if ( ! (o instanceof AttributeInfo ) ) return false;
            AttributeInfo other =  (AttributeInfo)o;
            return other.getName().equals(this.getName());
        }

    }

}
