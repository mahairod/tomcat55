/* ========================================================================= *
 *                                                                           *
 *                 The Apache Software License,  Version 1.1                 *
 *                                                                           *
 *         Copyright (c) 1999, 2000  The Apache Software Foundation.         *
 *                           All rights reserved.                            *
 *                                                                           *
 * ========================================================================= *
 *                                                                           *
 * Redistribution and use in source and binary forms,  with or without modi- *
 * fication, are permitted provided that the following conditions are met:   *
 *                                                                           *
 * 1. Redistributions of source code  must retain the above copyright notice *
 *    notice, this list of conditions and the following disclaimer.          *
 *                                                                           *
 * 2. Redistributions  in binary  form  must  reproduce the  above copyright *
 *    notice,  this list of conditions  and the following  disclaimer in the *
 *    documentation and/or other materials provided with the distribution.   *
 *                                                                           *
 * 3. The end-user documentation  included with the redistribution,  if any, *
 *    must include the following acknowlegement:                             *
 *                                                                           *
 *       "This product includes  software developed  by the Apache  Software *
 *        Foundation <http://www.apache.org/>."                              *
 *                                                                           *
 *    Alternately, this acknowlegement may appear in the software itself, if *
 *    and wherever such third-party acknowlegements normally appear.         *
 *                                                                           *
 * 4. The names  "The  Jakarta  Project",  "Tomcat",  and  "Apache  Software *
 *    Foundation"  must not be used  to endorse or promote  products derived *
 *    from this  software without  prior  written  permission.  For  written *
 *    permission, please contact <apache@apache.org>.                        *
 *                                                                           *
 * 5. Products derived from this software may not be called "Apache" nor may *
 *    "Apache" appear in their names without prior written permission of the *
 *    Apache Software Foundation.                                            *
 *                                                                           *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES *
 * INCLUDING, BUT NOT LIMITED TO,  THE IMPLIED WARRANTIES OF MERCHANTABILITY *
 * AND FITNESS FOR  A PARTICULAR PURPOSE  ARE DISCLAIMED.  IN NO EVENT SHALL *
 * THE APACHE  SOFTWARE  FOUNDATION OR  ITS CONTRIBUTORS  BE LIABLE  FOR ANY *
 * DIRECT,  INDIRECT,   INCIDENTAL,  SPECIAL,  EXEMPLARY,  OR  CONSEQUENTIAL *
 * DAMAGES (INCLUDING,  BUT NOT LIMITED TO,  PROCUREMENT OF SUBSTITUTE GOODS *
 * OR SERVICES;  LOSS OF USE,  DATA,  OR PROFITS;  OR BUSINESS INTERRUPTION) *
 * HOWEVER CAUSED AND  ON ANY  THEORY  OF  LIABILITY,  WHETHER IN  CONTRACT, *
 * STRICT LIABILITY, OR TORT  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN *
 * ANY  WAY  OUT OF  THE  USE OF  THIS  SOFTWARE,  EVEN  IF  ADVISED  OF THE *
 * POSSIBILITY OF SUCH DAMAGE.                                               *
 *                                                                           *
 * ========================================================================= *
 *                                                                           *
 * This software  consists of voluntary  contributions made  by many indivi- *
 * duals on behalf of the  Apache Software Foundation.  For more information *
 * on the Apache Software Foundation, please see <http://www.apache.org/>.   *
 *                                                                           *
 * ========================================================================= */

package org.apache.tester;


import java.io.Serializable;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionEvent;


/**
 * Simple JavaBean to use for session attribute tests.  It is Serializable
 * so that instances can be saved and restored across server restarts.
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public class SessionBean implements
    HttpSessionActivationListener, HttpSessionBindingListener, Serializable {


    // ------------------------------------------------------------- Properties


    /**
     * The lifecycle events that have happened on this bean instance.
     */
    private String lifecycle = "";

    public String getLifecycle() {
        return (this.lifecycle);
    }

    public void setLifecycle(String lifecycle) {
        this.lifecycle = lifecycle;
    }


    /**
     * A string property.
     */
    private String stringProperty = "Default String Property Value";

    public String getStringProperty() {
        return (this.stringProperty);
    }

    public void setStringProperty(String stringProperty) {
        this.stringProperty = stringProperty;
    }


    // ---------------------------------- HttpSessionActivationListener Methods


    /**
     * Receive notification that this session was activated.
     *
     * @param event The session event that has occurred
     */
    public void sessionDidActivate(HttpSessionEvent event) {

        lifecycle += "/sda";

    }


    /**
     * Receive notification that this session will be passivated.
     *
     * @param event The session event that has occurred
     */
    public void sessionWillPassivate(HttpSessionEvent event) {

        lifecycle += "/swp";

    }


    // ------------------------------------- HttpSessionBindingListener Methods


    /**
     * Receive notification that this attribute has been bound.
     *
     * @param event The session event that has occurred
     */
    public void valueBound(HttpSessionBindingEvent event) {

        lifecycle += "/vb";

    }


    /**
     * Receive notification that this attribute has been unbound.
     *
     * @param event The session event that has occurred
     */
    public void valueUnbound(HttpSessionBindingEvent event) {

        lifecycle += "/vu";

    }


}

