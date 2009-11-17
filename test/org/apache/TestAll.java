/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.catalina.connector.TestKeepAliveCount;
import org.apache.catalina.connector.TestRequest;
import org.apache.catalina.ha.session.TestSerializablePrincipal;
import org.apache.catalina.startup.TestTomcat;
import org.apache.el.TestELEvaluation;
import org.apache.el.lang.TestELSupport;
import org.apache.tomcat.util.http.TestCookies;
import org.apache.tomcat.util.res.TestStringManager;

public class TestAll {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for org.apache");
        // o.a.catalina
        // connector
        suite.addTestSuite(TestRequest.class);
        suite.addTestSuite(TestKeepAliveCount.class);
        // ha.session
        suite.addTestSuite(TestSerializablePrincipal.class);
        // startup
        suite.addTestSuite(TestTomcat.class);
        // tribes
        // suite.addTest(TribesTestSuite.suite());
        
        // o.a.el
        suite.addTestSuite(TestELSupport.class);
        suite.addTestSuite(TestELEvaluation.class);
        
        // o.a.tomcat.util
        // http
        suite.addTestSuite(TestCookies.class);
        // res
        suite.addTestSuite(TestStringManager.class);
        
        return suite;
    }

}
