@echo off
rem --------------------------------------------------------------------------
rem build.bat - Build Script for Tomcat
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
rem   JSSE_HOME        Must point at your JSSE installation [REQUIRED]
rem
rem   JMX_HOME         Must point at your JMX installation [REQUIRED]
rem
rem   REGEXP_HOME      Must point at your Regexp installation [REQUIRED]
rem
rem   SERVLETAPI_HOME  Must point at your "jakarta-servletapi" installation.
rem                    [REQUIRED]
rem
rem   CATALINA_JAXP_HOME
rem                    JAXP 1.0 compliant XML parser installation directory
rem                    used for catalina [JAXP_HOME]
rem   CATALINA_JAXP_PARSER_JAR
rem                    The jar filename of the JAXP compliant XML parser
rem                    used for catalina [JAXP_PARSER_JAR]
rem
rem   JASPER_JAXP_HOME
rem                    JAXP 1.1 compliant XML parser installation directory
rem                    used for jasper [JAXP_HOME]
rem   JASPER_JAXP_PARSER_JAR
rem                    The jar filename of the JAXP compliant XML parser
rem                    used for jasper [JAXP_PARSER_JAR]
rem
rem $Id$
rem ---------------------------------------------------------------------------


rem ----- Save Environment Variables ------------------------------------------

set _CLASSPATH=%CLASSPATH%
set _JAXP_PARSER_JAR=%JAXP_PARSER_JAR%
set _ANT_HOME=%ANT_HOME%
set _ANT_XML_CLASSPATH=%ANT_XML_CLASSPATH%
set _CATALINA_JAXP_HOME=%CATALINA_JAXP_HOME%
set _CATALINA_JAXP_PARSER_JAR=%CATALINA_JAXP_PARSER_JAR%
set _JASPER_JAXP_HOME=%JASPER_JAXP_HOME%
set _JASPER_JAXP_PARSER_JAR=%JASPER_JAXP_PARSER_JAR%


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

if not "%CATALINA_JAXP_HOME%" == "" goto gotCatalinaJaxpHome
if "%JAXP_HOME%" == "" goto jaxpHome
set CATALINA_JAXP_HOME=%JAXP_HOME%
:gotCatalinaJaxpHome

if not "%CATALINA_JAXP_PARSER_JAR%" == "" goto gotCatalinaJaxpParserJar
set CATALINA_JAXP_PARSER_JAR=%JAXP_PARSER_JAR%
:gotCatalinaJaxpParserJar

if not "%JASPER_JAXP_HOME%" == "" goto gotJasperJaxpHome
if "%JAXP_HOME%" == "" goto jaxpHome
set JASPER_JAXP_HOME=%JAXP_HOME%
:gotJasperJaxpHome

if not "%JASPER_JAXP_PARSER_JAR%" == "" goto gotJasperJaxpParserJar
set JASPER_JAXP_PARSER_JAR=%JAXP_PARSER_JAR%
:gotJasperJaxpParserJar

goto gotJaxpHome

:jaxpHome
echo You must set JAXP_HOME to point at your XML Parser install directory.
echo By default, ant, catalina, and jasper will use jaxp.jar and crimson.jar from
echo that directory.
echo - A different parser jar file can be specified globally for all
echo   components via environment variable JAXP_PARSER_JAR (e.g. xerces.jar).
echo - XML requirements for each component can also be set individually via
echo   the following environment variables:
echo      ANT_XML_CLASSPATH
echo      CATALINA_JAXP_HOME CATALINA_JAXP_PARSER_JAR
echo      JASPER_JAXP_HOME JASPER_JAXP_PARSER_JAR
goto cleanup
:gotJaxpHome

if not "%ANT_HOME%" == "" goto gotAntHome
set ANT_HOME=../jakarta-ant
:gotAntHome

if not "%JSSE_HOME%" == "" goto gotJsseHome
echo You must set JSSE_HOME to point at your Java Security Extensions install
goto cleanup
:gotJsseHome

if not "%JMX_HOME%" == "" goto gotJmxHome
echo You must set JMX_HOME to point at your Java Management Extensions install
goto cleanup
:gotJmxHome

if not "%REGEXP_HOME%" == "" goto gotRegexpHome
echo You must set REGEXP_HOME to point at your Regular Expressions distribution install
goto cleanup
:gotRegexpHome

if not "%SERVLETAPI_HOME%" == "" goto gotServletapiHome
echo You must set SERVLETAPI_HOME to your Servlet API distribution that includes the Servlet 2.3 and JSP 1.2 API classes.
goto cleanup
:gotServletapiHome


rem ----- Set Up The Runtime Classpath ----------------------------------------

if not "%CLASSPATH%" == "" set CLASSPATH=%CLASSPATH%;
set CLASSPATH=%CLASSPATH%;%ANT_HOME%\lib\ant.jar;%JAVA_HOME%\lib\tools.jar;%ANT_XML_CLASSPATH%
rem @@@ %JMX_HOME%\lib\jmxri.jar


rem ----- Execute The Requested Build -----------------------------------------

%JAVA_HOME%\bin\java %ANT_OPTS% org.apache.tools.ant.Main -Dant.home=%ANT_HOME% -Dcatalina.jaxp.home="%CATALINA_JAXP_HOME%" -Dcatalina.jaxp.parser.jar="%CATALINA_JAXP_PARSER_JAR%" -Djasper.jaxp.home="%JASPER_JAXP_HOME%" -Djasper.jaxp.parser.jar="%JASPER_JAXP_PARSER_JAR%" -Djsse.home="%JSSE_HOME%" -Djmx.home="%JMX_HOME%" -Dregexp.home="%REGEXP_HOME%" -Dservletapi.home="%SERVLETAPI_HOME%" -Djava.home="%JAVA_HOME%" %1 %2 %3 %4 %5 %6 %7 %8 %9


rem ----- Restore Environment Variables ---------------------------------------
:cleanup
set CLASSPATH=%_CLASSPATH%
set _CLASSPATH=
set JAXP_PARSER_JAR=%_JAXP_PARSER_JAR%
set _JAXP_PARSER_JAR=
set ANT_HOME=%_ANT_HOME%
set _ANT_HOME=
set ANT_XML_CLASSPATH=%_ANT_XML_CLASSPATH%
set _ANT_XML_CLASSPATH=
set CATALINA_JAXP_HOME=%_CATALINA_JAXP_HOME%
set _CATALINA_JAXP_HOME=
set CATALINA_JAXP_PARSER_JAR=%_CATALINA_JAXP_PARSER_JAR%
set _CATALINA_JAXP_PARSER_JAR=
set JASPER_JAXP_HOME=%_JASPER_JAXP_HOME%
set _JASPER_JAXP_HOME=
set JASPER_JAXP_PARSER_JAR=%_JASPER_JAXP_PARSER_JAR%
set _JASPER_JAXP_PARSER_JAR=
:finish

