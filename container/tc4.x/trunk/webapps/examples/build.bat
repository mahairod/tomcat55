@echo off
rem ---------------------------------------------------------------------------
rem build.bat - Build Script for webapps
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
rem   JAXP_HOME        Must point at your JAXP installation 
rem
rem   SERVLETAPI_HOME  Must point at your "jakarta-servletapi" installation.
rem                    [../../jakarta-servletapi]
rem
rem $Id$
rem ---------------------------------------------------------------------------


rem ----- Save Environment Variables ------------------------------------------

set _ANT_HOME=%ANT_HOME%
set _CLASSPATH=%CLASSPATH%
set _SERVLETAPI_HOME=%SERVLETAPI_HOME%


rem ----- Verify and Set Required Environment Variables -----------------------

if not "%JAVA_HOME%" == "" goto gotJavaHome
echo You must set JAVA_HOME to point at your Java Development Kit installation
goto cleanup
:gotJavaHome

if not "%ANT_HOME%" == "" goto gotAntHome
set ANT_HOME=../../jakarta-ant
:gotAntHome

if not "%SERVLETAPI_HOME%" == "" goto gotServletapiHome
set SERVLETAPI_HOME=../../jakarta-servletapi
:gotServletapiHome


rem ----- Set Up The Runtime Classpath ----------------------------------------

if not "%CLASSPATH%" == "" set CLASSPATH=%CLASSPATH%;
set CLASSPATH=%CLASSPATH%;%ANT_HOME%\lib\ant.jar;%JAVA_HOME%\lib\tools.jar


rem ----- Execute The Requested Build -----------------------------------------

java %ANT_OPTS% org.apache.tools.ant.Main -Dant.home=%ANT_HOME% -Djaxp.home="%JAXP_HOME%" -Dservletapi.home=%SERVLETAPI_HOME% %1 %2 %3 %4 %5 %6 %7 %8 %9


rem ----- Restore Environment Variables ---------------------------------------

:cleanup
set SERVLETAPI_HOME=%_SERVLETAPIHOME%
set _SERVLETAPIHOME=
set CLASSPATH=%_CLASSPATH%
set _CLASSPATH=
set ANT_HOME=%_ANT_HOME%
set _ANT_HOME=
:finish
