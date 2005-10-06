/*
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
package org.apache.jasper34.core;

/**
 * Customization for the class name generator. 
 * 
 * You can control attributes like classname, packagename etc by
 * plugging in your own mangler.
 *
 * The container liaison must implement this interface ( or use one 
 * of the existing manglers ).
 *
 * @author Anil K. Vijendran
 */
public abstract class Mangler {
    protected String baseDir;
    protected String classDir;
    protected String srcFile;
    

    public void init( String classDir, String baseDir, String srcFile ) {
	this.srcFile=srcFile;
	this.baseDir=baseDir;
	this.classDir=classDir;
    }
    
    
    /** The class name ( without package ) of the
     *  generated servlet, including the version number
     */
    public abstract String getClassName();

    /** The package name. It is based on the .jsp path, with
     *  all unsafe components escaped.
     */
    public abstract String getPackageName();

    /** The full name of the .java file, including
     *  version number ( based on className and outputDir )
     */
    public abstract String getJavaFileName();

    /** The full name of the .class file ( without version number)
     */
    public abstract String getClassFileName();

    // -------------------- Utils --------------------

    /** The class name ( package + class + versioning ) of the
     *  compilation result
     */
    public String getServletClassName() {
	if( getPackageName() !=null ) {
	    return getPackageName()  + "." + getClassName();
	} else {
	    return getClassName();
	}
    }
    
    // -------------------- Versioning support --------------------

    public int getVersion() {
	return 0;
    }

    public void nextVersion() {
    }

    // -------------------- Utils --------------------

    
    
}
