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
 * For a tag to declare that it accepts dynamic attributes, it must implement
 * this interface.  The entry for the tag in the Tag Library Descriptor must 
 * also be configured to indicate dynamic attributes are accepted.
 * <br>
 * For any attribute that is not declared in the Tag Library Descriptor for
 * this tag, instead of getting an error at translation time, the 
 * <code>setDynamicAttribute()</code> method is called, with the name and
 * value of the attribute.  It is the responsibility of the tag to 
 * remember the names and values of the dynamic attributes.
 */

public interface DynamicAttributes {
    
    /**
     * Called when a tag declared to accept dynamic attributes is passed
     * an attribute that is not declared in the Tag Library Descriptor.
     * 
     * @param uri the namespace of the attribute, or null if in the default
     *     namespace.
     * @param localName the name of the attribute being set.
     * @param value the value of the attribute
     * @throws AttributeNotSupportedException if the tag handler wishes to
     *     signal that it does not accept the given attribute.  The 
     *     container must catch this exception and rethrow a JspException
     *     to the calling page before calling doStartTag() or doTag(). 
     *     If a message is provided in the AttributeNotSupportedException, 
     *     the corresponding JspException must contain the same message.
     */
    public void setDynamicAttribute(
        String uri, String localName, Object value ) 
        throws AttributeNotSupportedException;
    
}
