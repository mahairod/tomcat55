package org.apache.jspxml;

import java.io.*;
import java.util.*;

import org.apache.jspxml.FileLister;
import org.apache.jspxml.jsp2XML;

//This file is an utility which helps running the entire jsp test suite with the 
//XML view of JSP pages. It saves the original JSP files so that they can be restored back
//XML views are generated dynamically and given the same file name as original jsp files

public class GetWorkspaceInXML
{

public static void main (String[] args )
{
 String jsp_file=null;
 String xml_file=null ;

if( args.length < 1)
  {
 System.out.println("Usage : java jsp2XML <diretory>  ");
 return;
  }

String root = args[0];
String extension="jsp" ; //files with extension .jsp

FileLister lister = new FileLister(root , extension) ;
Object[] files = lister.listFiles();

for(int i=0; i< files.length;i++)
{
jsp_file = (String)files[i];
int index = jsp_file.lastIndexOf(".jsp");

  if(index !=-1)
    xml_file = jsp_file.substring(0,index) + "XML" + ".jsp" ; 

//it should convert only if there has been no conversion so far
//The condition for which is no .original and no .xml file otherwise
//just rename the files properly

File xml_save = new File (jsp_file + ".xml" ) ;
File jsp_save = new File (jsp_file + ".original" ) ;

 if ( xml_save.exists() ) //the current workspace is in JSP and xml view 
			 // exists ..rename the files
  {
new File(jsp_file).renameTo(new File(jsp_file + ".original") );
xml_save.renameTo(new File(jsp_file) );
continue;
  }

 if ( jsp_save.exists() ) //the current workspace is in XML and JSP view 
			 // exists....do nothing
  {
   continue;
  }

//if we are here means this is the first time we are converting
//rename the JSp file and convert to XML view

new File(jsp_file).renameTo( new File(jsp_file +".original") );
System.out.println("Generating XML View of " + jsp_file);
jsp2XML jsp_converter = new jsp2XML(jsp_file + ".original" );
String xml= jsp_converter.ConvertJsp2XML();

try
 {
 FileWriter fw = new FileWriter(jsp_file);
 fw.write(xml);
 fw.close();
 }
catch(IOException ioe)
 {
System.err.println("Error writing to file" + xml_file );
 }

}  //end for

}

} //end class


