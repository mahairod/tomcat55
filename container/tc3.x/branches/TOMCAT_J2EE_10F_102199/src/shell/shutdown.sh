#! /bin/sh
#
# $Id$

# Shell script to shutdown the server

# There are other, simpler commands to shutdown the runner. The two
# commented commands good replacements. The first works well with
# Java Platform 1.1 based runtimes. The second works well with
# Java2 Platform based runtimes.

#jre -cp runner.jar:servlet.jar:classes org.apache.tomcat.shell.Shutdown $*
#java -cp runner.jar:servlet.jar:classes org.apache.tomcat.shell.Shutdown $*

baseDir=`dirname $0`

jsdkJars=${baseDir}/webserver.jar:${baseDir}/lib/servlet.jar
jspJars=${baseDir}/lib/jasper.jar
miscJars=${baseDir}/lib/xml.jar
appJars=${jsdkJars}:${jspJars}:${miscJars}
sysJars=${JAVA_HOME}/lib/tools.jar

appClassPath=${appJars}
cp=$CLASSPATH

# Backdoor classpath setting for development purposes when all classes
# are compiled into a /classes dir and are not yet jarred.

if [ -d ${baseDir}/classes ]; then
    appClassPath=${baseDir}/classes:${appClassPath}
fi

CLASSPATH=${appClassPath}:${sysJars}
export CLASSPATH

if [ "$cp" != "" ]; then
    CLASSPATH=${CLASSPATH}:${cp}
    export CLASSPATH
fi

echo Using classpath: ${CLASSPATH}

java org.apache.tomcat.shell.Shutdown $*

if [ "$cp" != "" ]; then
    CLASSPATH=${cp}
    export CLASSPATH
else
    unset CLASSPATH
fi
