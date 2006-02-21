package org.apache.jasper.el;

import java.util.Iterator;

import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.ListELResolver;
import javax.el.MapELResolver;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;
import javax.el.ResourceBundleELResolver;
import javax.servlet.jsp.el.VariableResolver;

public class ELResolverImpl extends ELResolver {
	
	public final static ELResolver DefaultResolver = new CompositeELResolver();

	static {
		((CompositeELResolver) DefaultResolver).add(new MapELResolver());
		((CompositeELResolver) DefaultResolver).add(new ResourceBundleELResolver());
		((CompositeELResolver) DefaultResolver).add(new ListELResolver());
		((CompositeELResolver) DefaultResolver).add(new ArrayELResolver());
		((CompositeELResolver) DefaultResolver).add(new BeanELResolver());
	}

	private final VariableResolver variableResolver;

	public ELResolverImpl(VariableResolver variableResolver) {
		this.variableResolver = variableResolver;
	}

	public Object getValue(ELContext context, Object base, Object property)
			throws NullPointerException, PropertyNotFoundException, ELException {
		if (context == null) {
			throw new NullPointerException();
		}

		if (base == null) {
			context.setPropertyResolved(true);
			if (property != null) {
				try {
					return this.variableResolver.resolveVariable(property
							.toString());
				} catch (javax.servlet.jsp.el.ELException e) {
					throw new ELException(e.getMessage(), e.getCause());
				}
			}
		}

		if (!context.isPropertyResolved()) {
			return DefaultResolver.getValue(context, base, property);
		}
		return null;
	}

	public Class<?> getType(ELContext context, Object base, Object property)
			throws NullPointerException, PropertyNotFoundException, ELException {
		if (context == null) {
			throw new NullPointerException();
		}

		if (base == null) {
			context.setPropertyResolved(true);
			if (property != null) {
				try {
					Object obj = this.variableResolver.resolveVariable(property
							.toString());
					return (obj != null) ? obj.getClass() : null;
				} catch (javax.servlet.jsp.el.ELException e) {
					throw new ELException(e.getMessage(), e.getCause());
				}
			}
		}

		if (!context.isPropertyResolved()) {
			return DefaultResolver.getType(context, base, property);
		}
		return null;
	}

	public void setValue(ELContext context, Object base, Object property,
			Object value) throws NullPointerException,
			PropertyNotFoundException, PropertyNotWritableException,
			ELException {
		if (context == null) {
			throw new NullPointerException();
		}

		if (base == null) {
			context.setPropertyResolved(true);
			throw new PropertyNotWritableException(
					"Legacy VariableResolver wrapped, not writable");
		}

		if (!context.isPropertyResolved()) {
			DefaultResolver.setValue(context, base, property, value);
		}
	}

	public boolean isReadOnly(ELContext context, Object base, Object property)
			throws NullPointerException, PropertyNotFoundException, ELException {
		if (context == null) {
			throw new NullPointerException();
		}

		if (base == null) {
			context.setPropertyResolved(true);
			return true;
		}

		return DefaultResolver.isReadOnly(context, base, property);
	}

	public Iterator getFeatureDescriptors(ELContext context, Object base) {
		return DefaultResolver.getFeatureDescriptors(context, base);
	}

	public Class<?> getCommonPropertyType(ELContext context, Object base) {
		if (base == null) {
			return String.class;
		}
		return DefaultResolver.getCommonPropertyType(context, base);
	}

}
