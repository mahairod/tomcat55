#! /bin/sh

# $Id$

cp=../jakarta-tools/ant.jar:../jakarta-tools/projectx-tr2.jar

java -classpath $cp:$CLASSPATH org.apache.tools.ant.Main $*
