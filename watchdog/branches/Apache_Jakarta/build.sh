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

if [[ $1 != clean ]]; then
  echo chmoding ...
  chmod 775 build/runTest
  chmod 775 build/runClient
fi

if [[ $1 == dist ]]; then
  chmod 775 dist/runTest
  chmod 775 dist/runClient
  echo BUILDING EAR....
  mkdir dist/foo
  mkdir dist/foo/META-INF
  cp src/etc/ear-dd.xml dist/foo/META-INF/application.xml
  cp dist/servlet-tests.war dist/foo
  cp dist/jsp-tests.war dist/foo
  cd dist/foo
  jar -cf ../jcheck.ear META-INF/application.xml jsp-tests.war servlet-tests.war
  cd ${baseDir}
  rm -rf dist/foo
fi

