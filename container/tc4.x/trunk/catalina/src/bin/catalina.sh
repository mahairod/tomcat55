#!/bin/sh
# -----------------------------------------------------------------------------
# catalina.sh - Start/Stop Script for the CATALINA Server
#
# Environment Variable Prequisites
#
#   CATALINA_HOME (Optional) May point at your Catalina "build" directory.
#                 If not present, the current working directory is assumed.
#
#   CATALINA_OPTS (Optional) Java runtime options used when the "start",
#                 "stop", or "run" command is executed.
#
#   JAVA_HOME     Must point at your Java Development Kit installation.
#
# $Id$
# -----------------------------------------------------------------------------


# ----- Verify and Set Required Environment Variables -------------------------

if [ "$CATALINA_HOME" = "" ] ; then
  CATALINA_HOME=`pwd`
fi

if [ "$CATALINA_OPTS" = "" ] ; then
  CATALINA_OPTS=""
fi

if [ "$JAVA_HOME" = "" ] ; then
  echo You must set JAVA_HOME to point at your Java Development Kit installation
  exit 1
fi

# ----- Set Up The System Classpath -------------------------------------------

CP=$CATALINA_HOME/bin/bootstrap.jar

for i in $CATALINA_HOME/lib/*.jar ; do
  CP=$CP:$i
done

if [ -f $JAVA_HOME/lib/tools.jar ] ; then
  CP=$CP:$JAVA_HOME/lib/tools.jar
fi

echo Using CLASSPATH: $CP


# ----- Execute The Requested Command -----------------------------------------

if [ "$1" = "debug" ] ; then

  shift
  pushd $CATALINA_HOME
  if [ "$1" = "-security" ] ; then
    shift
    jdb \
       $CATALINA_OPTS \
       -sourcepath ../../jakarta-tomcat-4.0/catalina/src/share \
       -classpath $CP -Dcatalina.home=$CATALINA_HOME \
       org.apache.catalina.startup.Bootstrap "$@" start
  else
    jdb \
       $CATALINA_OPTS \
       -sourcepath ../../jakarta-tomcat-4.0/catalina/src/share \
       -classpath $CP -Dcatalina.home=$CATALINA_HOME \
       org.apache.catalina.startup.Bootstrap "$@" start
  fi
  popd

elif [ "$1" = "embedded" ] ; then

  shift
  java $CATALINA_OPTS -classpath $CP \
   -Dcatalina.home=$CATALINA_HOME \
   org.apache.catalina.startup.Embedded "$@"

elif [ "$1" = "env" ] ; then

  export BP CATALINA_HOME CP
  exit 0

elif [ "$1" = "run" ] ; then

  shift
  if [ "$1" = "-security" ] ; then
    echo Using Security Manager
    shift
    java $CATALINA_OPTS -classpath $CP \
     -Djava.security.manager \
     -Djava.security.policy==$CATALINA_HOME/conf/catalina.policy \
     -Dcatalina.home=$CATALINA_HOME \
     org.apache.catalina.startup.Bootstrap "$@" start
  else
    java $CATALINA_OPTS -classpath $CP \
     -Dcatalina.home=$CATALINA_HOME \
     org.apache.catalina.startup.Bootstrap "$@" start
  fi

elif [ "$1" = "start" ] ; then

  shift
  touch $CATALINA_HOME/logs/catalina.out
  if [ "$1" = "-security" ] ; then
    echo Using Security Manager
    shift
    java $CATALINA_OPTS -classpath $CP \
     -Djava.security.manager \
     -Djava.security.policy==$CATALINA_HOME/conf/catalina.policy \
     -Dcatalina.home=$CATALINA_HOME \
     org.apache.catalina.startup.Bootstrap "$@" start \
     >> $CATALINA_HOME/logs/catalina.out 2>&1 &
  else
    java $CATALINA_OPTS -classpath $CP \
     -Dcatalina.home=$CATALINA_HOME \
     org.apache.catalina.startup.Bootstrap "$@" start \
     >> $CATALINA_HOME/logs/catalina.out 2>&1 &
  fi

elif [ "$1" = "stop" ] ; then

  shift
  java $CATALINA_OPTS -classpath $CP \
   -Dcatalina.home=$CATALINA_HOME \
   org.apache.catalina.startup.Bootstrap "$@" stop

else

  echo "Usage: catalina.sh ( env | run | start | stop)"
  echo "Commands:"
  echo "  debug             Start Catalina in a debugger"
  echo "  debug -security   Debug Catalina with a security manager"
  echo "  env               Set up environment variables that would be used"
  echo "  run               Start Catalina in the current window"
  echo "  run -security     Start in the current window with security manager"
  echo "  start             Start Catalina in a separate window"
  echo "  start -security   Start in a separate window with security manager"
  echo "  stop -            Stop Catalina"
  exit 1

fi
