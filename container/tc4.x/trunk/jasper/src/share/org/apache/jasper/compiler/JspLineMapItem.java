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

/**
 * Data structure used for each individual line map item.
 * This data structure has a set of numbers representing the 
 * beginning and ending jsp line numbers and the associated 
 * generated servlet lines.
 *
 * @author Justyna Horwat
 */
public class JspLineMapItem {

    private int[] itemArray;

    public JspLineMapItem() {
        itemArray = new int[8];
    }

    /**
     * Set the beginning servlet line number
     */
    public void setBeginServletLnr(int item) {
        itemArray[0] = item;
    }

    /**
     * Get the beginning servlet line number
     */
    public int getBeginServletLnr() {
        return itemArray[0];
    }

    /**
     * Set the ending servlet line number
     */
    public void setEndServletLnr(int item) {
        itemArray[1] = item;
    }

    /**
     * Get the ending servlet line number
     */
    public int getEndServletLnr() {
        return itemArray[1];
    }

    /**
     * Set the index of the starting jsp file
     */
    public void setStartJspFileNr(int item) {
        itemArray[2] = item;
    }

    /**
     * Get the index of the starting jsp file
     */
    public int getStartJspFileNr() {
        return itemArray[2];
    }

    /**
     * Set the beginning jsp line number
     */
    public void setBeginJspLnr(int item) {
        itemArray[3] = item;
    }

    /**
     * Get the beginning jsp line number
     */
    public int getBeginJspLnr() {
        return itemArray[3];
    }

    /**
     * Set the beginning jsp column number
     */
    public void setBeginJspColNr(int item) {
        itemArray[4] = item;
    }

    /**
     * Get the beginning jsp column number
     */
    public int getBeginJspColNr() {
        return itemArray[4];
    }

    /**
     * Set the index of the stopping jsp file
     */
    public void setStopJspFileNr(int item) {
        itemArray[5] = item;
    }

    /**
     * Get the index of the stopping jsp file
     */
    public int getStopJspFileNr() {
        return itemArray[5];
    }

    /**
     * Set the ending jsp line number
     */
    public void setEndJspLnr(int item) {
        itemArray[6] = item;
    }

    /**
     * Get the ending jsp line number
     */
    public int getEndJspLnr() {
        return itemArray[6];
    }

    /**
     * Set the ending jsp column number
     */
    public void setEndJspColNr(int item) {
        itemArray[7] = item;
    }

    /**
     * Get the ending jsp column number
     */
    public int getEndJspColNr() {
        return itemArray[7];
    }

    /**
     * Convert data structure to a string
     */
    public String toString() {
        StringBuffer out = new StringBuffer();

        for (int j=0; j < itemArray.length; j++) {
            out.append("["+j+"] " + itemArray[j]+" ");
        }
        out.append("\n");

        return out.toString();
    }
}
