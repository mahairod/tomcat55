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
SET CLASSPATH=%WATCHDOG_HOME%\lib\ant.jar;%CLASSPATH%

echo "using classpath=" %CLASSPATH%
if "%1"=="servlet" goto servlet

if "%1"=="jsp" goto jsp
if "%1"=="jsp-xml" goto jsp-xml
if "%1"=="jsp-all" goto jsp-all
if "%1"=="all" goto jsp-all


:jsp
java org.apache.tools.ant.Main -Dport %PORT% -Dhost %HOST% -Dwatchdog.home %WATCHDOG_HOME% -f %WATCHDOG_HOME%/conf/jsp-gtest.xml jsp-test
goto restore

:jsp-xml
if "%TOMCAT_HOME%=="" goto exit
java -DJSP_ROOT %TOMCAT_HOME%\webapps\jsp-tests\jsp -DWATCHDOG_HOME$ %WATCHDOG_HOME% org.apache.jspxml.GetWorkspaceInXML
java org.apache.tools.ant.Main -Dport %PORT% -Dhost %HOST% -Dwatchdog.home %WATCHDOG_HOME% -f %WATCHDOG_HOME%/conf/jsp-gtest-xml.xml jsp-test
goto restore

:jsp-all
java org.apache.tools.ant.Main -Dport %PORT% -Dhost %HOST% -Dwatchdog.home %WATCHDOG_HOME% -f %WATCHDOG_HOME%/conf/jsp-gtest.xml jsp-test

if "%TOMCAT_HOME%=="" goto exit
java -DJSP_ROOT %TOMCAT_HOME%\webapps\jsp-tests\jsp -DWATCHDOG_HOME$ %WATCHDOG_HOME% org.apache.jspxml.GetWorkspaceInXML
java org.apache.tools.ant.Main -Dport %PORT% -Dhost %HOST% -Dwatchdog.home %WATCHDOG_HOME% -f %WATCHDOG_HOME%/conf/jsp-gtest-xml.xml jsp-test

:servlet
java org.apache.tools.ant.Main -Dport %PORT% -Dhost %HOST% -Dwatchdog.home %WATCHDOG_HOME% -f %WATCHDOG_HOME%/conf/servlet-moo.xml servlet-test


:restore
set CLASSPATH=%TOM_PREV_CLASSPATH%
set WATCHDOG_HOME=
set PORT=
set HOST=

goto end

:exit
echo usage: %0 {all/jsp/jsp-xml/jsp-all/servlet} [serverhost] [serverport] 

:end
