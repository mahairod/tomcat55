/*
* Copyright 2004 The Apache Software Foundation
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

/**
 * Ant task to convert a given set of files from Text to HTML.
 * Inserts an HTML header including pre tags and replaces special characters
 * with their HTML escaped equivalents.
 *
 * <p>This task is currently used by the ant script to build our examples</p>
 *
 * @author Mark Roth
 */
public class Txt2Html 
    extends Task 
{
    
    /** The directory to contain the resulting files */
    private File todir;
    
    /** The file to be converted into HTML */
    private List filesets = new LinkedList();
    
    /**
     * Sets the directory to contain the resulting files
     *
     * @param todir The directory
     */
    public void setTodir( File todir ) {
        this.todir = todir;
    }
    
    /**
     * Sets the files to be converted into HTML
     *
     * @param fileset The fileset to be converted.
     */
    public void addFileset( FileSet fs ) {
        filesets.add( fs );
    }
    
    /**
     * Perform the conversion
     *
     * @param BuildException Thrown if an error occurs during execution of
     *    this task.
     */
    public void execute() 
        throws BuildException 
    {
        int count = 0;
        
        // Step through each file and convert.
        Iterator iter = filesets.iterator();
        while( iter.hasNext() ) {
            FileSet fs = (FileSet)iter.next();
            DirectoryScanner ds = fs.getDirectoryScanner( project );
            File basedir = ds.getBasedir();
            String[] files = ds.getIncludedFiles();
            for( int i = 0; i < files.length; i++ ) {
                File from = new File( basedir, files[i] );
                File to = new File( todir, files[i] + ".html" );
                if( !to.exists() || 
                    (from.lastModified() > to.lastModified()) ) 
                {
                    log( "Converting file '" + from.getAbsolutePath() + 
                        "' to '" + to.getAbsolutePath(), Project.MSG_VERBOSE );
                    try {
                        convert( from, to );
                    }
                    catch( IOException e ) {
                        throw new BuildException( "Could not convert '" + 
                            from.getAbsolutePath() + "' to '" + 
                            to.getAbsolutePath() + "'", e );
                    }
                    count++;
                }
            }
            if( count > 0 ) {
                log( "Converted " + count + " file" + (count > 1 ? "s" : "") + 
                    " to " + todir.getAbsolutePath() );
            }
        }
    }
    
    /**
     * Perform the actual copy and conversion
     *
     * @param from The input file
     * @param to The output file
     * @throws IOException Thrown if an error occurs during the conversion
     */
    private void convert( File from, File to )
        throws IOException
    {
        // Open files:
        BufferedReader in = new BufferedReader( new FileReader( from ) );
        PrintWriter out = new PrintWriter( new FileWriter( to ) );
        
        // Output header:
        out.println( "<html><body><pre>" );
        
        // Convert, line-by-line:
        String line;
        while( (line = in.readLine()) != null ) {
            StringBuffer result = new StringBuffer();
            int len = line.length();
            for( int i = 0; i < len; i++ ) {
                char c = line.charAt( i );
                switch( c ) {
                    case '&':
                        result.append( "&amp;" );
                        break;
                    case '<':
                        result.append( "&lt;" );
                        break;
                    default:
                        result.append( c );
                }
            }
            out.println( result.toString() );
        }
        
        // Output footer:
        out.println( "</pre></body></html>" );
        
        // Close streams:
        out.close();
        in.close();
    }
    
}


