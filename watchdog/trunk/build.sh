#!/bin/sh
#
# $Id$
# Ksh wrapper around ant build system.

baseDir=`pwd`

echo $baseDir .....

ADDL_CLASSPATH=../build/tomcat/classes:../jakarta-tools/ant.jar:../jakarta-tools/moo.jar:../jakarta-tools/projectx-tr2.jar:../build/tomcat/lib/servlet.jar:../build/tomcat/lib/jasper.jar:../build/tomcat/lib/xml.jar

if [[ -n $CLASSPATH ]]; then
  CLASSPATH=$ADDL_CLASSPATH:$CLASSPATH
else
  CLASSPATH=$ADDL_CLASSPATH
fi
export CLASSPATH

echo Building with classpath $CLASSPATH

java org.apache.tools.ant.Main $*

