package org.apache.tools.ant;

import java.io.File;
import java.io.IOException;
import java.io.FilenameFilter;

/**
 * Filters filenames to determine whether or not the file is desirable.
 *
 * @author Jason Hunter [jhunter@servlets.com]
 * @author james@x180.com
 */

public class DesirableFilter implements FilenameFilter {

    /**
     * Test the given filename to determine whether or not it's desirable.
     * This helps tasks filter temp files and files used by CVS.
     */

    public boolean accept(File dir, String name) {

	// emacs save file
	if (name.endsWith("~")) {
	    return false;
	}

	// emacs autosave file
	if (name.startsWith("#") && name.endsWith("#")) {
	    return false;
	}

	// openwindows text editor does this I think
	if (name.startsWith("%") && name.endsWith("%")) {
	    return false;
	}

	// cvs file
	if (dir.getName().equalsIgnoreCase("CVS") && 
                name.equalsIgnoreCase("root")) {
	    return false;
	}

	// cvs file
	if (dir.getName().equalsIgnoreCase("CVS") && 
                name.equalsIgnoreCase("entries")) {
	    return false;
	}

	// cvs file
	if (dir.getName().equalsIgnoreCase("CVS") && 
                name.equalsIgnoreCase("entries.log")) {
	    return false;
	}

	// cvs file
	if (dir.getName().equalsIgnoreCase("CVS") && 
                name.equalsIgnoreCase("repository")) {
	    return false;
	}

	// cvs file
	if (dir.getName().equalsIgnoreCase("CVS") && 
                name.equalsIgnoreCase("template")) {
	    return false;
	}

	// default
	return true;
    }
}








