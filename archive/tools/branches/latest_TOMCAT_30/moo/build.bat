@echo off
rem $Id$
rem Build file for stoopid dos machines

if "%CLASSPATH%" == "" got noclasspath

rem else
set _CLASSPATH=%CLASSPATH%
set CLASSPATH=.\..\ant.jar;.\..\servlet-2.2.0.jar;.\..\projectx-tr2.jar;%CLASSPATH%
goto next

:noclasspath
set _CLASSPATH=
set CLASSPATH=.\..\ant.jar
goto next

:next
echo Using classpath: %CLASSPATH%
java org.apache.tools.ant.Main %1 %2 %3 %4 %5 %6 %7 %8 %9

rem clean up classpath after
set CLASSPATH=%_CLASSPATH%
set _CLASSPATH=
