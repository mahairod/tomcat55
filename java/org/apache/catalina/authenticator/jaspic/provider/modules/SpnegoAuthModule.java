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
package org.apache.catalina.authenticator.jaspic.provider.modules;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.MessagePolicy;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.apache.catalina.Realm;
import org.apache.catalina.authenticator.Constants;
import org.apache.catalina.connector.Request;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.codec.binary.Base64;
import org.apache.tomcat.util.compat.JreVendor;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.Oid;

/**
 * A SPNEGO authenticator that uses the SPNEGO/Kerberos support built in to Java
 * 6. Successful Kerberos authentication depends on the correct configuration of
 * multiple components. If the configuration is invalid, the error messages are
 * often cryptic although a Google search will usually point you in the right
 * direction.
 */
public class SpnegoAuthModule extends TomcatAuthModule {
    private static final Log log = LogFactory.getLog(SpnegoAuthModule.class);

    private Class<?>[] supportedMessageTypes = new Class[] { HttpServletRequest.class,
            HttpServletResponse.class };

    private String loginConfigName = Constants.DEFAULT_LOGIN_MODULE_NAME;
    private boolean storeDelegatedCredential = true;
    private Pattern noKeepAliveUserAgents = null;
    private boolean applyJava8u40Fix = true;

    @Override
    public Class<?>[] getSupportedMessageTypes() {
        return supportedMessageTypes;
    }


    @Override
    public void initializeModule(MessagePolicy requestPolicy, MessagePolicy responsePolicy,
            CallbackHandler handler, Map<String, String> options) throws AuthException {
        this.loginConfigName = options.getOrDefault("loginConfigName", Constants.DEFAULT_LOGIN_MODULE_NAME);
        this.storeDelegatedCredential = Boolean.parseBoolean(options.getOrDefault("storeDelegatedCredential", Boolean.TRUE.toString()));
        this.noKeepAliveUserAgents = compilePattern(options.get("noKeepAliveUserAgents"));
        this.applyJava8u40Fix = Boolean.parseBoolean(options.getOrDefault("applyJava8u40Fix", Boolean.TRUE.toString()));

        configureKerberosFileLocation();
        configureJaasFileLocation();
    }


    private Pattern compilePattern(String noKeepAliveUserAgents) {
        if (noKeepAliveUserAgents == null || noKeepAliveUserAgents.length() == 0) {
            return null;
        }
        return Pattern.compile(noKeepAliveUserAgents);
    }


    private void configureKerberosFileLocation() {
        String krb5Conf = System.getProperty(Constants.KRB5_CONF_PROPERTY);
        if (krb5Conf == null) {
            File configFile = new File(context.getCatalinaBase(), Constants.DEFAULT_KRB5_CONF);
            System.setProperty(Constants.KRB5_CONF_PROPERTY, configFile.getAbsolutePath());
        }
    }


    private void configureJaasFileLocation() {
        String jaasConf = System.getProperty(Constants.JAAS_CONF_PROPERTY);
        if (jaasConf == null) {
            File configFile = new File(context.getCatalinaBase(), Constants.DEFAULT_JAAS_CONF);
            System.setProperty(Constants.JAAS_CONF_PROPERTY, configFile.getAbsolutePath());
        }
    }


    public SpnegoAuthModule(Context context) {
        super(context);
    }


    @Override
    public AuthStatus validateRequest(MessageInfo messageInfo, Subject clientSubject,
            Subject serviceSubject) throws AuthException {
        try {
            return validate(messageInfo, clientSubject);
        } catch (IOException e) {
            throw new AuthException(e.getMessage());
        }
    }


    private AuthStatus validate(MessageInfo messageInfo, Subject clientSubject) throws IOException {
        Request request = (Request) messageInfo.getRequestMessage();
        HttpServletResponse response = (HttpServletResponse) messageInfo.getResponseMessage();

        MessageBytes authorization =
                request.getCoyoteRequest().getMimeHeaders()
                .getValue("authorization");

            if (authorization == null) {
                if (log.isDebugEnabled()) {
                    log.debug(sm.getString("authenticator.noAuthHeader"));
                }
                response.setHeader("WWW-Authenticate", "Negotiate");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return AuthStatus.SEND_FAILURE;
            }

            authorization.toBytes();
            ByteChunk authorizationBC = authorization.getByteChunk();

            if (!authorizationBC.startsWithIgnoreCase("negotiate ", 0)) {
                if (log.isDebugEnabled()) {
                    log.debug(sm.getString(
                            "spnegoAuthenticator.authHeaderNotNego"));
                }
                response.setHeader("WWW-Authenticate", "Negotiate");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return AuthStatus.SEND_FAILURE;
            }

            authorizationBC.setOffset(authorizationBC.getOffset() + 10);

            byte[] decoded = Base64.decodeBase64(authorizationBC.getBuffer(),
                    authorizationBC.getOffset(),
                    authorizationBC.getLength());

            if (applyJava8u40Fix) {
                SpnegoTokenFixer.fix(decoded);
            }

            if (decoded.length == 0) {
                if (log.isDebugEnabled()) {
                    log.debug(sm.getString(
                            "spnegoAuthenticator.authHeaderNoToken"));
                }
                response.setHeader("WWW-Authenticate", "Negotiate");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return AuthStatus.SEND_FAILURE;
            }

            LoginContext lc = null;
            GSSContext gssContext = null;
            byte[] outToken = null;
            Principal principal = null;
            try {
                try {
                    lc = new LoginContext(loginConfigName);
                    lc.login();
                } catch (LoginException e) {
                    log.error(sm.getString("spnegoAuthenticator.serviceLoginFail"),
                            e);
                    response.sendError(
                            HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    return AuthStatus.FAILURE;
                }

                Subject subject = lc.getSubject();

                // Assume the GSSContext is stateless
                // TODO: Confirm this assumption
                final GSSManager manager = GSSManager.getInstance();
                // IBM JDK only understands indefinite lifetime
                final int credentialLifetime;
                if (JreVendor.IS_IBM_JVM) {
                    credentialLifetime = GSSCredential.INDEFINITE_LIFETIME;
                } else {
                    credentialLifetime = GSSCredential.DEFAULT_LIFETIME;
                }
                final PrivilegedExceptionAction<GSSCredential> action =
                    new PrivilegedExceptionAction<GSSCredential>() {
                        @Override
                        public GSSCredential run() throws GSSException {
                            return manager.createCredential(null,
                                    credentialLifetime,
                                    new Oid("1.3.6.1.5.5.2"),
                                    GSSCredential.ACCEPT_ONLY);
                        }
                    };
                gssContext = manager.createContext(Subject.doAs(subject, action));

                outToken = Subject.doAs(lc.getSubject(), new AcceptAction(gssContext, decoded));

                if (outToken == null) {
                    if (log.isDebugEnabled()) {
                        log.debug(sm.getString(
                                "spnegoAuthenticator.ticketValidateFail"));
                    }
                    // Start again
                    response.setHeader("WWW-Authenticate", "Negotiate");
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    return AuthStatus.SEND_FAILURE;
                }

                principal = Subject.doAs(subject, new AuthenticateAction(
                        context.getRealm(), gssContext, storeDelegatedCredential));

            } catch (GSSException e) {
                if (log.isDebugEnabled()) {
                    log.debug(sm.getString("spnegoAuthenticator.ticketValidateFail"), e);
                }
                response.setHeader("WWW-Authenticate", "Negotiate");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return AuthStatus.FAILURE;
            } catch (PrivilegedActionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof GSSException) {
                    if (log.isDebugEnabled()) {
                        log.debug(sm.getString("spnegoAuthenticator.serviceLoginFail"), e);
                    }
                } else {
                    log.error(sm.getString("spnegoAuthenticator.serviceLoginFail"), e);
                }
                response.setHeader("WWW-Authenticate", "Negotiate");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return AuthStatus.FAILURE;
            } finally {
                if (gssContext != null) {
                    try {
                        gssContext.dispose();
                    } catch (GSSException e) {
                        // Ignore
                    }
                }
                if (lc != null) {
                    try {
                        lc.logout();
                    } catch (LoginException e) {
                        // Ignore
                    }
                }
            }

            // Send response token on success and failure
            response.setHeader("WWW-Authenticate", "Negotiate "
                    + Base64.encodeBase64String(outToken));

            if (principal != null) {
                try {
                    handlePrincipalCallbacks(clientSubject, principal);
                } catch (IOException | UnsupportedCallbackException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                Pattern p = noKeepAliveUserAgents;
                if (p != null) {
                    MessageBytes ua =
                            request.getCoyoteRequest().getMimeHeaders().getValue(
                                    "user-agent");
                    if (ua != null && p.matcher(ua.toString()).matches()) {
                        response.setHeader("Connection", "close");
                    }
                }
                return AuthStatus.SUCCESS;
            }

            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return AuthStatus.FAILURE;
    }


    @Override
    public AuthStatus secureResponse(MessageInfo messageInfo, Subject serviceSubject)
            throws AuthException {
        return null;
    }


    @Override
    public void cleanSubject(MessageInfo messageInfo, Subject subject) throws AuthException {

    }


    /**
     * This class gets a gss credential via a privileged action.
     */
    private static class AcceptAction implements PrivilegedExceptionAction<byte[]> {

        GSSContext gssContext;

        byte[] decoded;

        AcceptAction(GSSContext context, byte[] decodedToken) {
            this.gssContext = context;
            this.decoded = decodedToken;
        }

        @Override
        public byte[] run() throws GSSException {
            return gssContext.acceptSecContext(decoded,
                    0, decoded.length);
        }
    }

    private static class AuthenticateAction implements PrivilegedAction<Principal> {

        private final Realm realm;
        private final GSSContext gssContext;
        private final boolean storeDelegatedCredential;

        public AuthenticateAction(Realm realm, GSSContext gssContext,
                boolean storeDelegatedCredential) {
            this.realm = realm;
            this.gssContext = gssContext;
            this.storeDelegatedCredential = storeDelegatedCredential;
        }

        @Override
        public Principal run() {
            return realm.authenticate(gssContext, storeDelegatedCredential);
        }
    }


    /**
     * This class implements a hack around an incompatibility between the
     * SPNEGO implementation in Windows and the SPNEGO implementation in Java 8
     * update 40 onwards. It was introduced by the change to fix this bug:
     * https://bugs.openjdk.java.net/browse/JDK-8048194
     * (note: the change applied is not the one suggested in the bug report)
     * <p>
     * It is not clear to me if Windows, Java or Tomcat is at fault here. I
     * think it is Java but I could be wrong.
     * <p>
     * This hack works by re-ordering the list of mechTypes in the NegTokenInit
     * token.
     */
    private static class SpnegoTokenFixer {

        public static void fix(byte[] token) {
            SpnegoTokenFixer fixer = new SpnegoTokenFixer(token);
            fixer.fix();
        }


        private final byte[] token;
        private int pos = 0;


        private SpnegoTokenFixer(byte[] token) {
            this.token = token;
        }


        // Fixes the token in-place
        private void fix() {
            /*
             * Useful references:
             * http://tools.ietf.org/html/rfc4121#page-5
             * http://tools.ietf.org/html/rfc2743#page-81
             * https://msdn.microsoft.com/en-us/library/ms995330.aspx
             */

            // Scan until we find the mech types list. If we find anything
            // unexpected, abort the fix process.
            if (!tag(0x60)) return;
            if (!length()) return;
            if (!oid("1.3.6.1.5.5.2")) return;
            if (!tag(0xa0)) return;
            if (!length()) return;
            if (!tag(0x30)) return;
            if (!length()) return;
            if (!tag(0xa0)) return;
            lengthAsInt();
            if (!tag(0x30)) return;
            // Now at the start of the mechType list.
            // Read the mechTypes into an ordered set
            int mechTypesLen = lengthAsInt();
            int mechTypesStart = pos;
            LinkedHashMap<String, int[]> mechTypeEntries = new LinkedHashMap<>();
            while (pos < mechTypesStart + mechTypesLen) {
                int[] value = new int[2];
                value[0] = pos;
                String key = oidAsString();
                value[1] = pos - value[0];
                mechTypeEntries.put(key, value);
            }
            // Now construct the re-ordered mechType list
            byte[] replacement = new byte[mechTypesLen];
            int replacementPos = 0;

            int[] first = mechTypeEntries.remove("1.2.840.113554.1.2.2");
            if (first != null) {
                System.arraycopy(token, first[0], replacement, replacementPos, first[1]);
                replacementPos += first[1];
            }
            for (int[] markers : mechTypeEntries.values()) {
                System.arraycopy(token, markers[0], replacement, replacementPos, markers[1]);
                replacementPos += markers[1];
            }

            // Finally, replace the original mechType list with the re-ordered
            // one.
            System.arraycopy(replacement, 0, token, mechTypesStart, mechTypesLen);
        }


        private boolean tag(int expected) {
            return (token[pos++] & 0xFF) == expected;
        }


        private boolean length() {
            // No need to retain the length - just need to consume it and make
            // sure it is valid.
            int len = lengthAsInt();
            return pos + len == token.length;
        }


        private int lengthAsInt() {
            int len = token[pos++] & 0xFF;
            if (len > 127) {
                int bytes = len - 128;
                len = 0;
                for (int i = 0; i < bytes; i++) {
                    len = len << 8;
                    len = len + (token[pos++] & 0xff);
                }
            }
            return len;
        }


        private boolean oid(String expected) {
            return expected.equals(oidAsString());
        }


        private String oidAsString() {
            if (!tag(0x06)) return null;
            StringBuilder result = new StringBuilder();
            int len = lengthAsInt();
            // First byte is special case
            int v = token[pos++] & 0xFF;
            int c2 = v % 40;
            int c1 = (v - c2) / 40;
            result.append(c1);
            result.append('.');
            result.append(c2);
            int c = 0;
            boolean write = false;
            for (int i = 1; i < len; i++) {
                int b = token[pos++] & 0xFF;
                if (b > 127) {
                    b -= 128;
                } else {
                    write = true;
                }
                c = c << 7;
                c += b;
                if (write) {
                    result.append('.');
                    result.append(c);
                    c = 0;
                    write = false;
                }
            }
            return result.toString();
        }
    }

}
