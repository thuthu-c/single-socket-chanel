package components;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServerStrategy implements ServerStrategy {
    @Override
    public void startServer(int port) {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("Servidor TCP rodando na porta " + port);
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String msg = in.readLine();
                    if (msg != null && msg.startsWith("REGISTER:")) {
                        String componentName = msg.substring("REGISTER:".length());
                        ApiGateway.registeredComponents.put(componentName, clientSocket.getRemoteSocketAddress().toString());
                        System.out.println("Registrado componente: " + componentName);
                    } else if (msg.startsWith("HEARTBEAT:")) {
                        String componentName = msg.substring("HEARTBEAT:".length());
                        ApiGateway.lastHeartbeats.put(componentName, System.currentTimeMillis());
                        System.out.println("Heartbeat de: " + componentName);
                    } else {
                        System.out.println("[TCP] Mensagem recebida: " + msg);
                    }
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}

