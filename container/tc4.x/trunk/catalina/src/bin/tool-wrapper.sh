#!/bin/sh
# -----------------------------------------------------------------------------
# tool-wrapper.sh - Wrapper for command line tools
#
# Environment Variable Prequisites:
#
#   CATALINA_HOME (Optional) Catalina binary distribution directory.
#                 If not present, the directory above this "bin" directory
#                 is assumed.
#
#   JAVA_HOME     (Required) Java Development Kit installation directory.
#
#   TOOL_OPTS     (Optional) Java execution options for the tool.
#
# $Id$
# -----------------------------------------------------------------------------


# ----- Verify and Set Required Environment Variables -------------------------

if [ -z "$CATALINA_HOME" ] ; then
  ## resolve links - $0 may be a link to  home
  PRG=$0
  progname=`basename $0`
  
  while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '.*/.*' > /dev/null; then
	PRG="$link"
    else
	PRG="`dirname $PRG`/$link"
    fi
  done
  
  CATALINA_HOME_1=`dirname "$PRG"`/..
  echo "Guessing CATALINA_HOME from catalina.sh to ${CATALINA_HOME_1}" 
    if [ -d ${CATALINA_HOME_1}/conf ] ; then 
	CATALINA_HOME=${CATALINA_HOME_1}
	echo "Setting CATALINA_HOME to $CATALINA_HOME"
    fi
fi

# Get user customizable environment variables
. $CATALINA_HOME/bin/setenv.sh

if [ -z "$TOOL_OPTS" ] ; then
  TOOL_OPTS=""
fi

if [ -z "$JAVA_HOME" ] ; then
  echo You must set JAVA_HOME to point at your Java Development Kit installation
  exit 1
fi


# ----- Cygwin Unix Paths Setup -----------------------------------------------

# Cygwin support.  $cygwin _must_ be set to either true or false.
case "`uname`" in
  CYGWIN*) cygwin=true ;;
  *) cygwin=false ;;
esac
 
# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
  [ -n "$CATALINA_HOME" ] &&
    CATALINA_HOME=`cygpath --unix "$CATALINA_HOME"`
    [ -n "$JAVA_HOME" ] &&
    JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
fi


# ----- Set Up The System Classpath -------------------------------------------

CP="$CATALINA_HOME/bin/bootstrap.jar"

if [ -f "$JAVA_HOME/lib/tools.jar" ] ; then
  CP=$CP:"$JAVA_HOME/lib/tools.jar"
fi


# ----- Cygwin Windows Paths Setup --------------------------------------------

# convert the existing path to windows
if $cygwin ; then
   CP=`cygpath --path --windows "$CP"`
   CATALINA_HOME=`cygpath --path --windows "$CATALINA_HOME"`
   JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
fi


# ----- Execute The Requested Command -----------------------------------------

# echo "Using CLASSPATH:     $CP"
# echo "Using CATALINA_HOME: $CATALINA_HOME"
# echo "Using JAVA_HOME:     $JAVA_HOME"

$JAVA_HOME/bin/java $TOOL_OPTS -classpath $CP \
 -Dcatalina.home=$CATALINA_HOME \
 org.apache.catalina.startup.Tool "$@"

