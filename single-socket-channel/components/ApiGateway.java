package components;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ApiGateway {

    public static Map<String, String> registeredComponents = new ConcurrentHashMap<>();
    public static Map<String, Long> lastHeartbeats = new ConcurrentHashMap<>();


    public static void main(String[] args) throws IOException {
        String protocol = args.length > 0 ? args[0] : "tcp";

        int port = 5000;

        ServerStrategy strategy;
        switch (protocol.toLowerCase()) {
            case "udp":
                strategy = new UdpServerStrategy();
                break;
            case "http":
                strategy = new HttpServerStrategy();
                break;
            case "tcp":
            default:
                strategy = new SingleSocketChannelServer();
                break;
        }

        ServerContext context = new ServerContext(strategy, port);
        context.startServer(port);

        new Thread(() -> monitorHeartbeats()).start();


//        Component componentA = ComponentFactory.createComponent("A", protocol);
//        Component componentB = ComponentFactory.createComponent("B", protocol);
//        Component componentC = ComponentFactory.createComponent("C", protocol);

        ComponentA component = new ComponentA(protocol);
        ComponentB componentB = new ComponentB(protocol);
        componentC componentC =

        new Thread(componentA).start();
        new Thread(componentB).start();
        new Thread(componentC).start();

    }

    private static void monitorHeartbeats() {
        while (true) {
            long currentTime = System.currentTimeMillis();
            for (Map.Entry<String, Long> entry : lastHeartbeats.entrySet()) {
                if (currentTime - entry.getValue() > 100) {
                    System.out.println("[ALERTA] Componente inativo: " + entry.getKey());
                }
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}

