#!/bin/sh
#

# Shell script to run watchdog test suite
 
if [ "$1" = "" ] ; then
    echo usage: "$0 {all|jsp|servlet} [serverhost] [serverport]"
    exit 0
fi

HOST=localhost
PORT=8080
default=$1

if [ "$2" != "" ] ; then
    PORT=$2
fi
if [ "$3" != "" ] ; then
    HOST=$3
fi

#WATCHDOG_HOME=`pwd`/..
WATCHDOG_HOME=..

cp=$CLASSPATH

CLASSPATH=${WATCHDOG_HOME}/lib/ant.jar:$CLASSPATH
CLASSPATH=${WATCHDOG_HOME}/lib/moo.jar:$CLASSPATH
CLASSPATH=${WATCHDOG_HOME}/lib/testdriver.jar:$CLASSPATH
CLASSPATH=${WATCHDOG_HOME}/lib/client.jar:$CLASSPATH
CLASSPATH=${WATCHDOG_HOME}/lib/xml.jar:$CLASSPATH

CLASSPATH=$CLASSPATH:${JAVA_HOME}/lib/tools.jar
CLASSPATH=$CLASSPATH:${JAVA_HOME}/lib/classes.zip

if [ "$cp" != "" ] ; then
    CLASSPATH=${CLASSPATH}:${cp}
fi

export CLASSPATH

echo Using classpath: ${CLASSPATH}
echo

if [ "${default}" = jsp -o "${default}" = all ] ; then
    java org.apache.tools.ant.Main -Dport=${PORT} -Dhost=${HOST} -Dwatchdog.home=\
        ${WATCHDOG_HOME} -f ${WATCHDOG_HOME}/conf/jsp.xml jsp-test
fi

if [ "${default}" = servlet -o "${default}" = all ] ; then
    java org.apache.tools.ant.Main -Dtest.port=${PORT} -Dtest.hostname=${HOST}=\
        -Dwatchdog.home=${WATCHDOG_HOME} -f ${WATCHDOG_HOME}/conf/servlet.xml servlet-test
fi

if [ "$cp" != "" ] ; then
    CLASSPATH=${cp}
    export CLASSPATH
else
    unset CLASSPATH
fi

exit 0
