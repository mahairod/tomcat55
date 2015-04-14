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
package org.apache.tomcat.util.net;

import java.util.HashSet;
import java.util.Set;

public class SSLHostConfig {

    public static final String DEFAULT_SSL_HOST_NAME = "*DEFAULT*";

    private String hostName;

    private Set<String> sslProtocols = new HashSet<>();


    public void setHostName(String hostName) {
        this.hostName = hostName;
    }


    public String getHostName() {
        return hostName;
    }


    public void setProtocols(String protocols) {
        // OpenSSL and JSSE use the same names.
        if (protocols.trim().equalsIgnoreCase("all")) {
            protocols = "TLSv1+TLSv1.1+TLSv1.2";
        }

        String[] values = protocols.split(",|\\+");

        for (String value: values) {
            String trimmed = value.trim();
            if (trimmed.length() > 0) {
                sslProtocols.add(trimmed);
            }
        }
    }


    public Set<String> getSslProtocols() {
        return sslProtocols;
    }
}
