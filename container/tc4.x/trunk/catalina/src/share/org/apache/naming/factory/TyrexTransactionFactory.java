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
import javax.naming.Name;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.Reference;

import org.apache.naming.TransactionRef;

/**
 * Object factory for Tyrex User transactions.<br>
 * Tyrex is an open-source transaction manager, developed by Assaf Arkin and
 * exolab.org. See the <a href="http://tyrex.exolab.org/">Tyrex homepage</a>
 * for more details about Tyrex and downloads.
 * 
 * @author David Haraburda
 * @author Remy Maucherat
 * @version $Revision$ $Date$
 */

public class TyrexTransactionFactory
    extends TyrexFactory {


    // ----------------------------------------------------------- Constructors


    // -------------------------------------------------------------- Constants


    // ----------------------------------------------------- Instance Variables


    // --------------------------------------------------------- Public Methods


    // -------------------------------------------------- ObjectFactory Methods


    /**
     * Crete a new UserTransaction.
     * 
     * @param obj The reference object
     */
    public Object getObjectInstance(Object obj, Name name, Context nameCtx,
                                    Hashtable environment)
        throws NamingException {
        
        if (obj instanceof TransactionRef) {
            Reference ref = (Reference) obj;
            if (ref.getClassName()
                .equals("javax.transaction.UserTransaction")) {
                
                try {
                    return getTransactionDomain().getUserTransaction();
                } catch (Throwable t) {
                    log("Cannot create Transaction, Exception", t);
                    throw new NamingException
                        ("Exception creating Transaction: " + t.getMessage());
                }
                
            }
            
        }
        
        return null;
        
    }


    // -------------------------------------------------------- Private Methods


    private void log(String message) {
        System.out.print("TyrexTransactionFactory:  ");
        System.out.println(message);
    }


    private void log(String message, Throwable exception) {
        log(message);
        exception.printStackTrace(System.out);
    }


}

