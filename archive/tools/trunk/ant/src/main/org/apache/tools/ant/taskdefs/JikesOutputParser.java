package org.apache.tools.ant.taskdefs;

import org.apache.tools.ant.*;
    
import java.io.*;

/**
 * Parses output from jikes and
 * passes errors and warnings
 * into the right logging channels of Project.
 *
 * TODO: 
 * Parsing could be much better
 * Emacs-mode of jikes (+E) cannot be parsed right now
 * @author skanthak@muehlheim.de
 */
public class JikesOutputParser {
    protected Project project;
    protected boolean errorFlag = false; // no errors so far
    protected boolean emacsMode;
    
    /**
     * Construct a new Parser object
     * @param project - project in whichs context we are called
     */
    protected JikesOutputParser(Project project, boolean emacsMode) {
	super();
	this.project = project;
        this.emacsMode = emacsMode;
    }

    /**
     * Parse the output of a jikes compiler
     * @param reader - Reader used to read jikes's output
     */
    protected void parseOutput(BufferedReader reader) throws IOException {
       if (emacsMode)
           parseEmacsOutput(reader);
       else
           parseStandardOutput(reader);
    }

    private void parseStandardOutput(BufferedReader reader) throws IOException {
	String line;
	String lower;
	// We assume, that every output, jike does, stands for an error/warning
	// XXX 
	// Is this correct?
	while ((line = reader.readLine()) != null) {
	    lower = line.toLowerCase();
	    if (line.trim().equals(""))
		continue;
	    if (lower.indexOf("error") != -1)
		logError(line);
	    else if (lower.indexOf("warning") != -1)
		logWarning(line);
	    else
		logError(line);
	}
    }

    private void parseEmacsOutput(BufferedReader reader) throws IOException {
       // This may change, if we add advanced parsing capabilities.
       parseStandardOutput(reader);
    }

    private void logWarning(String line) {
        // Empty lines from jikes are alredy eaten, so print new ones
        if (!emacsMode) {
	    project.log("",Project.MSG_WARN); 
        }

	project.log(line,Project.MSG_WARN);
    }

    private void logError(String line) {
	errorFlag = true;

        // Empty lines from jikes are alredy eaten, so print new ones
        if (!emacsMode) {
	    project.log("",Project.MSG_ERR); 
        }

	project.log(line,Project.MSG_ERR);
    }

    /**
     * Indicate if there were errors during the compile
     * @return if errors ocured
     */
    protected boolean getErrorFlag() {
	return errorFlag;
    }
}
