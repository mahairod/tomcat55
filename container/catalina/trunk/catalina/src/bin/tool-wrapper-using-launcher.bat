@echo off
if "%OS%" == "Windows_NT" setlocal

rem ---------------------------------------------------------------------------
rem
rem Script for running the Catalina tool wrapper using the Launcher
rem
rem ---------------------------------------------------------------------------

rem Get standard environment variables
set PRG=%0
if exist %PRG%\..\setenv.bat goto gotCmdPath
rem %0 must have been found by DOS using the %PATH% so we assume that
rem setenv.bat will also be found in the %PATH%
call setenv.bat
goto doneSetenv
:gotCmdPath
call %PRG%\..\setenv.bat
:doneSetenv

rem Make sure prerequisite environment variables are set
if not "%JAVA_HOME%" == "" goto gotJavaHome
echo The JAVA_HOME environment variable is not defined
echo This environment variable is needed to run this program
goto end
:gotJavaHome

rem Get command line arguments and save them with the proper quoting
set CMD_LINE_ARGS=
:setArgs
if ""%1""=="""" goto doneSetArgs
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto setArgs
:doneSetArgs

rem Set classpath to handle when %0 does not contain any path
set PRG_CLASSPATH=%PRG%\..
if "%OS%" == "Windows_NT" goto gotClasspath
set PRG_CLASSPATH=%PRG_CLASSPATH%;"%PATH%;."
:gotClasspath

rem Execute the Launcher using the "tool-wrapper" target
"%JAVA_HOME%\bin\java.exe" -classpath %PRG_CLASSPATH% LauncherBootstrap -launchfile catalina.xml -verbose tool-wrapper %CMD_LINE_ARGS%

:end
