$Id$

The Jakarta Tomcat Website Instructions
---------------------------------------

***NOTE***
DO NOT EDIT THE .html files in the docs directory.
Please follow the directions below for updating the website.
***NOTE***

The Tomcat web site is based on .xml files which are transformed
into .html files using Anakia. Anakia is simply an Ant task that 
performs the transformations using JDOM and Velocity.

<http://www.jdom.org/>
<http://jakarta.apache.org/velocity/>
<http://jakarta.apache.org/velocity/anakia.html>

In order to make modifications to the Tomcat web site, you need
to first check out the jakarta-site2 and jakarta-tomcat-site modules from CVS:

cvs -d /home/cvs login
cvs -d /home/cvs co jakarta-site2
cvs -d /home/cvs co jakarta-tomcat-site

Once you have the site checked out locally, cd into your 
jakarta-tomcat-site directory and execute:

./build.sh [UNIX]
or 
build.bat [Win32]

This will build the documentation into the docs/ directory. The output 
will show you which files got re-generated.

If you would like to make modifications to the web site documents,
you simply need to edit the files in the xdocs/ directory.

The files in xdocs/stylesheets are the global files for the site. If you make a 
modification to project.xml, it will affect the left side navigation for the 
web site and all of your .html files will be re-generated. style.vsl is the 
template that controls the look and feel for the overall web site. Editing 
this file will also cause all of the .html files to be re-generated as well.

Once you have built your documentation and confirmed that your changes are
ok, you can check your .xml and your .html files back into CVS. 

Then, in the /www/jakarta.apache.org/tomcat/ directory, you can do a 
cvs update -d -P
to have the changes reflected on the Tomcat web site.


