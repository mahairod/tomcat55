/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/* Generated By:JJTree: Do not edit this line. AstValue.java */

package org.apache.el.parser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.MethodInfo;
import javax.el.PropertyNotFoundException;
import javax.el.ValueReference;

import org.apache.el.lang.ELSupport;
import org.apache.el.lang.EvaluationContext;
import org.apache.el.util.MessageFactory;
import org.apache.el.util.ReflectionUtil;


/**
 * @author Jacob Hookom [jacob@hookom.net]
 * @version $Change: 181177 $$Date$$Author$
 */
public final class AstValue extends SimpleNode {

    protected static final boolean COERCE_TO_ZERO =
        Boolean.valueOf(System.getProperty(
                "org.apache.el.parser.COERCE_TO_ZERO", "true")).booleanValue();
    
    protected static class Target {
        protected Object base;

        protected Object property;
    }

    public AstValue(int id) {
        super(id);
    }

    @Override
    public Class<?> getType(EvaluationContext ctx) throws ELException {
        Target t = getTarget(ctx);
        ctx.setPropertyResolved(false);
        return ctx.getELResolver().getType(ctx, t.base, t.property);
    }

    private final Target getTarget(EvaluationContext ctx) throws ELException {
        // evaluate expr-a to value-a
        Object base = this.children[0].getValue(ctx);

        // if our base is null (we know there are more properties to evaluate)
        if (base == null) {
            throw new PropertyNotFoundException(MessageFactory.get(
                    "error.unreachable.base", this.children[0].getImage()));
        }

        // set up our start/end
        Object property = null;
        int propCount = this.jjtGetNumChildren();
        
        if (propCount > 2 &&
                this.jjtGetChild(propCount - 1) instanceof AstMethodParameters) {
            // Method call with paramaters. 
            propCount-=2;
        } else {
            propCount--;
        }
        int i = 1;

        // evaluate any properties before our target
        ELResolver resolver = ctx.getELResolver();
        if (propCount > 1) {
            while (base != null && i < propCount) {
                property = this.children[i].getValue(ctx);
                ctx.setPropertyResolved(false);
                base = resolver.getValue(ctx, base, property);
                i++;
            }
            // if we are in this block, we have more properties to resolve,
            // but our base was null
            if (base == null || property == null) {
                throw new PropertyNotFoundException(MessageFactory.get(
                        "error.unreachable.property", property));
            }
        }

        property = this.children[i].getValue(ctx);

        if (property == null) {
            throw new PropertyNotFoundException(MessageFactory.get(
                    "error.unreachable.property", this.children[i]));
        }

        Target t = new Target();
        t.base = base;
        t.property = property;
        return t;
    }

    @Override
    public Object getValue(EvaluationContext ctx) throws ELException {
        Object base = this.children[0].getValue(ctx);
        int propCount = this.jjtGetNumChildren();
        int i = 1;
        Object suffix = null;
        ELResolver resolver = ctx.getELResolver();
        while (base != null && i < propCount) {
            suffix = this.children[i].getValue(ctx);
            if (i + 1 < propCount &&
                    (this.children[i+1] instanceof AstMethodParameters)) {
                AstMethodParameters mps =
                    (AstMethodParameters) this.children[i+1];
                // This is a method
                base = resolver.invoke(ctx, base, suffix,
                        mps.getParameterTypes(ctx), mps.getParameters(ctx));
                i+=2;
            } else {
                // This is a property
                if (suffix == null) {
                    return null;
                } else {
                    ctx.setPropertyResolved(false);
                    base = resolver.getValue(ctx, base, suffix);
                }
                i++;
            }
        }
        return base;
    }

    @Override
    public boolean isReadOnly(EvaluationContext ctx) throws ELException {
        Target t = getTarget(ctx);
        ctx.setPropertyResolved(false);
        return ctx.getELResolver().isReadOnly(ctx, t.base, t.property);
    }

    @Override
    public void setValue(EvaluationContext ctx, Object value)
            throws ELException {
        Target t = getTarget(ctx);
        ctx.setPropertyResolved(false);
        ELResolver resolver = ctx.getELResolver();

        // coerce to the expected type
        Class<?> targetClass = resolver.getType(ctx, t.base, t.property);
        if (COERCE_TO_ZERO == true
                || !isAssignable(value, targetClass)) {
            resolver.setValue(ctx, t.base, t.property,
                    ELSupport.coerceToType(value, targetClass));
        } else {
            resolver.setValue(ctx, t.base, t.property, value);
        }
    }

    private boolean isAssignable(Object value, Class<?> targetClass) {
        if (targetClass == null) {
            return false;
        } else if (value != null && targetClass.isPrimitive()) {
            return false;
        } else if (value != null && !targetClass.isInstance(value)) {
            return false;
        }
        return true;
    }


    @Override
    // Interface el.parser.Node uses raw types (and is auto-generated)
    public MethodInfo getMethodInfo(EvaluationContext ctx, 
            @SuppressWarnings("unchecked") Class[] paramTypes)
            throws ELException {
        Target t = getTarget(ctx);
        Method m = ReflectionUtil.getMethod(t.base, t.property, paramTypes);
        return new MethodInfo(m.getName(), m.getReturnType(), m
                .getParameterTypes());
    }

    @Override
    // Interface el.parser.Node uses a raw type (and is auto-generated)
    public Object invoke(EvaluationContext ctx, 
            @SuppressWarnings("unchecked") Class[] paramTypes,
            Object[] paramValues) throws ELException {
        Target t = getTarget(ctx);
        Method m = ReflectionUtil.getMethod(t.base, t.property, paramTypes);
        Object result = null;
        try {
            result = m.invoke(t.base, paramValues);
        } catch (IllegalAccessException iae) {
            throw new ELException(iae);
        } catch (InvocationTargetException ite) {
            throw new ELException(ite.getCause());
        }
        return result;
    }

    /**
     * @since EL 2.2
     */
    @Override
    public ValueReference getValueReference(EvaluationContext ctx) {
        // Check this is a reference to a base and a property
        if (this.children.length > 2 &&
                this.jjtGetChild(2) instanceof AstMethodParameters) {
            // This is a method call
            return null;
        }
        Target t = getTarget(ctx);
        return new ValueReference(t.base, this.jjtGetChild(1).getValue(ctx));
    }

    
    /**
     * @ since EL 2.2
     */
    @Override
    public boolean isParametersProvided() {
        if (this.children.length > 2
                && this.jjtGetChild(2) instanceof AstMethodParameters) {
            return true;
        }
        return false;
    }
}
