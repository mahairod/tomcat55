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
rem   REGEXP_HOME      Must point at your Regexp installation
rem                    [../../jakarta-regexp]
rem
rem   SERVLETAPI_HOME  Must point at your "jakarta-servletapi" installation.
rem                    [../../jakarta-servletapi]
rem
rem $Id$
rem ---------------------------------------------------------------------------


rem ----- Save Environment Variables ------------------------------------------

set _ANT_HOME=%ANT_HOME%
set _CLASSPATH=%CLASSPATH%
set _REGEXP_HOME=%REGEXP_HOME%
set _SERVLETAPI_HOME=%SERVLETAPI_HOME%


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

if not "%ANT_HOME%" == "" goto gotAntHome
set ANT_HOME=../../jakarta-ant
:gotAntHome

if not "%REGEXP_HOME%" == "" goto gotRegexpHome
set REGEXP_HOME=../../jakarta-regexp
:gotRegexpHome

if not "%SERVLETAPI_HOME%" == "" goto gotServletapiHome
set SERVLETAPI_HOME=../../jakarta-servletapi
:gotServletapiHome


rem ----- Set Up The Runtime Classpath ----------------------------------------

if not "%CLASSPATH%" == "" set CLASSPATH=%CLASSPATH%;
set CLASSPATH=%CLASSPATH%;%ANT_HOME%\lib\ant.jar;%JAVA_HOME%\lib\tools.jar


rem ----- Execute The Requested Build -----------------------------------------

%JAVA_HOME%\bin\java %ANT_OPTS% org.apache.tools.ant.Main -Dant.home=%ANT_HOME% -Dxerces.home="%XERCES_HOME%" -Djsse.home=%JSSE_HOME% -Dregexp.home=%REGEXP_HOME% -Dservletapi.home=%SERVLETAPI_HOME% -Djdom.home="%JDOM_HOME%" %1 %2 %3 %4 %5 %6 %7 %8 %9


rem ----- Restore Environment Variables ---------------------------------------

:cleanup
set SERVLETAPI_HOME=%_SERVLETAPIHOME%
set _SERVLETAPIHOME=
set REGEXP_HOME=%_REGEXP_HOME%
set _REGEXP_HOME=
set CLASSPATH=%_CLASSPATH%
set _CLASSPATH=
set ANT_HOME=%_ANT_HOME%
set _ANT_HOME=
:finish
