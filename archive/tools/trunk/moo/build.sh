#!/bin/sh
#
# $Id$
# Ksh wrapper around ant build system.

ADDL_CLASSPATH=./../../jakarta-ant/lib/ant.jar:./../../jakarta-ant/lib/xml.jar

if test -n $CLASSPATH; then
  export CLASSPATH=$ADDL_CLASSPATH:$CLASSPATH
else
  export CLASSPATH=$ADDL_CLASSPATH
fi

echo Building with classpath $CLASSPATH

java org.apache.tools.ant.Main $*
