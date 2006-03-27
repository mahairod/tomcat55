/* Generated By:JJTree: Do not edit this line. AstDynamicExpression.java */

package org.apache.el.parser;

import javax.el.ELException;

import org.apache.el.lang.EvaluationContext;


/**
 * @author Jacob Hookom [jacob@hookom.net]
 * @version $Change: 181177 $$DateTime: 2001/06/26 08:45:09 $$Author: dpatil $
 */
public final class AstDynamicExpression extends SimpleNode {
    public AstDynamicExpression(int id) {
        super(id);
    }

    public Class getType(EvaluationContext ctx)
            throws ELException {
        return this.children[0].getType(ctx);
    }

    public Object getValue(EvaluationContext ctx)
            throws ELException {
        return this.children[0].getValue(ctx);
    }

    public boolean isReadOnly(EvaluationContext ctx)
            throws ELException {
        return this.children[0].isReadOnly(ctx);
    }

    public void setValue(EvaluationContext ctx, Object value)
            throws ELException {
        this.children[0].setValue(ctx, value);
    }
}
