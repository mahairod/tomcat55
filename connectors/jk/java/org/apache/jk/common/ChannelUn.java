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

package org.apache.jk.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.jk.core.JkHandler;
import org.apache.jk.core.Msg;
import org.apache.jk.core.MsgContext;
import org.apache.tomcat.util.threads.ThreadPool;
import org.apache.tomcat.util.threads.ThreadPoolRunnable;


/** Pass messages using unix domain sockets.
 *
 * @author Costin Manolache
 */
public class ChannelUn extends JniHandler {
    static final int CH_OPEN=4;
    static final int CH_CLOSE=5;
    static final int CH_READ=6;
    static final int CH_WRITE=7;

    String file;
    ThreadPool tp;

    /* ==================== Tcp socket options ==================== */

    public ThreadPool getThreadPool() {
        return tp;
    }
    
    public void setFile( String f ) {
        file=f;
    }
    
    public String getFile() {
        return file;
    }
    
    /* ==================== ==================== */
    int socketNote=1;
    int isNote=2;
    int osNote=3;
    
    int localId=0;
    
    public void init() throws IOException {
        if( file==null ) {
            log.debug("No file, disabling unix channel");
            return;
            //throw new IOException( "No file for the unix socket channel");
        }
        if( wEnv!=null && wEnv.getLocalId() != 0 ) {
            localId=wEnv.getLocalId();
        }

        if( localId != 0 ) {
            file=file+ localId;
        }
        File socketFile=new File( file );
        if( !socketFile.isAbsolute() ) {
            String home=wEnv.getJkHome();
            if( home==null ) {
                log.debug("No jkhome");
            } else {
                File homef=new File( home );
                socketFile=new File( homef, file );
                log.debug( "Making the file absolute " +socketFile);
            }
        }
        
        if( ! socketFile.exists() ) {
            try {
                FileOutputStream fos=new FileOutputStream(socketFile);
                fos.write( 1 );
                fos.close();
            } catch( Throwable t ) {
                log.error("Attempting to create the file failed, disabling channel" 
                        + socketFile);
                return;
            }
        }
        // The socket file cannot be removed ...
        if (!socketFile.delete()) {
            log.error( "Can't remove socket file " + socketFile);
            return;
        }
        

        super.initNative( "channel.un:" + file );

        if( apr==null || ! apr.isLoaded() ) {
            log.debug("Apr is not available, disabling unix channel ");
            apr=null;
            return;
        }
        
        // Set properties and call init.
        setNativeAttribute( "file", file );
        // unixListenSocket=apr.unSocketListen( file, 10 );

        setNativeAttribute( "listen", "10" );
        // setNativeAttribute( "debug", "10" );

        // Initialize the thread pool and execution chain
        if( next==null && wEnv!=null ) {
            if( nextName!=null ) 
                setNext( wEnv.getHandler( nextName ) );
            if( next==null )
                next=wEnv.getHandler( "dispatch" );
            if( next==null )
                next=wEnv.getHandler( "request" );
        }

        tp=new ThreadPool();
                
        super.initJkComponent();
        
        log.info("JK: listening on unix socket: " + file );
        
        // Run a thread that will accept connections.
        tp.start();
        AprAcceptor acceptAjp=new AprAcceptor(  this );
        tp.runIt( acceptAjp);
    }
    
    public void destroy() throws IOException {
        if( apr==null ) return;
        try {
            if( tp != null )
                tp.shutdown();
            
            //apr.unSocketClose( unixListenSocket,3);
            super.destroyJkComponent();
            
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /** Open a connection - since we're listening that will block in
        accept
    */
    public int open(MsgContext ep) throws IOException {
        // Will associate a jk_endpoint with ep and call open() on it.
        // jk_channel_un will accept a connection and set the socket info
        // in the endpoint. MsgContext will represent an active connection.
        return super.nativeDispatch( ep.getMsg(0), ep, CH_OPEN, 1 );
    }
    
    public void close(MsgContext ep) throws IOException {
        super.nativeDispatch( ep.getMsg(0), ep, CH_CLOSE, 1 );
    }

    public int send( Msg msg, MsgContext ep)
        throws IOException
    {
        return super.nativeDispatch( msg, ep, CH_WRITE, 0 );
    }

    public int receive( Msg msg, MsgContext ep )
        throws IOException
    {
        int rc=super.nativeDispatch( msg, ep, CH_READ, 1 );

        if( rc!=0 ) {
            log.error("receive error:   " + rc, new Throwable());
            return -1;
        }
        
        msg.processHeader();
        
        if (log.isDebugEnabled())
             log.debug("receive:  total read = " + msg.getLen());

	return msg.getLen();
    }
    
    boolean running=true;
    
    /** Accept incoming connections, dispatch to the thread pool
     */
    void acceptConnections() {
        if( apr==null ) return;

        if( log.isDebugEnabled() )
            log.debug("Accepting ajp connections on " + file);
        
        while( running ) {
            try {
                MsgContext ep=this.createMsgContext();

                // blocking - opening a server connection.
                int status=this.open(ep);
                if( status != 0 && status != 2 ) {
                    log.error( "Error acceptin connection on " + file );
                    break;
                }

                //    if( log.isDebugEnabled() )
                //     log.debug("Accepted ajp connections ");
        
                AprConnection ajpConn= new AprConnection(this, ep);
                tp.runIt( ajpConn );
            } catch( Exception ex ) {
                ex.printStackTrace();
            }
        }
    }

    /** Process a single ajp connection.
     */
    void processConnection(MsgContext ep) {
        if( log.isDebugEnabled() )
            log.debug( "New ajp connection ");
        try {
            MsgAjp recv=new MsgAjp();
            while( running ) {
                int res=this.receive( recv, ep );
                if( res<0 ) {
                    // EOS
                    break;
                }
                ep.setType(0);
                log.debug( "Process msg ");
                int status=next.invoke( recv, ep );
            }
            if( log.isDebugEnabled() )
                log.debug( "Closing un channel");
            try{
                MsgAjp endM = new MsgAjp();
                endM.reset();
                endM.appendByte((byte)HANDLE_THREAD_END);
                endM.end();
                endM.processHeader();
                next.invoke(endM, ep);
            } catch( Exception ee) {
                log.error( "Error, releasing connection",ee);
            }
            this.close( ep );
        } catch( Exception ex ) {
            ex.printStackTrace();
        }
    }

    public int invoke( Msg msg, MsgContext ep ) throws IOException {
        int type=ep.getType();

        switch( type ) {
        case JkHandler.HANDLE_RECEIVE_PACKET:
            return receive( msg, ep );
        case JkHandler.HANDLE_SEND_PACKET:
            return send( msg, ep );
        case JkHandler.HANDLE_FLUSH:
            return OK;
        }

        // return next.invoke( msg, ep );
        return OK;
    }

    private static org.apache.commons.logging.Log log=
        org.apache.commons.logging.LogFactory.getLog( ChannelUn.class );
}

class AprAcceptor implements ThreadPoolRunnable {
    ChannelUn wajp;
    
    AprAcceptor(ChannelUn wajp ) {
        this.wajp=wajp;
    }

    public Object[] getInitData() {
        return null;
    }

    public void runIt(Object thD[]) {
        wajp.acceptConnections();
    }
}

class AprConnection implements ThreadPoolRunnable {
    ChannelUn wajp;
    MsgContext ep;

    AprConnection(ChannelUn wajp, MsgContext ep) {
        this.wajp=wajp;
        this.ep=ep;
    }


    public Object[] getInitData() {
        return null;
    }
    
    public void runIt(Object perTh[]) {
        wajp.processConnection(ep);
    }
}
