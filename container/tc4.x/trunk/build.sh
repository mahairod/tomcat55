#!/bin/sh
# -----------------------------------------------------------------------------
# build.sh - Build Script for Tomcat
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
#   JMX_HOME         Must point at your JMX installation [REQUIRED]
#
#   JNDI_HOME        Must point at your JNDI installation [REQUIRED]
#
#   JSSE_HOME        Must point at your JSSE installation [REQUIRED]
#
#   REGEXP_HOME      Must point at your Regexp installation [REQUIRED]
#
#   SERVLETAPI_HOME  Must point at your "jakarta-servletapi" installation.
#                    [../jakarta-servletapi-4/dist]
# 
#   CATALINA_JAXP_HOME        
#                    JAXP 1.0 compliant XML parser installation directory 
#                    used for catalina [$JAXP_HOME]
#
#   CATALINA_JAXP_PARSER_JAR  
#                    The jar filename of the JAXP compliant XML parser 
#                    used for catalina [$JAXP_PARSER_JAR]
#
#   JASPER_JAXP_HOME        
#                    JAXP 1.1 compliant XML parser installation directory 
#                    used for jasper [$JAXP_HOME]
#
#   JASPER_JAXP_PARSER_JAR  
#                    The jar filename of the JAXP compliant XML parser 
#                    used for jasper [$JAXP_PARSER_JAR]
#
# $Id$
# -----------------------------------------------------------------------------


# ----- Verify and Set Required Environment Variables -------------------------

if [ "$JAVA_HOME" = "" ] ; then
  echo You must set JAVA_HOME to point at your Java Development Kit install
  exit 1
fi

if [ "$JMX_HOME" = "" ] ; then
  echo You must set JMX_HOME to point at your Java Management Extensions install
  exit 1
fi

if [ "$JNDI_HOME" = "" ] ; then
  echo You must set JNDI_HOME to point at your Java Naming and Directory Interface install
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
  SERVLETAPI_HOME=`pwd`/../jakarta-servletapi-4/dist
fi

if [ "$JAXP_PARSER_JAR" = "" ] ; then
  JAXP_PARSER_JAR=crimson.jar
fi

if [ "$ANT_XML_CLASSPATH" = "" ] ; then
  ANT_XML_CLASSPATH=$JAXP_HOME/$JAXP_PARSER_JAR:$JAXP_HOME/jaxp.jar
  ANT_USING_DEFAULT=true
fi

if [ "$CATALINA_JAXP_HOME" = "" ] ; then
  CATALINA_JAXP_HOME=$JAXP_HOME
fi

if [ "$JASPER_JAXP_HOME" = "" ] ; then
#  JASPER_JAXP_HOME=$JAXP_HOME
  JASPER_JAXP_HOME=../lib
fi

if [ "$CATALINA_JAXP_HOME" = "" -o "$JASPER_JAXP_HOME" = "" -o "$ANT_USING_DEFAULT" = "true" -a "$JAXP_HOME" = "" ] ; then
  echo You must set JAXP_HOME to point at your XML Parser install directory.
  echo By default, ant, catalina, and jasper will use jaxp.jar and crimson.jar from
  echo that directory. 
  echo - A different parser jar file can be specified globally for all
  echo "  components via environment variable JAXP_PARSER_JAR (e.g. xerces.jar)."
  echo - XML requirements for each component can also be set individually via 
  echo   the following environment variables:
  echo      ANT_XML_CLASSPATH
  echo      CATALINA_JAXP_HOME CATALINA_JAXP_PARSER_JAR
  echo      JASPER_JAXP_HOME JASPER_JAXP_PARSER_JAR
  exit 1
fi

if [ "$CATALINA_JAXP_PARSER_JAR" = "" ] ; then
  CATALINA_JAXP_PARSER_JAR=$JAXP_PARSER_JAR
fi

if [ "$JASPER_JAXP_PARSER_JAR" = "" ] ; then
#  JASPER_JAXP_PARSER_JAR=$JAXP_PARSER_JAR
  JASPER_JAXP_PARSER_JAR=crimson.jar
fi

if [ "$ANT_HOME" = "" ] ; then
  ANT_HOME=../jakarta-ant
fi

if [ "$ANT_OPTS" = "" ] ; then
  ANT_OPTS=""
fi

# ----- Set Up The Runtime Classpath ------------------------------------------

CP=$ANT_HOME/lib/ant.jar:$JAVA_HOME/lib/tools.jar:$ANT_XML_CLASSPATH
echo classpath is $CP

if [ "$CLASSPATH" != "" ] ; then
  CP=$CLASSPATH:$CP
  if [ "$OSTYPE" = "cygwin32" ] || [ "$OSTYPE" = "cygwin" ] ; then
     CP=`cygpath --path --windows "$CP"`
  fi
fi

# ----- Execute The Requested Build -------------------------------------------

$JAVA_HOME/bin/java $ANT_OPTS -classpath $CP org.apache.tools.ant.Main -Dant.home=$ANT_HOME -Dcatalina.jaxp.home=$CATALINA_JAXP_HOME -Dcatalina.jaxp.parser.jar=$CATALINA_JAXP_PARSER_JAR -Djasper.jaxp.home=$JASPER_JAXP_HOME -Djasper.jaxp.parser.jar=$JASPER_JAXP_PARSER_JAR -Djsse.home=$JSSE_HOME -Djmx.home=$JMX_HOME -Djndi.home=$JNDI_HOME -Dregexp.home=$REGEXP_HOME -Dservletapi.home=$SERVLETAPI_HOME -Djava.home=$JAVA_HOME "$@"

