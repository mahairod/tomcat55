#!/bin/sh

ADDL_CLASSPATH=./../ant.jar:./../projectx-tr2.jar:./../javac.jar

if [ "$CLASSPATH" != "" ] ; then
  CLASSPATH=$ADDL_CLASSPATH:$CLASSPATH
else
 CLASSPATH=$ADDL_CLASSPATH
fi
export CLASSPATH

echo Building with classpath $CLASSPATH

java org.apache.tools.ant.Main $*
