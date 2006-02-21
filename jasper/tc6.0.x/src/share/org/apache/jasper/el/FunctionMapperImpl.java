package org.apache.jasper.el;

import java.lang.reflect.Method;

import javax.servlet.jsp.el.FunctionMapper;

public class FunctionMapperImpl extends javax.el.FunctionMapper {
	
	private final FunctionMapper fnMapper;

	public FunctionMapperImpl(FunctionMapper fnMapper) {
		this.fnMapper = fnMapper;
	}

	public Method resolveFunction(String prefix, String localName) {
		return this.fnMapper.resolveFunction(prefix, localName);
	}

}
