#! /bin/sh
#!/bin/sh
#

# Shell script to run watchdog test suite
 
# This script is known to work with the standard Korn Shell under
# Solaris and the MKS Korn shell under Windows.


if [ "$1" = "" ]; then
    echo usage: "$0 {all|jsp|servlet} [serverhost] [serverport]"
    exit 0
fi


host=localhost
port=8080
default=$1


baseDir=..

miscJars=${baseDir}/lib/moo.jar:${baseDir}/lib/testdriver.jar:${baseDir}/lib/client.jar
appJars=${miscJars}:${baseDir}/lib/xml.jar:${baseDir}/lib/ant.jar
sysJars=${JAVA_HOME}/lib/tools.jar

appClassPath=${appJars}
cp=$CLASSPATH

CLASSPATH=${appClassPath}:${sysJars}

export CLASSPATH

if [ "$cp" != "" ]; then
    CLASSPATH=${CLASSPATH}:${cp}
    export CLASSPATH
fi

echo Using classpath: ${CLASSPATH}
echo



if [ "${default}" = jsp -o "${default}" = all ];then
    java org.apache.tools.ant.Main -Dport=${PORT} -Dhost=${HOST} -Dwatchdog.home=\
         $WATCHDOG_HOME -f ${WATCHDOG_HOME}/conf/jsp.xml jsp-test
fi

if [ "${default}" = servlet -o "${default}" = all ];then
    java org.apache.tools.ant.Main -Dtest.port=${PORT} -Dtest.hostname=${HOST}=\
         -Dwatchdog.home=${WATCHDOG_HOME} -f $WATCHDOG_HOME/conf/servlet.xml servlet-test
fi

if [ "$cp" != ""]; then
    CLASSPATH=${cp}
    export CLASSPATH
else
    unset CLASSPATH
fi
