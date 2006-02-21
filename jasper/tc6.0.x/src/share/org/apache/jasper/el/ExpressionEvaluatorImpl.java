package org.apache.jasper.el;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.el.ELParseException;
import javax.servlet.jsp.el.Expression;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.FunctionMapper;
import javax.servlet.jsp.el.VariableResolver;


public class ExpressionEvaluatorImpl extends ExpressionEvaluator {

	private final ExpressionFactory factory;
	
	public ExpressionEvaluatorImpl(ExpressionFactory factory) {
		this.factory = factory;
	}

	public Expression parseExpression(String expression, Class expectedType,
			FunctionMapper fMapper) throws ELException {
		try {
			ELContextImpl ctx = new ELContextImpl(ELResolverImpl.DefaultResolver);
            if (fMapper != null) {
                ctx.setFunctionMapper(new FunctionMapperImpl(fMapper));
            }
			ValueExpression ve = this.factory.createValueExpression(ctx, expression, expectedType);
			return new ExpressionImpl(ve);
		} catch (javax.el.ELException e) {
			throw new ELParseException(e.getMessage());
		}
	}

	public Object evaluate(String expression, Class expectedType,
			VariableResolver vResolver, FunctionMapper fMapper)
			throws ELException {
		return this.parseExpression(expression, expectedType, fMapper).evaluate(vResolver);
	}

}
