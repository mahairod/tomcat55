/**
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
 * reserved.
 */

package jsp2.examples.simpletag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.util.HashMap;
import java.io.IOException;

/**
 * SimpleTag handler that accepts a num attribute and 
 * invokes its body 'num' times.
 */
public class RepeatSimpleTag extends SimpleTagSupport {
    private int num;

    public void doTag() throws JspException, IOException {
        HashMap params = new HashMap();
        for (int i=0; i<num; i++) {
            params.put( "count", String.valueOf( i + 1 ) );
	    getJspBody().invoke(null, params);
        }
    }

    public void setNum(int num) {
	this.num = num;
    }
}
