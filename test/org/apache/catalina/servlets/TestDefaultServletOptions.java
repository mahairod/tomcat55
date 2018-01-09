/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.catalina.servlets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.Servlet;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestDefaultServletOptions extends ServletOptionsBaseTest {

    @Parameters
    public static Collection<Object[]> inputs() {
        Boolean[] booleans = new Boolean[] { Boolean.FALSE, Boolean.TRUE };
        String[] urls = new String[] { COLLECTION_NAME, FILE_NAME, UNKNOWN_NAME };
        String[] methods = new String[] { "GET", "POST", "HEAD", "TRACE", "PUT", "DELETE" };

        List<Object[]> result = new ArrayList<>();

        for (Boolean listingsValue : booleans) {
            for (Boolean readOnlyValue : booleans) {
                for (Boolean traceValue : booleans) {
                    for (String url : urls) {
                        for (String method : methods) {
                            result.add(new Object[] {
                                    listingsValue, readOnlyValue, traceValue, url, method } );
                        }
                    }
                }
            }

        }
        return result;
    }


    @Override
    protected Servlet createServlet() {
        return new DefaultServlet();
    }
}
