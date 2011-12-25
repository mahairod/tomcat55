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

package org.apache.tomcat.util.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.tomcat.util.buf.B2CConverter;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.buf.UDecoder;
import org.apache.tomcat.util.res.StringManager;

/**
 * 
 * @author Costin Manolache
 */
public final class Parameters {

    private static final org.apache.commons.logging.Log log =
            org.apache.commons.logging.LogFactory.getLog(Parameters.class);

    protected static final StringManager sm =
        StringManager.getManager("org.apache.tomcat.util.http");

    // HashMap<String,ArrayList<String>>
    private final HashMap paramHashValues = new HashMap();

    private boolean didQueryParameters=false;
    
    MessageBytes queryMB;

    UDecoder urlDec;
    MessageBytes decodedQuery=MessageBytes.newInstance();

    String encoding=null;
    String queryStringEncoding=null;

    private int limit = -1;
    private int parameterCount = 0;

    /**
     * Is set to <code>true</code> if there were failures during parameter
     * parsing.
     */
    private boolean parseFailed = false;

    public Parameters() {
        // NO-OP
    }

    public void setQuery( MessageBytes queryMB ) {
        this.queryMB=queryMB;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding( String s ) {
        encoding=s;
        if(log.isDebugEnabled()) {
            log.debug( "Set encoding to " + s );
        }
    }

    public void setQueryStringEncoding( String s ) {
        queryStringEncoding=s;
        if(log.isDebugEnabled()) {
            log.debug( "Set query string encoding to " + s );
        }
    }

    public boolean isParseFailed() {
        return parseFailed;
    }

    public void setParseFailed(boolean parseFailed) {
        this.parseFailed = parseFailed;
    }

    public void recycle() {
        parameterCount = 0;
        paramHashValues.clear();
        didQueryParameters=false;
        encoding=null;
        decodedQuery.recycle();
        parseFailed = false;
    }

    // -------------------- Data access --------------------
    // Access to the current name/values, no side effect ( processing ).
    // You must explicitly call handleQueryParameters and the post methods.
    
    public void addParameterValues(String key, String[] newValues) {
        if (key == null) {
            return;
        }
        ArrayList values = (ArrayList) paramHashValues.get(key);
        if (values == null) {
            values = new ArrayList(newValues.length);
            paramHashValues.put(key, values);
        } else {
            values.ensureCapacity(values.size() + newValues.length);
        }
        for (int i = 0; i < newValues.length; i++) {
            values.add(newValues[i]);
        }
    }

    public String[] getParameterValues(String name) {
        handleQueryParameters();
        // no "facade"
        ArrayList values = (ArrayList) paramHashValues.get(name);
        if (values == null) {
            return null;
        }
        return (String[]) values.toArray(new String[values.size()]);
    }
 
    public Enumeration getParameterNames() {
        handleQueryParameters();
        return Collections.enumeration(paramHashValues.keySet());
    }

    // Shortcut.
    public String getParameter(String name ) {
        handleQueryParameters();
        ArrayList values = (ArrayList) paramHashValues.get(name);
        if (values != null) {
            if(values.size() == 0) {
                return "";
            }
            return (String) values.get(0);
        } else {
            return null;
        }
    }
    // -------------------- Processing --------------------
    /** Process the query string into parameters
     */
    public void handleQueryParameters() {
        if( didQueryParameters ) return;

        didQueryParameters=true;

        if( queryMB==null || queryMB.isNull() )
            return;
        
        if(log.isDebugEnabled()) {
            log.debug("Decoding query " + decodedQuery + " " +
                    queryStringEncoding);
        }

        try {
            decodedQuery.duplicate( queryMB );
        } catch (IOException e) {
            // Can't happen, as decodedQuery can't overflow
            e.printStackTrace();
        }
        processParameters( decodedQuery, queryStringEncoding );
    }


    private void addParam( String key, String value ) {
        if( key==null ) return;
        ArrayList values = (ArrayList) paramHashValues.get(key);
        if (values == null) {
            values = new ArrayList(1);
            paramHashValues.put(key, values);
        }
        values.add(value);
    }

    public void setURLDecoder( UDecoder u ) {
        urlDec=u;
    }

    // -------------------- Parameter parsing --------------------
    // we are called from a single thread - we can do it the hard way
    // if needed
    ByteChunk tmpName=new ByteChunk();
    ByteChunk tmpValue=new ByteChunk();
    private ByteChunk origName=new ByteChunk();
    private ByteChunk origValue=new ByteChunk();
    private static final String DEFAULT_ENCODING = "ISO-8859-1";
    private static final Charset DEFAULT_CHARSET =
        Charset.forName(DEFAULT_ENCODING);
    
    
    public void processParameters( byte bytes[], int start, int len ) {
        processParameters(bytes, start, len, getCharset(encoding));
    }

    private void processParameters(byte bytes[], int start, int len,
                                  Charset charset) {
        
        if(log.isDebugEnabled()) {
            try {
                log.debug(sm.getString("parameters.bytes",
                        new String(bytes, start, len, DEFAULT_CHARSET.name())));
            } catch (UnsupportedEncodingException uee) {
                // Not possible. All JVMs must support ISO-8859-1
            }
        }

        int decodeFailCount = 0;
            
        int pos = start;
        int end = start + len;

        while(pos < end) {
            parameterCount ++;

            if (limit > -1 && parameterCount >= limit) {
                parseFailed = true;
                log.warn(sm.getString("parameters.maxCountFail",
                        Integer.toString(limit)));
                break;
            }
            int nameStart = pos;
            int nameEnd = -1;
            int valueStart = -1;
            int valueEnd = -1;

            boolean parsingName = true;
            boolean decodeName = false;
            boolean decodeValue = false;
            boolean parameterComplete = false;

            do {
                switch(bytes[pos]) {
                    case '=':
                        if (parsingName) {
                            // Name finished. Value starts from next character
                            nameEnd = pos;
                            parsingName = false;
                            valueStart = ++pos;
                        } else {
                            // Equals character in value
                            pos++;
                        }
                        break;
                    case '&':
                        if (parsingName) {
                            // Name finished. No value.
                            nameEnd = pos;
                        } else {
                            // Value finished
                            valueEnd  = pos;
                        }
                        parameterComplete = true;
                        pos++;
                        break;
                    case '%':
                    case '+':
                        // Decoding required
                        if (parsingName) {
                            decodeName = true;
                        } else {
                            decodeValue = true;
                        }
                        pos ++;
                        break;
                    default:
                        pos ++;
                        break;
                }
            } while (!parameterComplete && pos < end);

            if (pos == end) {
                if (nameEnd == -1) {
                    nameEnd = pos;
                } else if (valueStart > -1 && valueEnd == -1){
                    valueEnd = pos;
                }
            }
            
            if (log.isDebugEnabled() && valueStart == -1) {
                try {
                    log.debug(sm.getString("parameters.noequal",
                            Integer.toString(nameStart),
                            Integer.toString(nameEnd),
                            new String(bytes, nameStart, nameEnd-nameStart,
                                    DEFAULT_CHARSET.name())));
                } catch (UnsupportedEncodingException uee) {
                    // Not possible. All JVMs must support ISO-8859-1
                }
            }
            
            if (nameEnd <= nameStart ) {
                if (log.isInfoEnabled()) {
                    if (valueEnd >= nameStart && log.isDebugEnabled()) {
                        String extract = null;
                        try {
                            extract = new String(bytes, nameStart,
                                    valueEnd - nameStart,
                                    DEFAULT_CHARSET.name());
                        } catch (UnsupportedEncodingException uee) {
                            // Not possible. All JVMs must support ISO-8859-1
                        }
                        log.info(sm.getString("parameters.invalidChunk",
                                Integer.toString(nameStart),
                                Integer.toString(valueEnd),
                                extract));
                    } else {
                        log.info(sm.getString("parameters.invalidChunk",
                                Integer.toString(nameStart),
                                Integer.toString(nameEnd),
                                null));
                    }
                }
                parseFailed = true;
                continue;
                // invalid chunk - it's better to ignore
            }
            
            tmpName.setBytes(bytes, nameStart, nameEnd - nameStart);
            tmpValue.setBytes(bytes, valueStart, valueEnd - valueStart);

            // Take copies as if anything goes wrong originals will be
            // corrupted. This means original values can be logged.
            // For performance - only done for debug
            if (log.isDebugEnabled()) {
                try {
                    origName.append(bytes, nameStart, nameEnd - nameStart);
                    origValue.append(bytes, valueStart, valueEnd - valueStart);
                } catch (IOException ioe) {
                    // Should never happen...
                    log.error(sm.getString("parameters.copyFail"), ioe);
                }
            }
            
            try {
                String name;
                String value;

                if (decodeName) {
                    urlDecode(tmpName);
                }
                tmpName.setCharset(charset);
                name = tmpName.toString();

                if (decodeValue) {
                    urlDecode(tmpValue);
                }
                tmpValue.setCharset(charset);
                value = tmpValue.toString();

                addParam(name, value);
            } catch (IOException e) {
                parseFailed = true;
                decodeFailCount++;
                if (decodeFailCount == 1 || log.isDebugEnabled()) {
                    if (log.isDebugEnabled()) {
                        log.debug(sm.getString("parameters.decodeFail.debug",
                                origName.toString(), origValue.toString()), e);
                    } else if (log.isInfoEnabled()) {
                        log.info(sm.getString("parameters.decodeFail.info",
                                tmpName.toString(), tmpValue.toString()), e);
                    }
                }
            }

            tmpName.recycle();
            tmpValue.recycle();
            // Only recycle copies if we used them
            if (log.isDebugEnabled()) {
                origName.recycle();
                origValue.recycle();
            }
        }

        if (decodeFailCount > 1 && !log.isDebugEnabled()) {
            log.info(sm.getString("parameters.multipleDecodingFail",
                    Integer.toString(decodeFailCount)));
        }
    }

    private void urlDecode(ByteChunk bc)
        throws IOException {
        if( urlDec==null ) {
            urlDec=new UDecoder();   
        }
        urlDec.convert(bc);
    }

    public void processParameters( MessageBytes data, String encoding ) {
        if( data==null || data.isNull() || data.getLength() <= 0 ) return;

        if( data.getType() != MessageBytes.T_BYTES ) {
            data.toBytes();
        }
        ByteChunk bc=data.getByteChunk();
        processParameters( bc.getBytes(), bc.getOffset(),
                           bc.getLength(), getCharset(encoding));
    }

    private Charset getCharset(String encoding) {
        if (encoding == null) {
            return DEFAULT_CHARSET;
        }
        try {
            return B2CConverter.getCharset(encoding);
        } catch (UnsupportedEncodingException e) {
            return DEFAULT_CHARSET;
        }
    }

    /**
     * Debug purpose
     */
    public String paramsAsString() {
        StringBuffer sb = new StringBuffer();
        Iterator it = paramHashValues.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry e = (Map.Entry) it.next();
            sb.append(e.getKey()).append('=');
            ArrayList values = (ArrayList) e.getValue();
            for(int i = 0; i < values.size(); i++) {
                sb.append(values.get(i)).append(',');
            }
            sb.append('\n');
        }
        return sb.toString();
    }

}
