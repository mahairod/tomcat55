#!/bin/sh
# -----------------------------------------------------------------------------
# build.sh - Build Script for webapps
#
# Environment Variable Prerequisites:
#
#   ANT_HOME         Must point at your Ant installation [../../jakarta-ant]
#
#   ANT_OPTS         Command line options to the Java runtime
#                    that executes Ant [NONE]
#
#   JAVA_HOME        Must point at your Java Development Kit [REQUIRED]
#
#   XERCES_HOME      Must point at your XERCES installation [REQUIRED]
#
#   SERVLETAPI_HOME  Must point at your "jakarta-servletapi" installation.
#                    [REQUIRED]
#
# $Id$
# -----------------------------------------------------------------------------


# ----- Verify and Set Required Environment Variables -------------------------

if [ "$ANT_HOME" = "" ] ; then
  ANT_HOME=../../jakarta-ant
fi

if [ "$ANT_OPTS" = "" ] ; then
  ANT_OPTS=""
fi

if [ "$JAVA_HOME" = "" ] ; then
  echo You must set JAVA_HOME to point at your Java Development Kit install
  exit 1
fi

if [ "$SERVLETAPI_HOME" = "" ] ; then
  echo You must set SERVLETAPI_HOME to your Servlet API distribution that includes the Servlet 2.3 and JSP 1.2 API classes.
  exit 1
fi


# ----- Set Up The Runtime Classpath ------------------------------------------

CP=$ANT_HOME/lib/ant.jar:$JAVA_HOME/lib/tools.jar:$XERCES_HOME\xerces.jar
if [ "$CLASSPATH" != "" ] ; then
  CP=$CLASSPATH:$CP
fi


# ----- Execute The Requested Build -------------------------------------------

java $ANT_OPTS -classpath $CP org.apache.tools.ant.Main -Dant.home=$ANT_HOME -Dxerces.home=$XERCES_HOME -Dservletapi.home=$SERVLETAPI_HOME "$@"

