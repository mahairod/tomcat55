#! /bin/sh

# $Id$

if [ -z "$JAVA_HOME" ]
then
JAVACMD=`which java`
if [ -z "$JAVACMD" ]
then
echo "Cannot find JAVA. Please set your PATH."
exit 1
fi
JAVA_BINDIR=`dirname $JAVACMD`
JAVA_HOME=$JAVA_BINDIR/..
fi

JAVACMD=$JAVA_HOME/bin/java

cp=../jakarta-ant/lib/ant.jar:../jakarta-ant/lib/xml.jar:$JAVA_HOME/lib/tools.jar
$JAVACMD -classpath $cp:$CLASSPATH org.apache.tools.ant.Main "$@"

