/*
 * $Header$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
 * 4. The names "The Jakarta Project", "Struts", and "Apache Software
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
 */

package org.apache.webapp.admin.realm;

import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import java.net.InetAddress;
import java.util.List;

import org.apache.webapp.admin.ApplicationServlet;
import org.apache.webapp.admin.LabelValueBean;

/**
 * Form bean for the jdbc realm page.
 *
 * @author Manveen Kaur
 * @version $Revision$ $Date$
 */

public final class JDBCRealmForm extends ActionForm {
    
    // ----------------------------------------------------- Instance Variables
    
   /**
     * The administrative action represented by this form.
     */
    private String adminAction = "Edit";

    /**
     * The object name of the Realm this bean refers to.
     */
    private String objectName = null;

    /**
     * The text for the debug level.
     */
    private String debugLvl = "0";
    
    /**
     * The text for the realm name, used to retrieve
     * the corresponding realm mBean.
     */
    private String realmName = null;
    
    /**
     * The text for the realm type.
     * Specifies if it is a JNDI, JDBC or MemoryRealm.
     */
    private String realmType = null;
    
    /**
     * Set of valid values for debug level.
     */
    private List debugLvlVals = null;
    
    /**
     * The text for the digest.
     */
    private String digest = null;
    
    /**
     * The text for the roleNameCol.
     */
    private String roleNameCol = null;
    
    /**
     * The text for the passwordCol.
     */
    private String passwordCol = null;
    
    /**
     * The text for the driver.
     */
    private String driver = null;
    
    /**
     * The text for the node label.
     */
    private String nodeLabel = null;
    
    /**
     * The text for the role table.
     */
    private String roleTable = null;
    
    /**
     * The text for the user table.
     */
    private String userTable = null;
        
    /**
     * The text for the connection user name.
     */
    private String connectionName = null;
    
    /**
     * The text for the connection Password.
     */
    private String connectionPassword = null;
    
    /**
     * The text for the connection URL.
     */
    private String connectionURL = null;
    
    /**
     * The object name of the parent of this Realm.
     */
    private String parentObjectName = null;

    /**
     * Set of valid values for realms.
     */
    private List realmTypeVals = null;
    
    /**
     * The text for whether "delete this realm" operation is allowed
     * on the realm or not.
     */
    private String allowDeletion = null;
 
    // ------------------------------------------------------------- Properties

       /**
     * Return the administrative action represented by this form.
     */
    public String getAdminAction() {

        return this.adminAction;

    }

    /**
     * Set the administrative action represented by this form.
     */
    public void setAdminAction(String adminAction) {

        this.adminAction = adminAction;

    }

    /**
     * Return the Object Name.
     */
    public String getObjectName() {
        
        return this.objectName;
        
    }
    
    /**
     * Set the Object Name.
     */
    public void setObjectName(String objectName) {
        
        this.objectName = objectName;
        
    }
    
    /**
     * Return the Realm type.
     */
    public String getRealmType() {
        
        return this.realmType;
        
    }
    
    /**
     * Set the Realm type.
     */
    public void setRealmType(String realmType) {
        
        this.realmType = realmType;
        
    }
    
    /**
     * Return the debugVals.
     */
    public List getDebugLvlVals() {
        
        return this.debugLvlVals;
        
    }
    
    /**
     * Set the debugVals.
     */
    public void setDebugLvlVals(List debugLvlVals) {
        
        this.debugLvlVals = debugLvlVals;
        
    }
    
    /**
     * Return the Debug Level Text.
     */
    public String getDebugLvl() {
        
        return this.debugLvl;
        
    }
    
    /**
     * Set the Debug Level Text.
     */
    public void setDebugLvl(String debugLvl) {
        
        this.debugLvl = debugLvl;
        
    }
    
    /**
     * Return the digest.
     */
    public String getDigest() {
        
        return this.digest;
        
    }
    
    /**
     * Set the digest.
     */
    public void setDigest(String digest) {
        
        this.digest = digest;
        
    }
    
    /**
     * Return the roleNameCol.
     */
    public String getRoleNameCol() {
        
        return this.roleNameCol;
        
    }
    
    /**
     * Set the roleNameCol.
     */
    public void setRoleNameCol(String roleNameCol) {
        
        this.roleNameCol = roleNameCol;
        
    }
    /**
     * Return the driver.
     */
    public String getDriver() {
        
        return this.driver;
        
    }
    
    /**
     * Set the driver.
     */
    public void setDriver(String driver) {
        
        this.driver = driver;
        
    }
    
    /**
     * Return the role table.
     */
    public String getRoleTable() {
        
        return this.roleTable;
        
    }
    
    /**
     * Set the roleTable.
     */
    public void setRoleTable(String roleTable) {
        
        this.roleTable = roleTable;
        
    }
    
    /**
     * Return the user table.
     */
    public String getUserTable() {
        
        return this.userTable;
        
    }
    
    /**
     * Set the user Table.
     */
    public void setUserTable(String userTable) {
        
        this.userTable = userTable;
        
    }
    
    /**
     * Return the label of the node that was clicked.
     */
    public String getNodeLabel() {
        
        return this.nodeLabel;
        
    }
    
    /**
     * Set the node label.
     */
    public void setNodeLabel(String nodeLabel) {
        
        this.nodeLabel = nodeLabel;
        
    }
    
    /**
     * Return the passwordCol.
     */
    public String getPasswordCol() {
        
        return this.passwordCol;
        
    }
    
    /**
     * Set the passwordCol.
     */
    public void setPasswordCol(String passwordCol) {
        
        this.passwordCol = passwordCol;
        
    }
    
    
    /**
     * Return the connection name.
     */
    public String getConnectionName() {
        
        return this.connectionName;
        
    }
    
    /**
     * Set the connectionName.
     */
    public void setConnectionName(String connectionName) {
        
        this.connectionName = connectionName;
        
    }
    
    
    /**
     * Return the connection password.
     */
    public String getConnectionPassword() {
        
        return this.connectionPassword;
        
    }
    
    /**
     * Set the connection password.
     */
    public void setConnectionPassword(String connectionPassword) {
        
        this.connectionPassword = connectionPassword;
        
    }
    
    
    /**
     * Return the connection URL.
     */
    public String getConnectionURL() {
        
        return this.connectionURL;
        
    }
    
    /**
     * Set the connectionURL.
     */
    public void setConnectionURL(String connectionURL) {
        
        this.connectionURL = connectionURL;
        
    }
    
     /**
     * Return the parent object name of the Realm this bean refers to.
     */
    public String getParentObjectName() {

        return this.parentObjectName;

    }


    /**
     * Set the parent object name of the Realm this bean refers to.
     */
    public void setParentObjectName(String parentObjectName) {

        this.parentObjectName = parentObjectName;

    }
    
        
   /**
     * Return the realmTypeVals.
     */
    public List getRealmTypeVals() {
        
        return this.realmTypeVals;
        
    }
    
    /**
     * Set the realmTypeVals.
     */
    public void setRealmTypeVals(List realmTypeVals) {
        
        this.realmTypeVals = realmTypeVals;
        
    }
  
    /**
     * Return the allow deletion value.
     */
    public String getAllowDeletion() {
        
        return this.allowDeletion;
        
    }
    
    /**
     * Set the allow Deletion value.
     */
    public void setAllowDeletion(String allowDeletion) {
        
        this.allowDeletion = allowDeletion;
        
    }


    // --------------------------------------------------------- Public Methods
    
    /**
     * Reset all properties to their default values.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        
        this.objectName = null;
        this.debugLvl = "0";
        
        this.digest = null;
        this.driver = null;
        
        this.roleNameCol = null;
        this.passwordCol = null;
        this.userTable = null;
        this.roleTable = null;
        
        this.connectionName = null;
        this.connectionPassword = null;
        this.connectionURL = null;
        
    }
    
    /**
     * Render this object as a String.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("UserDatabaseRealmForm[adminAction=");
        sb.append(adminAction);
        sb.append(",debugLvl=");
        sb.append(debugLvl);
        sb.append(",digest=");
        sb.append(digest);
        sb.append("',driver='");
        sb.append(driver);
        sb.append("',roleNameCol=");
        sb.append(roleNameCol);
        sb.append(",passwordCol=");
        sb.append(passwordCol);
        sb.append("',userTable='");
        sb.append(userTable);
        sb.append("',roleTable=");
        sb.append(roleTable);
        sb.append(",connectionName=");
        sb.append(connectionName);        
        sb.append("',connectionPassword=");
        sb.append(connectionPassword);
        sb.append(",connectionURL=");
        sb.append(connectionURL);
        sb.append("',objectName='");
        sb.append(objectName);
        sb.append("',realmType=");
        sb.append(realmType);
        sb.append("]");
        return (sb.toString());

    }
    
    /**
     * Validate the properties that have been set from this HTTP request,
     * and return an <code>ActionErrors</code> object that encapsulates any
     * validation errors that have been found.  If no errors are found, return
     * <code>null</code> or an <code>ActionErrors</code> object with no
     * recorded error messages.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    
    public ActionErrors validate(ActionMapping mapping,
    HttpServletRequest request) {
        
        ActionErrors errors = new ActionErrors();
        
        String submit = request.getParameter("submit");
        String type = request.getParameter("realmType");
        
        // front end validation when save is clicked.        
         if (submit != null) {
             // the following fields are required.
            
            if ((driver == null) || (driver.length() < 1)) {
                errors.add("driver",
                new ActionError("error.driver.required"));
            }
         
            if ((roleNameCol == null) || (roleNameCol.length() < 1)) {
                errors.add("roleNameCol",
                new ActionError("error.roleNameCol.required"));
            }
         
             if ((passwordCol == null) || (passwordCol.length() < 1)) {
                errors.add("passwordCol",
                new ActionError("error.passwordCol.required"));
            }
            
            if ((userTable == null) || (userTable.length() < 1)) {
                errors.add("userTable",
                new ActionError("error.userTable.required"));
            }
            
            if ((roleTable == null) || (roleTable.length() < 1)) {
                errors.add("roleTable",
                new ActionError("error.roleTable.required"));
            }
            
            if ((connectionName == null) || (connectionName.length() < 1)) {
                errors.add("connectionName",
                new ActionError("error.connectionName.required"));
            }
            
            if ((connectionPassword == null) || (connectionPassword.length() < 1)) {
                errors.add("connectionPassword",
                new ActionError("error.connectionPassword.required"));
            }
            
             if ((connectionURL == null) || (connectionURL.length() < 1)) {
                errors.add("connectionURL",
                new ActionError("error.connectionURL.required"));
            }
        }
                 
        return errors;
    }
}
