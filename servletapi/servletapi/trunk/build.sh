#! /bin/sh

# $Id$

BUILDFILE=build.xml

if [ "$ANT_HOME" = "" ] ; then
  ANT_HOME=../jakarta-ant
fi

if test -z "${JAVA_HOME}" ; then
    echo "ERROR: JAVA_HOME not found in your environment."
    echo "Please, set the JAVA_HOME variable in your environment to match the"
    echo "location of the Java Virtual Machine you want to use."
    exit
fi

if test -f ${JAVA_HOME}/lib/tools.jar ; then
    CLASSPATH=${CLASSPATH}:${JAVA_HOME}/lib/tools.jar
fi

# convert the existing path to unix
if [ "$OSTYPE" = "cygwin32" ] || [ "$OSTYPE" = "cygwin" ] ; then
   CLASSPATH=`cygpath --path --unix "$CLASSPATH"`
fi

CLASSPATH=${CLASSPATH}:$ANT_HOME/lib/ant.jar

# convert the unix path to windows
if [ "$OSTYPE" = "cygwin32" ] || [ "$OSTYPE" = "cygwin" ] ; then
   CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
fi

${JAVA_HOME}/bin/java -classpath ${CLASSPATH} org.apache.tools.ant.Main \
                      -buildfile ${BUILDFILE} "$@"