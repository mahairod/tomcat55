@echo off
rem $Id$
rem Build file for stoopid dos machines

if "%CLASSPATH%" == "" goto noclasspath

rem else
set _CLASSPATH=%CLASSPATH%
set CLASSPATH=.\..\jakarta-tools\ant.jar;.\..\jakarta-tools\moo.jar;.\..\jakarta-tools\projectx-tr2.jar;.\..\jakarta-tools\servlet-2.2.0.jar;%CLASSPATH%
goto next

:noclasspath
set _CLASSPATH=
set CLASSPATH=.\src\lib\ant.jar;.\src\lib\moo.jar;.\src\lib\xml.jar
goto next

:next
echo Using classapth: %CLASSPATH%

java org.apache.tools.ant.Main %1 %2 %3 %4 %5 %6 %7 %8 %9

if "%1"=="dist" goto ear
goto clean

:ear
   echo BUILDING EAR....
   mkdir dist\foo
   mkdir dist\foo\META-INF
   copy src\etc\ear-dd.xml dist\foo\META-INF\application.xml
   copy dist\servlet-tests.war dist\foo
   copy dist\jsp-tests.war dist\foo
   jar -cf dist\jcheck.ear dist\foo\META-INF\application.xml dist\foo\jsp-tests.war dist\foo\servlet-tests.war
   rm -r dist\foo

:clean

rem clean up classpath after
set CLASSPATH=%_CLASSPATH%
set _CLASSPATH=
