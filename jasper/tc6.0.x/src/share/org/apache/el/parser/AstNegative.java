/* Generated By:JJTree: Do not edit this line. AstNegative.java */

package org.apache.el.parser;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.el.ELException;

import org.apache.el.lang.EvaluationContext;


/**
 * @author Jacob Hookom [jacob@hookom.net]
 * @version $Change: 181177 $$DateTime: 2001/06/26 08:45:09 $$Author: dpatil $
 */
public final class AstNegative extends SimpleNode {
    public AstNegative(int id) {
        super(id);
    }

    public Class getType(EvaluationContext ctx)
            throws ELException {
        return Number.class;
    }

    public Object getValue(EvaluationContext ctx)
            throws ELException {
        Object obj = this.children[0].getValue(ctx);

        if (obj == null) {
            return new Long(0);
        }
        if (obj instanceof BigDecimal) {
            return ((BigDecimal) obj).negate();
        }
        if (obj instanceof BigInteger) {
            return ((BigInteger) obj).negate();
        }
        if (obj instanceof String) {
            if (isStringFloat((String) obj)) {
                return new Double(-Double.parseDouble((String) obj));
            }
            return new Long(-Long.parseLong((String) obj));
        }
        Class type = obj.getClass();
        if (obj instanceof Long || Long.TYPE == type) {
            return new Long(-((Long) obj).longValue());
        }
        if (obj instanceof Double || Double.TYPE == type) {
            return new Double(-((Double) obj).doubleValue());
        }
        if (obj instanceof Integer || Integer.TYPE == type) {
            return new Integer(-((Integer) obj).intValue());
        }
        if (obj instanceof Float || Float.TYPE == type) {
            return new Float(-((Float) obj).floatValue());
        }
        if (obj instanceof Short || Short.TYPE == type) {
            return new Short((short) -((Short) obj).shortValue());
        }
        if (obj instanceof Byte || Byte.TYPE == type) {
            return new Byte((byte) -((Byte) obj).byteValue());
        }
        Long num = (Long) coerceToNumber(obj, Long.class);
        return new Long(-num.longValue());
    }
}
