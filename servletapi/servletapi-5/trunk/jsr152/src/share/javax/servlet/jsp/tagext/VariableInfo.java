/*
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
 
package javax.servlet.jsp.tagext;

/**
 * Information on the scripting variables that are created/modified by
 * a tag (at run-time). This information is provided by TagExtraInfo
 * classes and it is used by the translation phase of JSP.
 *
 * <p>
 * Scripting variables generated by a custom action may have scope values
 * of page, request, session, and application.
 *
 * <p>
 * The class name (VariableInfo.getClassName) in the returned objects
 * are used to determine the types of the scripting variables.
 * Because of this, a custom action cannot create a scripting variable
 * of a primitive type. The workaround is to use &quot;boxed&quot;
 * types.
 *
 * <p>
 * The class name may be a Fully Qualified Class Name, or a short
 * class name.
 *
 * <p>
 * If a Fully Qualified Class Name is provided, it should refer to a
 * class that should be in the CLASSPATH for the Web Application (see
 * Servlet 2.4 specification - essentially it is WEB-INF/lib and
 * WEB-INF/classes). Failure to be so will lead to a translation-time
 * error.
 *
 * <p>
 * If a short class name is given in the VariableInfo objects, then
 * the class name must be that of a public class in the context of the
 * import directives of the page where the custom action appears (will
 * check if there is a JLS verbiage to refer to). The class must also
 * be in the CLASSPATH for the Web Application (see Servlet 2.4
 * specification - essentially it is WEB-INF/lib and
 * WEB-INF/classes). Failure to be so will lead to a translation-time
 * error.
 *
 * <p><B>Usage Comments</B>
 * <p>
 * Frequently a fully qualified class name will refer to a class that
 * is known to the tag library and thus, delivered in the same JAR
 * file as the tag handlers. In most other remaining cases it will
 * refer to a class that is in the platform on which the JSP processor
 * is built (like J2EE). Using fully qualified class names in this
 * manner makes the usage relatively resistant to configuration
 * errors.
 *
 * <p>
 * A short name is usually generated by the tag library based on some
 * attributes passed through from the custom action user (the author),
 * and it is thus less robust: for instance a missing import directive
 * in the referring JSP page will lead to an invalid short name class
 * and a translation error.
 *
 * <p><B>Synchronization Protocol</B>
 *
 * <p>
 * The result of the invocation on getVariableInfo is an array of
 * VariableInfo objects.  Each such object describes a scripting
 * variable by providing its name, its type, whether the variable is
 * new or not, and what its scope is.  Scope is best described through
 * a picture:
 *
 * <p>
 * <IMG src="doc-files/VariableInfo-1.gif"/>
 *
 *<p>
 * The JSP 2.0 specification defines the interpretation of 3 values:
 * 
 * <ul>
 * <li> NESTED, if the scripting variable is available between
 * the start tag and the end tag of the action that defines it.
 * <li>
 * AT_BEGIN, if the scripting variable is available from the start tag
 * of the action that defines it until the end of the scope.
 * <li> AT_END, if the scripting variable is available after the end tag
 * of the action that defines it until the end of the scope.
 * </ul>
 *
 * The scope value for a variable implies what methods may affect its
 * value and thus where synchronization is needed:
 *
 * <ul>
 * <li>
 * for NESTED, after doInitBody and doAfterBody for a tag handler implementing
 * BodyTag, after doAfterBody for a tag handler implementing IterationTag,
 * and in all cases, after doStartTag.
 * <li>
 * for AT_BEGIN, after doInitBody, doAfterBody for a tag handler implementing
 * BodyTag, after doAfterBody for a tag handler implementing IterationTag, and
 * in all cases after doStartTag and doEndTag.
 * <li>
 * for AT_END, after doEndTag method.
 * </ul>
 *
 * <p><B>Variable Information in the TLD</B>
 * <p>
 * Scripting variable information can also be encoded directly for most cases
 * into the Tag Library Descriptor using the &lt;variable&gt; subelement of the
 * &lt;tag&gt; element.  See the JSP specification.
 */

public class VariableInfo {

    /**
     * Scope information that scripting variable is visible only within the
     * start/end tags.
     */
    public static final int NESTED = 0;

    /**
     * Scope information that scripting variable is visible after start tag.
     */
    public static final int AT_BEGIN = 1;

    /**
     * Scope information that scripting variable is visible after end tag.
     */
    public static final int AT_END = 2;


    /**
     * Constructor
     * These objects can be created (at translation time) by the TagExtraInfo
     * instances.
     *
     * @param varName The name of the scripting variable
     * @param className The type of this variable
     * @param declare If true, it is a new variable (in some languages this will
     *     require a declaration)
     * @param scope Indication on the lexical scope of the variable
     */

    public VariableInfo(String varName,
			String className,
			boolean declare,
			int scope) {
	this.varName = varName;
	this.className = className;
	this.declare = declare;
	this.scope = scope;
    }

    // Accessor methods
    
    /**
     * Returns the name of the scripting variable.
     *
     * @return the name of the scripting variable
     */
    public String getVarName() { 
        return varName; 
    }
    
    /**
     * Returns the type of this variable.
     *
     * @return the type of this variable
     */
    public String getClassName() { 
        return className; 
    }
    
    /**
     * Returns whether this is a new variable.
     * If so, in some languages this will require a declaration.
     *
     * @return whether this is a new variable.
     */
    public boolean getDeclare() { 
        return declare; 
    }
    
    /**
     * Returns the lexical scope of the variable.
     * 
     * @return the lexical scope of the variable, either AT_BEGIN, AT_END,
     *    or NESTED.
     * @see #AT_BEGIN
     * @see #AT_END
     * @see #NESTED
     */
    public int getScope() { 
        return scope; 
    }


    // == private data
    private String varName;
    private String className;
    private boolean declare;
    private int scope;
}

