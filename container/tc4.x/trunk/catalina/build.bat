@echo off
rem ---------------------------------------------------------------------------
rem build.bat - Build Script for Catalina
rem
rem Environment Variable Prerequisites:
rem
rem   ANT_HOME         Must point at your Ant installation [../../jakarta-ant]
rem
rem   ANT_OPTS         Command line options to the Java runtime
rem                    that executes Ant [NONE]
rem
rem   JAVA_HOME        Must point at your Java Development Kit [REQUIRED]
rem
rem   XERCES_HOME      Must point at your XERCES installation [REQUIRED]
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
rem $Id$
rem ---------------------------------------------------------------------------


rem ----- Save Environment Variables ------------------------------------------

set _ANT_HOME=%ANT_HOME%
set _CLASSPATH=%CLASSPATH%


rem ----- Verify and Set Required Environment Variables -----------------------

if not "%JAVA_HOME%" == "" goto gotJavaHome
echo You must set JAVA_HOME to point at your Java Development Kit installation
goto cleanup
:gotJavaHome

if not "%XERCES_HOME%" == "" goto gotXercesHome
echo You must set XERCES_HOME to point at your Xerces install
goto cleanup
:gotXercesHome

if not "%JSSE_HOME%" == "" goto gotJsseHome
echo You must set JSSE_HOME to point at your Java Security Extensions install
goto cleanup
:gotJsseHome

if not "%JMX_HOME%" == "" goto gotJmxHome
echo You must set JMX_HOME to point at your Java Management Extensions install
goto cleanup
:gotJmxHome

if not "%ANT_HOME%" == "" goto gotAntHome
set ANT_HOME=../../jakarta-ant
:gotAntHome

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
set CLASSPATH=%CLASSPATH%;%ANT_HOME%\lib\ant.jar;%JAVA_HOME%\lib\tools.jar;%XERCES_HOME%\xerces.jar;%JMX_HOME%\lib\jmxri.jar


rem ----- Execute The Requested Build -----------------------------------------

%JAVA_HOME%\bin\java %ANT_OPTS% org.apache.tools.ant.Main -Dant.home=%ANT_HOME% -Dxerces.home="%XERCES_HOME%" -Djsse.home=%JSSE_HOME% -Djmx.home=%JMX_HOME% -Dregexp.home=%REGEXP_HOME% -Dservletapi.home=%SERVLETAPI_HOME% %1 %2 %3 %4 %5 %6 %7 %8 %9


rem ----- Restore Environment Variables ---------------------------------------

:cleanup
set CLASSPATH=%_CLASSPATH%
set _CLASSPATH=
set ANT_HOME=%_ANT_HOME%
set _ANT_HOME=
:finish
