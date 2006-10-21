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


package jsp2.examples.simpletag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import javax.servlet.jsp.tagext.DynamicAttributes;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.IOException;

/**
 * SimpleTag handler that echoes all its attributes 
 */
public class EchoAttributesTag 
    extends SimpleTagSupport
    implements DynamicAttributes
{
    private ArrayList keys = new ArrayList();
    private ArrayList values = new ArrayList();

    public void doTag() throws JspException, IOException {
	JspWriter out = getJspContext().getOut();
	for( int i = 0; i < keys.size(); i++ ) {
	    String key = (String)keys.get( i );
	    Object value = values.get( i );
	    out.println( "<li>" + key + " = " + value + "</li>" );
        }
    }

    public void setDynamicAttribute( String uri, String localName, 
	Object value ) 
	throws JspException
    {
	keys.add( localName );
	values.add( value );
    }
}
