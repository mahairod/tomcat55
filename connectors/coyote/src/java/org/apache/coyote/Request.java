/*
 *  Copyright 1999-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.coyote;

import java.io.IOException;
import java.util.HashMap;

import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.buf.UDecoder;

import org.apache.tomcat.util.http.MimeHeaders;
import org.apache.tomcat.util.http.Parameters;
import org.apache.tomcat.util.http.ContentType;
import org.apache.tomcat.util.http.Cookies;

/**
 * This is a low-level, efficient representation of a server request. Most 
 * fields are GC-free, expensive operations are delayed until the  user code 
 * needs the information.
 *
 * Processing is delegated to modules, using a hook mechanism.
 * 
 * This class is not intended for user code - it is used internally by tomcat
 * for processing the request in the most efficient way. Users ( servlets ) can
 * access the information using a facade, which provides the high-level view
 * of the request.
 *
 * For lazy evaluation, the request uses the getInfo() hook. The following ids
 * are defined:
 * <ul>
 *  <li>req.encoding - returns the request encoding
 *  <li>req.attribute - returns a module-specific attribute ( like SSL keys, etc ).
 * </ul>
 *
 * Tomcat defines a number of attributes:
 * <ul>
 *   <li>"org.apache.tomcat.request" - allows access to the low-level
 *       request object in trusted applications 
 * </ul>
 *
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author Harish Prabandham
 * @author Alex Cruikshank [alex@epitonic.com]
 * @author Hans Bergsten [hans@gefionsoftware.com]
 * @author Costin Manolache
 * @author Remy Maucherat
 */
public final class Request {


    // ----------------------------------------------------------- Constructors


    public Request() {

        parameters.setQuery(queryMB);
        parameters.setURLDecoder(urlDecoder);
        parameters.setHeaders(headers);

        schemeMB.setString("http");
        methodMB.setString("GET");
        uriMB.setString("/");
        queryMB.setString("");
        protoMB.setString("HTTP/1.0");

    }


    // ----------------------------------------------------- Instance Variables


    private int serverPort = -1;
    private MessageBytes serverNameMB = MessageBytes.newInstance();

    private String localHost;
    
    private int remotePort;
    private int localPort;

    private MessageBytes schemeMB = MessageBytes.newInstance();

    private MessageBytes methodMB = MessageBytes.newInstance();
    private MessageBytes unparsedURIMB = MessageBytes.newInstance();
    private MessageBytes uriMB = MessageBytes.newInstance();
    private MessageBytes decodedUriMB = MessageBytes.newInstance();
    private MessageBytes queryMB = MessageBytes.newInstance();
    private MessageBytes protoMB = MessageBytes.newInstance();

    // remote address/host
    private MessageBytes remoteAddrMB = MessageBytes.newInstance();
    private MessageBytes localNameMB = MessageBytes.newInstance();
    private MessageBytes remoteHostMB = MessageBytes.newInstance();
    private MessageBytes localAddrMB = MessageBytes.newInstance();
     
    private MimeHeaders headers = new MimeHeaders();

    private MessageBytes instanceId = MessageBytes.newInstance();

    /**
     * Notes.
     */
    private Object notes[] = new Object[Constants.MAX_NOTES];


    /**
     * Associated input buffer.
     */
    private InputBuffer inputBuffer = null;


    /**
     * URL decoder.
     */
    private UDecoder urlDecoder = new UDecoder();


    /**
     * HTTP specific fields. (remove them ?)
     */
    private int contentLength = -1;
    // how much body we still have to read.
    // Apparently nobody uses this field...
    private int available = -1;
    private MessageBytes contentTypeMB = null;
    private String charEncoding = null;
    private Cookies cookies = new Cookies(headers);
    private Parameters parameters = new Parameters();

    private MessageBytes remoteUser=MessageBytes.newInstance();
    private MessageBytes authType=MessageBytes.newInstance();
    private HashMap attributes=new HashMap();

    private Response response;
    private ActionHook hook;

    private int bytesRead=0;
    // Time of the request - usefull to avoid repeated calls to System.currentTime
    private long startTime = 0L;

    private RequestInfo reqProcessorMX=new RequestInfo(this);
    // ------------------------------------------------------------- Properties


    /**
     * Get the instance id (or JVM route). Curently Ajp is sending it with each
     * request. In future this should be fixed, and sent only once ( or
     * 'negociated' at config time so both tomcat and apache share the same name.
     * 
     * @return the instance id
     */
    public MessageBytes instanceId() {
        return instanceId;
    }


    public MimeHeaders getMimeHeaders() {
        return headers;
    }


    public UDecoder getURLDecoder() {
        return urlDecoder;
    }

    // -------------------- Request data --------------------


    public MessageBytes scheme() {
        return schemeMB;
    }
    
    public MessageBytes method() {
        return methodMB;
    }
    
    public MessageBytes unparsedURI() {
        return unparsedURIMB;
    }

    public MessageBytes requestURI() {
        return uriMB;
    }

    public MessageBytes decodedURI() {
        return decodedUriMB;
    }

    public MessageBytes query() {
        return queryMB;
    }

    public MessageBytes queryString() {
        return queryMB;
    }

    public MessageBytes protocol() {
        return protoMB;
    }
    
    /** 
     * Return the buffer holding the server name, if
     * any. Use isNull() to check if there is no value
     * set.
     * This is the "virtual host", derived from the
     * Host: header.
     */
    public MessageBytes serverName() {
	return serverNameMB;
    }

    public int getServerPort() {
        return serverPort;
    }
    
    public void setServerPort(int serverPort ) {
	this.serverPort=serverPort;
    }

    public MessageBytes remoteAddr() {
	return remoteAddrMB;
    }

    public MessageBytes remoteHost() {
	return remoteHostMB;
    }

    public MessageBytes localName() {
	return localNameMB;
    }    

    public MessageBytes localAddr() {
	return localAddrMB;
    }
    
    public String getLocalHost() {
	return localHost;
    }

    public void setLocalHost(String host) {
	this.localHost = host;
    }    
    
    public int getRemotePort(){
        return remotePort;
    }
        
    public void setRemotePort(int port){
        this.remotePort = port;
    }
    
    public int getLocalPort(){
        return localPort;
    }
        
    public void setLocalPort(int port){
        this.localPort = port;
    }

    // -------------------- encoding/type --------------------


    /**
     * Get the character encoding used for this request.
     */
    public String getCharacterEncoding() {

        if (charEncoding != null)
            return charEncoding;

        charEncoding = ContentType.getCharsetFromContentType(getContentType());
        return charEncoding;

    }


    public void setCharacterEncoding(String enc) {
	this.charEncoding = enc;
    }


    public void setContentLength(int len) {
	this.contentLength = len;
	available = len;
    }


    public int getContentLength() {
        if( contentLength > -1 ) return contentLength;

	MessageBytes clB = headers.getValue("content-length");
        contentLength = (clB == null || clB.isNull()) ? -1 : clB.getInt();
	available = contentLength;

	return contentLength;
    }


    public String getContentType() {
        contentType();
        if ((contentTypeMB == null) || contentTypeMB.isNull()) 
            return null;
        return contentTypeMB.toString();
    }


    public void setContentType(String type) {
        contentTypeMB.setString(type);
    }


    public MessageBytes contentType() {
        if (contentTypeMB == null)
            contentTypeMB = headers.getValue("content-type");
        return contentTypeMB;
    }


    public void setContentType(MessageBytes mb) {
        contentTypeMB=mb;
    }


    public String getHeader(String name) {
        return headers.getHeader(name);
    }

    // -------------------- Associated response --------------------

    public Response getResponse() {
        return response;
    }

    public void setResponse( Response response ) {
        this.response=response;
        response.setRequest( this );
    }
    
    public void action(ActionCode actionCode, Object param) {
        if( hook==null && response!=null )
            hook=response.getHook();
        
        if (hook != null) {
            if( param==null ) 
                hook.action(actionCode, this);
            else
                hook.action(actionCode, param);
        }
    }


    // -------------------- Cookies --------------------


    public Cookies getCookies() {
	return cookies;
    }


    // -------------------- Parameters --------------------


    public Parameters getParameters() {
	return parameters;
    }


    // -------------------- Other attributes --------------------
    // We can use notes for most - need to discuss what is of general interest
    
    public void setAttribute( String name, Object o ) {
        attributes.put( name, o );
    }

    public HashMap getAttributes() {
        return attributes;
    }

    public Object getAttribute(String name ) {
        return attributes.get(name);
    }
    
    public MessageBytes getRemoteUser() {
        return remoteUser;
    }

    public MessageBytes getAuthType() {
        return authType;
    }

    // -------------------- Input Buffer --------------------


    public InputBuffer getInputBuffer() {
        return inputBuffer;
    }


    public void setInputBuffer(InputBuffer inputBuffer) {
        this.inputBuffer = inputBuffer;
    }


    /**
     * Read data from the input buffer and put it into a byte chunk.
     */
    public int doRead(ByteChunk chunk) 
        throws IOException {
        int n = inputBuffer.doRead(chunk, this);
        if (n > 0) {
            available -= n;
            bytesRead+=n;
        }
        return n;
    }


    // -------------------- debug --------------------

    public String toString() {
	return "R( " + requestURI().toString() + ")";
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    // -------------------- Per-Request "notes" --------------------


    public final void setNote(int pos, Object value) {
	notes[pos] = value;
    }


    public final Object getNote(int pos) {
	return notes[pos];
    }


    // -------------------- Recycling -------------------- 


    public void recycle() {
        bytesRead=0;

	contentLength = -1;
        contentTypeMB = null;
        charEncoding = null;
        headers.recycle();
        serverNameMB.recycle();
        serverPort=-1;
        localPort = -1;
        remotePort = -1;

	cookies.recycle();
        parameters.recycle();

        unparsedURIMB.recycle();
        uriMB.recycle(); 
        decodedUriMB.recycle();
	queryMB.recycle();
	methodMB.recycle();
	protoMB.recycle();
	//remoteAddrMB.recycle();
	//remoteHostMB.recycle();

	// XXX Do we need such defaults ?
        schemeMB.recycle();
	methodMB.setString("GET");
        uriMB.setString("/");
        queryMB.setString("");
        protoMB.setString("HTTP/1.0");
        //remoteAddrMB.setString("127.0.0.1");
        //remoteHostMB.setString("localhost");

        instanceId.recycle();
        remoteUser.recycle();
        authType.recycle();
        attributes.clear();
    }

    // -------------------- Info  --------------------
    public void updateCounters() {
        reqProcessorMX.updateCounters();
    }

    public RequestInfo getRequestProcessor() {
        return reqProcessorMX;
    }

    public int getBytesRead() {
        return bytesRead;
    }

    public void setBytesRead(int bytesRead) {
        this.bytesRead = bytesRead;
    }
}
