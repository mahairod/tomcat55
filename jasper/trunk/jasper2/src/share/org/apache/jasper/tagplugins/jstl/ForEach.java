/*
 * $Header$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.jasper.tagplugins.jstl;

import org.apache.jasper.compiler.tagplugin.*;

public final class ForEach implements TagPlugin {

    public void doTag(TagPluginContext ctxt) {

	String index = null;

	boolean hasVarStatus = ctxt.isAttributeSpecified("varStatus");
	if (hasVarStatus) {
	    ctxt.dontUseTagPlugin();
	    return;
	}

	boolean hasItems = ctxt.isAttributeSpecified("items");
	if (hasItems) {
	    doCollection(ctxt);
	    return;
	}

	boolean hasVar = ctxt.isAttributeSpecified("var");
	boolean hasBegin = ctxt.isAttributeSpecified("begin");
	boolean hasEnd = ctxt.isAttributeSpecified("end");
	boolean hasStep = ctxt.isAttributeSpecified("step");

	// We must have a begin and end attributes
	index = ctxt.getTemporaryVariableName();
	ctxt.generateJavaSource("for (int " + index + " = ");
	ctxt.generateAttribute("begin");
	ctxt.generateJavaSource("; " + index + " <= ");
	ctxt.generateAttribute("end");
	if (hasStep) {
	    ctxt.generateJavaSource("; " + index + "+=");
	    ctxt.generateAttribute("step");
	    ctxt.generateJavaSource(") {");
	}
	else {
	    ctxt.generateJavaSource("; " + index + "++) {");
	}

	// If var is specified and the body contains an EL, then sycn
	// the var attribute
	if (hasVar /* && ctxt.hasEL() */) {
	    ctxt.generateJavaSource("pageContext.setAttribute(");
	    ctxt.generateAttribute("var");
	    ctxt.generateJavaSource(", String.valueOf(" + index + "));");
	}
	ctxt.generateBody();
	ctxt.generateJavaSource("}");
    }

    /**
     * Generate codes for Collections
     * The pseudo code is:
     */
    private void doCollection(TagPluginContext ctxt) {
	boolean hasVar = ctxt.isAttributeSpecified("var");
	boolean hasBegin = ctxt.isAttributeSpecified("begin");
	boolean hasEnd = ctxt.isAttributeSpecified("end");
	boolean hasStep = ctxt.isAttributeSpecified("step");

	ctxt.generateImport("java.util.*");

        String itemsV = ctxt.getTemporaryVariableName();
        ctxt.generateJavaSource("Object " + itemsV + "= ");
        ctxt.generateAttribute("items");
        ctxt.generateJavaSource(";");
	
	String indexV=null, beginV=null, endV=null, stepV=null;
	if (hasBegin) {
	    beginV = ctxt.getTemporaryVariableName();
	    ctxt.generateJavaSource("int " + beginV + " = ");
	    ctxt.generateAttribute("begin");
	    ctxt.generateJavaSource(";");
	}
	if (hasEnd) {
	    indexV = ctxt.getTemporaryVariableName();
	    ctxt.generateJavaSource("int " + indexV + " = 0;");
	    endV = ctxt.getTemporaryVariableName();
	    ctxt.generateJavaSource("int " + endV + " = ");
	    ctxt.generateAttribute("end");
	    ctxt.generateJavaSource(";");
	}
	if (hasStep) {
	    stepV = ctxt.getTemporaryVariableName();
	    ctxt.generateJavaSource("int " + stepV + " = ");
	    ctxt.generateAttribute("step");
	    ctxt.generateJavaSource(";");
	}

        ctxt.generateJavaSource("if (" + itemsV + " instanceof Collection) {");
        String iterV = ctxt.getTemporaryVariableName();
        ctxt.generateJavaSource("Iterator " + iterV + " = ");
	ctxt.generateJavaSource("((Collection)" + itemsV + ").iterator();");

	if (hasBegin) {
            String tV = ctxt.getTemporaryVariableName();
	    ctxt.generateJavaSource("for (int " + tV + "=" + beginV + ";" +
			tV + ">0 && " + iterV + ".hasNext(); " +
			tV + "--)");
	    ctxt.generateJavaSource(iterV + ".next();");
	}

	ctxt.generateJavaSource("while (" + iterV + ".hasNext()){");
        String nextV = ctxt.getTemporaryVariableName();
	ctxt.generateJavaSource("Object " + nextV + " = " + iterV + ".next();");
	if (hasVar) {
	    ctxt.generateJavaSource("pageContext.setAttribute(");
	    ctxt.generateAttribute("var");
	    ctxt.generateJavaSource(", " + nextV + ");");
	}

	ctxt.generateBody();

	if (hasStep) {
	    String tV = ctxt.getTemporaryVariableName();
	    ctxt.generateJavaSource("for (int " + tV + "=" + stepV + "-1;" +
			tV + ">0 && " + iterV + ".hasNext(); " +
			tV + "--)");
	    ctxt.generateJavaSource(iterV + ".next();");
	}
	if (hasEnd) {
	    if (hasStep) {
		ctxt.generateJavaSource(indexV + "+=" + stepV + ";");
	    }
	    else {
		ctxt.generateJavaSource(indexV + "++;");
	    }
	    if (hasBegin) {
		ctxt.generateJavaSource("if(" + beginV + "+" + indexV +
			">"+ endV + ")");
	    }
	    else {
		ctxt.generateJavaSource("if(" + indexV + ">" + endV + ")");
	    }
	    ctxt.generateJavaSource("break;");
	}
	ctxt.generateJavaSource("}");	// while
	ctxt.generateJavaSource("}");	// if
    }
}
