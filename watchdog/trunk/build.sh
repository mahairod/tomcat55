#!/bin/ksh
#
# $Id$
# Ksh wrapper around ant build system.

baseDir=`pwd`

echo $baseDir .....

ADDL_CLASSPATH=./../jakarta-tools/ant.jar:./../jakarta-tools/moo.jar:./../jakarta-tools/projectx-tr2.jar:./../jakarta-tools/servlet-2.2.0.jar

if [[ -n $CLASSPATH ]]; then
  export CLASSPATH=$ADDL_CLASSPATH:$CLASSPATH
else
  export CLASSPATH=$ADDL_CLASSPATH
fi

echo Building with classpath $CLASSPATH

java org.apache.tools.ant.Main $*

