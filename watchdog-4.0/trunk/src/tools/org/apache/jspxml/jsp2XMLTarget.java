package org.apache.jspxml;

//Author : Santosh Singh
//This class is a utility class for running watchdog tests against
//The XML view of JSP Pages
//It creates the Gtest target file for XML pages

import java.io.*;

public class  jsp2XMLTarget
{
private String input_jsp ;

public jsp2XMLTarget(String jsp_file )
{
input_jsp=ReadFileintoString(jsp_file);
}

public static String ReadFileintoString(String jsp_file)
{
  if (jsp_file ==null)
  return null;

String new_line=System.getProperty("line.separator");
String input_jsp="";

try
{
FileReader in_file = new FileReader(jsp_file);
BufferedReader br = new BufferedReader(in_file);
String line=br.readLine() ;


    while(line !=null)
    {
    line=line+new_line ; //readLine removes new line
    input_jsp+=line;
    line=br.readLine() ;

    }
 br.close();
 in_file.close();

}
catch(IOException ioex)
 {
System.out.println("I/O Error in Reading");
 }
return input_jsp;
} 

public String getXMLTarget()
{

int start_index=0;
int end_index=0;
String start_element="GET" ;
String end_element ="HTTP/1.0" ;
String xml_target="" ;

//anything between GET and HTTP/1.0 is a URI

start_index = input_jsp.indexOf(start_element , end_index);

   while( start_index >0 )
   {
 xml_target += input_jsp.substring(end_index , start_index);
 end_index=input_jsp.indexOf(end_element , start_index);
 String uri = input_jsp.substring(start_index ,end_index);
 String param_string = null;

//Parse the URI 
  int param_index = uri.indexOf('?') ; //params in request

   if(param_index >=  0)
   {
     param_string = uri.substring(param_index);
     uri = uri.substring(0,param_index);
   }

//Now we have only Request String 
  int file_index = uri.lastIndexOf('/');  //this is the path separator in XML file
  int jsp_index = uri.lastIndexOf(".jsp");


  if((file_index >=0 ) && (jsp_index >=0) )
   {
   String file_name = uri.substring(file_index+1,jsp_index);

      if(param_string==null)
       param_string=uri.substring(jsp_index+4); //points beyond .jsp
   
    uri=uri.substring(0,file_index+1);
    file_name=file_name+"XML" + ".jsp"  ;
    uri=uri+file_name+param_string ;
   
    xml_target+=uri;
    int last_index = input_jsp.indexOf("\"",end_index) ;
    xml_target+=input_jsp.substring(end_index,last_index+1) ;
    end_index=last_index+1;
    start_index = input_jsp.indexOf(start_element , end_index);
    continue;
   }
//Unfortunately it was a template text ..should be written as it is
  end_index=end_index+end_element.length();
  xml_target+=input_jsp.substring(start_index,end_index);
  start_index = input_jsp.indexOf(start_element , end_index);

   } //end while

xml_target+=input_jsp.substring(end_index);
return xml_target ;

}

}



