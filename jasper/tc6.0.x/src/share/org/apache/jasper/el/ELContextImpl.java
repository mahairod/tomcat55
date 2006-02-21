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
package org.apache.jasper.el;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.FunctionMapper;
import javax.el.VariableMapper;

/**
 * Implementation of ELContext
 * 
 * @author Jacob Hookom
 */
public class ELContextImpl extends ELContext {
	
	private final ELResolver resolver;
	private FunctionMapper functionMapper;
	private VariableMapper variableMapper;
    
    public ELContextImpl() {
        this(ELResolverImpl.DefaultResolver);
    }

	public ELContextImpl(ELResolver resolver) {
		this.resolver = resolver;
	}

	public ELResolver getELResolver() {
		return this.resolver;
	}

	public FunctionMapper getFunctionMapper() {
		return this.functionMapper;
	}

	public VariableMapper getVariableMapper() {
		return this.variableMapper;
	}
	
	public void setFunctionMapper(FunctionMapper functionMapper) {
		this.functionMapper = functionMapper;
	}
	
	public void setVariableMapper(VariableMapper variableMapper) {
		this.variableMapper = variableMapper;
	}

}
