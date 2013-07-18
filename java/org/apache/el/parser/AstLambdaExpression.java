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
/* Generated By:JJTree: Do not edit this line. AstLambdaExpression.java Version 4.3 */
package org.apache.el.parser;

import java.util.ArrayList;
import java.util.List;

import javax.el.ELException;
import javax.el.LambdaExpression;

import org.apache.el.ValueExpressionImpl;
import org.apache.el.lang.EvaluationContext;
import org.apache.el.util.MessageFactory;

public class AstLambdaExpression extends SimpleNode {

    public AstLambdaExpression(int id) {
        super(id);
    }

    @Override
    public Object getValue(EvaluationContext ctx) throws ELException {

        // Check that there are not more sets of method parameters than there
        // are nested lambda expressions
        int methodParameterSetCount = jjtGetNumChildren() - 2;
        if (methodParameterSetCount > 0) {
            // We know this node is an expression
            methodParameterSetCount--;
            Node n = this.jjtGetChild(1);
            while (methodParameterSetCount > 0) {
                if (n.jjtGetNumChildren() <2 ||
                        !(n.jjtGetChild(0) instanceof AstLambdaParameters)) {
                    throw new ELException(MessageFactory.get(
                            "error.lambda.tooManyMethodParameterSets"));
                }
                n = n.jjtGetChild(1);
                methodParameterSetCount--;
            }
        }

        // First child is always parameters even if there aren't any
        AstLambdaParameters formalParametersNode =
                (AstLambdaParameters) children[0];
        Node[] formalParamNodes = formalParametersNode.children;

        // Second child is a value expression
        ValueExpressionImpl ve = new ValueExpressionImpl("", children[1],
                ctx.getFunctionMapper(), ctx.getVariableMapper(), null);

        // Build a LambdaExpression
        List<String> formalParameters = new ArrayList<>();
        if (formalParamNodes != null) {
            for (Node formalParamNode : formalParamNodes) {
                formalParameters.add(formalParamNode.getImage());
            }
        }
        LambdaExpression le = new LambdaExpression(formalParameters, ve);
        le.setELContext(ctx);

        if (jjtGetNumChildren() == 2) {
            if (formalParameters.isEmpty() &&
                    !(parent instanceof AstLambdaExpression)) {
                // No formal parameters or method parameters and not a nested
                // expression so invoke the expression.
                return le.invoke(ctx, (Object[]) null);
            } else {
                // Has formal parameters but no method parameters or is a nested
                // expression so return the expression for later evaluation
                return le;
            }
        }


        // Always have to invoke the outer-most expression
        int methodParameterIndex = 2;
        Object result = le.invoke(((AstMethodParameters)
                children[methodParameterIndex]).getParameters(ctx));
        methodParameterIndex++;

        /*
         * If there are multiple sets of method parameters there should be at
         * least that many nested expressions.
         *
         * If there are more nested expressions than sets of method parameters
         * this may return a LambdaExpression.
         *
         * If there are more sets of method parameters than nested expressions
         * an ELException will have been thrown by the check at the start of
         * this method.
         *
         * If the inner most expression(s) do not require parameters then a
         * value will be returned once the outermost expression that does
         * require a parameter has been evaluated.
         */
        while (result instanceof LambdaExpression &&
                methodParameterIndex < jjtGetNumChildren()) {
            result = ((LambdaExpression) result).invoke(((AstMethodParameters)
                    children[methodParameterIndex]).getParameters(ctx));
            methodParameterIndex++;
        }

        return result;
    }


    @Override
    public String toString() {
        // Purely for debug purposes. May not be complete or correct. Certainly
        // is not efficient. Be sure not to call this from 'real' code.
        StringBuilder result = new StringBuilder();
        for (Node n : children) {
            result.append(n.toString());
        }
        return result.toString();
    }
}
/* JavaCC - OriginalChecksum=071159eff10c8e15ec612c765ae4480a (do not edit this line) */
