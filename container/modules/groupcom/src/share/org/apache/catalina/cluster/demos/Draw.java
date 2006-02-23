package org.apache.catalina.cluster.demos;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Random;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.catalina.cluster.ClusterMessage;
import org.apache.catalina.cluster.Member;
import org.apache.catalina.cluster.group.ChannelInterceptorBase;
import org.apache.catalina.cluster.group.GroupChannel;
import org.apache.catalina.cluster.mcast.McastService;
import org.apache.catalina.cluster.tcp.ReplicationListener;
import org.apache.catalina.cluster.tcp.ReplicationTransmitter;
import org.apache.commons.logging.impl.LogFactoryImpl;
/**
 * Shared whiteboard, each new instance joins the same group. Each instance chooses a random color,
 * mouse moves are broadcast to all group members, which then apply them to their canvas<p>
 * @author Bela Ban, Oct 17 2001
 */
public class Draw extends ChannelInterceptorBase implements ActionListener {
    static LogFactoryImpl dependencyhack = new LogFactoryImpl();
    static org.apache.commons.logging.impl.SimpleLog depHack2 = new org.apache.commons.logging.impl.SimpleLog("test");
    
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    String groupname = "DrawGroupDemo";
    private GroupChannel channel = null;
    private int member_size = 1;
    final boolean first = true;
    final boolean cummulative = true;
    private JFrame mainFrame = null;
    private JPanel sub_panel = null;
    private DrawPanel panel = null;
    private JButton clear_button, leave_button;
    private final Random random = new Random(System.currentTimeMillis());
    private final Font default_font = new Font("Helvetica", Font.PLAIN, 12);
    private final Color draw_color = selectColor();
    private final Color background_color = Color.white;
    boolean no_channel = false;
    boolean jmx;

    public Draw(int port, boolean debug, boolean cummulative, boolean no_channel, boolean jmx) throws Exception {
        this.no_channel = no_channel;
        this.jmx = jmx;
        if (no_channel)
            return;

        ReplicationListener rl = new ReplicationListener();
        rl.setTcpListenAddress("auto");
        rl.setTcpListenPort(port);
        rl.setTcpSelectorTimeout(100);
        rl.setTcpThreadCount(4);
        rl.getBind();
        
        ReplicationTransmitter ps = new ReplicationTransmitter();
        ps.setReplicationMode("pooled");
        ps.setAckTimeout(15000);
        ps.setAutoConnect(true);
        ps.setCompress(false);
        
        McastService service = new McastService();
        service.setMcastAddr("228.0.0.5");
        service.setMcastFrequency(500);
        service.setMcastDropTime(5000);
        service.setMcastPort(45565);
        service.setLocalMemberProperties(rl.getHost(),port);
        
        channel = new GroupChannel();
        channel.setClusterReceiver(rl);
        channel.setClusterSender(ps);
        channel.setMembershipService(service);
        channel.start(channel.DEFAULT);
        
        channel.addInterceptor(this);

        if (debug) {
        }
    }

    public String getGroupName() {
        return groupname;
    }

    public void setGroupName(String groupname) {
        if (groupname != null)
            this.groupname = groupname;
    }

    public static void main(String[] args) {
        Draw draw = null;
        int port = 4000;
        boolean debug = false;
        boolean cummulative = false;
        boolean no_channel = false;
        boolean jmx = false;
        String group_name = null;

        for (int i = 0; i < args.length; i++) {
            if ("-help".equals(args[i])) {
                help();
                return;
            }
            if ("-debug".equals(args[i])) {
                debug = true;
                continue;
            }
            if ("-cummulative".equals(args[i])) {
                cummulative = true;
                continue;
            }
            if ("-port".equals(args[i])) {
                port = Integer.parseInt(args[++i]);
                continue;
            }
            if ("-no_channel".equals(args[i])) {
                no_channel = true;
                continue;
            }
            if ("-jmx".equals(args[i])) {
                jmx = true;
                continue;
            }
            if ("-groupname".equals(args[i])) {
                group_name = args[++i];
                continue;
            }

            help();
            return;
        }

        try {
            draw = new Draw(port, debug, cummulative, no_channel, jmx);
            if (group_name != null)
                draw.setGroupName(group_name);
            draw.go();
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    static void help() {
        System.out.println("\nDraw [-help] [-debug] [-cummulative] [-no_channel] [-props <protocol stack definition>]" +
                           " [-groupname <name>]");
        System.out.println("-debug: brings up a visual debugger");
        System.out.println("-no_channel: doesn't use JGroups at all, any drawing will be relected on the " +
                           "whiteboard directly");
        System.out.println("-props: argument can be an old-style protocol stack specification, or it can be " +
                           "a URL. In the latter case, the protocol specification will be read from the URL\n");
    }

    private Color selectColor() {
        int red = (Math.abs(random.nextInt()) % 255);
        int green = (Math.abs(random.nextInt()) % 255);
        int blue = (Math.abs(random.nextInt()) % 255);
        return new Color(red, green, blue);
    }

    public void go() throws Exception {
        mainFrame = new JFrame();
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        panel = new DrawPanel(this);
        panel.setBackground(background_color);
        sub_panel = new JPanel();
        mainFrame.getContentPane().add("Center", panel);
        clear_button = new JButton("Clear");
        clear_button.setFont(default_font);
        clear_button.addActionListener(this);
        leave_button = new JButton("Leave & Exit");
        leave_button.setFont(default_font);
        leave_button.addActionListener(this);
        sub_panel.add("South", clear_button);
        sub_panel.add("South", leave_button);
        mainFrame.getContentPane().add("South", sub_panel);
        mainFrame.setBackground(background_color);
        clear_button.setForeground(Color.blue);
        leave_button.setForeground(Color.blue);
        setTitle();
        mainFrame.pack();
        mainFrame.setLocation(15, 25);
        mainFrame.setBounds(new Rectangle(250, 250));
        mainFrame.setVisible(true);
        
    }

    void setTitle(String title) {
        String tmp = "";
        if (no_channel) {
            mainFrame.setTitle(" Draw Demo ");
            return;
        }
        if (title != null) {
            mainFrame.setTitle(title);
        } else {
            if (channel.getMembershipService().getLocalMember() != null)
                tmp += channel.getMembershipService().getLocalMember().getName();
            tmp += " (" + channel.getMembershipService().getMembers().length + ")";
            mainFrame.setTitle(tmp);
        }
    }

    void setTitle() {
        setTitle(null);
    }

    

    /* --------------- Callbacks --------------- */



    public void clearPanel() {
        if (panel != null)
            panel.clear();
    }
    
    public DrawMessage getEmptyMessage() {
        DrawMessage msg = new DrawMessage();
        msg.setAddress(channel.getMembershipService().getLocalMember());
        msg.setTimestamp(System.currentTimeMillis());
        msg.setCompress(0);
        return msg;
    }

    public void sendClearPanelMsg() {
        int tmp[] = new int[1];
        tmp[0] = 0;
        DrawCommand comm = new DrawCommand(DrawCommand.CLEAR);
        ObjectOutputStream os;

        try {
            out.reset();
            os = new ObjectOutputStream(out);
            os.writeObject(comm);
            os.flush();
            DrawMessage msg = this.getEmptyMessage();
            msg.setDrawCommand(comm);
            channel.send(null,msg,0);
            //draw on the local picture
            messageReceived(msg);
        } catch (Exception ex) {
            System.err.println(ex);
        }
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if ("Clear".equals(command)) {
            if (no_channel) {
                clearPanel();
                return;
            }
            sendClearPanelMsg();
        } else if ("Leave & Exit".equals(command)) {
            if (!no_channel) {
                try {
                    channel.stop(channel.DEFAULT);
                } catch (Exception ex) {
                    System.err.println(ex);
                }
            }
            mainFrame.setVisible(false);
            mainFrame.dispose();
            System.exit(0);
        } else
            System.out.println("Unknown action");
    }

    /* ------------------------------ ChannelListener interface -------------------------- */

    

    public void memberAdded(Member addr) {
        setTitle();
    }

    public void memberDisappeared(Member addr) {
        setTitle();
    }
    
    public void messageReceived(ClusterMessage msg) { 
        if ( msg instanceof DrawMessage ) {
            DrawMessage dmsg = (DrawMessage)msg;
            DrawCommand comm = dmsg.getDrawCommand();
            switch (comm.mode) {
                case DrawCommand.DRAW:
                    if (panel != null)
                        panel.drawPoint(comm);
                    break;
                case DrawCommand.CLEAR:
                    clearPanel();
                    break;
                default:
                    System.err.println("***** Draw.run(): received invalid draw command " + comm.mode);
                    break;
            }
        } else {
            System.out.println("Invalid message="+msg);
        }
    }
    
    
    public static class DrawMessage implements ClusterMessage {
        private Member address;
        private long timestamp;
        private String id;
        private int resend;
        private int compress;
        private DrawCommand comm;
        public Member getAddress() { return address;}
        public void setAddress(Member member) { address = member;}
        public long getTimestamp() { return timestamp;}
        public void setTimestamp(long timestamp) {this.timestamp = timestamp;}
        public String getUniqueId() {return id;}
        public int getResend() { return resend;}
        public void setResend(int resend) {this.resend = resend;}
        public int getCompress() {return compress;}
        public void setCompress(int compress) {this.compress = compress;}
        public DrawCommand getDrawCommand(){return comm;}
        public void setDrawCommand(DrawCommand command) {this.comm = command;}

    }
    /* --------------------------- End of ChannelListener interface ---------------------- */



    private class DrawPanel
        extends JPanel implements MouseMotionListener {
        final Dimension preferred_size = new Dimension(235, 170);
        Image img = null; // for drawing pixels
        Dimension d, imgsize;
        Graphics gr = null;
        Draw parent;
        public DrawPanel(Draw parent) {
            this.parent = parent;
            createOffscreenImage();
            addMouseMotionListener(this);
            addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent e) {
                    if (getWidth() <= 0 || getHeight() <= 0)return;
                    createOffscreenImage();
                }
            });
        }

        void createOffscreenImage() {
            d = getSize();
            if (img == null || imgsize == null || imgsize.width != d.width || imgsize.height != d.height) {
                img = createImage(d.width, d.height);
                if (img != null)
                    gr = img.getGraphics();
                imgsize = d;
            }
        }

        /* ---------------------- MouseMotionListener interface------------------------- */

        public void mouseMoved(MouseEvent e) {}

        public void mouseDragged(MouseEvent e) {
            ObjectOutputStream os;
            int x = e.getX(), y = e.getY();
            DrawCommand comm = new DrawCommand(DrawCommand.DRAW, x, y,
                                               draw_color.getRed(), draw_color.getGreen(), draw_color.getBlue());

            if (no_channel) {
                drawPoint(comm);
                return;
            }

            try {
                out.reset();
                os = new ObjectOutputStream(out);
                os.writeObject(comm);
                os.flush();
                DrawMessage msg = parent.getEmptyMessage();
                msg.setDrawCommand(comm);
                channel.send(null,msg,0);
                parent.messageReceived(msg);
                Thread.yield(); // gives the repainter some breath
            } catch (Exception ex) {
                System.err.println(ex);
            }
        }

        /* ------------------- End of MouseMotionListener interface --------------------- */


        /**
         * Adds pixel to queue and calls repaint() whenever we have MAX_ITEMS pixels in the queue
         * or when MAX_TIME msecs have elapsed (whichever comes first). The advantage compared to just calling
         * repaint() after adding a pixel to the queue is that repaint() can most often draw multiple points
         * at the same time.
         */
        public void drawPoint(DrawCommand c) {
            if (c == null || gr == null)return;
            gr.setColor(new Color(c.r, c.g, c.b));
            gr.fillOval(c.x, c.y, 10, 10);
            repaint();
        }

        public void clear() {
            if (gr == null)return;
            gr.clearRect(0, 0, getSize().width, getSize().height);
            repaint();
        }

        public Dimension getPreferredSize() {
            return preferred_size;
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img != null) {
                g.drawImage(img, 0, 0, null);
            }
        }

    }

    public static class DrawCommand
        implements Serializable {
        static final int DRAW = 1;
        static final int CLEAR = 2;
        final int mode;
        int x = 0;
        int y = 0;
        int r = 0;
        int g = 0;
        int b = 0;

        DrawCommand(int mode) {
            this.mode = mode;
        }

        DrawCommand(int mode, int x, int y, int r, int g, int b) {
            this.mode = mode;
            this.x = x;
            this.y = y;
            this.r = r;
            this.g = g;
            this.b = b;
        }

        DrawCommand Copy() {
            return new DrawCommand(mode, x, y, r, g, b);
        }

        public String toString() {
            StringBuffer ret = new StringBuffer();
            switch (mode) {
                case DRAW:
                    ret.append("DRAW(" + x + ", " + y + ") [" + r + '|' + g + '|' + b + ']');
                    break;
                case CLEAR:
                    ret.append("CLEAR");
                    break;
                default:
                    return "<undefined>";
            }
            return ret.toString();
        }

    }

}

