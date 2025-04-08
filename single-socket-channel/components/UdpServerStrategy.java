package components;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UdpServerStrategy implements ServerStrategy {
    @Override
    public void startServer(int port) {
        new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket(port)) {
                System.out.println("Servidor UDP rodando na porta " + port);
                byte[] buffer = new byte[1024];
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String msg = new String(packet.getData(), 0, packet.getLength());
                    if (msg.startsWith("REGISTER:")) {
                        String componentName = msg.substring("REGISTER:".length());
                        ApiGateway.registeredComponents.put(componentName, packet.getAddress().getHostAddress() + ":" + packet.getPort());
                        System.out.println("Registrado componente: " + componentName);
                    }  else if (msg.startsWith("HEARTBEAT:")) {
                        String componentName = msg.substring("HEARTBEAT:".length());
                        ApiGateway.lastHeartbeats.put(componentName, System.currentTimeMillis());
                        System.out.println("Heartbeat de: " + componentName);
                    } else {
                        System.out.println("[UDP] Mensagem recebida: " + msg);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
