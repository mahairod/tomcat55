#!/bin/sh
# -----------------------------------------------------------------------------
# digest.bat - Digest password using the algorithm specificied
#
#   CATALINA_HOME (Optional) May point at your Catalina "build" directory.
#                 If not present, the current working directory is assumed.
#
#   JAVA_HOME     Must point at your Java Development Kit installation.
#
#   This script is assumed to run from the bin directory or have the
#   CATALINA_HOME env variable set.
#
# $Id$
# -----------------------------------------------------------------------------

BASEDIR=`dirname $0`
$BASEDIR/tool-wrapper.sh -server org.apache.catalina.realm.RealmBase "$@"
