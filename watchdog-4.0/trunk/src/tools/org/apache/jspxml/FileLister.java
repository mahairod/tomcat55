package org.apache.jspxml;

import java.io.*;
import java.util.*;

public class FileLister
{
protected Vector file_list ;
protected File start_dir ;
protected String extension ;

public FileLister()
{
file_list = new Vector();
start_dir = new File(System.getProperty("user.dir") );
extension ="jsp" ; //default
}

public FileLister(String absolute_path , String extension )
{
file_list = new Vector();
start_dir = new File(absolute_path);
this.extension = extension ;
}

public Object[] listFiles()
{

addFiles(start_dir);
return  file_list.toArray();
}

protected void  addFiles(File start_dir)
{

  if ( !start_dir.isDirectory()   ) //if its a file 
   {
   String file_name = start_dir.getName();
   int dot_index = file_name .lastIndexOf(".");

     if(dot_index <0 ) //not found
   {
     if(extension==null)
     	file_list.add( start_dir.getAbsolutePath() );

     	return ;

  }

String file_extension = file_name.substring(dot_index+1 , file_name.length() );
       
	if(file_extension.equals(extension) ) 
     	    file_list.add( start_dir.getAbsolutePath() );
     return;
   }
//we are here means we have a directory

File[] sub_files = start_dir.listFiles();
    for(int i=0 ; i< sub_files.length ; i++)
     addFiles(sub_files[i])   ;


 }

}
