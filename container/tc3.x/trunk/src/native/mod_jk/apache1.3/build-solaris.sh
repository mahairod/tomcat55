#!/bin/sh

# build-solaris.sh for mod_jk.so
# Usage: # sh build-solaris.sh

# Path to Apache or PREFIX used to build Apache
if [ -z "$APACHE_HOME" ] ; then
   echo APACHE_HOME=/usr/local/apache
   APACHE_HOME=/usr/local/apache
fi

if [ -f $APACHE_HOME/bin/apxs ] ; then
   APXS=$APACHE_HOME/bin/apxs
else
   echo Error: Unable to locate apxs.  Verify that APACHE_HOME is correct in this script.
   exit 1
fi

# Check JAVA_HOME
if [ -z "$JAVA_HOME" ] ; then
   echo Please set JAVA_HOME
   exit 1
fi

JAVA_INCLUDE="-I${JAVA_HOME}/include -I${JAVA_HOME}/include/solaris"

INCLUDE="-I../common $JAVA_INCLUDE"
SRC="../common/*.c mod_jk.c"

# Run APXS to compile module
echo Compiling mod_jk
$APXS -S CFLAGS="-DSOLARIS -DUSE_EXPAT -I../lib/expat-lite" -o mod_jk.so $INCLUDE -lposix4 -c $SRC

# Check to see if the last command completed
if [ $? -ne 0 ] ; then
  echo Error with apxs
  exit 1
fi

echo mod_jk build complete.

#
# Clean up
#
rm jk_*.o
rm mod_jk.o

echo configuring apache...

# Use apxs to add the correct lines to httpd.conf
# Since our auto-config does this in the include
# file (mod_jk-conf-auto), we'll add them as
# commented statements for change later if
# we decide not to use the auto-conf.
#
#$APXS -i -a mod_jk.so
$APXS -i -A mod_jk.so

# Check to see if the last command completed
if [ $? -ne 0 ] ; then
  echo Error using apxs to add configuration to httpd.conf
  exit 1
fi

# Steps to complete install
cat<<END

Build and configuration of mod_jk is complete.

To finish the installation, edit your apache/conf/httpd.conf file and
add the following line to the end of the file:
(Note: Change TOMCAT_HOME to the value of $TOMCAT_HOME)

Include TOMCAT_HOME/conf/jk/mod_jk.conf-auto

Example (/usr/local/apache/conf/httpd.conf):

Include /usr/local/jakarta-tomcat-3.3/conf/jk/mod_jk.conf-auto

Next copy $TOMCAT_HOME/conf/jk/workers.properties.unix to
$TOMCAT_HOME/conf/jk/workers.properties

Finally, add the apache auto-config setting to Tomcat.
See the release notes for Tomcat 3.3 for information on enabling
the auto-configure script in section 2, Tomcat Configuration:

"To turn these on, add the following modules after the
  <AutoWebApp ... /> module in the server.xml file:

  Apache configs:  <ApacheConfig />"

Example ($TOMCAT_HOME/conf/serverl.xml):

        <AutoWebApp dir="webapps" host="DEFAULT" />

        <ApacheConfig />

For more information, see the mod_jk-howto located in the docs dir
of TOMCAT. (doc/mod_jk-howto.html)
END
