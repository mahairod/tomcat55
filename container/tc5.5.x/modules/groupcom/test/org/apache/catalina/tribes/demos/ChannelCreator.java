/*
 * Copyright 1999,2004-2006 The Apache Software Foundation.
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
package org.apache.catalina.tribes.demos;

import java.util.Iterator;
import java.util.Properties;

import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.ManagedChannel;
import org.apache.catalina.tribes.group.GroupChannel;
import org.apache.catalina.tribes.group.interceptors.FragmentationInterceptor;
import org.apache.catalina.tribes.group.interceptors.GzipInterceptor;
import org.apache.catalina.tribes.group.interceptors.OrderInterceptor;
import org.apache.catalina.tribes.mcast.McastService;
import org.apache.catalina.tribes.tcp.MultiPointSender;
import org.apache.catalina.tribes.tcp.ReceiverBase;
import org.apache.catalina.tribes.tcp.ReplicationTransmitter;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.catalina.tribes.tcp.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 *
 * <p>Company: </p>
 *
 * @author fhanik
 * @version 1.0
 */
public class ChannelCreator {

    public static StringBuffer usage() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\t\t[-bind tcpbindaddress]")
           .append("\n\t\t[-tcpselto tcpselectortimeout]") 
           .append("\n\t\t[-tcpthreads tcpthreadcount]") 
           .append("\n\t\t[-port tcplistenport]")
           .append("\n\t\t[-ack true|false]")
           .append("\n\t\t[-ackto acktimeout]") 
           .append("\n\t\t[-sync true|false]")
           .append("\n\t\t[-receiver org.apache.catalina.tribes.tcp.nio.NioReceiver|org.apache.catalina.tribes.tcp.bio.BioReceiver|]")
           .append("\n\t\t[-transport org.apache.catalina.tribes.tcp.nio.PooledParallelSender|org.apache.catalina.tribes.tcp.bio.PooledMultiSender]")
           .append("\n\t\t[-transport.xxx transport specific property]")
           .append("\n\t\t[-maddr multicastaddr]")
           .append("\n\t\t[-mport multicastport]")
           .append("\n\t\t[-mbind multicastbindaddr]")
           .append("\n\t\t[-mfreq multicastfrequency]")
           .append("\n\t\t[-mdrop multicastdroptime]")
           .append("\n\t\t[-gzip]")
           .append("\n\t\t[-order]")
           .append("\n\t\t[-ordersize maxorderqueuesize]")
           .append("\n\t\t[-frag]")
           .append("\n\t\t[-fragsize maxmsgsize]");
       return buf;

    }

    public static Channel createChannel(String[] args) throws Exception {
        String bind = "auto";
        int port = 4001;
        String mbind = null;
        boolean ack = false;
        boolean sync = false;
        boolean gzip = false;
        int tcpseltimeout = 100;
        int tcpthreadcount = 4;
        int acktimeout = 15000;
        String mcastaddr = "228.0.0.5";
        int mcastport = 45565;
        long mcastfreq = 500;
        long mcastdrop = 2000;
        boolean order = false;
        int ordersize = Integer.MAX_VALUE;
        boolean frag = false;
        int fragsize = 1024;
        Properties transportProperties = new Properties();
        String transport = "org.apache.catalina.tribes.tcp.nio.PooledParallelSender";
        String receiver = "org.apache.catalina.tribes.tcp.nio.NioReceiver";
        
        for (int i = 0; i < args.length; i++) {
            if ("-bind".equals(args[i])) {
                bind = args[++i];
            } else if ("-port".equals(args[i])) {
                port = Integer.parseInt(args[++i]);
            } else if ("-tcpselto".equals(args[i])) {
                tcpseltimeout = Integer.parseInt(args[++i]);
            } else if ("-tcpthreads".equals(args[i])) {
                tcpthreadcount = Integer.parseInt(args[++i]);
            } else if ("-gzip".equals(args[i])) {
                gzip = true;
            } else if ("-order".equals(args[i])) {
                order = true;
            } else if ("-ordersize".equals(args[i])) {
                ordersize = Integer.parseInt(args[++i]);
                System.out.println("Setting OrderInterceptor.maxQueue="+ordersize);
            } else if ("-frag".equals(args[i])) {
                frag = true;
            } else if ("-fragsize".equals(args[i])) {
                fragsize = Integer.parseInt(args[++i]);
                System.out.println("Setting FragmentationInterceptor.maxSize="+fragsize);
            } else if ("-ack".equals(args[i])) {
                ack = Boolean.parseBoolean(args[++i]);
            } else if ("-ackto".equals(args[i])) {
                acktimeout = Integer.parseInt(args[++i]);
            } else if ("-sync".equals(args[i])) {
                sync = Boolean.parseBoolean(args[++i]);
            } else if ("-transport".equals(args[i])) {
                transport = args[++i];
            } else if (args[i]!=null && args[i].startsWith("transport.")) {
                String key = args[i];
                String val = args[++i];
                transportProperties.setProperty(key,val);
            } else if ("-receiver".equals(args[i])) {
                receiver = args[++i];
            } else if ("-maddr".equals(args[i])) {
                mcastaddr = args[++i];
            } else if ("-mport".equals(args[i])) {
                mcastport = Integer.parseInt(args[++i]);
            } else if ("-mfreq".equals(args[i])) {
                mcastfreq = Long.parseLong(args[++i]);
            } else if ("-mdrop".equals(args[i])) {
                mcastdrop = Long.parseLong(args[++i]);
            } else if ("-mbind".equals(args[i])) {
                mbind = args[++i];
            }
        }
        
        System.out.println("Creating receiver class="+receiver);
        Class cl = Class.forName(receiver,true,ChannelCreator.class.getClassLoader());
        ReceiverBase rx = (ReceiverBase)cl.newInstance();
        rx.setTcpListenAddress(bind);
        rx.setTcpListenPort(port);
        rx.setTcpSelectorTimeout(tcpseltimeout);
        rx.setTcpThreadCount(tcpthreadcount);
        rx.getBind();
        rx.setSendAck(ack);
        rx.setSynchronized(sync);
        rx.setRxBufSize(43800);
        rx.setTxBufSize(25188);

        
        ReplicationTransmitter ps = new ReplicationTransmitter();
        System.out.println("Creating transport class="+transport);
        MultiPointSender sender = (MultiPointSender)Class.forName(transport,true,ChannelCreator.class.getClassLoader()).newInstance();
        sender.setTimeout(acktimeout);
        sender.setWaitForAck(ack);
        sender.setMaxRetryAttempts(2);
        sender.setRxBufSize(43800);
        sender.setTxBufSize(25188);

        Iterator i = transportProperties.keySet().iterator();
        while ( i.hasNext() ) {
            String key = (String)i.next();
            IntrospectionUtils.setProperty(sender,key,transportProperties.getProperty(key));
        }
        ps.setTransport(sender);

        McastService service = new McastService();
        service.setMcastAddr(mcastaddr);
        if (mbind != null) service.setMcastBindAddress(mbind);
        service.setMcastFrequency(mcastfreq);
        service.setMcastDropTime(mcastdrop);
        service.setMcastPort(mcastport);

        ManagedChannel channel = new GroupChannel();
        channel.setChannelReceiver(rx);
        channel.setChannelSender(ps);
        channel.setMembershipService(service);

        if (gzip) channel.addInterceptor(new GzipInterceptor());
        if ( frag ) {
            FragmentationInterceptor fi = new FragmentationInterceptor();
            fi.setMaxSize(fragsize);
            channel.addInterceptor(fi);
        }
        if (order) {
            OrderInterceptor oi = new OrderInterceptor();
            oi.setMaxQueue(ordersize);
            channel.addInterceptor(oi);
        }
        return channel;
        
    }

}