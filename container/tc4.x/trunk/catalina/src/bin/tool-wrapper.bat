@echo off
rem ---------------------------------------------------------------------------
rem tool-wrapper.bat - Wrapper for command line tools
rem
rem Environment Variable Prequisites:
rem
rem   CATALINA_HOME (Optional) Catalina binary distribution directory.
rem                 If not present, the directory above this "bin" directory
rem                 is assumed.
rem
rem   JAVA_HOME     (Required) Java Development Kit installation directory.
rem
rem   TOOL_OPTS     (Optional) Java execution options for the tool.
rem
rem $Id$
rem ---------------------------------------------------------------------------


rem ----- Save Environment Variables That May Change --------------------------

set _CATALINA_HOME=%CATALINA_HOME%
set _CLASSPATH=%CLASSPATH%


rem ----- Verify and Set Required Environment Variables -----------------------

if not "%JAVA_HOME%" == "" goto gotJava
echo You must set JAVA_HOME to point at your Java Development Kit installation
goto cleanup
:gotJava

if not "%CATALINA_HOME%" == "" goto gotHome
set CATALINA_HOME=.
if exist "%CATALINA_HOME%\bin\tool-wrapper.bat" goto okHome
set CATALINA_HOME=..
:gotHome
if exist "%CATALINA_HOME%\bin\tool-wrapper.bat" goto okHome
echo Cannot find tool-wrapper.bat in %CATALINA_HOME%\bin
echo Please check your CATALINA_HOME setting
goto cleanup
:okHome


rem ----- Prepare Appropriate Java Execution Commands -------------------------

if not "%OS%" == "Windows_NT" goto noTitle
set _RUNJAVA="%JAVA_HOME%\bin\java"
goto gotTitle
:noTitle
set _RUNJAVA="%JAVA_HOME%\bin\java"
:gotTitle

rem ----- Set Up The Runtime Classpath ----------------------------------------

set CLASSPATH=%CATALINA_HOME%\bin\bootstrap.jar;%JAVA_HOME%\lib\tools.jar
rem echo Using CLASSPATH:     %CLASSPATH%
rem echo Using CATALINA_HOME: %CATALINA_HOME%
rem echo Using JAVA_HOME:     %JAVA_HOME%


rem ----- Execute The Requested Command ---------------------------------------

%_RUNJAVA% %TOOL_OPTS% -Dcatalina.home="%CATALINA_HOME%" org.apache.catalina.startup.Tool %1 %2 %3 %4 %5 %6 %7 %8 %9


rem ----- Restore Environment Variables ---------------------------------------

:cleanup
set CATALINA_HOME=%_CATALINA_HOME%
set _CATALINA_HOME=
set CLASSPATH=%_CLASSPATH%
set _CLASSPATH=
set _RUNJAVA=
:finish
