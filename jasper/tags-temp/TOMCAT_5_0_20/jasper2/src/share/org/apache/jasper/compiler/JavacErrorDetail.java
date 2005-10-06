/*
 * Copyright 1999,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jasper.compiler;

/**
 * Class providing details about a javac compilation error.
 *
 * @author Jan Luehe
 * @author Kin-man Chung
 */
public class JavacErrorDetail {

    private String javaFileName;
    private int javaLineNum;
    private String jspFileName;
    private int jspBeginLineNum;
    private String errMsg;

    /**
     * Constructor.
     *
     * @param javaFileName The name of the Java file in which the 
     * compilation error occurred
     * @param javaLineNum The compilation error line number
     * @param jspFileName The name of the JSP file from which the Java source
     * file was generated
     * @param jspBeginLineNum The start line number of the JSP element
     * responsible for the compilation error
     * @param errMsg The compilation error message
     */
    public JavacErrorDetail(String javaFileName,
			    int javaLineNum,
			    String jspFileName,
			    int jspBeginLineNum,
			    String errMsg) {
	this.javaFileName = javaFileName;
	this.javaLineNum = javaLineNum;
	this.jspFileName = jspFileName;
	this.jspBeginLineNum = jspBeginLineNum;
	this.errMsg = errMsg;
    }

    /**
     * Gets the name of the Java source file in which the compilation error
     * occurred.
     *
     * @return Java source file name
     */
    public String getJavaFileName() {
	return this.javaFileName;
    }

    /**
     * Gets the compilation error line number.
     * 
     * @return Compilation error line number
     */
    public int getJavaLineNumber() {
	return this.javaLineNum;
    }

    /**
     * Gets the name of the JSP file from which the Java source file was
     * generated.
     *
     * @return JSP file from which the Java source file was generated.
     */
    public String getJspFileName() {
	return this.jspFileName;
    }

    /**
     * Gets the start line number (in the JSP file) of the JSP element
     * responsible for the compilation error.
     *
     * @return Start line number of the JSP element responsible for the
     * compilation error
     */
    public int getJspBeginLineNumber() {
	return this.jspBeginLineNum;
    }

    /**
     * Gets the compilation error message.
     *
     * @return Compilation error message
     */
    public String getErrorMessage() {
	return this.errMsg;
    }
}
