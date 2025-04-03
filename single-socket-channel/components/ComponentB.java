package components;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

class ComponentB {
    public static void main(String[] args) {
        String serverAddress = "localhost";
        int port = 5001;
        try (DatagramSocket socket = new DatagramSocket()) {
            byte[] sendData = "Mensagem do Componente B via UDP".getBytes();
            InetAddress address = InetAddress.getByName(serverAddress);
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
            socket.send(sendPacket);

            byte[] receiveBuffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(receivePacket);
            String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("Resposta: " + response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}