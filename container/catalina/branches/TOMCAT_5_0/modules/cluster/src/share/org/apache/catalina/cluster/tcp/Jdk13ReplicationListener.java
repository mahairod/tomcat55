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

package org.apache.catalina.cluster.tcp;




import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Iterator;
import org.apache.catalina.cluster.io.ListenCallback;
import org.apache.catalina.cluster.io.Jdk13ObjectReader;
import org.apache.catalina.cluster.io.XByteBuffer;
/**
 */
public class Jdk13ReplicationListener implements Runnable
{

    private static org.apache.commons.logging.Log log =
        org.apache.commons.logging.LogFactory.getLog( Jdk13ReplicationListener.class );
    private ThreadPool pool = null;
    private boolean doListen = false;
    private ListenCallback callback;
    private java.net.InetAddress bind;
    private int port;
    private long timeout = 0;
    private boolean synchronous = false;
    ServerSocket serverSocket = null;

    public Jdk13ReplicationListener(ListenCallback callback,
                               int poolSize,
                               java.net.InetAddress bind,
                               int port,
                               long timeout,
                               boolean synchronous)
    {
        this.synchronous=synchronous;
        this.callback = callback;
        this.bind = bind;
        this.port = port;
        this.timeout = timeout;
    }

    public void run()
    {
        try
        {
            listen();
        }
        catch ( Exception x )
        {
            log.fatal("Unable to start cluster listener.",x);
        }
    }

    public void listen ()
        throws Exception
    {
        doListen = true;
        // Get the associated ServerSocket to bind it with
        serverSocket = new ServerSocket();
        serverSocket.bind (new InetSocketAddress (bind,port));
        while (doListen) {
            Socket socket = serverSocket.accept();
            ClusterListenThread t = new ClusterListenThread(socket,new Jdk13ObjectReader(socket,callback));
            t.setDaemon(true);
            t.start();
        }//while
        serverSocket.close();
    }

    public void stopListening(){
        doListen = false;
        try {
            serverSocket.close();
        } catch ( Exception x ) {
            log.error("Unable to stop the replication listen socket",x);
        }
    }

    protected static class ClusterListenThread extends Thread {
        private Socket socket;
        private Jdk13ObjectReader reader;
        private boolean keepRunning = true;
        private static byte[] ackMsg = new byte[] {6,2,3};
        ClusterListenThread(Socket socket, Jdk13ObjectReader reader) {
            this.socket = socket;
            this.reader = reader;
        }

        public void run() {
            try {
                byte[] buffer = new byte[1024];
                while (keepRunning) {
                    java.io.InputStream in = socket.getInputStream();
                    int cnt = in.read(buffer);
                    int ack = 0;
                    if ( cnt > 0 ) {
                        ack = reader.append(buffer, 0, cnt);
                    }
                    while ( ack > 0 ) {
                        sendAck();
                        ack--;
                    }
                }
            } catch ( Exception x ) {
                keepRunning = false;
                log.error("Unable to read data from client, disconnecting.",x);
                try { socket.close(); } catch ( Exception ignore ) {}
            }
        }

        private void sendAck() throws java.io.IOException {
            //send a reply-acknowledgement
            socket.getOutputStream().write(ackMsg);
        }

    }
}
