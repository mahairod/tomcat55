#! /bin/ksh
#
# $Id$

# Shell script to run test harness
 
# This script is known to work with the standard Korn Shell under
# Solaris and the MKS Korn shell under Windows.

host=localhost
port=8080
stest=servlet-testlist.txt
jtest=jsp-testlist.txt
default=all

baseDir=`dirname $0`

miscJars=${baseDir}/../../jakarta-tools/projectx-tr2.jar:${baseDir}/../../jakarta-tools/moo.jar:${baseDir}/client.jar
appJars=${miscJars}
sysJars=${JAVA_HOME}/lib/tools.jar

appClassPath=${appJars}
cp=$CLASSPATH

# Backdoor classpath setting for development purposes when all classes
# are compiled into a /classes dir and are not yet jarred.
 
if [ -d ${baseDir}/classes ]; then
    appClassPath=${baseDir}/classes:${appClassPath}
fi

export CLASSPATH=${appClassPath}:${sysJars}

if [[ -n $cp ]]; then
    export CLASSPATH=${appClassPath}:${cp}
fi

echo Using classpath: ${CLASSPATH}
echo

if [[ -n $1 ]]; then
  default=$1
fi

java org.apache.tomcat.shell.Startup $* &
sleep 50

if [[ ${default} == jsp || ${default} == all ]];then
java -Dtest.hostname=$host -Dtest.port=$port org.apache.tools.moo.Main \
    -testfile $jtest
fi

if [[ ${default} == servlet || ${default} == all ]];then
java -Dtest.hostname=$host -Dtest.port=$port org.apache.tools.moo.Main \
    -testfile $stest
fi

java org.apache.tomcat.shell.Shutdown $*

if [[ -n $cp ]]; then
    export CLASSPATH=${cp}
else
    unset CLASSPATH
fi
