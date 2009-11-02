/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.tomcat.util.modeler;


import java.io.Serializable;

import javax.management.MBeanNotificationInfo;


/**
 * <p>Internal configuration information for a <code>Notification</code>
 * descriptor.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public class NotificationInfo extends FeatureInfo implements Serializable {
    static final long serialVersionUID = -6319885418912650856L;

    // ----------------------------------------------------- Instance Variables


    /**
     * The <code>ModelMBeanNotificationInfo</code> object that corresponds
     * to this <code>NotificationInfo</code> instance.
     */
    transient MBeanNotificationInfo info = null;
    protected String notifTypes[] = new String[0];

    // ------------------------------------------------------------- Properties


    /**
     * Override the <code>description</code> property setter.
     *
     * @param description The new description
     */
    public void setDescription(String description) {
        super.setDescription(description);
        this.info = null;
    }


    /**
     * Override the <code>name</code> property setter.
     *
     * @param name The new name
     */
    public void setName(String name) {
        super.setName(name);
        this.info = null;
    }


    /**
     * The set of notification types for this MBean.
     */
    public String[] getNotifTypes() {
        return (this.notifTypes);
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Add a new notification type to the set managed by an MBean.
     *
     * @param notifType The new notification type
     */
    public void addNotifType(String notifType) {

        synchronized (notifTypes) {
            String results[] = new String[notifTypes.length + 1];
            System.arraycopy(notifTypes, 0, results, 0, notifTypes.length);
            results[notifTypes.length] = notifType;
            notifTypes = results;
            this.info = null;
        }

    }


    /**
     * Create and return a <code>ModelMBeanNotificationInfo</code> object that
     * corresponds to the attribute described by this instance.
     */
    public MBeanNotificationInfo createNotificationInfo() {

        // Return our cached information (if any)
        if (info != null)
            return (info);

        // Create and return a new information object
        info = new MBeanNotificationInfo
            (getNotifTypes(), getName(), getDescription());
        //Descriptor descriptor = info.getDescriptor();
        //addFields(descriptor);
        //info.setDescriptor(descriptor);
        return (info);

    }


    /**
     * Return a string representation of this notification descriptor.
     */
    public String toString() {

        StringBuilder sb = new StringBuilder("NotificationInfo[");
        sb.append("name=");
        sb.append(name);
        sb.append(", description=");
        sb.append(description);
        sb.append(", notifTypes=");
        sb.append(notifTypes.length);
        sb.append("]");
        return (sb.toString());

    }


}
