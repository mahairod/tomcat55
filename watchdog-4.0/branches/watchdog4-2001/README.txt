$Id$

                      Watchdog Validation Test Suite
                      ==============================

This subproject contains a set of tests to assist you in validating whether
your servlet container conforms to the requirements of the Servlet 2.3 and
JSP 1.2 specifications.  It includes the following contents:

  BUILDING.txt                Instructions for building from sources
  LICENSE                     Apache Software License for this release
  README.txt                  This document
  RUNNING.txt                 Instructions for running tests on your
                              servlet container
  build.xml                   Ant script used to manage test execution
  jcheck.ear                  Test suite web applications, packaged as an
                              Enterprise Application Archive for J2EE servers
  classes/                    Client and tool classes for the test suite
  conf/                       Configuration files for the test suite
  lib/                        JAR libraries required by the test suite, and
                              "golden" files for output comparisons
  webapps/                    Web application archive (WAR) files for the
                              JSP and servlet test applications

If you wish to build the Watchdog test suite from a source distribution,
please consult the documentation in "BUILDING.txt".

If you wish to run the Watchdog test suite against your servlet container,
please consult the documentation in "RUNNING.txt".
