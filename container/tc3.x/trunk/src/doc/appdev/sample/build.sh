#!/bin/sh
# build.sh -- Build Script for the "Hello, World" Application
# $Id$

# Identify the custom class path components we need
CP=$TOMCAT_HOME/webapps/admin/WEB-INF/lib/ant.jar:$TOMCAT_HOME/lib/common/servlet.jar
CP=$CP:$TOMCAT_HOME/lib/container/crimson.jar
CP=$CP:$JAVA_HOME/lib/tools.jar

# Execute ANT to perform the requested build target
java -classpath $CP:$CLASSPATH org.apache.tools.ant.Main \
  -Dtomcat.home=$TOMCAT_HOME "$@"
