#!/bin/sh
# -----------------------------------------------------------------------------
# build.sh - Build Script for Tomcat
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
#   XERCES_HOME      Must point at your XERCES installation [REQUIRED]
#
#   JSSE_HOME        Must point at your JSSE installation [REQUIRED]
#
#   REGEXP_HOME      Must point at your "jakarta-regexp" installation
#                    [../jakarta-regexp]
#
#   SERVLETAPI_HOME  Must point at your "jakarta-servletapi" installation.
#                    [../jakarta-servletapi]
#
# $Id$
# -----------------------------------------------------------------------------


# ----- Verify and Set Required Environment Variables -------------------------

if [ "$ANT_HOME" = "" ] ; then
  ANT_HOME=../jakarta-ant
fi

if [ "$ANT_OPTS" = "" ] ; then
  ANT_OPTS=""
fi

if [ "$JAVA_HOME" = "" ] ; then
  echo You must set JAVA_HOME to point at your Java Development Kit install
  exit 1
fi

if [ "$XERCES_HOME" = "" ] ; then
  echo You must set XERCES_HOME to point at your Xerces install
  exit 1
fi

if [ "$JSSE_HOME" = "" ] ; then
  echo You must set JSSE_HOME to point at your Java Security Extensions install
  exit 1
fi

if [ "$REGEXP_HOME" = "" ] ; then
  echo You must set REGEXP_HOME to point at your Regular Expressions distribution install
  exit 1
fi

if [ "$SERVLETAPI_HOME" = "" ] ; then
  echo You must set SERVLETAPI_HOME to your Servlet API distribution that includes the Servlet 2.3 and JSP 1.2 API classes.
  exit 1
fi


# ----- Set Up The Runtime Classpath ------------------------------------------

CP=$ANT_HOME/lib/ant.jar:$JAVA_HOME/lib/tools.jar:$XERCES_HOME/xerces.jar

if [ "$CLASSPATH" != "" ] ; then
  CP=$CLASSPATH:$CP
fi


# ----- Execute The Requested Build -------------------------------------------

java $ANT_OPTS -classpath $CP org.apache.tools.ant.Main -Dant.home=$ANT_HOME -Dxerces.home=$XERCES_HOME -Djsse.home=$JSSE_HOME -Dregexp.home=$REGEXP_HOME -Dservletapi.home=$SERVLETAPI_HOME "$@"
