#! /bin/sh

# $Id$

cp=../jakarta-tools/ant.jar:../jakarta-tools/projectx-tr2.jar:../build/tomcat/classes

java -classpath $cp:$CLASSPATH org.apache.tools.ant.Main "$@"

chmod +x `find ../build -name "*.sh"`
