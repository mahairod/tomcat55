@echo off

if "%1"=="" goto exit

set HOST=localhost
set PORT=8080

if "%2"=="" goto nohost
set HOST=%2
:nohost
if "%3"=="" goto noport
set PORT=%3
:noport

SET WATCHDOG_HOME=..

set TOM_PREV_CLASSPATH=%CLASSPATH%

SET CLASSPATH=%WATCHDOG_HOME%\lib\moo.jar;%CLASSPATH%
SET CLASSPATH=%WATCHDOG_HOME%\lib\testdriver.jar;%CLASSPATH%
SET CLASSPATH=%WATCHDOG_HOME%\lib\client.jar;%CLASSPATH%
SET CLASSPATH=%WATCHDOG_HOME%\lib\xml.jar;%CLASSPATH%
SET CLASSPATH=%WATCHDOG_HOME%\lib\ant.jar;%CLASSPATH%

echo "using classpath=" %CLASSPATH%
if "%1"=="servlet" goto servlet

java org.apache.tools.ant.Main -Dport %PORT% -Dhost %HOST% -Dwatchdog.home %WATCHDOG_HOME% -f %WATCHDOG_HOME%/conf/jsp.xml jsp-test

if "%1"=="jsp" goto restore

:servlet
java org.apache.tools.ant.Main -Dtest.port %PORT% -Dtest.hostname %HOST% -Dwatchdog.home %WATCHDOG_HOME% -f %WATCHDOG_HOME%/conf/servlet.xml servlet-test


:restore
set CLASSPATH=%TOM_PREV_CLASSPATH%
set WATCHDOG_HOME=
set PORT=
set HOST=

goto end

:exit
echo usage: %0 {all/jsp/servlet} [serverhost] [serverport] 

:end