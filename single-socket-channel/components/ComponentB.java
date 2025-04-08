package components;


//class ComponentB extends Component {
//    public ComponentB(String protocol) {
//        super("Componente B", protocol);
//    }
//
//    @Override
//    public void execute() {
//        sendMessage(name + " reporta status: OK");
//    }
//}

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ComponentB {
    private static final String COMPONENT_NAME = "ComponenteB"; // troque para B ou C conforme necessário
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 6000;
    private static final int HEARTBEAT_INTERVAL = 3000;


    public ComponentB(String COMPONENT_NAME) {
        super();
    }

    public static void main(String[] args) {
        try {
            SocketChannel client = SocketChannel.open();
            client.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));

            sendMessage(client, "register:" + COMPONENT_NAME);

            while (true) {
                sendMessage(client, "heartbeat:" + COMPONENT_NAME);
                Thread.sleep(HEARTBEAT_INTERVAL);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void sendMessage(SocketChannel client, String message) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
        client.write(buffer);
        buffer.clear();
        System.out.println("Enviado: " + message);
    }
}


