/*
 * Copyright 1999-2001,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.catalina.storeconfig;

import java.io.File;

import org.apache.catalina.Container;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardHost;

/**
 * store StandardCOntext Attributes ...
 * @author Peter Rossbach
 *  
 */
public class StoreContextAppender extends StoreAppender {

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.catalina.config.StoreAppender#isPrintValue(java.lang.Object,
     *      java.lang.Object, java.lang.String,
     *      org.apache.catalina.config.StoreDescription)
     */
    public boolean isPrintValue(Object bean, Object bean2, String attrName,
            StoreDescription desc) {
        boolean isPrint = super.isPrintValue(bean, bean2, attrName, desc);
        if (isPrint && "workDir".equals(attrName)) {
            StandardContext context = ((StandardContext) bean);
            String defaultWorkDir = getDefaultWorkDir(context);
            isPrint = !defaultWorkDir.equals(context.getWorkDir());
        }
        return isPrint;
    }

    /**
     * @param context
     * @return
     */
    protected String getDefaultWorkDir(StandardContext context) {
        String defaultWorkDir = null;
        String contextPath = context.getPath().length() == 0 ? "_" : context
                .getPath().substring(1);
        Container host = context.getParent();
        if (host instanceof StandardHost) {
            String hostWorkDir = ((StandardHost) host).getWorkDir();
            if (hostWorkDir != null) {
                defaultWorkDir = hostWorkDir + File.separator + contextPath;
            } else {
                String engineName = context.getParent().getParent().getName();
                String hostName = context.getParent().getName();
                defaultWorkDir = "work" + File.separator + engineName
                        + File.separator + hostName + File.separator
                        + contextPath;
            }
        }
        return defaultWorkDir;
    }

    /*
     * Generate a real default StandardContext 
     * TODO read and interpret the
     * default context.xml and context.xml.default 
     * TODO Cache a Default
     * StandardContext ( with reloading strategy) 
     * TODO remove really all
     * elements, but detection is hard... To Listener or Valve from same class?>
     * 
     * @see org.apache.catalina.storeconfig.StoreAppender#defaultInstance(java.lang.Object)
     */
    public Object defaultInstance(Object bean) throws InstantiationException,
            IllegalAccessException {
        if (bean instanceof StandardContext) {
            StandardContext defaultContext = new StandardContext();
            /*
             * if (!((StandardContext) bean).getOverride()) {
             * defaultContext.setParent(((StandardContext)bean).getParent());
             * ContextConfig contextConfig = new ContextConfig();
             * defaultContext.addLifecycleListener(contextConfig);
             * contextConfig.setContext(defaultContext);
             * contextConfig.processContextConfig(new File(contextConfig
             * .getBaseDir(), "conf/context.xml"));
             * contextConfig.processContextConfig(new File(contextConfig
             * .getConfigBase(), "context.xml.default")); }
             */
            return defaultContext;
        } else
            return super.defaultInstance(bean);
    }
}