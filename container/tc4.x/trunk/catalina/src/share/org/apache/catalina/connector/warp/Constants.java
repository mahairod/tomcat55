/* ========================================================================= *
 *                                                                           *
 *                 The Apache Software License,  Version 1.1                 *
 *                                                                           *
 *          Copyright (c) 1999-2001 The Apache Software Foundation.          *
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
package org.apache.catalina.connector.warp;

public class Constants {
    /** Our package name. */
    public static final String PACKAGE="org.apache.catalina.connector.warp";

    /** Compile-in debug flag. */
    public static final boolean DEBUG=false;

    /**
     * The WARP protocol major version.
     */
    public static final int VERS_MAJOR=0;

    /**
     * The WARP protocol minor version.
     */
    public static final int VERS_MINOR=9;

    /**
     * INVALID: The packet type hasn't been set yet.
     */
    public static final int TYPE_INVALID=-1;

    /**
     * ERROR: The last operation didn't completed correctly.
     * <br>
     * Payload description:<br>
     * [string] An error message.<br>
     */
    public static final int TYPE_ERROR=0x00;

    /**
     * DISCONNECT: The connection is being closed.
     * <br>
     * No payload:<br>
     */
    public static final int TYPE_DISCONNECT=0xfe;

    /**
     * FATAL: A protocol error occourred, the connection must be closed.
     * <br>
     * Payload description:<br>
     * [string] An error message.<br>
     */
    public static final int TYPE_FATAL=0xff;

    /**
     * CONF_WELCOME: The server issues this packet when a connection is
     * opened. The server awaits for configuration information.
     * <br>
     * Payload description:<br>
     * [ushort] Major protocol version.<br>
     * [ushort] Minor protocol version.<br>
     * [integer] The server unique-id.<br>
     */
    public static final int TYPE_CONF_WELCOME=0x01;

    /**
     * CONF_DEPLOY: The client attempts deploy a web application.
     * <br>
     * Payload description:<br>
     * [string] The application name.<br>
     * [string] The virtual host name.<br>
     * [ushort] The virtual host port.<br>
     * [string] The web-application URL path.<br>
     */
    public static final int TYPE_CONF_DEPLOY=0x02;

    /**
     * CONF_APPLIC: The server replies to a CONF_DEPLOY message with the web
     * application identifier of the configured application.
     * <br>
     * Payload description:<br>
     * [integer] The web application unique id for this server.<br>
     * [string] The web application real path (where it's expanded).<br>
     */
    public static final int TYPE_CONF_APPLIC=0x03;

    /**
     * CONF_DONE: Client issues this message when all configurations have been
     * processed.
     * <br>
     * No payload:<br>
     */
    public static final int TYPE_CONF_DONE=0x04;

    /**
     * CONF_PROCEED: Server issues this message in response to a CONF_DONE
     * message, to acknowledge its readiness to accept requests.
     * <br>
     * No payload:<br>
     */
    public static final int TYPE_CONF_PROCEED=0x05;
    
    public static final int TYPE_REQ_INIT=0x10;
    public static final int TYPE_REQ_CONTENT=0x11;
    public static final int TYPE_REQ_SCHEME=0x12;
    public static final int TYPE_REQ_AUTH=0x13;
    public static final int TYPE_REQ_HEADER=0x14;
    public static final int TYPE_REQ_PROCEED=0x1f;
    
    public static final int TYPE_RES_STATUS=0x20;
    public static final int TYPE_RES_HEADER=0x21;
    public static final int TYPE_RES_COMMIT=0x2f;
    public static final int TYPE_RES_BODY=0x30;
    public static final int TYPE_RES_DONE=0x3f;


}
