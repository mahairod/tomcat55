			Apache Tomcat 4.0
			=================


Introduction
============

This CVS module contains the code for Tomcat 4.0, and will also be
used for further development of 4.x versions.  It is divided into several
major components, each of which you can build and use separately, that are
combined to create Tomcat.  The components are:

* Catalina - Servlet container conforming to the Servlet API Specification,
  version 2.3 (currently in Initial Public Draft).

* Jasper - JSP compiler and runtime environment conforming to the
  JavaServer Pages (JSP) Specification, version 1.2 (currently in
  Initial Public Draft).

* Webapps - Example and test web applications, and associated documentation,
  that are packaged with Tomcat.


Installing Tomcat 4.0 Source Code
=================================

In order to successfully build Tomcat 4.0, you will need to do the following.
In the instructions below, $JAKARTA_HOME is assumed to be the directory into
which you are installing all of the required Jakarta distributions.

* Download and install a version 1.2 or later Java Development Kit
  implementation for your operating system platform.  Set a "JAVA_HOME"
  environment variable to point at the directory where your JDK is installed,
  and add "$JAVA_HOME/bin" to your PATH.  Configure the CLASSPATH environment
  variable as well, if required.

* If you are running a JDK earlier than 1.3, download and install the current
  version of the Java Naming and Directory Interface (JNDI) package from
  <http://java.sun.com/products/jndi>, and configure your CLASSPATH to include
  the "jndi.jar" file (or copy this file to $JAVA_HOME/jre/lib/ext in a
  JDK 1.2 environment).  None of the JNDI providers are required, unless you
  need to use them in your own applications.

* Download and install the Xerces Java Parser (release 1.2.0 or above)
  from <http://xml.apache.org/xerces-j/index.html>. Set an
  environment variable "XERCES_HOME" pointing at the directory to which you
  installed this distribution.  In addition, add the "xerces.jar" 
  file to your classpath. 
  [Please note that Xerces is included with Xalan, so if
   you have Xalan already you don't need a separate download.]

* Download and install the Java Secure Sockets Extension (JSSE) implementation
  (current version number is 1.0.1) from <http://java.sun.com/products/jsse>.
  Set an environment variable "JSSE_HOME" pointing at the directory to which
  you installed this distribution.

* Download and install the Ant distribution (subproject "jakarta-ant") into
  a directory named "$JAKARTA_HOME/jakarta-ant".  If you have downloaded the
  source distribution, you will need to build the executable version by
  executing the following commands:

	cd $JAKARTA_HOME/jakarta-ant
	./bootstrap.sh		<-- Unix
	bootstrap		<-- Windows

  This should result in the creation of a file "ant.jar" in the "lib"
  subdirectory, which will be used when building Tomcat.

* Download and install the Regular Expressions distribution (module
  "jakarta-regexp"). Assuming an install directory <install-dir>:

  Binary distribution
    Set the REGEXP_HOME env variable to point to <install-dir>

  Source distribution
    Build the library by executing the following commands:
        cd <install-dir>/build
        ./build-regexp.sh       <-- Unix
        build-regexp            <-- Windows
    Set the REGEXP_HOME env variable to point to <install-dir>/bin

* Download and install the Servletapi distribution that includes the
  Servlet 2.3 and JSP 1.2 API classes.  A link is available on the Jakarta
  web site at <http://jakarta.apache.org/downloads/binindex.html>.  When
  unpacked, this distribution will create a directory named
  "jakarta-servletapi-4.0".  Set an environment variable named
  SERVLETAPI_HOME that points to where you have placed this directory.

  If you downloaded the source distribution, you will need to build it:

        cd $JAKARTA_HOME/jakarta-servletapi-4.0
	./build.sh dist		<-- Unix
	build dist		<-- Windows

  to create the required servlet.jar file.

* Download the Tomcat 4.0 source repository (module
  "jakarta-tomcat-4.0") via anonymous CVS into a directory named
  "$JAKARTA_HOME/jakarta-tomcat-4.0", or download a nightly distribution.
  A link is provided on the Jakarta web site at
  <http://jakarta.apache.org/downloads/binindex.html>.


BUILDING TOMCAT 4.0
===================

* After downloading and installing the required software components,
  described in the previous section, you can build the entire Tomcat 4.0
  suite as follows:

        cd $JAKARTA_HOME/jakarta-tomcat-4.0
	./build.sh		<-- Unix
	build			<-- Windows

  This will create a complete build of Catalina, Jasper, and the example
  web applications in "$JAKARTA_HOME/build/tomcat-4.0".

* To create a "distribution" build of Tomcat 4.0, with file contents that
  are equivalent to the nightly distributions, do this:

        cd $JAKARTA_HOME/jakarta-tomcat-4.0
	./build.sh dist		<-- Unix
	build dist		<-- Windows

* If you are interested in building only the individual components, there
  are separate build scripts for each in the corresponding subdirectories.
  Use the "deploy" task if you want to compile this component into the
  consolidated "build/tomcat-4.0" directory (this is what the top level
  build scripts do for you), or build the individual pieces like this:

        cd $JAKARTA_HOME/catalina
	./build.sh		<-- Unix
	build			<-- Windows

	cd ../jasper
	./build.sh		<-- Unix
	build			<-- Windows

	cd ../webapps
	./build.sh		<-- Unix
	build			<-- Windows

  These steps create compiled versions of the components in the following
  subdirectories:

        $JAKARTA_HOME/build/catalina
	$JAKARTA_HOME/build/jasper
	$JAKARTA_HOME/build/webapps

* If you modify source files in any of the component, be sure that you
  run the "deploy" target for that component (once you are satisfied that
  your code compiles with no errors).  You can do "deploys" from any of the
  components in any desired sequence, but all of them must have succeeeded
  in order to have a runnable build.

* Each of the component build scripts also has a "dist" target, which
  packages that component under directories "dist/catalina",
  "dist/jasper", and "dist/webapps", respectively.  These targets are
  useful if you wish to package the components of Tomcat in combinations
  with other application bundles.


Running the Build
=================

* You can start the unpacked version of Tomcat 4.0 as follows:

	cd $JAKARTA_HOME/build/tomcat-4.0
	./bin/catalina.sh start	<-- Unix
	bin\catalina start	<-- Windows

* To access the default content and examples, access the
  following URL with your browser:

	http://localhost:8080

* To shut down the unpacked version of Tomcat 4.0:

	cd $JAKARTA_HOME/build/tomcat-4.0
	./bin/catalina.sh stop	<-- Unix
	bin\catalina stop	<-- Windows

* You can also set an environment variable CATALINA_HOME so that you
  can start and stop Tomcat 4.0 from any directory.  For example:

        export CATALINA_HOME=$JAKARTA_HOME/build/tomcat-4.0
        $CATALINA_HOME/bin/catalina.sh start


Reporting Bugs
==============

If you encounter any bugs in Catalina, or wish to contribute a patch, or
wish to suggest any functionality improvements, please report them to our
bug tracking system, at:

	http://jakarta.apache.org/bugs

against product categories "Catalina", "Jasper", and "Webapps", depending
on where the problem you are reporting exists.


Before Committing Changes
=========================

Before committing any changes to the Tomcat 4.0 CVS repository, you MUST do a
"build clean" followed by a "build deploy.main dist" with the top level build
scripts, to ensure that the entire build process runs cleanly.  Also, ensure
that all current tests (both internal to Tomcat and those in Watchdog) run
correctly with your updated code.
