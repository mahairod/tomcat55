@echo off

for %%i in (..\jakarta-site2\lib\*.jar) do call cpappend.bat %%i

echo CLASSPATH="%_CP%"

java -classpath "%_CP%" org.apache.tools.ant.Main -Dant.home=%_AH% %1 %2 %3

SET _CP=
