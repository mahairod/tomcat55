@echo off
rem $Id$
rem Startup batch file for servlet runner.

rem This batch file written and tested under Windows NT
rem Improvements to this file are welcome

set baseDir=%pwd%
set host=localhost
set port=8080
if "%2"=="" goto nohost
set host=%2
:nohost
if "%3"=="" goto noport
set port=%3
:noport
set stest=.\conf\servlet-testlist.txt
set jtest=.\conf\jsp-testlist.txt
set default=all

set miscJars=.\lib\xml.jar;.\lib\moo.jar;.\lib\client.jar
set appJars=%miscJars%
set sysJars=%JAVA_HOME%\lib\tools.jar

set appClassPath=.\clients;%appJars%
set cp=%CLASSPATH%

set CLASSPATH=%appClassPath%;%sysJars%

if "%cp%"=="" goto next
rem else
set CLASSPATH=%CLASSPATH%;%cp%

:next
echo Using classpath: %CLASSPATH%

if "%1"=="" goto run
set default=%1

:run
set TOMCAT_HOME=..\tomcat
SET _ANTHOME=%ANT_HOME%
if "%ANT_HOME%" == "" SET ANT_HOME=..\jakarta-ant


rem THIS PART INCLUDED from jakarta-tomcat/src/shell/tomcatEnv.bat
rem -------------------------- begin tomcatEnv.bat -----------------------------
set TOM_CLASSPATH=.
set TOM_CLASSPATH=%TOMCAT_HOME%\webserver.jar;%TOM_CLASSPATH%
set TOM_CLASSPATH=%TOMCAT_HOME%\lib\servlet.jar;%TOM_CLASSPATH%
set TOM_CLASSPATH=%TOMCAT_HOME%\lib\jasper.jar;%TOM_CLASSPATH%
set TOM_CLASSPATH=%TOMCAT_HOME%\lib\xml.jar;%TOM_CLASSPATH%
set TOM_CLASSPATH=%TOMCAT_HOME%\webpages\WEB-INF\classes\jsp\beans;%TOM_CLASSPATH%
set TOM_CLASSPATH=%TOMCAT_HOME%\classes;%TOM_CLASSPATH%
set TOM_CLASSPATH=%TOMCAT_HOME%\lib\ant.jar;%TOM_CLASSPATH%

set TOM_CLASSPATH=%JAVA_HOME%\lib\tools.jar;%TOM_CLASSPATH%


set TOM_PREV_CLASSPATH=%CLASSPATH%
set CLASSPATH=%TOM_CLASSPATH%;%CLASSPATH%
rem -------------------------- end tomcatEnv.bat -------------------------------

rem Only start Tomcat if no host and port parameters have been specified
if not "%port%"=="8080" goto otherserver

java org.apache.tomcat.startup.Tomcat -config .\conf\server-test.xml %4 %5 %6 %7 %8 %9

sleep 25

:otherserver

if "%default%"=="servlet" goto servlet

rem java -Dtest.hostName=%host% -Dtest.port=%port% org.apache.tools.moo.Main -testfile %jtest%

rem test jsp using non-moo framework
%TOMCAT_HOME%/ant -Dwatchdog.home %WATCHDOG_HOME% -f conf/jsp.xml jsp-test

if "%default%"=="all" goto servlet
goto shutdown

:servlet
rem java -Dtest.hostName=%host% -Dtest.port=%port% org.apache.tools.moo.Main -testfile %stest%
%TOMCAT_HOME%/ant -Dwatchdog.home $WATCHDOG_HOME -f conf/servlet.xml servlet-test

:shutdown
rem Only shutdown Tomcat if no host and port parameters have been specified
if not "%port%"=="8080" goto cleanup

java org.apache.tomcat.startup.Tomcat -stop -config .\conf\server-test.xml %4 %5 %6 %7 %8 %9

:cleanup
rem clean up

set CLASSPATH=%cp%
set port=
set host=
set test=
set jsdkJars=
set jspJars=
set beanJars=
set miscJars=
set appJars=
set appClassPath=
set cp=
SET ANT_HOME=%_ANTHOME%
SET _ANTHOME=

rem pause
