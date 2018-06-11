import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class test_1 {
    public test_1() {
        scrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        receiveTA.setLineWrap(true);
        System.setErr(new PrintStream(new OutputStream() {
            public void write(int b){}
            public void write(byte[] bytes,int a,int b) {
                if(a<b-10) receiveTA.append("ERROR: "+new String(bytes,a,b)+"\n");
            }
        }));
        inputButton.addActionListener(e -> {
            DatagramPacket packet;
            DatagramSocket sendSocket;
            byte[] buf = new byte[64];
            try {
                InetAddress ip = InetAddress.getByName(ipaddrTF.getText());
                int port = Integer.parseInt(hostTF.getText());
                sendSocket = new DatagramSocket();
                buf[0]=1;
                packet = new DatagramPacket(buf, buf.length, ip, port);
                sendSocket.send(packet);
                buf = sendTF.getText().getBytes();
                packet = new DatagramPacket(buf, buf.length, ip, port);
                sendSocket.send(packet);
                receiveTA.append("SEND:\n  " + sendTF.getText() + "\n");
                sendSocket.close();
            } catch (UnknownHostException ee) {
                System.err.println("Ip not found or misspell.");
            } catch (NumberFormatException ee) {
                System.err.println("Port number misspell.");
            } catch (SocketException ee) {
                System.err.println("Can't start socket.");
            } catch (Exception ee) {
                ee.printStackTrace();
            }
            sendTF.setText("");
        });
        startButton.addActionListener(e -> new Thread() {
            DatagramSocket socket = null;
            int p = Integer.parseInt(myhostTF.getText());
            public void run() {
                try {
                    socket = new DatagramSocket(p);
                } catch (SocketException e) {
                    System.err.println("Can't listen to socket.");
                    return;
                }
                while (true) try {
                    byte[] buf = new byte[256];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);
                    if(packet.getData()[0]==1) {        //接受消息
                        socket.receive(packet);
                        String dString = new String(packet.getData());
                        receiveTA.append("RECEIVE :\n  " + dString + "\n");
                    }else if(packet.getData()[0]==2) {  //接受文件
                        receiveTA.append("Prepare to receive file.\n");
                        String fileTo = JOptionPane.showInputDialog("Please input restore position.");
                        File file = new File(fileTo);
                        FileOutputStream fStream = new FileOutputStream(file);
                        while(true){
                            socket.receive(packet);
                            if(packet.getData()[0]==126 && packet.getData()[63]==126){
                                break;
                            }
                            fStream.write(packet.getData(),0,packet.getLength());
                        }
                        fStream.close();
                        receiveTA.append("File has been successfully received in "+ fileTo +" \n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Socket has closed.");
                    break;
                }
                socket.close();
            }
        }.start());
        sendButton.addActionListener(e -> {
            DatagramPacket packet;
            DatagramSocket sendSocket;
            byte[] buf = new byte[256];
            try {
                InetAddress ip = InetAddress.getByName(ipaddrTF.getText());
                int port = Integer.parseInt(hostTF.getText());
                sendSocket = new DatagramSocket();
                buf[0]=2;
                packet = new DatagramPacket(buf, buf.length, ip, port);
                sendSocket.send(packet);
                receiveTA.append("Preparing to send file.\n");

                File file = new File(sendTF.getText());
                if(file.exists()) {
                    ByteArrayOutputStream oStream = new ByteArrayOutputStream();
                    DataOutputStream dataStream = new DataOutputStream(oStream);
                    FileInputStream fStream = new FileInputStream(file);
                    int length;
                    while((length = fStream.read(buf, 0, buf.length))!=-1) {
                        //  dataStream.write(buf,0,length);
                        packet = new DatagramPacket(buf, length, ip, port);
                        sendSocket.send(packet);
                    }
                    buf[0]=buf[63]=126;
                    packet = new DatagramPacket(buf, buf.length, ip, port);
                    sendSocket.send(packet);

                    receiveTA.append("File " + sendTF.getText() + " has been successfully sent. \n");

                    //dataStream.close();
                    //buf = oStream.toByteArray();
                }
                else{
                    receiveTA.append("File not exists or has no permission. Please check your input.\n");
                }
                sendSocket.close();
            } catch (UnknownHostException ee) {
                System.err.println("Ip not found or misspell.");
            } catch (NumberFormatException ee) {
                System.err.println("Port number misspell.");
            } catch (SocketException ee) {
                System.err.println("Can't start socket.");
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        });
        ipaddrTF.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                ipaddrTF.selectAll();
                super.focusGained(e);
            }
        });
    }
    private JButton inputButton;
    private JTextArea receiveTA;
    private JTextField sendTF;
    private JPanel mainPanel;
    private JTextField hostTF;
    private JTextField ipaddrTF;
    private JTextField myhostTF;
    private JButton startButton;
    private JButton sendButton;
    private JScrollPane scrollPane;

    public static void main(String[] args) {
        JFrame frame = new JFrame("test_1");
        frame.setContentPane(new test_1().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}