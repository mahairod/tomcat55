/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.catalina.deploy;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Representation of a jsp-property-group element in web.xml.
 */
public class JspPropertyGroup {
    private Boolean deferredSyntax = null;
    public void setDeferredSyntax(String deferredSyntax) {
        this.deferredSyntax = Boolean.valueOf(deferredSyntax);
    }
    public Boolean getDeferredSyntax() { return deferredSyntax; }

    private Boolean elIgnored = null;
    public void setElIgnored(String elIgnored) {
        this.elIgnored = Boolean.valueOf(elIgnored);
    }
    public Boolean getElIgnored() { return elIgnored; }

    private Set<String> includeCodas = new LinkedHashSet<String>();
    public void addIncludeCoda(String includeCoda) {
        includeCodas.add(includeCoda);
    }
    public Set<String> getIncludeCodas() { return includeCodas; }

    private Set<String> includePreludes = new LinkedHashSet<String>();
    public void addIncludePrelude(String includePrelude) {
        includePreludes.add(includePrelude);
    }
    public Set<String> getIncludePreludes() { return includePreludes; }

    private Boolean isXml = null;
    public void setIsXml(String isXml) {
        this.isXml = Boolean.valueOf(isXml);
    }
    public Boolean getIsXml() { return isXml; }

    private String pageEncoding = null;
    public void setPageEncoding(String pageEncoding) {
        this.pageEncoding = pageEncoding;
    }
    public String getPageEncoding() { return this.pageEncoding; }
    
    private Boolean scriptingInvalid = null;
    public void setScriptingInvalid(String scriptingInvalid) {
        this.scriptingInvalid = Boolean.valueOf(scriptingInvalid);
    }
    public Boolean getScriptingInvalid() { return scriptingInvalid; }

    private Boolean trimWhitespace = null;
    public void setTrimWhitespace(String trimWhitespace) {
        this.trimWhitespace = Boolean.valueOf(trimWhitespace);
    }
    public Boolean getTrimWhitespace() { return trimWhitespace; }

    private String urlPattern = null;
    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }
    public String getUrlPattern() { return this.urlPattern; }
    
}
