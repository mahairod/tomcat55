/*
 * $Header$
 * $Revision$
 * $Date$
 *
 *
 *  Copyright 1999-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language 
 */

package org.apache.jasper.compiler;

import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.JasperException;

/**
 * Helpful abstract base class that generators can extend. 
 *
 * @author Anil K. Vijendran
 */
abstract class GeneratorBase implements Generator {
    protected JspCompilationContext ctxt;

    public void init(JspCompilationContext ctxt) throws JasperException {
        this.ctxt = ctxt;
    }
    
    public boolean generateCoordinates(Class phase) {
        return true;
    }
}
