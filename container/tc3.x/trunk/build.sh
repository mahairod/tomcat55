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

if [ "$ANT_HOME" = "" ] ; then
  ANT_HOME=../jakarta-ant-1.3
fi

$ANT_HOME/bin/ant "$@" 
