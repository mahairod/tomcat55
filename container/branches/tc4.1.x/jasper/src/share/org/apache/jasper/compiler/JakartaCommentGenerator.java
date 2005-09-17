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

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.MalformedURLException;

import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;

import org.apache.jasper.JasperException;
import org.apache.jasper.Constants;
import org.apache.jasper.JspCompilationContext;

/**
 * Generates original "Jakarta"-style comments
 *
 * @author Mandar Raje [patch submitted by Yury Kamen]
 */
public class JakartaCommentGenerator implements CommentGenerator {
    
    // -------------------- Generate comments --------------------
    // The code generator also maintains line number info.

    // JakartaCommentGenerator can generate the line numbers
    // ( the way it generates the comments )
    JspLineMapItem lineMapItem;

    /**
     * Generates "start-of the JSP-embedded code block" comment
     *
     * @param out The ServletWriter
     * @param start Start position of the block
     * @param stop End position of the block
     * @exception JasperException 
     */
    public void generateStartComment(Generator generator, ServletWriter out, 
                                     Mark start, Mark stop) 
        throws JasperException 
    {
	String html = "";

        if (generator instanceof CharDataGenerator) {
	   html = "// HTML ";
	}
 	if (start != null && stop != null) {
	    if (start.fileid == stop.fileid) {
		String fileName = out.quoteString(start.getFile ());
		out.println(html + "// begin [file=" + fileName+";from=" + start.toShortString() + ";to=" + stop.toShortString() + "]");
	    } else {
		out.println(html + "// begin [from="+start+";to="+stop+"]");
            }
	} else {
	    out.println(html + "// begin");
        }

        // add the jsp to servlet line mappings
        lineMapItem = new JspLineMapItem();
        lineMapItem.setBeginServletLnr(out.getJavaLine());

        out.pushIndent();
    }

   /**
     * Generates "end-of the JSP-embedded code block" comment
     *
     * @param out The ServletWriter
     * @param start Start position of the block
     * @param stop End position of the block
     * @exception JasperException
     */
    public void generateEndComment(Generator generator, ServletWriter out, Mark start, Mark stop) throws JasperException {

	out.popIndent();
        out.println("// end");

        JspLineMap myLineMap = out.getLineMap();

        // add the jsp to servlet line mappings
        lineMapItem.setEndServletLnr(out.getJavaLine());
        lineMapItem.setStartJspFileNr(myLineMap.addFileName(start.getSystemId()));
        lineMapItem.setBeginJspLnr(start.getLineNumber() + 1);
        lineMapItem.setBeginJspColNr(start.getColumnNumber() + 1);
        lineMapItem.setStopJspFileNr(myLineMap.addFileName(stop.getSystemId()));
        lineMapItem.setEndJspLnr(stop.getLineNumber() + 1);
        lineMapItem.setEndJspColNr(stop.getColumnNumber() + 1);

        myLineMap.add(lineMapItem);
    }


    // The format may change
    private String toShortString( Mark mark ) {
        return "("+mark.getLineNumber() + ","+mark.getColumnNumber() +")";
    }

    //
    private String toString( Mark mark ) {
        return mark.getSystemId()+"("+mark.getLineNumber()+","+mark.getColumnNumber() +")";
    }


}
//        String fileName = "null";
//         if(start != null) {
//              fileName = out.quoteString(start.getFile());
//         }
//         String startString = "null";
//         if(null != start) {
//            startString =  start.toShortString();
//         }

//         String stopString = "null";
//         if(null != stop) {
//            stopString =  stop.toShortString();
//         }
//         out.println("// begin [file="+fileName+";from="+startString+";to="+stopString+"]");
