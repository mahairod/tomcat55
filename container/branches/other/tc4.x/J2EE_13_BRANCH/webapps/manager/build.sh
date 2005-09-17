#!/bin/sh
# -----------------------------------------------------------------------------
# build.sh - Build Script for webapp subdir
#
# Environment Variable Prerequisites:
#
#   JAVA_HOME        Must point at your Java Development Kit [REQUIRED]
#
#   JAXP_HOME        Points at a JAXP compliant XML parser 
#                    installation directory [NONE]
#
#   JAXP_PARSER_JAR  The jar filename of the JAXP compliant 
#                    'XML parser' [crimson.jar]
#
#   ANT_HOME         Must point at your Ant installation [../jakarta-ant]
#
#   ANT_OPTS         Command line options to the Java runtime
#                    that executes Ant [NONE]
#
#   ANT_XML_CLASSPATH  
#                    Jar files added to the classpath for the XML parsing
#                    requirements of ant
#                    [$JAXP_HOME/$JAXP_PARSER_JAR:$JAXP_HOME/jaxp.jar]
# 
#   SERVLETAPI_HOME  Must point at your "jakarta-servletapi" installation.
#                    [REQUIRED]
#
# $Id$
# -----------------------------------------------------------------------------


# ----- Verify and Set Required Environment Variables -------------------------

if [ "$JAVA_HOME" = "" ] ; then
  echo You must set JAVA_HOME to point at your Java Development Kit install
  exit 1
fi

if [ "$SERVLETAPI_HOME" = "" ] ; then
  echo You must set SERVLETAPI_HOME to your Servlet API distribution that includes the Servlet 2.3 and JSP 1.2 API classes.
  exit 1
fi

if [ "$JAXP_PARSER_JAR" = "" ] ; then
  JAXP_PARSER_JAR=crimson.jar
fi

if [ "$ANT_XML_CLASSPATH" = "" ] ; then
  ANT_XML_CLASSPATH=$JAXP_HOME/$JAXP_PARSER_JAR:$JAXP_HOME/jaxp.jar
  ANT_USING_DEFAULT=true
fi

if [ "$ANT_USING_DEFAULT" = "true" -a "$JAXP_HOME" = "" ] ; then
  echo You must set JAXP_HOME to point at your XML Parser install directory.
  echo By default, ant will use jaxp.jar and crimson.jar from
  echo that directory. 
  echo - A different parser jar file can be specified globally for all
  echo "  components via environment variable JAXP_PARSER_JAR (e.g. xerces.jar)."
  echo - XML requirements for each component can also be set individually via 
  echo   the following environment variables:
  echo      ANT_XML_CLASSPATH
  exit 1
fi

if [ "$ANT_HOME" = "" ] ; then
  ANT_HOME=../jakarta-ant
fi

if [ "$ANT_OPTS" = "" ] ; then
  ANT_OPTS=""
fi

# ----- Set Up The Runtime Classpath ------------------------------------------

CP=$ANT_HOME/lib/ant.jar:$JAVA_HOME/lib/tools.jar:$ANT_XML_CLASSPATH

if [ "$CLASSPATH" != "" ] ; then
  CP=$CLASSPATH:$CP
fi

# ----- Execute The Requested Build -------------------------------------------

$JAVA_HOME/bin/java $ANT_OPTS -classpath $CP org.apache.tools.ant.Main -Dservletapi.home=$SERVLETAPI_HOME -Djava.home=$JAVA_HOME "$@"
