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
 * limitations under the License.
 */

package org.apache.catalina.cluster;



public interface ClusterSender
{

    public void add(Member member);

    public void remove(Member member);

    public void start() throws java.io.IOException;

    public void stop();

    public void sendMessage(String messageId, byte[] indata, Member member) throws java.io.IOException;

    public void sendMessage(String messageId, byte[] indata) throws java.io.IOException;
    
    public boolean getIsSenderSynchronized();

}
