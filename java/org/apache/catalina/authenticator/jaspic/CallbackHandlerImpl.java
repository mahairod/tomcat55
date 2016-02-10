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
package org.apache.catalina.authenticator.jaspic;

import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.auth.message.callback.GroupPrincipalCallback;

import org.apache.catalina.connector.Request;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;

public class CallbackHandlerImpl implements CallbackHandler {

    private static final Log log = LogFactory.getLog(CallbackHandlerImpl.class);
    private static final StringManager sm = StringManager.getManager(CallbackHandlerImpl.class);

    private Request request;
    private String name;
    private Principal principal;
    private Subject subject;
    private String[] groups;


    public CallbackHandlerImpl(Request request) {
        this.request = request;
    }


    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        if (callbacks != null) {
            // Need to combine data from multiple callbacks so use this to hold
            // the data
            // Process the callbacks
            for (Callback callback : callbacks) {
                if (callback instanceof CallerPrincipalCallback) {
                    CallerPrincipalCallback cpc = (CallerPrincipalCallback) callback;
                    name = cpc.getName();
                    principal = cpc.getPrincipal();
                    subject = cpc.getSubject();
                } else if (callback instanceof GroupPrincipalCallback) {
                    GroupPrincipalCallback gpc = (GroupPrincipalCallback) callback;
                    groups = gpc.getGroups();
                } else {
                    log.error(sm.getString("callbackHandlerImpl.jaspicCallbackMissing",
                            callback.getClass().getName()));
                }
            }

            // Create the GenericPrincipal
            GenericPrincipal gp = getGenericPrincipal();
            if (gp != null) {
                request.setUserPrincipal(gp);

                if (subject != null) {
                    subject.getPrivateCredentials().add(gp);
                }
            }
        }
    }


    public GenericPrincipal getGenericPrincipal() {
        String name = this.name;
        if (name == null && principal != null) {
            name = principal.getName();
        }
        if (name == null) {
            return null;
        }
        List<String> roles;
        if (groups == null || groups.length == 0) {
            roles = Collections.emptyList();
        } else {
            roles = Arrays.asList(groups);
        }

        return new GenericPrincipal(name, null, roles, principal);
    }
}
