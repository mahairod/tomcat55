/*
 * Copyright 1999,2004 The Apache Software Foundation.
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


package org.apache.naming.factory;

import java.util.Hashtable;
import java.util.Iterator;
import javax.naming.Name;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.RefAddr;
import javax.naming.spi.ObjectFactory;
import java.net.URL;
import org.apache.naming.ResourceRef;
import tyrex.tm.TransactionDomain;
import tyrex.resource.Resources;
import tyrex.resource.ResourceConfig;

/**
 * Object factory for Tyrex Resources.<br>
 *
 * This class retrieves Tyrex resources that are configured in the
 * TransactionDomain.  The type of Resource returned is specified in
 * Tyrex's domain configuration file.
 *
 * Tyrex is an open-source transaction manager, developed by Assaf Arkin and
 * exolab.org. See the <a href="http://tyrex.exolab.org/">Tyrex homepage</a>
 * for more details about Tyrex and downloads.
 *
 * @author David Haraburda
 * @version $Revision$ $Date$
 */

public class TyrexResourceFactory
    extends TyrexFactory {


    // ----------------------------------------------------------- Constructors


    // -------------------------------------------------------------- Constants


    public static final String RESOURCE_NAME = "name";
    public static final String DEFAULT_RESOURCE_NAME = "tomcat";


    // ----------------------------------------------------- Instance Variables


    // --------------------------------------------------------- Public Methods


    // -------------------------------------------------- ObjectFactory Methods


    /**
     * Create a new Resource instance.  The type of Resource is dependant
     * upon Tyrex's domain configuration.
     *
     * @param obj The reference object describing the Resource
     */
    public Object getObjectInstance(Object obj, Name name, Context nameCtx,
                                    Hashtable environment)
        throws NamingException {

        if (obj instanceof ResourceRef) {
            Reference ref = (Reference) obj;

            if (ref.getClassName().equals("tyrex.resource.Resource")) {

                try {

                    Resources resources = 
                        getTransactionDomain().getResources();
                    RefAddr nameAddr = ref.get(RESOURCE_NAME);
                    if (nameAddr != null) {
                        return resources
                            .getResource(nameAddr.getContent().toString())
                            .getClientFactory();
                    } else {
                        return resources.getResource(DEFAULT_RESOURCE_NAME)
                            .getClientFactory();
                    }

                } catch (Throwable t) {
                    log("Cannot create Tyrex Resource, Exception", t);
                    throw new NamingException
                        ("Exception creating Tyrex Resource: " 
                         + t.getMessage());
                }

            }

        }

        return null;

    }


    // -------------------------------------------------------- Private Methods


    private void log(String message) {
        System.out.print("TyrexResourceFactory:  ");
        System.out.println(message);
    }


    private void log(String message, Throwable exception) {
        log(message);
        exception.printStackTrace(System.out);
    }


}


