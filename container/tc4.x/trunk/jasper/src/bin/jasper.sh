#!/bin/sh
# -----------------------------------------------------------------------------
# jasper.sh - Global Script Jasper
#
# Environment Variable Prequisites
#
# Environment Variable Prequisites:
#   JASPER_HOME (Optional)
#       May point at your Jasper "build" directory.
#       If not present, the current working directory is assumed.
#   JASPER_OPTS (Optional) 
#       Java runtime options
#   JAVA_HOME     
#       Must point at your Java Development Kit installation.
#
# $Id$
# -----------------------------------------------------------------------------


# ----- Verify and Set Required Environment Variables -------------------------

if [ "$JASPER_HOME" = "" ] ; then
  JASPER_HOME=`pwd`
fi

if [ "$JASPER_OPTS" = "" ] ; then
  JASPER_OPTS=""
fi

if [ "$JAVA_HOME" = "" ] ; then
  echo You must set JAVA_HOME to point at your Java Development Kit installation
  exit 1
fi

# ----- Set Up The System Classpath -------------------------------------------

# FIXME CP=$JASPER_HOME/dummy
# FIXME below
CP=$CP:$JASPER_HOME/classes
for i in $JASPER_HOME/lib/*.jar ; do
  CP=$CP:$i
done

echo Using CLASSPATH: $CP


# ----- Execute The Requested Command -----------------------------------------

if [ "$1" = "debug" ] ; then

  shift
  java $JASPER_OPTS -classpath $CP \
   -Djasper.home=$JASPER_HOME \
   org.apache.jasper.JspC "$@"

else

  echo "Usage: jasper.sh ( jspc )"
  echo "Commands:"
  echo   jspc - Run the jasper offline JSP compiler
  exit 1

fi
