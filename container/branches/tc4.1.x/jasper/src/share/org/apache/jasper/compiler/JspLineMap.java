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

import java.util.ArrayList;

/**
 * Data structure to store the line and file map information.
 * The line map has a mapping of which jsp lines correspond to
 * the generated servlet. The file map has all of jsp files
 * that are included in the servlet.
 *
 * @author Justyna Horwat
 */
public class JspLineMap {

    private ArrayList lineMap;
    private ArrayList fileMap;

    public JspLineMap() {
        lineMap = new ArrayList();
        fileMap = new ArrayList();
    }

    /**
     * Add an item to the line map data structure
     */
    public void add(JspLineMapItem lineMapItem) {
        lineMap.add(lineMapItem);
    }

    /**
     * Get an item to the line map data structure
     */
    public JspLineMapItem get(int index) {
        return (JspLineMapItem) lineMap.get(index);
    }

    /**
     * Get line map data structure size
     */
    public int size() {
        return lineMap.size();
    }

    public void clear() {
        lineMap.clear();
        fileMap.clear();
    }

    /**
     * Add a file to the file map data structure. The index is
     * stored in the line map to associate a file with the line
     * of code.
     */
    public int addFileName(String fileName) {
        int idx = fileMap.indexOf(fileName);

        if (idx>=0) return idx;

        fileName = fileName.replace( '\\', '/' );
        fileMap.add(fileName);
        idx = fileMap.size() - 1 ; // added item

        return idx;
    }

    /**
     * Get a file from the file map data structure. Use the index
     * to grab the right file name.
     */
    public String getFileName(int index) {
        return (String) fileMap.get(index);
    }

    /**
     * Convert data structures to a string
     */
    public String toString() {
        int i;
        JspLineMapItem lineMapItem;
        StringBuffer out = new StringBuffer();

        out.append("JspLineMap Debugging:\n");
        out.append("lineMap: \n");
        for (i=0; i < lineMap.size(); i++) {
            lineMapItem = (JspLineMapItem) lineMap.get(i);
            out.append("#" + i + ": ");
            out.append(lineMapItem.toString());
        }

        out.append("fileMap: \n");

        for (i=0; i < fileMap.size(); i++) {
            out.append("#" + i + ": " + fileMap.get(i) + "\n");
        }

        return out.toString();
    }

}
