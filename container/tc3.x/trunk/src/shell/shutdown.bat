@echo off
rem $Id$
rem Startup batch file for tomcat server.

if "%TOMCAT_HOME%" == "" goto bin
cmd /c "cd %TOMCAT_HOME% & bin\tomcat stop"
goto :eof

:bin
call bin\tomcat stop

:eof
