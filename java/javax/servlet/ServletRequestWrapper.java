/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package javax.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;



/**
 * 
 * Provides a convenient implementation of the ServletRequest interface that
 * can be subclassed by developers wishing to adapt the request to a Servlet.
 * This class implements the Wrapper or Decorator pattern. Methods default to
 * calling through to the wrapped request object.
  * @since	v 2.3
 * 
 * 
 *
 * @see 	javax.servlet.ServletRequest
 *
 */

public class ServletRequestWrapper implements ServletRequest {
    private ServletRequest request;

	/**
	* Creates a ServletRequest adaptor wrapping the given request object. 
	* @throws java.lang.IllegalArgumentException if the request is null
	*/

    public ServletRequestWrapper(ServletRequest request) {
	if (request == null) {
	    throw new IllegalArgumentException("Request cannot be null");   
	}
	this.request = request;
    }

	/**
	* Return the wrapped request object.
	*/
	public ServletRequest getRequest() {
		return this.request;
	}
	
	/**
	* Sets the request object being wrapped. 
	* @throws java.lang.IllegalArgumentException if the request is null.
	*/
	
	public void setRequest(ServletRequest request) {
	    if (request == null) {
		throw new IllegalArgumentException("Request cannot be null");
	    }
	    this.request = request;
	}

    /**
     *
     * The default behavior of this method is to call getAttribute(String name)
     * on the wrapped request object.
     */

    public Object getAttribute(String name) {
	return this.request.getAttribute(name);
	}
    
    

    /**
     * The default behavior of this method is to return getAttributeNames()
     * on the wrapped request object.
     */

    public Enumeration<String> getAttributeNames() {
	return this.request.getAttributeNames();
	}    
    
    
    
    /**
      * The default behavior of this method is to return getCharacterEncoding()
     * on the wrapped request object.
     */

    public String getCharacterEncoding() {
	return this.request.getCharacterEncoding();
	}
	
    /**
      * The default behavior of this method is to set the character encoding
     * on the wrapped request object.
     */

    public void setCharacterEncoding(String enc) throws java.io.UnsupportedEncodingException {
	this.request.setCharacterEncoding(enc);
	}
    
    
    /**
      * The default behavior of this method is to return getContentLength()
     * on the wrapped request object.
     */

    public int getContentLength() {
	return this.request.getContentLength();
    }
    
    
    

       /**
      * The default behavior of this method is to return getContentType()
     * on the wrapped request object.
     */
    public String getContentType() {
	return this.request.getContentType();
    }
    
    
    

     /**
      * The default behavior of this method is to return getInputStream()
     * on the wrapped request object.
     */

    public ServletInputStream getInputStream() throws IOException {
	return this.request.getInputStream();
	}
     
    
    

    /**
      * The default behavior of this method is to return getParameter(String name)
     * on the wrapped request object.
     */

    public String getParameter(String name) {
	return this.request.getParameter(name);
    }
    
    /**
      * The default behavior of this method is to return getParameterMap()
     * on the wrapped request object.
     */
    public Map<String,String[]> getParameterMap() {
	return this.request.getParameterMap();
    }
    
    
    

    /**
      * The default behavior of this method is to return getParameterNames()
     * on the wrapped request object.
     */
     
    public Enumeration<String> getParameterNames() {
	return this.request.getParameterNames();
    }
    
    
    

       /**
      * The default behavior of this method is to return getParameterValues(String name)
     * on the wrapped request object.
     */
    public String[] getParameterValues(String name) {
	return this.request.getParameterValues(name);
	}
    
    
    

     /**
      * The default behavior of this method is to return getProtocol()
     * on the wrapped request object.
     */
    
    public String getProtocol() {
	return this.request.getProtocol();
	}
    
    
    

    /**
      * The default behavior of this method is to return getScheme()
     * on the wrapped request object.
     */
    

    public String getScheme() {
	return this.request.getScheme();
	}
    
    
    

    /**
      * The default behavior of this method is to return getServerName()
     * on the wrapped request object.
     */
    public String getServerName() {
	return this.request.getServerName();
	}
    
    
    

   /**
      * The default behavior of this method is to return getServerPort()
     * on the wrapped request object.
     */

    public int getServerPort() {
	return this.request.getServerPort();
	}
    
    
    
  /**
      * The default behavior of this method is to return getReader()
     * on the wrapped request object.
     */

    public BufferedReader getReader() throws IOException {
	return this.request.getReader();
	}
    
    
    

    /**
      * The default behavior of this method is to return getRemoteAddr()
     * on the wrapped request object.
     */
    
    public String getRemoteAddr() {
	return this.request.getRemoteAddr();
    }
    
    
    

      /**
      * The default behavior of this method is to return getRemoteHost()
     * on the wrapped request object.
     */

    public String getRemoteHost() {
	return this.request.getRemoteHost();
    }
    
    
    

    /**
      * The default behavior of this method is to return setAttribute(String name, Object o)
     * on the wrapped request object.
     */

    public void setAttribute(String name, Object o) {
	this.request.setAttribute(name, o);
    }
    
    
    

    /**
      * The default behavior of this method is to call removeAttribute(String name)
     * on the wrapped request object.
     */
    public void removeAttribute(String name) {
	this.request.removeAttribute(name);
    }
    
    
    

   /**
      * The default behavior of this method is to return getLocale()
     * on the wrapped request object.
     */

    public Locale getLocale() {
	return this.request.getLocale();
    }
    
    
    

     /**
      * The default behavior of this method is to return getLocales()
     * on the wrapped request object.
     */

    public Enumeration<Locale> getLocales() {
	return this.request.getLocales();
    }
    
    
    

    /**
      * The default behavior of this method is to return isSecure()
     * on the wrapped request object.
     */

    public boolean isSecure() {
	return this.request.isSecure();
    }
    
    
    

    /**
      * The default behavior of this method is to return getRequestDispatcher(String path)
     * on the wrapped request object.
     */

    public RequestDispatcher getRequestDispatcher(String path) {
	return this.request.getRequestDispatcher(path);
    }
    
    
    

    /**
      * The default behavior of this method is to return getRealPath(String path)
     * on the wrapped request object.
     * @deprecated As of Version 3.0 of the Java Servlet API
     */

    public String getRealPath(String path) {
	return this.request.getRealPath(path);
    }
    
    /**
     * The default behavior of this method is to return
     * getRemotePort() on the wrapped request object.
     *
     * @since 2.4
     */    
    public int getRemotePort(){
        return this.request.getRemotePort();
    }


    /**
     * The default behavior of this method is to return
     * getLocalName() on the wrapped request object.
     *
     * @since 2.4
     */
    public String getLocalName(){
        return this.request.getLocalName();
    }

    /**
     * The default behavior of this method is to return
     * getLocalAddr() on the wrapped request object.
     *
     * @since 2.4
     */       
    public String getLocalAddr(){
        return this.request.getLocalAddr();
    }


    /**
     * The default behavior of this method is to return
     * getLocalPort() on the wrapped request object.
     *
     * @since 2.4
     */
    public int getLocalPort(){
        return this.request.getLocalPort();
    }

    /**
     * The default behavior of this method is to return
     * getServletContext() on the wrapped request object.
     * 
     * @return
     * @since Servlet 3.0
     */
    public ServletContext getServletContext() {
        return request.getServletContext();
    }
    
    /**
     * The default behavior of this method is to return
     * startAsync() on the wrapped request object.
     * 
     * @return
     * @throws java.lang.IllegalStateException
     * @since Servlet 3.0
     */
    public AsyncContext startAsync() {
        return request.startAsync();
    }
    
    /**
     * The default behavior of this method is to return
     * startAsync(Runnable) on the wrapped request object.
     * 
     * @param servletRequest
     * @param servletResponse
     * @return
     * @throws java.lang.IllegalStateException
     * @since Servlet 3.0
     */
    public AsyncContext startAsync(ServletRequest servletRequest,
            ServletResponse servletResponse)
            throws java.lang.IllegalStateException {
        return request.startAsync(servletRequest, servletResponse);
    }
    
    /**
     * The default behavior of this method is to return
     * isAsyncStarted() on the wrapped request object.
     * 
     * @return
     * @since Servlet 3.0
     */
    public boolean isAsyncStarted() {
        return request.isAsyncStarted();
    }

    /**
     * The default behavior of this method is to return
     * isAsyncSupported() on the wrapped request object.
     * 
     * @return
     * @since Servlet 3.0
     */
    public boolean isAsyncSupported() {
        return request.isAsyncSupported();
    }
    
    /**
     * The default behavior of this method is to return
     * getAsyncContext() on the wrapped request object.
     * 
     * @return
     * @since Servlet 3.0
     */
    public AsyncContext getAsyncContext() {
        return request.getAsyncContext();
    }

    /**
     * The default behavior of this method is to call
     * addAsyncListener(AsyncListener) on the wrapped request object.
     * 
     * @param listener
     * @since Servlet 3.0
     */
    public void addAsyncListener(AsyncListener listener) {
        request.addAsyncListener(listener);
    }
    
    /**
     * The default behavior of this method is to call
     * addAsyncListener(AsyncListener, ServletRequest, ServletResponse) on the
     * wrapped request object.
     * 
     * @param listener
     * @param servletRequest
     * @param servletResponse
     * @since Servlet 3.0
     */
    public void addAsyncListener(AsyncListener listener,
            ServletRequest servletRequest, ServletResponse servletResponse) {
        request.addAsyncListener(listener, servletRequest, servletResponse);
    }
    
    /**
     * The default behavior of this method is to call
     * setAsyncTimeout(long) on the wrapped request object.
     * 
     * @param listener
     * @since Servlet 3.0
     */
    public void setAsyncTimeout(long timeout) {
        request.setAsyncTimeout(timeout);
    }
    
    /**
     * The default behavior of this method is to call
     * getAsyncTimeout() on the wrapped request object.
     * 
     * @since Servlet 3.0
     */
    public long getAsyncTimeout() {
        return request.getAsyncTimeout();
    }
    
    /**
     * 
     * @param wrapped
     * @since Servlet 3.0
     * TODO SERVLET3 - Add comments
     */
    public boolean isWrapperFor(ServletRequest wrapped) {
        if (request == wrapped) {
            return true;
        }
        if (request instanceof ServletRequestWrapper) {
            return ((ServletRequestWrapper)request).isWrapperFor(wrapped);
        }
        return false;
    }
    
    /**
     * 
     * @param wrappedType
     * @since Servlet 3.0
     * TODO SERVLET3 - Add comments
     */
    public boolean isWrapperFor(Class<? extends ServletRequest> wrappedType) {
        if (wrappedType.isAssignableFrom(request.getClass())) {
            return true;
        }
        if (request instanceof ServletRequestWrapper) {
            return ((ServletRequestWrapper)request).isWrapperFor(wrappedType);
        }
        return false;
    }
    
    /**
     * The default behavior of this method is to call
     * getDispatcherType() on the wrapped request object.
     * 
     * @since Servlet 3.0
     */
    public DispatcherType getDispatcherType() {
        return this.request.getDispatcherType();
    }
}

