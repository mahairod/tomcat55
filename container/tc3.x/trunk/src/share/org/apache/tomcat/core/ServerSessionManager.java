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


package org.apache.tomcat.core;

import org.apache.tomcat.server.*;
import org.apache.tomcat.util.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.http.*;

/**
 * 
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 */

public class ServerSessionManager {

    private StringManager sm =
        StringManager.getManager(Constants.Package);
    private static ServerSessionManager manager; // = new ServerSessionManager();

    static {
	manager = new ServerSessionManager();
    }
    
    static ServerSessionManager getManager() {
	return manager;
    }

    private Hashtable sessions = new Hashtable();
    private Reaper reaper;
    
    private ServerSessionManager() {
	reaper = Reaper.getReaper();
	reaper.setServerSessionManager(this);
	reaper.start();
    }

    ServerSession getServerSession(Request request, Response response,
        boolean create) {
	// Look for session id -- cookies only right now

	String sessionId = null;
	ServerSession session = null;

	//	Enumeration enum = request.getCookies().elements();
	Cookie cookies[]=request.getCookies(); // assert !=null
	
	//while (enum.hasMoreElements()) {
	for( int i=0; i<cookies.length; i++ ) {
	    Cookie cookie = cookies[i]; // (Cookie)enum.nextElement();

	    if (cookie.getName().equals(
                Constants.Cookie.SESSION_COOKIE_NAME)) {
		sessionId = cookie.getValue();

		if (sessionId != null) {
		    request.setRequestedSessionId(sessionId);
		    session = (ServerSession)sessions.get(sessionId);
		}
	    }
	}

	if (session == null && create) {
	    if (sessionId == null) {
		sessionId = SessionIdGenerator.generateId();
		Cookie cookie = new Cookie(
                    Constants.Cookie.SESSION_COOKIE_NAME, sessionId);

		cookie.setMaxAge(-1);
		cookie.setPath("/");
		cookie.setVersion(1);

		response.addSystemCookie(cookie);
	    }

	    session = new ServerSession(sessionId);
	    sessions.put(sessionId, session);
	}

	return session;
    }

    // XXX
    // sync'd for safty -- no other thread should be getting something
    // from this while we are reaping. This isn't the most optimal
    // solution for this, but we'll determine something else later.
    
    synchronized void reap() {
	Enumeration enum = sessions.keys();

	while (enum.hasMoreElements()) {
	    Object key = enum.nextElement();
	    ServerSession session = (ServerSession)sessions.get(key);

	    session.reap();
	    session.validate();
	}
    }

    synchronized void removeSession(ServerSession session) {
	String id = session.getId();

	session.invalidate();
	sessions.remove(id);
    }

    void removeApplicationSessions(Context context) {
	Enumeration enum = sessions.keys();

	while (enum.hasMoreElements()) {
	    Object key = enum.nextElement();
	    ServerSession session = (ServerSession)sessions.get(key);
	    ApplicationSession appSession =
		session.getApplicationSession(context, false);

	    if (appSession != null) {
		appSession.invalidate();
	    }
	}
    }
}
