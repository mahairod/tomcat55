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

if not "%WATCHDOG_HOME%" == "" goto gotWatchdogHome
set WATCHDOG_HOME=..
:gotWatchdogHome

if not "%TOMCAT_HOME%" == "" goto gotTomcatHome
if exist %WATCHDOG_HOME%\..\tomcat set TOMCAT_HOME=%WATCHDOG_HOME%\..\tomcat
if exist %WATCHDOG_HOME%\..\jakartatomcat set TOMCAT_HOME=%WATCHDOG_HOME%\..\jakartatomcat
:gotTomcatHome

set TOM_PREV_CLASSPATH=%CLASSPATH%

if exist %TOMCAT_HOME%\classes SET CLASSPATH=%TOMCAT_HOME%\classes;%CLASSPATH%
if exist %TOMCAT_HOME%\lib\servlet.jar SET CLASSPATH=%TOMCAT_HOME%\lib\servlet.jar;%CLASSPATH%
if exist %TOMCAT_HOME%\lib\jasper.jar SET CLASSPATH=%TOMCAT_HOME%\lib\jasper.jar;%CLASSPATH%
SET CLASSPATH=%TOMCAT_HOME%\lib\webserver.jar;%CLASSPATH%

SET CLASSPATH=%WATCHDOG_HOME%\lib\moo.jar;%CLASSPATH%
SET CLASSPATH=%WATCHDOG_HOME%\lib\testdriver.jar;%CLASSPATH%
SET CLASSPATH=%WATCHDOG_HOME%\lib\client.jar;%CLASSPATH%
SET CLASSPATH=%WATCHDOG_HOME%\lib\ant.jar;%CLASSPATH%

echo "using classpath=" %CLASSPATH%
if "%1"=="servlet" goto servlet
if "%1"=="jsp" goto jsp

java -Dtomcat.home=%TOMCAT_HOME% org.apache.tools.ant.Main -Dport %PORT% -Dhost %HOST% -Dwatchdog.home %WATCHDOG_HOME% -f %WATCHDOG_HOME%/conf/runtest.xml startup servlet-test jsp-test shutdown

:jsp
java -Dtomcat.home=%TOMCAT_HOME% org.apache.tools.ant.Main -Dport %PORT% -Dhost %HOST% -Dwatchdog.home %WATCHDOG_HOME% -f %WATCHDOG_HOME%/conf/runtest.xml startup jsp-test shutdown
goto restore


:servlet
java -Dtomcat.home=%TOMCAT_HOME% org.apache.tools.ant.Main -Dport %PORT% -Dhost %HOST% -Dwatchdog.home %WATCHDOG_HOME% -f %WATCHDOG_HOME%/conf/runtest.xml startup servlet-test shutdown


:restore
set CLASSPATH=%TOM_PREV_CLASSPATH%
set WATCHDOG_HOME=
set PORT=
set HOST=

goto end

:exit
echo usage: %0 {all/jsp/servlet} [serverhost] [serverport]

:end
