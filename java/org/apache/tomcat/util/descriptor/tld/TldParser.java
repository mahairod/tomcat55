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
package org.apache.tomcat.util.descriptor.tld;

import java.io.IOException;
import java.io.InputStream;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.descriptor.DigesterFactory;
import org.apache.tomcat.util.descriptor.XmlErrorHandler;
import org.apache.tomcat.util.digester.Digester;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Parses a Tag Library Descriptor.
 */
public class TldParser {
    private static final Log LOG = LogFactory.getLog(TldParser.class);
    private final Digester digester;

    public TldParser(boolean namespaceAware, boolean validation) {
        TldRuleSet ruleSet = new TldRuleSet();
        digester = DigesterFactory.newDigester(validation, namespaceAware, ruleSet);
    }

    public TaglibXml parse(TldResourcePath path) throws IOException, SAXException {
        try (InputStream is = path.openStream()) {
            XmlErrorHandler handler = new XmlErrorHandler();
            digester.setErrorHandler(handler);

            TaglibXml taglibXml = new TaglibXml();
            digester.push(taglibXml);

            InputSource source = new InputSource(path.toExternalForm());
            source.setByteStream(is);
            digester.parse(source);
            if (!handler.getWarnings().isEmpty() || !handler.getErrors().isEmpty()) {
                handler.logFindings(LOG, source.getSystemId());
                if (!handler.getErrors().isEmpty()) {
                    // throw the first to indicate there was a error during processing
                    throw handler.getErrors().iterator().next();
                }
            }
            return taglibXml;
        } finally {
            digester.reset();
        }
    }
}
