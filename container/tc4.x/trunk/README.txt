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

* Tester - Unit test web application, with tests that are primarily focused
  on Tomcat features rather than spec compliance.

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

* Download and install the Java Naming and Directory Interface (JNDI) package,
  version 1.2.1 or later, from <http://java.sun.com/products/jndi>.  Set an
  environment variable "JNDI_HOME" pointing at the directory to which you
  installed the distribution.  None of the standard JNDI providers are required
  unless you need them in your own applications.

* If you want to build in support for JNDI JDBC DataSources you need to
  download the following packages and put their jar files in your classpath.

  Tyrex http://tyrex.exolab.org/

  Java Transaction API (JTA) http://java.sun.com/products/jta

  Java JDBC 2.0 Optional Extensions http://java.sun.com/products/jdbc/

* If you are running a JDK earlier than 1.3, you will also need to place the
  "jndi.jar" file on your CLASSPATH in order to build Tomcat.

* Download and install the Java Secure Sockets Extension (JSSE) implementation
  (current version number is 1.0.2) from <http://java.sun.com/products/jsse>.
  Set an environment variable "JSSE_HOME" pointing at the directory to which
  you installed this distribution.

* Download and install the Java Management Extensions (JMX) "JMX
  Instrumentation and Agent Reference Implementation" (current version
  number is 1.0) from
  <http://java.sun.com/products/JavaManagement/download.html>.
  Set an environment variable "JMX_HOME" pointing at the directory to which
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
  If you download a binary distribution, you must get Ant version 1.2 or later.

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
  "jakarta-servletapi-4".  Set an environment variable named
  SERVLETAPI_HOME that points to where you have placed this directory.

  If you wish to create this package from the CVS archives, you will need
  to follow these steps:

        cd $JAKARTA_HOME
        cvs checkout jakarta-servletapi-4
        cd jakarta-servletapi-4
        ./build.sh dist         <-- Unix
        build dist              <-- Windows

  to create the required servlet.jar file and Javadoc documentation.

  If you do this, you should set your SERVLETAPI_HOME environment variable
  to point at "$JAKARTA_HOME/jakarta-servletapi-4/dist".

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
  web applications in "$JAKARTA_HOME/jakarta-tomcat-4.0/build".

* To create a "distribution" build of Tomcat 4.0, with file contents that
  are equivalent to the nightly distributions, do this:

        cd $JAKARTA_HOME/jakarta-tomcat-4.0
	./build.sh dist		<-- Unix
	build dist		<-- Windows

  This will create a complete distribution build of Catalina, Jasper, and
  the example web applications in
  "$JAKARTA_HOME/jakarta-tomcat-4.0/dist".

* If you are interested in building only the individual components, there
  are separate build scripts for each in the corresponding subdirectories.
  Use the "deploy" task if you want to compile this component into the
  consolidated Tomcat 4.0 "build" directory (this is what the top level
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

        $JAKARTA_HOME/jakarta-tomcat-4.0/catalina/build
	$JAKARTA_HOME/jakarta-tomcat-4.0/jasper/build
	$JAKARTA_HOME/jakarta-tomcat-4.0/webapps/build

* If you modify source files in any of the component, be sure that you
  run the "deploy" target for that component (once you are satisfied that
  your code compiles with no errors).  You can do "deploys" from any of the
  components in any desired sequence, but all of them must have succeeeded
  in order to have a runnable build.

* Each of the component build scripts also has a "dist" target, which
  packages that component under directories "catalina/dist",
  "jasper/dist", and "webapps/dist", respectively.  These targets are
  useful if you wish to package the components of Tomcat in combinations
  with other application bundles.


Running the Build
=================

* You can start the unpacked version of Tomcat 4.0 as follows:

	cd $JAKARTA_HOME/jakarta-tomcat-4.0/build
	./bin/catalina.sh start	<-- Unix
	bin\catalina start	<-- Windows

* To access the default content and examples, access the
  following URL with your browser:

	http://localhost:8080

* To shut down the unpacked version of Tomcat 4.0:

	cd $JAKARTA_HOME/jakarta-tomcat-4.0/build
	./bin/catalina.sh stop	<-- Unix
	bin\catalina stop	<-- Windows

* You can also set an environment variable CATALINA_HOME so that you
  can start and stop Tomcat 4.0 from any directory.  For example:

        export CATALINA_HOME=$JAKARTA_HOME/jakarta-tomcat-4.0/build
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

XML parsers in Tomcat 4.0
=========================

  In the tomcat-4.0 build procedure, an XML parser is required for three
  different components:
  
     1. ant
        See ant specific requirements for an XML parser
     2. catalina
        At least JAXP1.0 compliant XML parser
     3. jasper
        At least JAXP1.1 compliant XML parser
  
  All of these requirements can be handled globally by setting
  environment variable JAXP_HOME (as described above).
  
  The default jar files used for XML parsing are then:
     JAXP_HOME/jaxp.jar and JAXP_HOME/crimson.jar
  
  * To change the 'XML parser' jar file used:
  
    JAXP_PARSER_JAR [default: crimson.jar]
    (e.g. xerces.jar)
  
  * To set the XML parser of each component individually:
  
    ANT_XML_CLASSPATH [default: JAXP_HOME/JAXP_PARSER_JAR;JAXP_HOME/jaxp.jar]
  
    CATALINA_JAXP_HOME [default: JAXP_HOME]
    CATALINA_JAXP_PARSER_JAR [default: JAXP_PARSER_JAR]
  
    JASPER_JAXP_HOME [default: JAXP_HOME]
    JASPER_JAXP_PARSER_JAR [default: JAXP_PARSER_JAR]
