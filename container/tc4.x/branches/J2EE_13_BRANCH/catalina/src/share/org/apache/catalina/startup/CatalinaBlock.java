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

package org.apache.catalina.startup;

import org.apache.avalon.ConfigurationException;
import org.apache.avalon.Configuration;
import org.apache.avalon.blocks.AbstractBlock;

/**
 * Catalina wrapper for Avalon.
 * 
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @version $Revision$
 */

public final class CatalinaBlock
    extends AbstractBlock {
    
    
    // ----------------------------------------------------- Instance Variables
    
    
    /**
     * Catalina path (as read in the configuration file).
     */
    private String path = null;
    
    
    
    
    
    // ---------------------------------------------------------- Block Methods
    
    
    public void configure(final Configuration configuration) 
	throws ConfigurationException {
        super.configure(configuration);
        try {
            Configuration pathElement = configuration.getChild("path");
            path = pathElement.getValue();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        if (path != null) {
            System.setProperty("catalina.home", path);
        }
    }


    public void init() 
        throws Exception {
        try {
            String[] args = { "start" };
            Bootstrap.main(args);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


    public void dispose()
        throws Exception {
        try {
            String[] args = { "stop" };
            Bootstrap.main(args);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


}
