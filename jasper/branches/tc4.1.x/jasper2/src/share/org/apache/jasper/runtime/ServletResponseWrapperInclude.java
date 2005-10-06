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

package org.apache.jasper.runtime;

import java.lang.IllegalStateException;
import java.io.PrintWriter;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;

/**
 * ServletResponseWrapper used for the JSP 'include' action.
 *
 * This 'wrapped' response object is passed as the second argument 
 * to the internal RequestDispatcher.include(). It channels
 * all output text into the current JspWriter.
 *
 * @author Pierre Delisle
 */

public class ServletResponseWrapperInclude
    extends HttpServletResponseWrapper
{
    /**
     * The PrintWriter writes all output to the JspWriter of the 
     * including page.
     */
    PrintWriter printWriter;

    public ServletResponseWrapperInclude(ServletResponse response, 
                                         JspWriter jspWriter) 
    {
        super((HttpServletResponse)response);
        this.printWriter = new PrintWriter(jspWriter);
    }

    /**
     * Returns a wrapper around the JspWriter of the including page.
     */
    public java.io.PrintWriter getWriter()
        throws java.io.IOException 
    {
        return printWriter;
    }

    public ServletOutputStream getOutputStream()
        throws java.io.IOException
    {
        throw new IllegalStateException();
    }
}
