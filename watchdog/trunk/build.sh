#!/bin/sh
#
# $Id$
# Ksh wrapper around ant build system.

baseDir=`pwd`

echo $baseDir .....

ADDL_CLASSPATH=../jakarta-tools/ant.jar:../jakarta-tools/projectx-tr2.jar

if [ -n $CLASSPATH ]; then
  CLASSPATH=$ADDL_CLASSPATH:$CLASSPATH
else
  CLASSPATH=$ADDL_CLASSPATH
fi
export CLASSPATH

echo Building with classpath $CLASSPATH

java org.apache.tools.ant.Main $*

