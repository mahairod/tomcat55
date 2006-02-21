package org.apache.jasper.el;

import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ValueExpression;
import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.el.Expression;
import javax.servlet.jsp.el.VariableResolver;

import org.apache.jasper.runtime.JspApplicationContextImpl;

public class ExpressionImpl extends Expression {

	private final ValueExpression ve;
	
	public ExpressionImpl(ValueExpression ve) {
		this.ve = ve;
	}

	public Object evaluate(VariableResolver vResolver) throws ELException {
		ELContext ctx = new ELContextImpl(new ELResolverImpl(vResolver));
		return ve.getValue(ctx);
	}

}
