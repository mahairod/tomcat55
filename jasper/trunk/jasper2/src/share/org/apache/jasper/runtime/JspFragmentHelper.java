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
 */

package org.apache.jasper.runtime;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.PageContext;

import javax.servlet.jsp.tagext.JspTag;
import javax.servlet.jsp.tagext.JspFragment;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Helper class from which all Jsp Fragment helper classes extend.
 * This class allows for the emulation of numerous fragments within
 * a single class, which in turn reduces the load on the class loader
 * since there are potentially many JspFragments in a single page.
 * <p>
 * The class also provides various utility methods for JspFragment
 * implementations.
 *
 * @author Mark Roth
 */
public abstract class JspFragmentHelper 
    implements JspFragment 
{
    
    protected int discriminator;
    protected JspContext jspContext;
    protected PageContext pageContext;
    protected JspTag parentTag;

    public JspFragmentHelper( int discriminator, JspContext jspContext, 
        JspTag parentTag ) 
    {
        this.discriminator = discriminator;
        this.jspContext = jspContext;
        this.pageContext = null;
        if( jspContext instanceof PageContext ) {
            pageContext = (PageContext)jspContext;
        }
        this.parentTag = parentTag;
    }
    
    public JspContext getJspContext() {
        return this.jspContext;
    }
    
    public JspTag getParentTag() {
        return this.parentTag;
    }
    
    /**
     * Takes a snapshot of the current JspContext and stores
     * the results in a Map for later restoration.  Also sets the
     * new values in the page context, given the provided parameters.
     *
     * @param params the parameters to set in the page scope
     * @return A map that contains a snapshot of the old page scope.
     */
    protected Map preparePageScope( Map params ) {
        Map originalValues = new HashMap();
        Iterator keys = params.keySet().iterator();
        while( keys.hasNext() ) {
            String key = (String)keys.next();
            // Remember original values to restore later
            originalValues.put( key, jspContext.getAttribute( key ) );
            // Set new values, based on params
            jspContext.setAttribute( key, params.get( key ) );
        }
        return originalValues;
    }
    
    /**
     * Restores the state of the page scope in the current page context,
     * from the given map.
     *
     * @param originalValues the values to restore in the page context.
     */
    protected void restorePageScope( Map originalValues ) {
        Iterator keys = originalValues.keySet().iterator();
        while( keys.hasNext() ) {
            String key = (String)keys.next();
            Object value = originalValues.get( key );
            if( value == null ) {
                // Value to be cleared:
                jspContext.removeAttribute( key );
            }
            else {
                // Value to be restored:
                jspContext.setAttribute( key, value );
            }
        }
    }
    
}
