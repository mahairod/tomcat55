@echo off
rem ---------------------------------------------------------------------------
rem catalina.bat - Start/Stop Script for the CATALINA Server
rem
rem Environment Variable Prequisites:
rem
rem   CATALINA_HOME (Optional) May point at your Catalina "build" directory.
rem                 If not present, the current working directory is assumed.
rem
rem   CATALINA_OPTS (Optional) Java runtime options used when the "start",
rem                 "stop", or "run" command is executed.
rem
rem   JAVA_HOME     Must point at your Java Development Kit installation.
rem
rem $Id$
rem ---------------------------------------------------------------------------


rem ----- Save Environment Variables That May Change --------------------------

set _BP=%BP%
set _CATALINA_HOME=%CATALINA_HOME%
set _CLASSPATH=%CLASSPATH%


rem ----- Verify and Set Required Environment Variables -----------------------

if not "%JAVA_HOME%" == "" goto gotJavaHome
echo You must set JAVA_HOME to point at your Java Development Kit installation
goto cleanup
:gotJavaHome

if not "%CATALINA_HOME%" == "" goto gotCatalinaHome
set CATALINA_HOME=.
:gotCatalinaHome


rem ----- Set Up The Bootstrap Classpath --------------------------------------

set BP=%CATALINA_HOME%\bin\bootstrap.jar;%JAVA_HOME%\jre\lib\i18n.jar;%JAVA_HOME%\jre\lib\rt.jar;%JAVA_HOME%\lib\tools.jar

echo Using BOOT PATH: %BP%


rem ----- Set Up The Runtime Classpath ----------------------------------------

set CLASSPATH=%CATALINA_HOME%\dummy
for %%i in (%CATALINA_HOME%\lib\*.jar) do call %CATALINA_HOME%\bin\cpappend.bat %%i
echo Using CLASSPATH: %CLASSPATH%


rem ----- Execute The Requested Command ---------------------------------------

if "%1" == "env" goto doEnv
if "%1" == "run" goto doRun
if "%1" == "start" goto doStart
if "%1" == "stop" goto doStop

:doUsage
echo Usage:  catalina ( env | run | start | stop )
echo Commands:
echo   env -   Set up environment variables that Catalina would use
echo   run -   Start Catalina in the current window
echo   start - Start Catalina in a separate window
echo   stop -  Stop Catalina
goto cleanup

:doEnv
goto finish

:doRun
java %CATALINA_OPTS% -Xbootclasspath:%BP% -Dcatalina.home=%CATALINA_HOME% org.apache.catalina.startup.Bootstrap %2 %3 %4 %5 %6 %7 %8 %9 start
goto cleanup


:doStart
start java %CATALINA_OPTS% -Xbootclasspath:%BP% -Dcatalina.home=%CATALINA_HOME% org.apache.catalina.startup.Bootstrap %2 %3 %4 %5 %6 %7 %8 %9 start
goto cleanup

:doStop
java %CATALINA_OPTS% -Xbootclasspath:%BP% -Dcatalina.home=%CATALINA_HOME% org.apache.catalina.startup.Bootstrap %2 %3 %4 %5 %6 %7 %8 %9 stop
goto cleanup



rem ----- Restore Environment Variables ---------------------------------------

:cleanup
set BP=%_BP%
set _BP=
set CATALINA_HOME=%_CATALINA_HOME%
set _CATALINA_HOME=
set CLASSPATH=%_CLASSPATH%
set _CLASSPATH=
:finish
