#!/bin/sh
# build.sh -- Build Script for the "Hello, World" Application
# $Id$

# Identify the custom class path components we need
CP=$CATALINA_HOME/lib/ant.jar:$CATALINA_HOME/lib/servlet.jar
CP=$CP:$CATALINA_HOME/lib/jaxp.jar:$CATALINA_HOME/lib/parser.jar
CP=$CP:$JAVA_HOME/lib/tools.jar

# Execute ANT to perform the requested build target
java -classpath $CP:$CLASSPATH org.apache.tools.ant.Main \
  -Dtomcat.home=$CATALINA_HOME "$@"
