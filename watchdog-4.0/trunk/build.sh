#!/bin/sh
#
# $Id$
# Ksh wrapper around ant build system.

baseDir=`pwd`

echo $baseDir .....

if [ "$ANT_HOME" = "" ] ; then
  ANT_HOME=../jakarta-ant
fi

ADDL_CLASSPATH=$ANT_HOME/lib/ant.jar:$JAVA_HOME/lib/tools.jar

if [ -n "$CLASSPATH" ]; then
  CLASSPATH=$CLASSPATH:$ADDL_CLASSPATH
else
  CLASSPATH=$ADDL_CLASSPATH
fi
export CLASSPATH

echo Building with classpath $CLASSPATH

java org.apache.tools.ant.Main $*

