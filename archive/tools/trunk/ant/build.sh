#!/bin/sh

ADDL_CLASSPATH=./../ant.jar:./../projectx-tr2.jar:./../javac.jar

if test -n $CLASSPATH ; then
  export CLASSPATH=$ADDL_CLASSPATH:$CLASSPATH
else
  export CLASSPATH=$ADDL_CLASSPATH
fi

echo Building with classpath $CLASSPATH

java org.apache.tools.ant.Main $*
