@echo off
rem $Id$
rem Build file for stoopid dos machines

SET _ANTHOME=%ANT_HOME%
if "%ANT_HOME%" == "" SET ANT_HOME=..\jakarta-ant

if "%CLASSPATH%" == "" goto noclasspath

rem else
set _CLASSPATH=%CLASSPATH%
set CLASSPATH=%CLASSPATH%;%ANT_HOME%\lib\ant.jar;%JAVA_HOME%\lib\tools.jar
goto next

:noclasspath
set _CLASSPATH=
set CLASSPATH=%ANT_HOME%\lib\ant.jar
goto next

:next
echo Using classpath: %CLASSPATH%

java org.apache.tools.ant.Main -Dant.home=%ANT_HOME% %1 %2 %3 %4 %5 %6 %7 %8 %9

:clean

rem clean up classpath after
set CLASSPATH=%_CLASSPATH%
set _CLASSPATH=
SET ANT_HOME=%_ANTHOME%
SET _ANTHOME=
