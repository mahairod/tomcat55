#! /bin/ksh
#

# Shell script to run servlet test client against a server

basedir=`dirname $0`
default_host=localhost
default_port=8080
stestlist=${basedir}/conf/servlet-testlist.txt
jtestlist=${basedir}/conf/jsp-testlist.txt
default_arg=all

if [[ -n $1 && -n $2 && ! -n $3 ]]; then
  echo Usage: runClient {jsp/servlet/all} [host] [port]
  exit
fi

if [[  $1 != all && $1 != jsp && $1 != servlet ]]; then
  echo Usage: runClient {jsp/servlet/all} [host] [port]
  exit
fi

if [[ -n $1 ]]; then
  default_arg=$1
fi

if [[ -n $2 ]]; then
  host=$2
else
  host=$default_host
fi

if [[ -n $3 ]]; then
  port=$3
else
  port=$default_port
fi

echo Running tests against ${host}:${port}

addtl_classpath=${basedir}/classes:${basedir}/client.jar:${basedir}/lib/moo.jar:${basedir}/../tomcat/classes
# XXX add tomcat.jar
# XXX test if classes exists

if [[ -n $CLASSPATH ]]; then
  export CLASSPATH=${addtl_classpath}:${CLASSPATH}
else
  export CLASSPATH=${addtl_classpath}
fi

if [[ ${default_arg} == jsp || ${default_arg} == all ]]; then
java -Dtest.hostName=${host} -Dtest.port=${port} org.apache.tools.moo.Main \
    -testfile ${jtestlist}
fi

if [[ ${default_arg} == servlet || ${default_arg} == all ]]; then
java -Dtest.hostName=${host} -Dtest.port=${port} org.apache.tools.moo.Main \
    -testfile ${stestlist}
fi
