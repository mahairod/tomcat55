#! /bin/ksh
#
# $Id$

# Shell script to run test harness
 
# This script is known to work with the standard Korn Shell under
# Solaris and the MKS Korn shell under Windows.

# Does this need to be ksh?? - akv

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

export CLASSPATH=${appClassPath}:${sysJars}

if [[ -n $cp ]]; then
    export CLASSPATH=${appClassPath}:${cp}
fi

echo Using classpath: ${CLASSPATH}
echo

if [[ -n $1 ]]; then
  default=$1
fi

java org.apache.tomcat.shell.Startup -config ./conf/server-test.xml $* &
sleep 25

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
