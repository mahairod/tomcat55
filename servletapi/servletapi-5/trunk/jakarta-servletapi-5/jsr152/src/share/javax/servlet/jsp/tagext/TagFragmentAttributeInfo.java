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
 * Information on the fragment attributes of a Tag, available at translation
 * time.  This class is instantiated from the Tag Library Descriptor file (TLD).
 *
 * <p>
 * Only the information needed to generate code is included here.  Other information
 * like SCHEMA for validation belongs elsewhere.
 */

public class TagFragmentAttributeInfo {

    /**
     * Information on the types of the inputs that may be passed to a fragment
     * by a tag handler.  This class is instantiated from the Tag Library
     * Descriptor file (TLD).
     */
    public static class FragmentInput {

        /**
         * Constructor for FragmentInput.
         *
         * @param name Name of the fragment input
         * @param type Type of the fragment input
         * @param description Description of the fragment input
         */
        public FragmentInput(String name, String type, String description) {
            this.name = name;
            this.type = type;
            this.description = description;
        }

        /**
         * The name of the fragment input.
         *
         * @return The name of the fragment input
         */

        public String getName() {
            return name;
        }

        /**
         * The type (as a String) of the fragment input.
         *
         * @return The type of the fragment input
         */

        public String getTypeName() {
            return type;
        }

        /**
         * The description of the fragment input.
         *
         * @return The description of the fragment input
         */

        public String getDescription() {
            return description;
        }

        private String name;
        private String type;
        private String description;
    }

    /**
     * Constructor for TagFragmentAttributeInfo.
     * This class is to be instantiated only from the
     * TagLibrary code under request from some JSP code that is parsing a
     * TLD (Tag Library Descriptor).
     *
     * @param name The name of the attribute.
     * @param required True if this fragment is required
     * @param description Decribes the purpose of this fragment
     * @param fragmentInputs An arry of TagFragmentAttributeInfo.FragmentInput
     *        that specifies the types of inputs that may be passed to this
     *        fragment by the tag handler
     */

    public TagFragmentAttributeInfo(String name, boolean required,
                        String description, FragmentInput[] fragmentInputs) {

	this.name = name;
        this.required = required;
        this.description = description;
        this.fragmentInputs = fragmentInputs;
    }

    /**
     * The name of the fragment attribute.
     *
     * @return The name of the fragment attribute
     */

    public String getName() {
	return name;
    }

    /**
     * The description of the fragment attribute.
     *
     * @return The description of the fragment attribute
     */

    public String getDescription() {
        return description;
    }

    /**
     * The fragment inputs of the fragment attribute.
     *
     * @return An array of FragmentInput
     */

    public FragmentInput[] getFragmentInputs() {
        return fragmentInputs;
    }

    /**
     * Whether this attribute is required.
     *
     * @return if the attribute is required.
     */
    public boolean isRequired() {
        return required;
    }

    /*
     * fields
     */

    private String name;
    private boolean required;
    private String description;
    private FragmentInput[] fragmentInputs;
}
