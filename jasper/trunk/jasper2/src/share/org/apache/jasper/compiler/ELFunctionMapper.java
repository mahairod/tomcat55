/* ========================================================================= *
 *                                                                           *
 *                 The Apache Software License,  Version 1.1                 *
 *                                                                           *
 *      Copyright (c) 1999, 2000, 2001  The Apache Software Foundation.      *
 *                           All rights reserved.                            *
 *                                                                           *
 * ========================================================================= *
 *                                                                           *
 * Redistribution and use in source and binary forms,  with or without modi- *
 * fication, are permitted provided that the following conditions are met:   *
 *                                                                           *
 * 1. Redistributions of source code  must retain the above copyright notice *
 *    notice, this list of conditions and the following disclaimer.          *
 *                                                                           *
 * 2. Redistributions  in binary  form  must  reproduce the  above copyright *
 *    notice,  this list of conditions  and the following  disclaimer in the *
 *    documentation and/or other materials provided with the distribution.   *
 *                                                                           *
 * 3. The end-user documentation  included with the redistribution,  if any, *
 *    must include the following acknowlegement:                             *
 *                                                                           *
 *       "This product includes  software developed  by the Apache  Software *
 *        Foundation <http://www.apache.org/>."                              *
 *                                                                           *
 *    Alternately, this acknowlegement may appear in the software itself, if *
 *    and wherever such third-party acknowlegements normally appear.         *
 *                                                                           *
 * 4. The names  "The  Jakarta  Project",  "Tomcat",  and  "Apache  Software *
 *    Foundation"  must not be used  to endorse or promote  products derived *
 *    from this  software without  prior  written  permission.  For  written *
 *    permission, please contact <apache@apache.org>.                        *
 *                                                                           *
 * 5. Products derived from this software may not be called "Apache" nor may *
 *    "Apache" appear in their names without prior written permission of the *
 *    Apache Software Foundation.                                            *
 *                                                                           *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES *
 * INCLUDING, BUT NOT LIMITED TO,  THE IMPLIED WARRANTIES OF MERCHANTABILITY *
 * AND FITNESS FOR  A PARTICULAR PURPOSE  ARE DISCLAIMED.  IN NO EVENT SHALL *
 * THE APACHE  SOFTWARE  FOUNDATION OR  ITS CONTRIBUTORS  BE LIABLE  FOR ANY *
 * DIRECT,  INDIRECT,   INCIDENTAL,  SPECIAL,  EXEMPLARY,  OR  CONSEQUENTIAL *
 * DAMAGES (INCLUDING,  BUT NOT LIMITED TO,  PROCUREMENT OF SUBSTITUTE GOODS *
 * OR SERVICES;  LOSS OF USE,  DATA,  OR PROFITS;  OR BUSINESS INTERRUPTION) *
 * HOWEVER CAUSED AND  ON ANY  THEORY  OF  LIABILITY,  WHETHER IN  CONTRACT, *
 * STRICT LIABILITY, OR TORT  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN *
 * ANY  WAY  OUT OF  THE  USE OF  THIS  SOFTWARE,  EVEN  IF  ADVISED  OF THE *
 * POSSIBILITY OF SUCH DAMAGE.                                               *
 *                                                                           *
 * ========================================================================= *
 *                                                                           *
 * This software  consists of voluntary  contributions made  by many indivi- *
 * duals on behalf of the  Apache Software Foundation.  For more information *
 * on the Apache Software Foundation, please see <http://www.apache.org/>.   *
 *                                                                           *
 * ========================================================================= */

package org.apache.jasper.compiler;

import java.util.*;
import javax.servlet.jsp.tagext.FunctionInfo;
import org.apache.jasper.JasperException;

/**
 * This class generates a mapper for an EL expression
 * Instead of a global mapper, a mapper is used for ecah call to EL
 * evaluator, thus avoiding the prefix overlapping and redefinition
 * issues.
 */

public class ELFunctionMapper {
    static private int currFunc = 0;
    private ErrorDispatcher err;
    StringBuffer ds;
    StringBuffer ss;

    public static void map(Compiler compiler, Node.Nodes page) 
		throws JasperException {

	currFunc = 0;
	ELFunctionMapper map = new ELFunctionMapper();
	map.err = compiler.getErrorDispatcher();
	map.ds = new StringBuffer();
	map.ss = new StringBuffer();

	map.ds.append("static {\n");
	page.visit(map.new ELFunctionVisitor());
	map.ds.append("}\n");

	// Append the declarations to the root node
	Node root = page.getRoot();
	new Node.Declaration(map.ss.toString(), root.getStart(), root);
	new Node.Declaration(map.ds.toString(), root.getStart(), root);
    }

    class ELFunctionVisitor extends Node.Visitor {
	
	public void visit(Node.ParamAction n) throws JasperException {
	    doMap(n.getValue());
	}

	public void visit(Node.IncludeAction n) throws JasperException {
	    doMap(n.getPage());
	}

	public void visit(Node.ForwardAction n) throws JasperException {
	    doMap(n.getPage());
	}

        public void visit(Node.SetProperty n) throws JasperException {
	    doMap(n.getValue());
	}

        public void visit(Node.UseBean n) throws JasperException {
	    doMap(n.getBeanName());
	}

        public void visit(Node.PlugIn n) throws JasperException {
	    doMap(n.getHeight());
	    doMap(n.getWidth());
	}

        public void visit(Node.JspElement n) throws JasperException {

	    Node.JspAttribute[] attrs = n.getJspAttributes();
	    for (int i = 0; i < attrs.length; i++) {
		doMap(attrs[i]);
	    }
	    doMap(n.getNameAttribute());
	}

        public void visit(Node.CustomTag n) throws JasperException {
	    Node.JspAttribute[] attrs = n.getJspAttributes();
	    for (int i = 0; i < attrs.length; i++) {
		doMap(attrs[i]);
	    }
	}

        public void visit(Node.ELExpression n) throws JasperException {
	    doMap(n.getEL());
	}

	private void doMap(Node.JspAttribute attr) 
		throws JasperException {
	    if (attr != null) {
		doMap(attr.getEL());
	    }
	}

	private void doMap(ELNode.Nodes el) 
		throws JasperException {

	    class Fvisitor extends ELNode.Visitor {
		ArrayList funcs = new ArrayList();
		public void visit(ELNode.Function n) throws JasperException {
		    funcs.add(n);
		}
	    }

	    if (el == null) {
		return;
	    }

	    // First locate all functions in this expression
	    Fvisitor fv = new Fvisitor();
	    el.visit(fv);
	    ArrayList functions = fv.funcs;

	    // TODO Some optimization here: if the fmap has only one entry,
	    // if it was generated before, use it.

	    if (functions.size() == 0) {
		return;
	    }

	    // Generate declaration for the map statically
	    String decName = getMapName();
	    ss.append("static private org.apache.jasper.runtime.ProtectedFunctionMapper " + decName + ";\n");
	    ds.append("  " + decName + " = org.apache.jasper.runtime.ProtectedFunctionMapper.getInstance();\n");

	    for (int i = 0; i < functions.size(); i++) {
		ELNode.Function f = (ELNode.Function)functions.get(i);
		FunctionInfo funcInfo = f.getFunctionInfo();
		String key = f.getPrefix()+ ":" + f.getName();
		ds.append("  " + decName + ".mapFunction(\"" + key + "\", " +
			funcInfo.getFunctionClass() + ".class, " +
			'\"' + getMethod(f) + "\", " +
			"new Class[] {" + getParameters(f) + "}" + 
			");\n");
	    }
	    el.setMapName(decName);
	}

	private String getMapName() {
	    return "_jspx_fnmap_" + currFunc++;
	}

	private String getMethod(ELNode.Function func)
		throws JasperException {
	    FunctionInfo funcInfo = func.getFunctionInfo();
	    String signature = funcInfo.getFunctionSignature();
	    
	    int start = signature.indexOf(' ');
	    if (start < 0) {
		err.jspError("jsp.error.tld.fn.invalid.signature",
			func.getPrefix(), func.getName());
	    }
	    int end = signature.indexOf('(');
	    if (end < 0) {
		err.jspError("jsp.error.tld.fn.invalid.signature.parenexpected",
			func.getPrefix(), func.getName());
	    }
	    return signature.substring(start+1, end).trim();
	}

	private String getParameters(ELNode.Function func) 
		throws JasperException {
	    FunctionInfo funcInfo = func.getFunctionInfo();
	    StringBuffer buf = new StringBuffer();
	    String signature = funcInfo.getFunctionSignature();
	    // Signature is of the form
	    // <return-type> S <method-name S? '('
	    // < <arg-type> ( ',' <arg-type> )* )? ')'
	    int start = signature.indexOf('(') + 1;
	    boolean lastArg = false;
	    while (true) {
		int p = signature.indexOf(',', start);
		if (p < 0) {
		    p = signature.indexOf(')', start);
		    if (p < 0) {
			err.jspError("jsp.error.tld.fn.invalid.signature",
				func.getPrefix(), func.getName());
		    }
		    lastArg = true;
		}
		String arg = signature.substring(start, p).trim();
		buf.append(arg + ".class");
		if (lastArg) {
		    break;
		}
		buf.append(',');
		start = p+1;
	    }
	    return buf.toString();
	}
    }
}

