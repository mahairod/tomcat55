@echo off
rem ---------------------------------------------------------------------------
rem build.bat - Build Script for webapps
rem
rem Environment Variable Prerequisites:
rem
rem   JAVA_HOME        Must point at your Java Development Kit [REQUIRED]
rem
rem   JAXP_HOME        Points at a JAXP compliant XML parser 
rem                    installation directory [NONE]
rem   JAXP_PARSER_JAR  The jar filename of the JAXP compliant 
rem                    'XML parser' [crimson.jar]
rem
rem   ANT_HOME         Must point at your Ant installation [../jakarta-ant]
rem
rem   ANT_OPTS         Command line options to the Java runtime
rem                    that executes Ant [NONE]
rem   ANT_XML_CLASSPATH  
rem                    Jar files added to the classpath for the XML parsing
rem                    requirements of ant
rem                    [%JAXP_HOME%\%JAXP_PARSER_JAR%;%JAXP_HOME%\jaxp.jar]
rem 
rem   SERVLETAPI_HOME  Must point at your "jakarta-servletapi" installation.
rem                    [REQUIRED]
rem 
rem
rem $Id$
rem ---------------------------------------------------------------------------


rem ----- Save Environment Variables ------------------------------------------

set _CLASSPATH=%CLASSPATH%
set _JAXP_PARSER_JAR=%JAXP_PARSER_JAR%
set _ANT_HOME=%ANT_HOME%
set _ANT_XML_CLASSPATH=%ANT_XML_CLASSPATH%


rem ----- Verify and Set Required Environment Variables -----------------------

if not "%JAVA_HOME%" == "" goto gotJavaHome
echo You must set JAVA_HOME to point at your Java Development Kit installation
goto cleanup
:gotJavaHome

if not "%JAXP_PARSER_JAR%" == "" goto gotJaxpParserJar
set JAXP_PARSER_JAR=crimson.jar
:gotJaxpParserJar

if not "%ANT_XML_CLASSPATH%" == "" goto gotAntXmlClasspath
if "%JAXP_HOME%" == "" goto jaxpHome
set ANT_XML_CLASSPATH=%JAXP_HOME%\%JAXP_PARSER_JAR%;%JAXP_HOME%\jaxp.jar
:gotAntXmlClasspath

goto gotJaxpHome

:jaxpHome
echo You must set JAXP_HOME to point at your XML Parser install directory.
echo By default, ant will use jaxp.jar and crimson.jar from
echo that directory. 
echo - A different parser jar file can be specified globally for all
echo   components via environment variable JAXP_PARSER_JAR (e.g. xerces.jar).
echo - XML requirements for each component can also be set individually via 
echo   the following environment variables:
echo      ANT_XML_CLASSPATH
goto cleanup
:gotJaxpHome

if not "%ANT_HOME%" == "" goto gotAntHome
set ANT_HOME=../jakarta-ant
:gotAntHome

if not "%SERVLETAPI_HOME%" == "" goto gotServletapiHome
echo You must set SERVLETAPI_HOME to your Servlet API distribution that includes the Servlet 2.3 and JSP 1.2 API classes.
goto cleanup
:gotServletapiHome


rem ----- Set Up The Runtime Classpath ----------------------------------------

if not "%CLASSPATH%" == "" set CLASSPATH=%CLASSPATH%;
set CLASSPATH=%CLASSPATH%;%ANT_HOME%\lib\ant.jar;%JAVA_HOME%\lib\tools.jar;%ANT_XML_CLASSPATH%


rem ----- Execute The Requested Build -----------------------------------------

java %ANT_OPTS% org.apache.tools.ant.Main -Dservletapi.home=%SERVLETAPI_HOME% %1 %2 %3 %4 %5 %6 %7 %8 %9


rem ----- Restore Environment Variables ---------------------------------------

:cleanup
set CLASSPATH=%_CLASSPATH%
set _CLASSPATH=
set ANT_HOME=%_ANT_HOME%
set _ANT_HOME=
set XML_PARSER_JAR=%_XML_PARSER_JAR%
set _XML_PARSER_JAR=
set XML_PARSER_JAR_ANT=%_XML_PARSER_JAR_ANT%
set _XML_PARSER_JAR_ANT=
:finish
