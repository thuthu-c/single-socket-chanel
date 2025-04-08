package components;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServerStrategy implements ServerStrategy {
    @Override
    public void startServer(int port) {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("Servidor HTTP rodando na porta " + port);
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    OutputStream out = clientSocket.getOutputStream();

                    StringBuilder requestBuilder = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null && !line.isEmpty()) {
                        requestBuilder.append(line).append("\n");
                    }
                    String request = requestBuilder.toString();

                    if (request.contains("REGISTER:")) {
                        int idx = request.indexOf("REGISTER:");
                        String componentName = request.substring(idx + 9).split("\\s")[0];
                        ApiGateway.registeredComponents.put(componentName, clientSocket.getRemoteSocketAddress().toString());
                        System.out.println("Registrado componente: " + componentName);
                    }else if (request.contains("HEARTBEAT:")) {
                        int idx = request.indexOf("HEARTBEAT:");
                        String componentName = request.substring(idx + 10).split("\\s")[0];
                        ApiGateway.lastHeartbeats.put(componentName, System.currentTimeMillis());
                        System.out.println("Heartbeat de: " + componentName);
                    } else {
                        System.out.println("[HTTP] " + request);
                    }

                    String httpResponse = "HTTP/1.1 200 OK\r\n\r\nResposta do Servidor HTTP";
                    out.write(httpResponse.getBytes("UTF-8"));
                    out.flush();
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
