#! /bin/sh
#
# $Id$

# Shell script to startup the server

BASEDIR=`dirname $0`

$BASEDIR/tomcat start $@
