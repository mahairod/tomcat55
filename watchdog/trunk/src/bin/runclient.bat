@echo off
rem $Id$
rem Startup batch file for servlet check. 

rem This batch file written and tested under Windows NT
rem Improvements to this file are welcome

set host=localhost
set port=8080
set jsp-test=jsp-testlist.txt
set servlet-test=servlet-testlist.txt
set default=all
set baseDir=%pwd%

set libDir=.\lib\

set addtl_classPath=.\client.jar;.\..\..\jakarta-tools\moo.jar
set cp=%CLASSPATH%

set CLASSPATH=%addtl_classPath%

if "%cp%"=="" goto next
set CLASSPATH=%CLASSPATH%;%cp%

:next

if "%1"=="servlet" goto parm
if "%1"=="jsp" goto parm
if "%1"=="all" goto parm

rem else
echo Usage: runClient.bat {jsp/servlet/all} [host] [port]
goto end

:parm
set default=%1
set host=%2
set port=%3

echo Running test against %host%:%port%

if "%default%"=="servlet" goto servlet

java -Dtest.hostName=%host% -Dtest.port=%port% org.apache.tools.moo.Main -testfile %jsp-test%

if "%default%"=="all" goto servlet
goto end

:servlet

java -Dtest.hostName=%host% -Dtest.port=%port% org.apache.tools.moo.Main -testfile %servlet-test%

rem clean up

:end
set CLASSPATH=%cp%
set port=
set host=
set libDir=
set cp=
