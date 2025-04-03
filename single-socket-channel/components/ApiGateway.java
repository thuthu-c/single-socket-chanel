package components;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

public class ApiGateway {
    public static void main(String[] args) {
        int tcpPort = 5000;
        int udpPort = 5001;
        int httpPort = 8080;

        new Thread(() -> startTCPServer(tcpPort)).start();
        new Thread(() -> startUDPServer(udpPort)).start();
        new Thread(() -> startHTTPServer(httpPort)).start();
    }

    private static void startTCPServer(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("API Gateway TCP rodando na porta " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void startUDPServer(int port) {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            byte[] buffer = new byte[1024];
            System.out.println("API Gateway UDP rodando na porta " + port);
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Recebido via UDP: " + received);

                String response = "Mensagem recebida pelo API Gateway via UDP";
                byte[] responseData = response.getBytes();
                DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, packet.getAddress(), packet.getPort());
                socket.send(responsePacket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void startHTTPServer(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("API Gateway HTTP rodando na porta " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleHTTPRequest(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleHTTPRequest(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String requestLine = in.readLine();
            System.out.println("Recebido via HTTP: " + requestLine);

            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: text/plain");
            out.println("\r\n");
            out.println("Mensagem recebida pelo API Gateway via HTTP");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

