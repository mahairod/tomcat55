/*
 * Copyright 1999,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */

package org.apache.catalina.groups.group.interceptors;

import org.apache.catalina.groups.group.ChannelInterceptorBase;
import org.apache.catalina.groups.InterceptorPayload;
import org.apache.catalina.groups.Member;
import java.io.IOException;
import org.apache.catalina.groups.ChannelMessage;
import org.apache.catalina.groups.io.ClusterData;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;



/**
 *
 *
 * @author Filip Hanik
 * @version 1.0
 */
public class GzipInterceptor extends ChannelInterceptorBase {
   
    public void sendMessage(Member[] destination, ChannelMessage msg, InterceptorPayload payload) throws IOException {
        try {
            msg.setMessage(compress(msg.getMessage()));
            getNext().sendMessage(destination, msg, payload);
        } catch ( IOException x ) {
            log.error("Unable to compress byte contents");
            throw x;
        }
    }

    public void messageReceived(ChannelMessage msg) {
        try {
            msg.setMessage(decompress(msg.getMessage()));
            getPrevious().messageReceived(msg);
        } catch ( IOException x ) {
            log.error("Unable to decompress byte contents",x);
        }
    }
    
    public byte[] compress(byte[] data) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        GZIPOutputStream gout = new GZIPOutputStream(bout);
        gout.write(data);
        gout.flush();
        gout.close();
        return bout.toByteArray();
    }
    
    public byte[] decompress(byte[] data) throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        GZIPInputStream gin = new GZIPInputStream(bin);
        byte[] tmp = new byte[data.length];
        int length = gin.read(tmp);
        byte[] result = new byte[length];
        System.arraycopy(tmp,0,result,0,length);
        return result;
    }
    
}
