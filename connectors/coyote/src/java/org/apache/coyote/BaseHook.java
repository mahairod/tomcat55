/*
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
package org.apache.coyote;

import javax.management.MBeanRegistration;
import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.NotificationListener;
import javax.management.Notification;


/**
 * Action hook.
 *
 * @author Remy Maucherat
 * @author Costin Manolache
 */
public class BaseHook
        implements ActionHook, MBeanRegistration, NotificationListener
 {
    protected BaseHook next;

    /**
     * XXX DO WE REALLY NEED THIS ? handleNotification should be enough !!
     *
     * Send an action to the connector.
     * Both recursive and iterative invocation is possible - the
     * hook may call itself the next hook ( like catalina ) or the
     * controller may call the next.
     *
     * The call pattern for this is:
     *   while( hook!=null ) {
     *      hook=action( code, param );
     *   }
     * If the hooks has special needs - it can call next itself and decide
     * to return null or whatever the next hook returned.
     *
     * The reason for iterative calls - smaller stacks ( and stack traces ).
     *
     * @param param Action parameter
     * @return  A hook that can be executed next.
     */
    public BaseHook action(int code, Object param) {
        return next;
    };

    /** Backward compat
     *
     * @param actionCode
     * @param param
     */
    public void action( ActionCode actionCode, Object param) {
       action( actionCode.getCode(), param );
    }

    public void setNextHook(BaseHook next) {
        this.next=next;
    }

    public BaseHook getNextHook() {
        return next;
    }

    // -------------------- Mbean registration  --------------------

    protected String domain;
    protected ObjectName oname;
    protected MBeanServer mserver;

    public ObjectName getObjectName() {
        return oname;
    }

    public String getDomain() {
        return domain;
    }

    public ObjectName preRegister(MBeanServer server,
                                  ObjectName name) throws Exception {
        oname=name;
        mserver=server;
        domain=name.getDomain();
        return name;
    }

    public void postRegister(Boolean registrationDone) {
    }

    public void preDeregister() throws Exception {
    }

    public void postDeregister() {
    }

    // -------------------- Notification listener  --------------------

    public void handleNotification(Notification notification, Object handback) {
        // we need a special CoyoteNotification that will include the code
        // and param.

        // It would be better to use a generic class in modeler.
    }

}
