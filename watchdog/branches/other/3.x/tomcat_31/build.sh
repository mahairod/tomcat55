#!/bin/sh
#
# $Id$
# Ksh wrapper around ant build system.

baseDir=`pwd`

echo $baseDir .....

ADDL_CLASSPATH=../jakarta-ant/lib/ant.jar:../jakarta-ant/lib/xml.jar:../jakarta-tools/servlet-2.2.0.jar

if [ -n $CLASSPATH ]; then
  CLASSPATH=$CLASSPATH:$ADDL_CLASSPATH
else
  CLASSPATH=$ADDL_CLASSPATH
fi
export CLASSPATH

echo Building with classpath $CLASSPATH

java org.apache.tools.ant.Main $*

