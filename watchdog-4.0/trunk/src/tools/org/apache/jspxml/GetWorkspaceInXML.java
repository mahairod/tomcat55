package org.apache.jspxml;

import java.io.*;
import java.util.*;
import  org.apache.jspxml.jsp2XML;
import  org.apache.jspxml.jsp2XMLTarget;

//This file is an utility which helps running the entire jsp test suite with the 
//XML view of JSP pages. It saves the original JSP files so that they can be restored back
//XML views are generated dynamically and given the same file name as original jsp files

public class GetWorkspaceInXML
{

public static void main (String[] args )
{
 String jsp_file=null;
 String xml_file=null ;
 String jsp_root_dir = System.getProperty("JSP_ROOT");
 String watchdog_home = System.getProperty("WATCHDOG_HOME");
 String file_separator=System.getProperty("file.separator");

if(jsp_root_dir==null)
{
System.out.println("JSP_ROOT variable is not set...exiting");
return;
}

if(watchdog_home==null )
  {
  System.out.println("WATCHDOG_HOME variable is not set...exiting");
  return;
  }

String extension="jsp" ; //files with extension .jsp

FileLister lister = new FileLister(jsp_root_dir , extension) ;
Object[] files = lister.listFiles();

for(int i=0; i< files.length;i++)
{
jsp_file = (String)files[i];
int index = jsp_file.lastIndexOf(".jsp");

  if(index !=-1)
    xml_file = jsp_file.substring(0,index) + "XML" + ".jsp" ; 

//it should convert only if the jsp file is newer than the XML file

File file_jsp = new File (jsp_file) ;
File file_xml = new File (xml_file);

 if ( file_xml.exists()  )  //there was already a conversion
  {
if( file_xml.lastModified() > file_jsp.lastModified() )
continue;
  }

//if we are here means we need to convert the jsp file to xml

jsp2XML jsp_converter = new jsp2XML(jsp_file);
String xml= jsp_converter.ConvertJsp2XML();

try
 {
 FileWriter fw = new FileWriter(xml_file);
 fw.write(xml);
 fw.close();
 }
catch(IOException ioe)
 {
System.err.println("Error writing to file" + xml_file );
 }

}  //end for

//we generated the workspace in XML...now we need to create the 
//jsp-gtest-xml file that has targets for running the tests against 
//the XML view of the JSP pages

File jsp_target = new File(watchdog_home + file_separator +"conf" +
		           file_separator + "jsp-gtest.xml" );

File xml_target = new File(watchdog_home + file_separator +"conf" +
                           file_separator +"jsp-gtest-xml.xml" );

if( xml_target.exists() ) //the converted target file exists
{

if(jsp_target.lastModified() < xml_target.lastModified() ) //do nothing
return;
}

//The jsp file is latest so we need a conversion
jsp2XMLTarget xml_gtest = new jsp2XMLTarget(watchdog_home + file_separator+
				"conf" + file_separator + "jsp-gtest.xml" );

String targets_in_xml=xml_gtest.getXMLTarget();

try
 {
 FileWriter fw = new FileWriter(watchdog_home + file_separator +"conf" +
                           file_separator +"jsp-gtest-xml.xml" );
 fw.write(targets_in_xml);
 fw.close();
 }
catch(IOException ioe)
 {
System.err.println("Error writing to XML Gtest file");
 }

}


} //end class


