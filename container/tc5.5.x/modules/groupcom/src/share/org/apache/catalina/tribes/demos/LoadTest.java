package org.apache.catalina.tribes.demos;

import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.MembershipListener;
import org.apache.catalina.tribes.ChannelListener;
import java.io.Serializable;
import org.apache.catalina.tribes.ManagedChannel;
import java.io.Externalizable;
import java.io.ObjectOutput;
import java.io.IOException;
import java.util.Random;
import java.io.ObjectInput;
import org.apache.catalina.tribes.io.XByteBuffer;
import org.apache.catalina.tribes.tcp.ReplicationListener;
import org.apache.catalina.tribes.group.GroupChannel;
import org.apache.catalina.tribes.tcp.ReplicationTransmitter;
import org.apache.catalina.tribes.mcast.McastService;
import org.apache.catalina.tribes.ByteMessage;
import org.apache.catalina.tribes.group.interceptors.GzipInterceptor;
import org.apache.catalina.tribes.ChannelException;


/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class LoadTest implements MembershipListener,ChannelListener, Runnable {
    protected static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(LoadTest.class);
    public static Object mutex = new Object();
    public boolean doRun = true;
    
    public long bytesReceived = 0;
    public int  messagesReceived = 0;
    public boolean send = true;
    public boolean debug = false;
    public int msgCount = 100;
    ManagedChannel channel=null;
    public int statsInterval = 10000;
    public long pause = 0;
    public boolean breakonChannelException = false;
    public long receiveStart = 0;
    
    static int messageSize = 0;
    
    public static long messagesSent = 0;
    public static long messageSendTime = 0;
    
    public static synchronized long addSendStats(long count, long time) {
        messagesSent+=count;
        messageSendTime+=time;
        return 0l;
    }    
    
    
    public LoadTest(ManagedChannel channel, 
                    boolean send,
                    int msgCount,
                    boolean debug,
                    long pause,
                    int stats,
                    boolean breakOnEx) {
        this.channel = channel;
        this.send = send;
        this.msgCount = msgCount;
        this.debug = debug;
        this.pause = pause;
        this.statsInterval = stats;
        this.breakonChannelException = breakOnEx;
    }
    
    
    
    public void run() {
        
        long counter = 0;
        LoadMessage msg = new LoadMessage();
        int messageSize = LoadTest.messageSize;
        long sendTime = 0;
        try {
            while (counter < msgCount) {
                if (channel.getMembers().length == 0 || (!send)) {
                    synchronized (mutex) {
                        try {
                            mutex.wait();
                        } catch (InterruptedException x) {
                            log.info("Thread interrupted from wait");
                        }
                    }
                } else {
                    try {
                        msg.setMsgNr((int)++counter);
                        long start = System.currentTimeMillis();
                        if (debug) {
                            printArray(msg.getMessage());
                        }
                        channel.send(null, msg);
                        if ( pause > 0 ) {
                            if ( debug) System.out.println("Pausing sender for "+pause+" ms.");
                            Thread.sleep(pause);
                        }
                        sendTime += (System.currentTimeMillis() - start);
                    } catch (ChannelException x) {
                        log.error("Unable to send message.");
                        Member[] faulty = x.getFaultyMembers();
                        for (int i=0; i<faulty.length; i++ ) log.error("Faulty: "+faulty[i]);
                        --counter;
                        if ( this.breakonChannelException ) throw x;
                    }
                }
                if ( (counter % statsInterval) == 0 && (counter > 0)) {
                    //add to the global counter
                    //counter = sendTime = addSendStats(counter,sendTime);
                    //print from the global counter
                    //printSendStats(LoadTest.messagesSent, LoadTest.messageSize, LoadTest.messageSendTime);
                    printSendStats(counter, LoadTest.messageSize, sendTime);
                    
                }

            }
        }catch ( Exception x ) {
            x.printStackTrace();
            printSendStats(counter, messageSize, sendTime);
        }
    }

    private void printSendStats(long counter, int messageSize, long sendTime) {
        float cnt = (float)counter;
        float size = (float)messageSize;
        float time = (float)sendTime / 1000;
        log.info("****SEND STATS-"+Thread.currentThread().getName()+"*****"+
                 "\n\tMessage count:"+counter+
                 "\n\tTotal bytes  :"+(long)(size*cnt)+
                 "\n\tTotal seconds:"+(time)+
                 "\n\tBytes/second :"+(size*cnt/time)+
                 "\n\tMBytes/second:"+(size*cnt/time/1024f/1024f));
    }

    /**
     * memberAdded
     *
     * @param member Member
     * @todo Implement this org.apache.catalina.tribes.MembershipListener
     *   method
     */
    public void memberAdded(Member member) {
        log.info("Member added:"+member);
        synchronized (mutex) {
            mutex.notifyAll();
        }
    }

    /**
     * memberDisappeared
     *
     * @param member Member
     * @todo Implement this org.apache.catalina.tribes.MembershipListener
     *   method
     */
    public void memberDisappeared(Member member) {
        log.info("Member disappeared:"+member);
    }
    
    public boolean accept(Serializable msg, Member mbr){ 
       return (msg instanceof LoadMessage);
    }
    
    public void messageReceived(Serializable msg, Member mbr){ 
        if ( receiveStart == 0 ) receiveStart = System.currentTimeMillis();
        if ( debug ) {
            if ( msg instanceof LoadMessage ) {
                printArray(((LoadMessage)msg).getMessage());
            }
        }
        
        if ( msg instanceof ByteMessage ) {
            LoadMessage tmp = new LoadMessage();
            tmp.setMessage(((ByteMessage)msg).getMessage());
            msg = tmp;
            tmp = null;
        }
        
        bytesReceived+=((LoadMessage)msg).getMessage().length;
        messagesReceived++;
        if ( (messagesReceived%statsInterval)==0 || (messagesReceived==msgCount)) {
            float bytes = (float)(((LoadMessage)msg).getMessage().length*messagesReceived);
            float seconds = ((float)(System.currentTimeMillis()-receiveStart)) / 1000f;
            log.info("****RECEIVE STATS-"+Thread.currentThread().getName()+"*****"+
                     "\n\tMessage count :"+(long)messagesReceived+
                     "\n\tTotal bytes   :"+(long)bytes+
                     "\n\tTime since 1st:"+seconds+" seconds"+
                     "\n\tBytes/second  :"+(bytes/seconds)+
                     "\n\tMBytes/second :"+(bytes/seconds/1024f/1024f));

        }
    }
    
    
    public static void printArray(byte[] data) {
        System.out.print("{");
        for (int i=0; i<data.length; i++ ) {
            System.out.print(data[i]);
            System.out.print(",");
        }
        System.out.println("} size:"+data.length);
    }

    
    
    public static class LoadMessage extends ByteMessage implements Serializable  {
        public static int size = 1020;
        public static byte[] outdata = new byte[size];
        public static Random r = new Random(System.currentTimeMillis());
        public static int getMessageSize (LoadMessage msg) {
            int messageSize = msg.getMessage().length;
            if ( ((Object)msg) instanceof ByteMessage ) return messageSize;
            try {
                messageSize  = XByteBuffer.serialize(new LoadMessage()).length;
                log.info("Average message size:" + messageSize + " bytes");
            } catch (Exception x) {
                log.error("Unable to calculate test message size.", x);
            }
            return messageSize;
        }
        
        protected byte[] message = null;
        private int msgNr;

        static {
            r.nextBytes(outdata);
            
        }
        
        public int getMsgNr() {
            return XByteBuffer.toInt(getMessage(),0);
        }
        
        public void setMsgNr(int nr) {
            byte[] data = XByteBuffer.toBytes(nr);
            System.arraycopy(data,0,getMessage(),0,4);
            setMessage(getMessage());
        }
        
        public byte[] getMessage() {
            byte[] data = new byte[size+4];
            XByteBuffer.toBytes(msgNr,data,0);
            if ( message != null ) {
                System.arraycopy(message, 0, data, 4, message.length);
            }else {
                System.arraycopy(outdata, 0, data, 4, outdata.length);
            }
            return data;
        }
        
        public void setMessage(byte[] data) {
            this.msgNr = XByteBuffer.toInt(data,0);
            this.message = new byte[data.length-4];
            System.arraycopy(data,4,message,0,message.length);
        }
    }
    
    public static void usage() {
        System.out.println("Tribes Load tester.");
        System.out.println("The load tester can be used in sender or received mode or both");
        System.out.println("Usage:\n\t"+
                           "java LoadTest [options]\n\t"+
                           "Options:\n\t\t"+
                           "[-bind tcpbindaddress] \n\t\t"+
                           "[-port tcplistenport]  \n\t\t"+
                           "[-mbind multicastbindaddr]  \n\t\t"+
                           "[-mode receive|send|both]  \n\t\t"+
                           "[-debug]  \n\t\t"+
                           "[-count messagecount]  \n\t\t"+
                           "[-stats statinterval]  \n\t\t"+
                           "[-ack true|false]  \n\t\t"+
                           "[-sync true|false]  \n\t\t"+
                           "[-gzip]  \n\t\t"+
                           "[-pause nrofsecondstopausebetweensends]  \n\t\t"+
                           "[-sender pooled|fastasyncqueue]  \n\t\t"+
                           "[-threads numberofsenderthreads]  \n\t\t"+
                           "[-break (halts execution on exception)]\n"+
                           "Example:\n\t"+
                           "java LoadTest -port 4004\n\t"+
                           "java LoadTest -bind 192.168.0.45 -port 4005\n\t"+
                           "java LoadTest -bind 192.168.0.45 -port 4005 -mbind 192.168.0.45 -count 100 -stats 10\n");
    }
    
    public static void main(String[] args) throws Exception {
        String bind  = "auto";
        int port = 4001;
        boolean send = true;
        String mbind = null;
        boolean debug = false;
        boolean ack = true;
        boolean sync = true;
        boolean gzip = false;
        long pause = 0;
        int count = 1000000;
        int stats = 10000;
        boolean breakOnEx = false;
        int threads = 1;
        String sender = "pooled";
        if ( args.length == 0 ) {
            args = new String[] {"-help"};
        }
        for (int i = 0; i < args.length; i++) {
            if ("-bind".equals(args[i])) {
                bind = args[++i];
            } else if ("-sender".equals(args[i])) {
                sender = args[++i];
            } else if ("-port".equals(args[i])) {
                port = Integer.parseInt(args[++i]);
            } else if ("-threads".equals(args[i])) {
                threads = Integer.parseInt(args[++i]);
            } else if ("-count".equals(args[i])) {
                count = Integer.parseInt(args[++i]);
            } else if ("-pause".equals(args[i])) {
                pause = Long.parseLong(args[++i])*1000;
            } else if ("-gzip".equals(args[i])) {
                gzip = true;
            } else if ("-break".equals(args[i])) {
                breakOnEx = true;
            } else if ("-ack".equals(args[i])) {
                ack = Boolean.parseBoolean(args[++i]);
            } else if ("-sync".equals(args[i])) {
                sync = Boolean.parseBoolean(args[++i]);
            } else if ("-stats".equals(args[i])) {
                stats = Integer.parseInt(args[++i]);
                System.out.println("Stats every "+stats+" message");
            } else if ("-mbind".equals(args[i])) {
                mbind = args[++i];
            } else if ("-mode".equals(args[i])) {
                if ( "receive".equals(args[++i]) ) send = false;
            } else if ("-debug".equals(args[i])) {
                debug = true;
            } else //("-help".equals(args[i])) 
            {
                usage();
                System.exit(1);
            }
        }
        
        
        ReplicationListener rl = new ReplicationListener();
        rl.setTcpListenAddress(bind);
        rl.setTcpListenPort(port);
        rl.setTcpSelectorTimeout(100);
        rl.setTcpThreadCount(4);
        rl.getBind();
        rl.setSendAck(ack);
        rl.setSynchronized(sync);

        ReplicationTransmitter ps = new ReplicationTransmitter();
        ps.setReplicationMode(sender);
        ps.setAckTimeout(15000);
        ps.setAutoConnect(true);
        ps.setWaitForAck(ack);

        McastService service = new McastService();
        service.setMcastAddr("228.0.0.5");
        if ( mbind != null ) service.setMcastBindAddress(mbind);
        service.setMcastFrequency(500);
        service.setMcastDropTime(2000);
        service.setMcastPort(45565);

        ManagedChannel channel = new GroupChannel();
        channel.setChannelReceiver(rl);
        channel.setChannelSender(ps);
        channel.setMembershipService(service);
        
        if ( gzip ) channel.addInterceptor(new GzipInterceptor());
        
        LoadTest test = new LoadTest(channel,send,count,debug,pause,stats,breakOnEx);
        LoadMessage msg = new LoadMessage();
        messageSize = LoadMessage.getMessageSize(msg);
        channel.setChannelListener(test);
        channel.setMembershipListener(test);
        channel.start(channel.DEFAULT);
        Runtime.getRuntime().addShutdownHook(new Shutdown(channel));
        while ( threads > 1 ) {
            Thread t = new Thread(test);
            t.setDaemon(true);
            t.start();
            threads--;
            test = new LoadTest(channel,send,count,debug,pause,stats,breakOnEx);
        }
        test.run();
        
        System.out.println("System test complete, sleeping to let threads finish.");
        Thread.sleep(60*1000*60);
    } 
    
    public static class Shutdown extends Thread {
        ManagedChannel channel = null;
        public Shutdown(ManagedChannel channel) {
            this.channel = channel;
        }
        
        public void run() {
            System.out.println("Shutting down...");
            try {
                channel.stop(channel.DEFAULT);
                
            }catch ( Exception x ) {
                x.printStackTrace();
            }
            System.out.println("Channel stopped.");
        }
    }
    
}