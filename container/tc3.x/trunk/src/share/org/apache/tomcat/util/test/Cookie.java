/*   
 *  Copyright 1999-2004 The Apache Sofware Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.tomcat.util.test;



/**
 *  Part of GTest
 * 
 */
public class Cookie {
    private String name;
    private String value;
    private int version;
    
    public Cookie() {}

    public void setName( String n ) {
	name=n;
    }

    public String getName() {
	return name;
    }
    
    public void setValue( String v ) {
	value=v;
    }

    public String getValue() {
	return value;
    }
    
    public void execute()
    {
    }

}
