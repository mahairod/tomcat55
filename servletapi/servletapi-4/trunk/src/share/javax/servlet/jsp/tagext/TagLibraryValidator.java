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

package javax.servlet.jsp.tagext;

import java.util.Map;

/**
 * Translation-time validator class for a JSP page. 
 * A validator operates on the XML document associated with the JSP page.
 *
 * <p>
 * The TLD file associates a TagLibraryValidator class and some init
 * arguments with a tag library.
 *
 * <p>
 * The JSP container is reponsible for locating an appropriate
 * instance of the appropriate subclass by
 *
 * <ul>
 * <li> new a fresh instance, or reuse an available one
 * <li> invoke the setInitParams(Map) method on the instance
 * </ul>
 *
 * once initialized, the validate(String, String, PageData) method will
 * be invoked, where the first two arguments are the prefix
 * and uri arguments used in the taglib directive.
 *
 * <p>
 * A TagLibraryValidator instance
 * may create auxiliary objects internally to perform
 * the validation (e.g. an XSchema validator) and may reuse it for all
 * the pages in a given translation run.
 *
 * <p>
 * The JSP container is not guaranteed to serialize invocations of
 * validate() method, and TagLibraryValidators should perform any
 * synchronization they may require.
 *
 * <p>
 * A JSP container may optionally support a jsp:id attribute to
 * provide higher quality validation errors.
 * When supported, the container will track the JSP pages
 * as passed to the container, and will assign to each element
 * a unique "id", which is passed as the value of the jsp:id
 * attribute.  Each XML element in the XML view available will
 * be extended with this attribute.  The TagLibraryValidator
 * can then use the attribute in one or more ValidationMessage
 * objects.  The container then, in turn, can use these
 * values to provide more precise information on the location
 * of an error.
 */

abstract public class TagLibraryValidator {

    /**
     * Set the init data in the TLD for this validator.
     * Parameter names are keys, and parameter values are the values.
     *
     * @param initMap A Map describing the init parameters
     */
    public void setInitParameters(Map map) {
	initParameters = map;
    }


    /**
     * Get the init parameters data as an immutable Map.
     * Parameter names are keys, and parameter values are the values.
     *
     * @return The init parameters as an immutable map.
     */
    public Map getInitParameters() {
	return initParameters;
    }

    /**
     * Validate a JSP page.
     * This will get invoked once per directive in the JSP page.
     * This method will return null if the page is valid; otherwise
     * the method should return an array of ValidationMessage objects.
     * An array of length zero is also interpreted as no errors.
     *
     * @param prefix the value of the prefix argument in the directive
     * @param uri the value of the uri argument in the directive
     * @param thePage the JspData page object
     * @return A null object, or zero length array if no errors, an array
     * of ValidationMessages otherwise.
     */
    public ValidationMessage[] validate(String prefix, String uri, PageData page) {
	return null;
    }

    /**
     * Release any data kept by this instance for validation purposes
     */
    public void release() {
	initParameters = null;
    };

    // Private data
    private Map initParameters;

}
