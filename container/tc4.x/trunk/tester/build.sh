#!/bin/sh
# -----------------------------------------------------------------------------
# build.sh - Build Script for Tester
#
# Environment Variable Prerequisites:
#
#   ANT_HOME         Must point at your Ant installation [../jakarta-ant]
#
#   ANT_OPTS         Command line options to the Java runtime
#                    that executes Ant [NONE]
#
#   JAVA_HOME        Must point at your Java Development Kit [REQUIRED]
#
# $Id$
# -----------------------------------------------------------------------------


# ----- Verify and Set Required Environment Variables -------------------------

if [ "$JAVA_HOME" = "" ] ; then
  echo You must set JAVA_HOME to point at your Java Development Kit install
  exit 1
fi

if [ "$ANT_HOME" = "" ] ; then
  ANT_HOME=../jakarta-ant
fi

if [ "$ANT_OPTS" = "" ] ; then
  ANT_OPTS=""
fi

# ----- Set Up The Runtime Classpath ------------------------------------------

CP=$ANT_HOME/lib/ant.jar:$JAVA_HOME/lib/tools.jar

if [ "$CLASSPATH" != "" ] ; then
  CP=$CLASSPATH:$CP
fi

# ----- Execute The Requested Build -------------------------------------------

$JAVA_HOME/bin/java $ANT_OPTS -classpath $CP org.apache.tools.ant.Main \
 -Dant.home=$ANT_HOME \
 -Djava.home=$JAVA_HOME \
 "$@"
