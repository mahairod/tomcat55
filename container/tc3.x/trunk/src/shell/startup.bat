@echo off
rem $Id$
rem Startup batch file for tomcat servner.

rem This batch file written and tested under Windows NT
rem Improvements to this file are welcome

set arg=start
if not "%1" == "-nospawn" goto noSpawnControl
set arg=-nospawn start
shift
:noSpawnControl

call tomcat %arg% %1 %2 %3 %4 %5 %6 %7 %8 %9 
rem pause
