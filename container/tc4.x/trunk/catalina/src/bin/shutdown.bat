@echo off
rem ---------------------------------------------------------------------------
rem shutdown.bat - Stop Script for the CATALINA Server
rem
rem $Id$
rem ---------------------------------------------------------------------------

set _CATALINA_HOME=%CATALINA_HOME%
if not "%CATALINA_HOME%" == "" goto gotCatalinaHome
set CATALINA_HOME=.
:gotCatalinaHome
%CATALINA_HOME%\bin\catalina stop %1 %2 %3 %4 %5 %6 %7 %8 %9
set CATALINA_HOME=%_CATALINA_HOME%
set _CATALINA_HOME=
