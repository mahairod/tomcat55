@echo off
rem ---------------------------------------------------------------------------
rem digest.bat - Digest password using the algorithm specified
rem
rem   CATALINA_HOME (Optional) May point at your Catalina "build" directory.
rem                 If not present, the current working directory is assumed.
rem
rem   JAVA_HOME     Must point at your Java Development Kit installation.
rem
rem   This script is assumed to run from the bin directory or have the
rem   CATALINA_HOME env variable set.
rem
rem $Id$
rem ---------------------------------------------------------------------------

set _CATALINA_HOME=%CATALINA_HOME%
if not "%CATALINA_HOME%" == "" goto gotCatalinaHome
set CATALINA_HOME=.
if exist "%CATALINA_HOME%\bin\tool-wrapper.bat" goto gotCatalinaHome
set CATALINA_HOME=..
if exist "%CATALINA_HOME%\bin\tool-wrapper.bat" goto gotCatalinaHome
echo Unable to determine the value of CATALINA_HOME
goto cleanup
:gotCatalinaHome
"%CATALINA_HOME%\bin\tool-wrapper" -server org.apache.catalina.realm.RealmBase %1 %2 %3 %4 %5 %6 %7 %8 %9
:cleanup
set CATALINA_HOME=%_CATALINA_HOME%
set _CATALINA_HOME=
