@echo off

if "%1"=="" goto exit

rem Save Environment Variables We Will Modify
set _CLASSPATH=%CLASSPATH%
set _HOST=%HOST%
set _PORT=%PORT%
set _SUITE=%SUITE%
set _WATCHDOG_HOME=%WATCHDOG_HOME%

rem Estabish the test suite we will execute
set SUITE=%1

rem Establish host and port of the server under test
set HOST=localhost
set PORT=8080
if "%2"=="" goto nohost
set HOST=%2
:nohost
if "%3"=="" goto noport
set PORT=%3
:noport

rem Establish the Watchdog Home Directory
set WATCHDOG_HOME=..

rem Establish the Execution Classpath
set CLASSPATH=%WATCHDOG_HOME%\lib\ant.jar;%CLASSPATH%
set CLASSPATH=%WATCHDOG_HOME%\lib\testdriver.jar;%CLASSPATH%
set CLASSPATH=%WATCHDOG_HOME%\lib\client.jar;%CLASSPATH%
set CLASSPATH=%CLASSPATH%;%JAVA_HOME%\lib\tools.jar
set CLASSPATH=%CLASSPATH%;%JAVA_HOME%\lib\classes.zip

echo "using classpath=" %CLASSPATH%

#if not "%SUITE%"=="jsp-xml" goto runit
#if not "%TOMCAT_HOME%" == "" goto gotTomcatHome
#echo You must set TOMCAT_HOME to point at your TOMCAT-4.0 installation
#goto end
#:gotTomcatHome

#%JAVA_HOME%\bin\java -DJSP_ROOT=%TOMCAT_HOME%\webapps\jsp-tests\jsp -DWATCHDOG_HOME=%WATCHDOG_HOME% org.apache.jspxml.GetWorkspaceInXML

#:runit
rem Execute the Requested Test Suite
%JAVA_HOME%\bin\java org.apache.tools.ant.Main -Dport=%PORT% -Dhost=%HOST% "-Dwatchdog.home=%WATCHDOG_HOME%" -f "%WATCHDOG_HOME%\conf\runtest.xml" %SUITE%

rem Restore Environment Variables
set WATCHDOG_HOME=%_WATCHDOG_HOME%
set SUITE=%_SUITE%
set PORT=%_PORT%
set HOST=%_HOST%
set CLASSPATH=%_CLASSPATH%

set _WATCHDOG_HOME=
set _SUITE=
set _PORT=
set _HOST=
set _CLASSPATH=

goto end

:exit
echo usage: %0 {all/jsp/jsp-xml/jsp-all/servlet} [serverhost] [serverport] 

:end
