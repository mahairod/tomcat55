@echo off
rem $Id$
rem Startup batch file for servlet runner.

rem This batch file written and tested under Windows NT
rem Improvements to this file are welcome

set baseDir=%pwd%
set host=localhost
set port=8080
set stest=.\conf\servlet-testlist.txt
set jtest=.\conf\jsp-testlist.txt
set default=all

set miscJars=.\lib\xml.jar;.\lib\moo.jar;.\client.jar
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

rem THIS PART INCLUDED from jakarta-tomcat/src/shell/tomcatEnv.bat
rem -------------------------- begin tomcatEnv.bat -----------------------------
set TOM_CLASSPATH=.
set TOM_CLASSPATH=%TOMCAT_HOME%\webserver.jar;%TOM_CLASSPATH%
set TOM_CLASSPATH=%TOMCAT_HOME%\servlet.jar;%TOM_CLASSPATH%
set TOM_CLASSPATH=%TOMCAT_HOME%\jasper.jar;%TOM_CLASSPATH%
set TOM_CLASSPATH=%TOMCAT_HOME%\xml.jar;%TOM_CLASSPATH%
set TOM_CLASSPATH=%TOMCAT_HOME%\webpages\WEB-INF\classes\jsp\beans;%TOM_CLASSPATH%
set TOM_CLASSPATH=%TOMCAT_HOME%\classes;%TOM_CLASSPATH%

set TOM_CLASSPATH=%JAVA_HOME%\lib\tools.jar;%TOM_CLASSPATH%


set TOM_PREV_CLASSPATH=%CLASSPATH%
set CLASSPATH=%TOM_CLASSPATH%;%CLASSPATH%
rem -------------------------- end tomcatEnv.bat -------------------------------

start java org.apache.tomcat.shell.Startup -config .\conf\server-test.xml %1 %2 %3 %4 %5 %6 %7 %8 %9

sleep 25

if "%default%"=="servlet" goto servlet

java -Dtest.hostName=%host% -Dtest.port=%port% org.apache.tools.moo.Main -testfile %jtest%

if "%default%"=="all" goto servlet
goto shutdown

:servlet
java -Dtest.hostName=%host% -Dtest.port=%port% org.apache.tools.moo.Main -testfile %stest%

:shutdown
java org.apache.tomcat.shell.Shutdown %1 %2 %3 %4 %5 %6 %7 %8 %9

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

rem pause
