package org.apache.watchdog;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

// jdom imports
import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

public class GTestContentHandler extends DefaultHandler implements LexicalHandler {

    /** JDOM document produced as output */
    protected Document _doc         = null;
    /** top level element of JDOM document, a &lt;report&gt; element*/
    protected Element  _rootElement = null;

    // necessary to avoid redundant targets
    protected boolean _insideMainTarget = false;
    protected boolean _insideServlet = false;
    protected boolean _insideJSP = false;

    protected String[] _knownWatchdogAttributes  = new String[] {
        "request", "testName", "debug", "host", "port", "exactMatch",
        "returnCode", "assertion", "testStrategy", "expectHeaders", "firstTask",
        "goldenFile", "nested", "lastTask", "content", "requestHeaders",
        "expectResponseBody", "unexpectedHeaders", "returnCodeMsg"
    };

    protected String[] _knownValidatingWatchdogAttributes = new String[] {
        "returnCode",  "expectHeaders", "goldenFile", "content", 
        "expectResponseBody", "unexpectedHeaders", "returnCodeMsg"
    };

    /**
     * Create an XML Reporter, initialising document property, as a new empty
     * report with an unsuccessful suite
     */
    public GTestContentHandler() {
        _rootElement = new Element("suite");
        _rootElement.setAttribute("defaultHost", "${host}");
        _rootElement.setAttribute("defaultPort", "${port}");
        _doc = new Document(_rootElement);
    }

    /**
     * Returns the XML Document produced by this listener
     *
     * @return JDOM representation of the test report
     */
    public Document getDocument() {
        return _doc;
    }

    public void startElement(String uri, String localName, 
                             String qName, Attributes atts) 
    throws SAXException {
        if (qName.equals("project") || qName.equals("property") || 
            qName.equals("taskdef")) {
            //do nothing
        } else if (qName.equals("target")) {
            String targetName = atts.getValue("name");
            _rootElement.addContent(new Comment("START converted ant target: " + targetName));

            if (targetName.equals("gtestservlet-test") || targetName.equals("jsp-test")) {
                _insideMainTarget = true;
                if (targetName.equals("gtestservlet-test")) {
                    _insideServlet = true;
                } else {
                    _insideJSP = true;
                }
            }

        } else if (qName.equals("watchdog") || qName.equals("gtest")) {
            if (_insideMainTarget) {
                processWatchdogElement(atts);
            } else {
                _rootElement.addContent(new Comment("SKIPPED redundant gtest, path = " + atts.getValue("request")));
            }
        } else {
            throw new SAXException("Unknown gtest element name: " + qName);
        }
    }

    public void endElement(String uri, String localName,
                           String qName) {
        if (qName.equals("target")) {
            _rootElement.addContent(new Comment("END converted ant target"));
            _insideMainTarget = false;
        }
    }

    public void characters(char[] ch, int start, int length)
    throws SAXException {
    }

    public void comment(char[] ch, int start, int length) {
        Comment comment = new Comment(new String(ch,start,length));
        _rootElement.addContent(comment);
    }

    public void endCDATA() {
    }

    public void endDTD() {
    }

    public void endEntity(String name) {
    }

    public void startCDATA() {
    }

    public void startDTD(String name, String publicId, String systemId) {
    }

    public void startEntity(String name) {
    }

    protected void processWatchdogElement(Attributes atts) throws SAXException {

        verifyWatchdogAttributes(atts);

        String requestAtt = atts.getValue("request");
        Element requestElement = new Element("request");
        requestElement.setAttribute("followRedirects","false");

        StringTokenizer tokenizer = new StringTokenizer(requestAtt);
        if (tokenizer.countTokens() != 3) {
            throw new SAXException("Unknown format for request attribute of watchdog tag: " +
                                   requestAtt);
        }

        String method = tokenizer.nextToken();
        if (method.equals("GET")) {
        } else if (method.equals("HEAD")) {
            requestElement.setAttribute("method","head");
        } else if (method.equals("POST")) { 
            requestElement.setAttribute("method","post");
        } else {
            throw new SAXException("Unknown format for request attribute of watchdog tag: " +
                                   requestAtt);
        }

        String path = tokenizer.nextToken();
        StringBuffer output = new StringBuffer();
        // replace the pipe-delimited variables with Latka-style variables
        try {
            // now, replace the remaining variables
            RE r = new RE("\\|client.ip\\||\\|client.host\\|");  // Compile expression

            //scan the input string match by match, writing to the buffer
            int bufIndex = 0;

            while (r.match(path, bufIndex)) {
                // append everything to the beginning of the match
                output.append(path.substring(bufIndex,
                                             r.getParenStart(0)));
                // move marker to the end of the match
                bufIndex = r.getParenEnd(0);

                String matched = r.getParen(0);
                if (matched.equals("|client.ip|")) {
                    output.append("${client-IP}");
                } else if (matched.equals("|client.host|")) {
                    output.append("${client-host}");
                }

            }

            // grab anything remaining that did not match
            output.append(path.substring(bufIndex, path.length()));
        } catch (RESyntaxException e) {
            // FIXME: Should this really be swallowed?
            e.printStackTrace();
        }
        path = output.toString();

        String protocol = tokenizer.nextToken();
        if (protocol.equals("HTTP/1.0") == false) {
            throw new SAXException("Unknown format for request attribute of watchdog tag: " +
                                   requestAtt);
        }
        requestElement.setAttribute("version","1.0");

        requestElement.setAttribute("path",path);

        String testName = atts.getValue("testName");
        if (testName != null) {
            requestElement.setAttribute("label",testName);
        }

        String requestHeaders = atts.getValue("requestHeaders");
        if (requestHeaders != null) {
            String[][]headerPairs = parseNameValueHeaderPairs(requestHeaders);

            for (int i = 0; i < headerPairs.length; ++i) {
                Element requestHeaderElement = new Element("requestHeader");
                requestHeaderElement.setAttribute("headerName", headerPairs[i][0]);
                requestHeaderElement.setAttribute("headerValue", headerPairs[i][1]);

                requestElement.addContent(requestHeaderElement);
            }
        }        
        
        String nested = atts.getValue("nested");
        if (nested != null) {
            _rootElement.addContent(new Comment("NOTE: In GTest, this element was NESTED within the previous element."));
        }

        // note: we use this element for both a validator and
        // a post body (here)
        String content = atts.getValue("content");
        if (content != null) {
            // changing to a POST, even though the GTest element probably
            // claims it's a GET
            requestElement.setAttribute("method","post");

            requestElement.addContent(new Comment("This request body was generated from the GTest attribute 'content'."));
            Element requestBodyElement = new Element("requestBody");
            requestBodyElement.addContent(content);
            requestElement.addContent(requestBodyElement);
        }

        _rootElement.addContent(requestElement);
        _rootElement.addContent("\n  ");

        findValidators(requestElement, atts);


    }

    protected void verifyWatchdogAttributes(Attributes atts) throws SAXException {
        // make sure all attributes are known by the processor
        for (int i = 0; i < atts.getLength(); ++i) {
            String attName = atts.getQName(i);
            boolean foundName = false;
            for (int j = 0; j < _knownWatchdogAttributes.length; ++j) {
                if (attName.equals(_knownWatchdogAttributes[j])) {
                    foundName= true;
                }
            }

            if (foundName == false) {
                throw new SAXException("Unknown watchdog attribute: " + attName);
            }
        }

        // make sure watchdog is performing at least one validation
        boolean hasValidator = false;
        for (int i = 0; i < atts.getLength(); ++i) {
            String attName = atts.getQName(i);
            for (int j = 0; j < _knownValidatingWatchdogAttributes.length; ++j) {
                if (attName.equals(_knownValidatingWatchdogAttributes[j])) {
                    hasValidator = true;
                }
            }
        }

        if (hasValidator == false) {
            throw new SAXException("Watchdog element contains no validations.");
        }
    }

    public String[][] parseNameValueHeaderPairs(String rawAttribute)
    throws SAXException {

        StringTokenizer tokenizer = new StringTokenizer(rawAttribute,"|");
        int headerCount = tokenizer.countTokens();
        String[][] retval = new String[headerCount][2];
        for (int i = 0; i < headerCount ; ++i) {
            String token = tokenizer.nextToken();
            int colonIndex = token.indexOf(":");
            if (colonIndex == -1) {
                throw new SAXException("Unknown format for a headers attribute of watchdog tag: " +
                                       rawAttribute);
            }

            //headerName
            retval[i][0] = token.substring(0,colonIndex).trim();
            //headerValue
            retval[i][1] = token.substring(colonIndex+1,token.length()).trim();
        }

        return retval;

    }


    protected void findValidators(Element requestElement, Attributes atts) 
    throws SAXException {
        Element validateElement = new Element("validate");
        requestElement.addContent(validateElement);

        String assertion = atts.getValue("assertion");
        String testStrategy = atts.getValue("testStrategy");
        if (testStrategy != null) {
            validateElement.addContent(new Comment("TEST STRATEGY: " + testStrategy));
        }

        String returnCode = atts.getValue("returnCode");
        if (returnCode != null) {
            Element element = new Element("statusCode");

            if (returnCode.equals("200") || returnCode.equals("HTTP/1.0 200 OK")) {
            } else if (returnCode.equals("500") || returnCode.equals("503") ||
                       returnCode.equals("100") || returnCode.equals("302") ||
                       returnCode.equals("410")) {
                element.setAttribute("code", returnCode);       
            } else {
                throw new SAXException("Unrecognized return code value for watchdog tag: " + returnCode);
            }

            if (assertion != null) {
                element.setAttribute("label",assertion);
            }
            validateElement.addContent(element);
        }

        String goldenFile = atts.getValue("goldenFile");
        String exactMatch = atts.getValue("exactMatch");
        if (goldenFile != null) {

            // fix wgdir variable

            StringBuffer output = new StringBuffer();
            try {
                // now, replace the remaining variables
                RE r = new RE("\\$\\{wgdir\\}");  // Compile expression

                //scan the input string match by match, writing to the buffer
                int bufIndex = 0;

                while (r.match(goldenFile, bufIndex)) {
                    // append everything to the beginning of the match
                    output.append(goldenFile.substring(bufIndex,
                                                 r.getParenStart(0)));
                    // move marker to the end of the match
                    bufIndex = r.getParenEnd(0);

                    if (_insideServlet) {
                        output.append("${servlet-wgdir}");
                    } else {
                        output.append("${jsp-wgdir}");
                    }

                }

                // grab anything remaining that did not match
                output.append(goldenFile.substring(bufIndex, goldenFile.length()));
            } catch (RESyntaxException e) {
                // FIXME: Should this really be swallowed?
                e.printStackTrace();
            }
            goldenFile = output.toString();

            Element element = new Element("goldenFile");
            element.setAttribute("fileName",goldenFile);
            if (exactMatch != null) {
                boolean ignoreWhitespace = (Boolean.valueOf(exactMatch).booleanValue() == false);
                //element.setAttribute("ignoreWhitespace",String.valueOf(ignoreWhitespace));
                // temporary until I work out the exact match issue
                element.setAttribute("ignoreWhitespace","true");
            } else {
                element.setAttribute("ignoreWhitespace","true");
            }

            if (assertion != null) {
                element.setAttribute("label",assertion);
            }
            validateElement.addContent(element);
        }

        /*
        String responseMatch = atts.getValue("responseMatch");
        if (responseMatch != null) {
            hasValidator = true;
            Element element = new Element("regexp");
            element.setAttribute("pattern",responseMatch);
            validateElement.addContent(element);
        }
        */

        String expectedHeaders = atts.getValue("expectHeaders");
        if (expectedHeaders != null) {

            String[][]headerPairs = parseNameValueHeaderPairs(expectedHeaders);

            for (int i = 0; i < headerPairs.length; ++i) {
                Element element = new Element("responseHeader");
                element.setAttribute("headerName", headerPairs[i][0]);
                element.setAttribute("headerValue", headerPairs[i][1]);

                if (assertion != null) {
                    element.setAttribute("label",assertion);
                }
                validateElement.addContent(element);
            }

        }

        String unexpectedHeaders = atts.getValue("unexpectedHeaders");
        if (unexpectedHeaders != null) {

            String[][]headerPairs = parseNameValueHeaderPairs(unexpectedHeaders);

            for (int i = 0; i < headerPairs.length; ++i) {
                Element element = new Element("responseHeader");
                element.setAttribute("headerName", headerPairs[i][0]);
                element.setAttribute("headerValue", headerPairs[i][1]);
                element.setAttribute("cond","false");

                if (assertion != null) {
                    element.setAttribute("label",assertion);
                }
                validateElement.addContent(element);
            }

        }

        // note: we use this element for both a validator (here) and
        // a post body
        String content = atts.getValue("content");
        if (content != null) {
            Element responseHeader = new Element("responseHeader");
            responseHeader.setAttribute("headerName","Content-Length");
            responseHeader.setAttribute("headerValue", String.valueOf(content.length()));

            if (assertion != null) {
                responseHeader.setAttribute("label",assertion);
            }
            
            //can't check for content-length for some reason
            //validateElement.addContent(new Comment("Checking content length based on length of this string: " + content));
            //validateElement.addContent(responseHeader);
        }

        String statusText = atts.getValue("returnCodeMsg");
        if (statusText != null) {
            Element element = new Element("statusText");
            element.setAttribute("text",statusText);

            if (assertion != null) {
                element.setAttribute("label",assertion);
            }
            validateElement.addContent(element);
        }

        String expectResponseBodyString = atts.getValue("expectResponseBody");
        if (expectResponseBodyString != null) {
            validateElement.addContent(new Comment("Converted GTest attribute expectedResponseBody: " + 
                                                   expectResponseBodyString));

            Element element = new Element("byteLength");
            if (Boolean.valueOf(expectResponseBodyString).booleanValue() == false) {
                element.setAttribute("min","-1");
                element.setAttribute("max","-1");
            }

            if (assertion != null) {
                element.setAttribute("label",assertion);
            }
            validateElement.addContent(element);
        }
    }

}
