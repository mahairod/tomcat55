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

package org.apache.catalina.core;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletSecurityElement;

import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.deploy.SecurityCollection;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.catalina.util.ParameterMap;
import org.apache.tomcat.util.res.StringManager;

public class ApplicationServletRegistration
        implements ServletRegistration.Dynamic {

    /**
     * The string manager for this package.
     */
    private static final StringManager sm =
      StringManager.getManager(Constants.Package);
    
    private Wrapper wrapper;
    private Context context;
    
    public ApplicationServletRegistration(Wrapper wrapper,
            Context context) {
        this.wrapper = wrapper;
        this.context = context;
        
    }

    @Override
    public String getClassName() {
        return wrapper.getServletClass();
   }

    @Override
    public String getInitParameter(String name) {
        return wrapper.findInitParameter(name);
    }

    @Override
    public Map<String, String> getInitParameters() {
        ParameterMap<String,String> result = new ParameterMap<String,String>();
        
        String[] parameterNames = wrapper.findInitParameters();
        
        for (String parameterName : parameterNames) {
            result.put(parameterName, wrapper.findInitParameter(parameterName));
        }

        result.setLocked(true);
        return result;
    }

    @Override
    public String getName() {
        return wrapper.getName();
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        if (name == null || value == null) {
            throw new IllegalArgumentException(
                    sm.getString("applicationFilterRegistration.nullInitParam",
                            name, value));
        }
        if (getInitParameter(name) != null) {
            return false;
        }
        
        wrapper.addInitParameter(name, value);

        return true;
    }

    @Override
    public Set<String> setInitParameters(Map<String, String> initParameters) {
        
        Set<String> conflicts = new HashSet<String>();
        
        for (Map.Entry<String, String> entry : initParameters.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                throw new IllegalArgumentException(sm.getString(
                        "applicationFilterRegistration.nullInitParams",
                                entry.getKey(), entry.getValue()));
            }
            if (getInitParameter(entry.getKey()) != null) {
                conflicts.add(entry.getKey());
            }
        }

        // Have to add in a separate loop since spec requires no updates at all
        // if there is an issue
        for (Map.Entry<String, String> entry : initParameters.entrySet()) {
            setInitParameter(entry.getKey(), entry.getValue());
        }
        
        return conflicts;
    }

    @Override
    public void setAsyncSupported(boolean asyncSupported) {
        wrapper.setAsyncSupported(asyncSupported);
    }

    @Override
    public void setLoadOnStartup(int loadOnStartup) {
        wrapper.setLoadOnStartup(loadOnStartup);
    }

    @Override
    public void setMultipartConfig(MultipartConfigElement multipartConfig) {
        wrapper.setMultipartConfigElement(multipartConfig);
    }

    @Override
    public void setRunAsRole(String roleName) {
        wrapper.setRunAs(roleName);
    }

    @Override
    public Set<String> setServletSecurity(ServletSecurityElement constraint) {
        if (constraint == null) {
            throw new IllegalArgumentException(sm.getString(
                    "applicationServletRegistration.setServletSecurity.iae",
                    getName(), context.getPath()));
        }
        
        if (context.isInitialized()) {
            throw new IllegalStateException(sm.getString(
                    "applicationServletRegistration.setServletSecurity.ise",
                    getName(), context.getPath()));
        }

        Set<String> conflicts = new HashSet<String>();

        Collection<String> urlPatterns = getMappings();
        for (String urlPattern : urlPatterns) {
            boolean foundConflict = false;
            
            SecurityConstraint[] securityConstraints =
                context.findConstraints();
            for (SecurityConstraint securityConstraint : securityConstraints) {
                
                SecurityCollection[] collections =
                    securityConstraint.findCollections();
                for (SecurityCollection collection : collections) {
                    if (collection.findPattern(urlPattern)) {
                        // First pattern found will indicate if there is a
                        // conflict since for any given pattern all matching
                        // constraints will be from either the descriptor or
                        // not. It is not permitted to have a mixture
                        if (collection.isFromDescriptor()) {
                            // Skip this pattern
                            foundConflict = true;
                        } else {
                            // Need to overwrite constraint for this pattern
                            // so remove every pattern found
                            context.removeConstraint(securityConstraint);
                        }
                    }
                    if (foundConflict) {
                        break;
                    }
                }
                if (foundConflict) {
                    break;
                }
            }
            if (!foundConflict) {
                SecurityConstraint[] newSecurityConstraints =
                        SecurityConstraint.createConstraints(constraint,
                                urlPattern);
                for (SecurityConstraint securityConstraint :
                        newSecurityConstraints) {
                    context.addConstraint(securityConstraint);
                }
            }
        }
        
        return conflicts;
    }


    @Override
    public Set<String> addMapping(String... urlPatterns) {
        if (urlPatterns == null) {
            return Collections.emptySet();
        }
        
        Set<String> conflicts = new HashSet<String>();
        
        for (String urlPattern : urlPatterns) {
            if (context.findServletMapping(urlPattern) != null) {
                conflicts.add(urlPattern);
            }
        }

        if (!conflicts.isEmpty()) {
            return conflicts;
        }
        
        for (String urlPattern : urlPatterns) {
            context.addServletMapping(urlPattern, wrapper.getName());
        }
        return Collections.emptySet();
    }

    @Override
    public Collection<String> getMappings() {

        Set<String> result = new HashSet<String>();
        String servletName = wrapper.getName();
        
        String[] urlPatterns = context.findServletMappings();
        for (String urlPattern : urlPatterns) {
            String name = context.findServletMapping(urlPattern);
            if (name.equals(servletName)) {
                result.add(urlPattern);
            }
        }
        return result;
    }

    @Override
    public String getRunAsRole() {
        return wrapper.getRunAs();
    }
    
}
