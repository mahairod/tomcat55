package org.apache.jspxml;

import java.io.*;
import java.util.*;



/*
commented ....initial design...not using anymore
class JSP_elements
{
//JSP Elements as known till JSP 1.2 specs

public static final String JSP_COMMENT= "<%--" ; //comment
public static final String JSP_DIRECTIVE= "<%@" ;//Directive
public static final String JSP_SCRIPTLET="<%" ; //Scriptlet
public static final String JSP_DECLARATION="<%!" ; //Declaration 
public static final String JSP_EXPRESSION="<%=" ; //Expression

public static final String isJSPToken(String token)
{
  if(token==null) 
  return null;

  if(token.equals(JSP_COMMENT) )
  return JSP_COMMENT;
  if(token.equals(JSP_DIRECTIVE) )
  return JSP_DIRECTIVE;
  if(token.equals(JSP_SCRIPTLET) )
  return JSP_SCRIPTLET;
  if(token.equals(JSP_DECLARATION) )
  return JSP_DECLARATION;
  if(token.equals(JSP_EXPRESSION) )
  return JSP_EXPRESSION;

  return null;

}

}


class JSP_block
{
//this class is a helper class that is used to keep track of a block of code
//which is under a JSP element

//start_index and end_index give the start and end index in a string having a jsp block of code

public int start_index;
public int end_index;
//element_type tells the type of jsp element for this block
public String element_type;

public JSP_block(int start , int end , String type)
{
start_index=start;
end_index=end;
element_type=type;
}

public static JSP_block getNextJSPBlock(String input_str , int start , int end)
              throws Exception
{
//returns the next block of JSP from a string between the given indexes
 if(input_str ==null)
  return null;

String work_str = input_str.substr(start , end+1);
int start_index=work_str.indexOf("<%"); //every element starts with <%
int end_index=0;
 if(start_index==-1) //no JSp element is there
 return(null);

char next_char =work_str.charAt(temp_index+2) ;

  if(next_char=='@' )  //A directive
  {
 end_index=work_str.indexOf("%>");

    if(end_index==-1)
    throw Exception;
  return(new JSP_block(start_index,end_index+2,JSP_elements.JSP_DIRECTIVE );
  }

  if(next_char=='!' )  //A Decalaration
  {
 end_index=work_str.indexOf("%>");

    if(end_index==-1)
    throw Exception;
  return(new JSP_block(start_index,end_index+2,JSP_elements.JSP_DECLARATION );
  }

  if(next_char=='=' )  //An Expression
  {
 end_index=work_str.indexOf("%>");

    if(end_index==-1)
    throw Exception;
  return(new JSP_block(start_index,end_index+2,JSP_elements.JSP_EXPRESSION );
  }

  if(next_char=='-' )  //Might be a comment..treat a comment for time being
  {
 end_index=work_str.indexOf("--%>");

    if(end_index==-1)
    throw Exception;
  return(new JSP_block(start_index,end_index+4,JSP_elements.JSP_COMMENT );
  }

//if none of above then it should be a scriptlet

 end_index=work_str.indexOf("%>");

    if(end_index==-1)
    throw Exception;
  return(new JSP_block(start_index,end_index+2,JSP_elements.JSP_SCRIPTELT );


}


}
*/


public class jsp2XML
{

public String xml_ns;  //for xml name spaces
public String input_jsp;  //String representation of input JSP file

protected static String new_line;
public static  String xml_prolog;
public static  String jsp_root_tag;
public static String author_comments;
public static String jsp_end_tag;
protected Hashtable tag_prefix ;
//Initialize above variables Here

 static {
new_line = System.getProperty("line.separator");

//The final xml will be prolog + comments + root_tag +xmlns+ body + end_tag
//Note new_line is not required but we use it for formatting

//xml_prolog = "<?xml version=\'1.0\' encoding=\'us-ascii\'?>" + new_line ;
xml_prolog="<!DOCTYPE root" + new_line;
xml_prolog+="PUBLIC \"-//Sun Microsystems Inc.//DTD JavaServer Pages Version 1.2//EN\"" + new_line ;
xml_prolog+="\"http://java.sun.com/products/jsp/dtd/jspcore_1_2.dtd\" > " ;

author_comments = new_line + "<!-- This File is generated automatically by jsp2XML converter tool --> " + new_line ;
author_comments+= "<!-- Written By Santosh Singh -->" ;

jsp_root_tag = new_line + "<jsp:root" + new_line ;
//note that we haven't yet closed the jsp:root since taglib directives will also be added here
jsp_end_tag = new_line +"</jsp:root>" ;



}//initialization complete

public jsp2XML(String input_jsp_file)
{
input_jsp="";
xml_ns="xmlns:jsp=\"http://java.sun.com/products/jsp/dtd/jsp_1_2.dtd\"" ;
xml_ns +=new_line ;
tag_prefix = new Hashtable();
readJspFileintoString(input_jsp_file) ;
}

public String parseDirective(String directive_str )
{
// \t is not same as a space

StringTokenizer st= new StringTokenizer(directive_str," \t\n\r\f" );
String directive = st.nextToken();
String out_file="";
   if( ! (directive.equals("taglib")) )  //not a taglib directive
   {
    out_file+="<jsp:directive." + directive +" " ;

    while(st.hasMoreTokens() )
    out_file+=st.nextToken() + " " ;

   out_file+="/>" ;
   return out_file;
   }

//If it is a taglib directive we need to change the xml NameSpace
//Construct a new String Tokenizer with =\t\n" as delimiters	

st =new StringTokenizer(directive_str," =\t\n\r\"" , false );
directive = st.nextToken();
out_file+="<jsp:directive." + directive +" ";
String uri="";
String prefix="";
   while(st.hasMoreTokens() )
  {
String next_token=st.nextToken();

       if(next_token.equals("uri") )
       uri=st.nextToken();

       if(next_token.equals("prefix") )
       prefix=st.nextToken();

  }
//update XML Name Space
xml_ns+="xmlns:" + prefix +"=" ;
xml_ns+="\"" + uri +"\"" + new_line  ;
//put the prefix in the hashtable

tag_prefix.put(prefix , prefix);


//update xml output
out_file += "uri=\"" + uri + "\"" + " " ;
out_file+="prefix=\"" + prefix +"\"" + " ";
out_file+="/>" ; //end of taglib
return out_file;
}


public String convert(String jsp )
{
int element_index=0;
int last_index=0;
int end_index=0;
int action_index=0;
int tag_level=0;
String xml="";
boolean jsp_element_first=false;
boolean cdata_closed = true; 
 
while(element_index>=0 || action_index>=0)
{
element_index=jsp.indexOf("<%" , last_index);
action_index=jsp.indexOf(":" , last_index); //might be an action

  if((element_index < action_index) || (action_index==-1) )
         jsp_element_first=true;

       else
  	 jsp_element_first=false;


if( cdata_closed )
{
xml+=new_line+ "<![CDATA[ " + new_line ;
cdata_closed=false;
}

 if(element_index!=-1 && jsp_element_first) //JSP element was found before action
 {
 xml+=jsp.substring(last_index , element_index);
 xml+=new_line + "]]>" + new_line ; //end of CDATA section
 cdata_closed=true;

 char jsp_char = jsp.charAt(element_index+2);

    if( jsp_char =='-' ) //jsp Comment
    {
   xml+= new_line +"<!--" ; //XML comment
   end_index=jsp.indexOf("--%>" , last_index );
   xml+=jsp.substring(element_index+4,end_index);
   xml+="-->" ;
   last_index=end_index+4;
   continue;
    }//end JSP Comment

    if( jsp_char =='=' ) //Jsp Expression
    {
   xml+=new_line+"<jsp:expression>" ; 
   end_index=jsp.indexOf("%>" , last_index );
   xml+= new_line + "<![CDATA[" + new_line ;
   xml+=jsp.substring(element_index+3,end_index);
   xml+=new_line +"]]>"+new_line ; //end of CDATA
   xml+= new_line +"</jsp:expression>"+new_line ;
   last_index=end_index+2;
   continue;
    }//end JSP Expression

if( jsp_char =='!' ) //jsp Declaration
    {
   xml+=new_line+"<jsp:declaration>" ;
   end_index=jsp.indexOf("%>" , last_index );
   xml+=new_line+"<![CDATA[" + new_line ;
   xml+=jsp.substring(element_index+3,end_index);
   xml+=new_line+"]]>" + new_line ; //end of CDATA
   xml+=new_line+"</jsp:declaration>"+new_line ;
   last_index=end_index+2;
   continue;
    }//end JSP Declaration

if( jsp_char =='@' ) //jsp Directive
    {
   end_index=jsp.indexOf("%>" , last_index );
 String directive = jsp.substring(element_index+3,end_index);

   xml+=parseDirective(directive); //this adds to xml String itself
   last_index=end_index+2;
   continue;
    }//end JSP Directive

 //if we reach here it means we got a JSP Scriptlet
   xml+=new_line+"<jsp:scriptlet>" +new_line;
   end_index=jsp.indexOf("%>" , last_index );
   xml+=new_line+"<![CDATA[" +new_line ;
   xml+=jsp.substring(element_index+2,end_index);
   xml+=new_line+"]]>" +new_line ; //end of CDATA
   xml+=new_line+"</jsp:scriptlet>" +new_line ;
   last_index=end_index+2;
   continue;
 } //end if

//This is the code to take care of jsp actions like jsp:forward and custom actions
//Here for simplicity a am assuming that these standard strings are 
//not used as part of template text. Note that all these elements
//start with "jsp:" 

if(action_index!=-1) // might be a custom or standard action
  {
//find the previous element
int save_index=action_index ;
char ch= jsp.charAt(action_index);
 while( ch!='<' )
 {
 action_index--;

    //don't wanna go back to where we already are
   if(action_index < last_index)
   break;
   
 ch=jsp.charAt(action_index);
 }

 String action_name = jsp.substring(action_index+1,save_index);

   if( (action_index<last_index) || action_name.endsWith(" ") )
    {
      //don't close the existing CDATA section
     // because it's a template text
      xml+=jsp.substring(last_index , save_index+1);
      last_index= save_index+1;
      continue;
    }

//Check for an end tag

   if(action_name.startsWith("/") )
   {
     //close the existiong CDATA section first
      xml+=jsp.substring(last_index , action_index);
      end_index=jsp.indexOf( ">" , action_index);
      xml+=jsp.substring(action_index , end_index+1);
      last_index=end_index+1;
      tag_level--;
                if(tag_level==0)
      		cdata_closed=true;
      continue;
   }


//we found a Standard action or a custom action
//the attributes of these action might contain runtime expressions

action_name = action_name.trim();

   if(action_name.equals("jsp") || tag_prefix.containsKey(action_name) )
    {
	 xml+=jsp.substring(last_index , action_index);

         if(tag_level==0)
         {
	 xml+=new_line + "]]>"+new_line ; //end of CDATA section
         }

         tag_level++;
         cdata_closed=false;

         end_index=jsp.indexOf(">" , action_index); 
         ch=jsp.charAt(end_index-1);
           while(ch=='%')
           {
            last_index=end_index+1;
            end_index=jsp.indexOf(">" , last_index); 
            ch=jsp.charAt(end_index-1);
           }

         xml+=parseAttributes( jsp.substring(action_index ,end_index+1) );
 	 last_index=end_index+1;

         if( jsp.charAt(end_index-1)=='/' ) //end of tag
        {
                tag_level--;
                if(tag_level==0)
         	cdata_closed=true;

        }
  }
else  //template text again
  {
      xml+=jsp.substring(last_index , save_index+1);
      last_index= save_index+1;
  }
} //end action

}//end while

//Remaining part of the string
     xml+=jsp.substring(last_index);
//close the CDATA section
     xml+=new_line + "]]>" + new_line ;
 
return xml;

} //end convert

public String parseAttributes(String xml_tag)
{
//parse the request time attributes if any
String parsed_string="";
int element_index = xml_tag.indexOf("<%=");
int save_index=element_index;
int last_index=0;

  while (element_index !=-1)  //found a reqquest time attribute
  {
    //find the previous element skipping spaces
  char ch=xml_tag.charAt(element_index);

    while(ch!='\'')
    {
      element_index--;
      ch=xml_tag.charAt(element_index);
       if(ch=='\"')
       break;
       if(element_index <=last_index) //might be a syntax error
       return null;
    }

  parsed_string+=xml_tag.substring(last_index,element_index) ;
  parsed_string+="\"%=" ;
  last_index=save_index+3;
  element_index = xml_tag.indexOf("%>" ,last_index);
  parsed_string+=xml_tag.substring(last_index,element_index);
  parsed_string+="%\"" ;

  last_index=element_index+2;
//skip the blaank spaces
   while ( true)
   {
     ch = xml_tag.charAt(last_index);

     if( ch=='\'' || ch=='\"' )
      {
       last_index++;
       break;
      }
     if(ch==' ')
     last_index++;
     else //Syntax error
     return null ;

   }


  element_index=xml_tag.indexOf("<%=" ,last_index);
  save_index=element_index;
  }
 parsed_string+=xml_tag.substring(last_index);
 return parsed_string;

}

private void readJspFileintoString( String jsp_file )
{
  if (jsp_file ==null)
  return;

try
{
FileReader in_file = new FileReader(jsp_file);
BufferedReader br = new BufferedReader(in_file);

String line=br.readLine() ;


    while(line !=null)
    {
  // remove all the spaces from beginning and end of the line
 //and convert to quoting to XML

 //   line=convertToXMLQuoting(line);

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

} //end readJspFileintoString

public String ConvertJsp2XML()
{

String output_xml =convert( input_jsp);

//we have the xml file in output_xml
//The final file is
//xml_prolog + xml_ns + jsp_root_tag +xml+ jsp_end_tag

xml_ns=xml_ns.trim() ;
xml_ns+=">" + new_line ; //close the XML Name Space
output_xml=xml_prolog+ author_comments + jsp_root_tag +xml_ns+ output_xml + jsp_end_tag ;
return output_xml;
}

public void ConvertJsp2XML(String xml_file)
{

String output_xml=convert( input_jsp);

//we have the xml file in output_xml
//The final file is
//xml_prolog + xml_ns + jsp_root_tag +xml+ jsp_end_tag

xml_ns=xml_ns.trim() ;
xml_ns+=">" + new_line ; //close the XML Name Space
output_xml=xml_prolog+ author_comments + jsp_root_tag +xml_ns+ output_xml + jsp_end_tag ;
//write it to the file
try
{
FileWriter out_file = new FileWriter(xml_file);
  out_file.write(output_xml, 0, output_xml.length() ) ;
  out_file.flush();
  out_file.close();
}
catch(IOException ioex)
 {
System.out.println("I/O Error in writing");
 }

} //end convertToXML

} //end class


