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
package org.apache.catalina.ha.context;

import java.util.AbstractMap;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;

import org.apache.catalina.Globals;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Loader;
import org.apache.catalina.core.ApplicationContext;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.ha.CatalinaCluster;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.tipis.AbstractReplicatedMap.MapOwner;
import org.apache.catalina.tribes.tipis.ReplicatedMap;
import org.apache.catalina.util.Enumerator;
import org.apache.catalina.util.LifecycleBase;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

/**
 * @author Filip Hanik
 * @version 1.0
 */
public class ReplicatedContext extends StandardContext implements MapOwner {
    private int mapSendOptions = Channel.SEND_OPTIONS_DEFAULT;
    private static final Log log = LogFactory.getLog( ReplicatedContext.class );
    protected static long DEFAULT_REPL_TIMEOUT = 15000;//15 seconds
    
    /**
     * Start this component and implement the requirements
     * of {@link LifecycleBase#startInternal()}.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    @Override
    protected synchronized void startInternal() throws LifecycleException {

        try {
            CatalinaCluster catclust = (CatalinaCluster)this.getCluster();
            if (this.context == null) this.context = new ReplApplContext(this);
            if ( catclust != null ) {
                ReplicatedMap map = new ReplicatedMap(this,catclust.getChannel(),DEFAULT_REPL_TIMEOUT,
                                                      getName(),getClassLoaders());
                map.setChannelSendOptions(mapSendOptions);
                ((ReplApplContext)this.context).setAttributeMap(map);
                if (getAltDDName() != null) context.setAttribute(Globals.ALT_DD_ATTR, getAltDDName());
            }
            super.startInternal();
        }  catch ( Exception x ) {
            log.error("Unable to start ReplicatedContext",x);
            throw new LifecycleException("Failed to start ReplicatedContext",x);
        }
    }
    
    /**
     * Stop this component and implement the requirements
     * of {@link LifecycleBase#stopInternal()}.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    @Override
    protected synchronized void stopInternal() throws LifecycleException {
        
        super.stopInternal();

        AbstractMap<String,Object> map =
            ((ReplApplContext)this.context).getAttributeMap();
        if ( map!=null && map instanceof ReplicatedMap) {
            ((ReplicatedMap)map).breakdown();
        }
    }


    public void setMapSendOptions(int mapSendOptions) {
        this.mapSendOptions = mapSendOptions;
    }

    public int getMapSendOptions() {
        return mapSendOptions;
    }
    
    public ClassLoader[] getClassLoaders() {
        Loader loader = null;
        ClassLoader classLoader = null;
        loader = this.getLoader();
        if (loader != null) classLoader = loader.getClassLoader();
        if ( classLoader == null ) classLoader = Thread.currentThread().getContextClassLoader();
        if ( classLoader == Thread.currentThread().getContextClassLoader() ) {
            return new ClassLoader[] {classLoader};
        } else {
            return new ClassLoader[] {classLoader,Thread.currentThread().getContextClassLoader()};
        }
    }
    
    @Override
    public ServletContext getServletContext() {
        if (context == null) {
            context = new ReplApplContext(this);
            if (getAltDDName() != null)
                context.setAttribute(Globals.ALT_DD_ATTR,getAltDDName());
        }

        return ((ReplApplContext)context).getFacade();

    }

    
    protected static class ReplApplContext extends ApplicationContext {
        protected ConcurrentHashMap<String, Object> tomcatAttributes =
            new ConcurrentHashMap<String, Object>();
        
        public ReplApplContext(ReplicatedContext context) {
            super(context);
        }
        
        protected ReplicatedContext getParent() {
            return (ReplicatedContext)getContext();
        }
        
        @Override
        protected ServletContext getFacade() {
             return super.getFacade();
        }
        
        public AbstractMap<String,Object> getAttributeMap() {
            return (AbstractMap<String,Object>)this.attributes;
        }
        public void setAttributeMap(AbstractMap<String,Object> map) {
            this.attributes = map;
        }
        
        @Override
        public void removeAttribute(String name) {
            tomcatAttributes.remove(name);
            //do nothing
            super.removeAttribute(name);
        }
        
        @Override
        public void setAttribute(String name, Object value) {
            if ( (!getParent().getState().isAvailable()) || "org.apache.jasper.runtime.JspApplicationContextImpl".equals(name) ){
                tomcatAttributes.put(name,value);
            } else
                super.setAttribute(name,value);
        }
        
        @Override
        public Object getAttribute(String name) {
            if (tomcatAttributes.containsKey(name) )
                return tomcatAttributes.get(name);
            else 
                return super.getAttribute(name);
        }
        
        @Override
        public Enumeration<String> getAttributeNames() {
            return new MultiEnumeration<String>(new Enumeration[] {super.getAttributeNames(),new Enumerator<String>(tomcatAttributes.keySet(), true)});
        }
        
    }

    protected static class MultiEnumeration<T> implements Enumeration<T> {
        Enumeration<T>[] e=null;
        public MultiEnumeration(Enumeration<T>[] lists) {
            e = lists;
        }
        public boolean hasMoreElements() {
            for ( int i=0; i<e.length; i++ ) {
                if ( e[i].hasMoreElements() ) return true;
            }
            return false;
        }
        public T nextElement() {
            for ( int i=0; i<e.length; i++ ) {
                if ( e[i].hasMoreElements() ) return e[i].nextElement();
            }
            return null;

        }
    }
    
    public void objectMadePrimay(Object key, Object value) {
        //noop
    }


}