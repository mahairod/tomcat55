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


/**
 * A validation message from a TagLibraryValidator.
 * <p>
 * A JSP container may (optionally) support a jsp:id attribute
 * to provide higher quality validation errors.
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

public class ValidationMessage {

    /**
     * Create a ValidationMessage.  The message String should be
     * non-null.  The value of id may be null, if the message
     * is not specific to any XML element, or if no jsp:id
     * attributes were passed on.  If non-null, the value of
     * id must be the value of a jsp:id attribute for the PageData
     * passed into the validate() method.
     *
     * @param id Either null, or the value of a jsp:id attribute.
     * @param message A localized validation message.
     */
    public ValidationMessage(String id, String message) {
	this.id = id;
	this.message = message;
    }


    /**
     * Get the jsp:id.
     * Null means that there is no information available.
     *
     * @return The jsp:id information.
     */
    public String getId() {
	return id;
    }

    /**
     * Get the localized validation message.
     *
     * @return A validation message
     */
    public String getMessage(){
	return message;
    }

    // Private data
    private String id;
    private String message;
}
