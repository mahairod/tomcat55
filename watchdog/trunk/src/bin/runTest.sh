#! /bin/sh
#
# $Id$

# Shell script to run test harness
 
# This script is known to work with the standard Korn Shell under
# Solaris and the MKS Korn shell under Windows.

host=localhost
port=8080
stest=./conf/servlet-testlist.txt
jtest=./conf/jsp-testlist.txt
default=all

baseDir=`dirname $0`

miscJars=${baseDir}/../../jakarta-tools/projectx-tr2.jar:${baseDir}/../../jakarta-tools/moo.jar
appJars=${miscJars}
sysJars=${JAVA_HOME}/lib/tools.jar

appClassPath=${appJars}
cp=$CLASSPATH

# Backdoor classpath setting for development purposes when all classes
# are compiled into a /clients dir and are not yet jarred.
 
if [ -d ${baseDir}/clients ]; then
    appClassPath=${baseDir}/clients:${appClassPath}
fi

CLASSPATH=${appClassPath}:${sysJars}
export CLASSPATH
echo $CLASSPATH

if [ "$cp" != "" ]; then
    CLASSPATH=${appClassPath}:${cp}
    export CLASSPATH
fi

echo Using classpath: ${CLASSPATH}
echo

if [ "$1" != "" ]; then
  default=$1
fi

java org.apache.tomcat.shell.Startup -config ./conf/server-test.xml $* &
sleep 25

if [ "${default}" = jsp -o "${default}" = all ];then
java -Dtest.hostname=$host -Dtest.port=$port org.apache.tools.moo.Main \
    -testfile $jtest
fi

if [ "${default}" = servlet -o "${default}" = all ];then
java -Dtest.hostname=$host -Dtest.port=$port org.apache.tools.moo.Main \
    -testfile $stest
fi

java org.apache.tomcat.shell.Shutdown $*

if ["$cp" != ""]; then
    CLASSPATH=${cp}
    export CLASSPATH
else
    unset CLASSPATH
fi
