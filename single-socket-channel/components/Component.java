package components;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

abstract class Component implements Runnable {
    protected final String name;
    protected final String protocol;

    public Component(String name, String protocol) {
        this.name = name;
        this.protocol = protocol;
    }

    public abstract void execute();

    @Override
    public void run() {
        System.out.println(name + " iniciado usando protocolo: " + protocol);
        register();
        new Thread(() -> sendHeartbeats()).start();
        execute();
    }

    private void register() {
        sendMessage("REGISTER:" + name);
    }

    private void sendHeartbeats() {
        while (true) {
            sendMessage("HEARTBEAT:" + name);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected void sendMessage(String message){
        try {
            switch (protocol.toLowerCase()) {
                case "tcp":
                    Socket tcpSocket = new Socket("localhost", 5000);
                    PrintWriter tcpOut = new PrintWriter(tcpSocket.getOutputStream(), true);
                    tcpOut.println(message);
                    tcpSocket.close();
                    break;
                case "udp":
                    DatagramSocket udpSocket = new DatagramSocket();
                    byte[] msg = message.getBytes();
                    DatagramPacket packet = new DatagramPacket(msg, msg.length, InetAddress.getByName("localhost"), 5001);
                    udpSocket.send(packet);
                    udpSocket.close();
                    break;
                case "http":
                    Socket httpSocket = new Socket("localhost", 5002);
                    PrintWriter httpOut = new PrintWriter(httpSocket.getOutputStream(), true);
                    httpOut.println("GET / HTTP/1.1");
                    httpOut.println("Host: localhost");
                    httpOut.println(message);
                    httpOut.println("");
                    httpSocket.close();
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

