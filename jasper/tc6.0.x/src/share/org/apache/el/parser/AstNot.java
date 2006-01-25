/* Generated By:JJTree: Do not edit this line. AstNot.java */

package org.apache.el.parser;

import javax.el.ELException;

import org.apache.el.lang.EvaluationContext;


/**
 * @author Jacob Hookom [jacob@hookom.net]
 * @version $Change: 181177 $$DateTime: 2001/06/26 08:45:09 $$Author: dpatil $
 */
public final class AstNot extends SimpleNode {
    public AstNot(int id) {
        super(id);
    }

    public Class getType(EvaluationContext ctx)
            throws ELException {
        return Boolean.class;
    }

    public Object getValue(EvaluationContext ctx)
            throws ELException {
        Object obj = this.children[0].getValue(ctx);
        Boolean b = coerceToBoolean(obj);
        return Boolean.valueOf(!b.booleanValue());
    }
}
