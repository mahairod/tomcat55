#!/bin/sh
#

# Shell script to run watchdog test suite
 
if [ "$1" = "" ] ; then
    echo usage: "$0 {all|jsp|jsp-xml|jsp-all|servlet} [serverhost] [serverport]"
    exit 0
fi

HOST=localhost
PORT=8080
default=$1

if [ "$2" != "" ] ; then
    HOST=$2
fi
if [ "$3" != "" ] ; then
    PORT=$3
fi

if [ -f $HOME/.watchdogrc ] ; then 
  . $HOME/.watchdogrc
fi

if [ "$WATCHDOG_HOME" = "" ] ; then
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
  
  WATCHDOG_HOME_1=`dirname "$PRG"`/..
  echo "Guessing WATCHDOG_HOME from watchdog.sh to ${WATCHDOG_HOME_1}" 
    if [ -d ${WATCHDOG_HOME_1}/conf ] ; then 
	WATCHDOG_HOME=${WATCHDOG_HOME_1}
	echo "Setting WATCHDOG_HOME to $WATCHDOG_HOME"
    fi
fi

if [ "$WATCHDOG_HOME" = "" ] ; then
    echo WATCHDOG_HOME not set, you need to set it or install in a standard location
    exit 1
fi

if [ "$JAKARTA_HOME" != "" ] ; then
   TOMCAT_HOME=${JAKARTA_HOME}/jakarta-tomcat-4.0
fi


cp=$CLASSPATH

CLASSPATH=${WATCHDOG_HOME}/lib/ant.jar:$CLASSPATH
CLASSPATH=${WATCHDOG_HOME}/lib/moo.jar:$CLASSPATH
CLASSPATH=${WATCHDOG_HOME}/lib/testdriver.jar:$CLASSPATH
CLASSPATH=${WATCHDOG_HOME}/lib/client.jar:$CLASSPATH

CLASSPATH=$CLASSPATH:${JAVA_HOME}/lib/tools.jar
CLASSPATH=$CLASSPATH:${JAVA_HOME}/lib/classes.zip

if [ "$cp" != "" ] ; then
    CLASSPATH=${CLASSPATH}:${cp}
fi

export CLASSPATH

echo Using classpath: ${CLASSPATH}
echo

if [ "${default}" = jsp -o "${default}" = all -o "${default}" = jsp-all ] ; then

	if [ "$TOMCAT_HOME" = "" ] ; then
    java org.apache.tools.ant.Main -Dport=${PORT} -Dhost=${HOST} \
        -Dwatchdog.home=${WATCHDOG_HOME} -f ${WATCHDOG_HOME}/conf/jsp-gtest.xml jsp-test
        fi

    echo "TOMCAT_HOME is " $TOMCAT_HOME

    java org.apache.tools.ant.Main -Dport=${PORT} -Dhost=${HOST} \
        -Dtomcat.home=${TOMCAT_HOME} -f ${WATCHDOG_HOME}/conf/jsp-xml.xml jsp-test
    java org.apache.tools.ant.Main -Dport=${PORT} -Dhost=${HOST} \
        -Dwatchdog.home=${WATCHDOG_HOME} -f ${WATCHDOG_HOME}/conf/jsp-gtest.xml jsp-test
fi

if [ "${default}" = servlet -o "${default}" = all ] ; then
    java org.apache.tools.ant.Main -Dport=${PORT} -Dhost=${HOST} \
        -Dwatchdog.home=${WATCHDOG_HOME} -f ${WATCHDOG_HOME}/conf/servlet-moo.xml servlet-test
fi

if [ "${default}" = gtestservlet -o "${default}" = all ] ; then
    java org.apache.tools.ant.Main -Dport=${PORT} -Dhost=${HOST} \
        -Dwatchdog.home=${WATCHDOG_HOME} -f ${WATCHDOG_HOME}/conf/servlet-gtest.xml gtestservlet-test
fi

if [ "${default}" = jsp-xml -o "${default}" = all -o "${default}" = jsp-all ] ; then
	if [ "$TOMCAT_HOME" = "" ] ; then
        	echo TOMCAT_HOME not set, you need to set it or install in a standard location
    exit 1
	fi
    echo "TOMCAT_HOME is " $TOMCAT_HOME
    echo "Running JSP tests with the XML view of jsp pages......"

    java org.apache.tools.ant.Main -Dport=${PORT} -Dhost=${HOST} \
        -Dtomcat.home=${TOMCAT_HOME} -f ${WATCHDOG_HOME}/conf/jsp-xml.xml xml-test
    java org.apache.tools.ant.Main -Dport=${PORT} -Dhost=${HOST} \
        -Dwatchdog.home=${WATCHDOG_HOME} -f ${WATCHDOG_HOME}/conf/jsp-gtest.xml jsp-test
fi


if [ "$cp" != "" ] ; then
    CLASSPATH=${cp}
    export CLASSPATH
else
    unset CLASSPATH
fi

exit 0
