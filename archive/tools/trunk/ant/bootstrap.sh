SRCDIR=src/main/org/apache/tools/ant
CLASSDIR=classes
CLASSPATH=../projectx-tr2.jar:../javac.jar:src:${CLASSDIR}

mkdir -p ${CLASSDIR}

export CLASSPATH
echo $CLASSPATH

javac  -d ${CLASSDIR} ${SRCDIR}/*.java
javac  -d ${CLASSDIR} ${SRCDIR}/taskdefs/*.java

cp src/main/org/apache/tools/ant/taskdefs/defaults.properties ${CLASSDIR}/org/apache/tools/ant/taskdefs
cp src/main/org/apache/tools/ant/parser.properties ${CLASSDIR}/org/apache/tools/ant

java org.apache.tools.ant.Main jar
java org.apache.tools.ant.Main clean 

rm -rf ${CLASSDIR}

