/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
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
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
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
 * Portions of this software are based upon public domain software
 * originally written at the National Center for Supercomputing Applications,
 * University of Illinois, Urbana-Champaign.
 */
package org.apache.webapp.balancer;

import java.io.IOException;
import java.io.InputStream;

import java.net.URL;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * The balancer filter redirects incoming requests
 * based on what rules they match.  The rules
 * are configurable via an XML document whose URL
 * is specified as an init-param to this filter.
 *
 * @author Yoav Shapira
 */
public class BalancerFilter implements Filter {
    /**
     * The rules this filter consults.
     */
    private RuleChain ruleChain;

    /**
     * The servlet context.
     */
    private ServletContext context;

    /**
     * Returns the rule chain.
     *
     * @return The rule chain
     */
    protected RuleChain getRuleChain() {
        return ruleChain;
    }

    /**
     * Initialize this filter.
     *
     * @param filterConfig The filter config
     * @throws ServletException If an error occurs
     */
    public void init(FilterConfig filterConfig) throws ServletException {
        context = filterConfig.getServletContext();

        String configUrlParam = filterConfig.getInitParameter("configUrl");

        if (configUrlParam == null) {
            throw new ServletException("configUrl is required.");
        }

        try {
            InputStream input = context.getResourceAsStream(configUrlParam);
            RulesParser parser = new RulesParser(input);
            ruleChain = parser.getResult();
            context.log(
                getClass().getName() + ": init(): ruleChain: " + ruleChain);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /**
     * Filter the incoming request.
     * Consults the rule chain to see if
     * any rules match this request, and if
     * so redirects.  Otherwise simply
     * let request through.
     *
     * @param request The request
     * @param response The response
     * @param chain The filter chain
     * @throws IOException If an error occurs
     * @throws ServletException If an error occurs
     */
    public void doFilter(
        ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        if (response.isCommitted()) {
            context.log(
                getClass().getName()
                + ": doFilter(): not inspecting committed response.");
            chain.doFilter(request, response);
        } else if (!(request instanceof HttpServletRequest)) {
            context.log(
                getClass().getName()
                + ": doFilter(): not inspecting non-Http request.");
            chain.doFilter(request, response);
        } else {
            HttpServletRequest hreq = (HttpServletRequest) request;
            HttpServletResponse hres = (HttpServletResponse) response;

            URL redirectUrl = getRuleChain().evaluate(hreq);

            if (redirectUrl != null) {
                String encoded =
                    hres.encodeRedirectURL(redirectUrl.toString());

                context.log(
                    getClass().getName()
                    + ": doFilter(): redirecting request for "
                    + hreq.getRequestURL().toString() + " to " + encoded);

                hres.sendRedirect(encoded);
            } else {
                chain.doFilter(request, response);
            }
        }
    }

    /**
     * Destroy this filter.
     */
    public void destroy() {
        context = null;
        ruleChain = null;
    }
}


// End of file: BalanceFilter.java
