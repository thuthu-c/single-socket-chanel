package components;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.*;

public class SingleSocketChannelServer implements ServerStrategy {
    private static final int PORT = 6000;
    private static final int BUFFER_SIZE = 1024;
    private static final int HEARTBEAT_TIMEOUT = 10000;
    private static final int HEARTBEAT_CHECK_INTERVAL = 5000;

    private Selector selector;
    private ServerSocketChannel serverChannel;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(4);
    private final BlockingQueue<Runnable> requestQueue = new LinkedBlockingQueue<>();
    private final ConcurrentHashMap<SocketChannel, Long> clientLastSeen = new ConcurrentHashMap<>();

    @Override
    public void startServer(int PORT) {
        try {
            selector = Selector.open();
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.bind(new InetSocketAddress(PORT));
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            new Thread(this::startHeartbeatMonitor).start();
            new Thread(this::processQueue).start();

            System.out.println("[Gateway] SingleSocketChannel rodando na porta " + PORT);

            while (true) {
                selector.select();
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (key.isAcceptable()) {
                        acceptConnection(serverChannel);
                    } else if (key.isReadable()) {
                        requestQueue.offer(() -> handleRequest(key));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void acceptConnection(ServerSocketChannel server) {
        try {
            SocketChannel client = server.accept();
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
            clientLastSeen.put(client, System.currentTimeMillis());
            System.out.println("[Gateway] Nova conexão: " + client.getRemoteAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRequest(SelectionKey key) {
        threadPool.submit(() -> {
            SocketChannel client = (SocketChannel) key.channel();
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            try {
                int bytesRead = client.read(buffer);
                if (bytesRead == -1) {
                    client.close();
                    clientLastSeen.remove(client);
                    return;
                }
                buffer.flip();
                String message = new String(buffer.array(), 0, buffer.limit());

                clientLastSeen.put(client, System.currentTimeMillis());
                processMessage(client, message);

            } catch (IOException e) {
                try {
                    client.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                clientLastSeen.remove(client);
            }
        });
    }

    private void processMessage(SocketChannel client, String message) throws IOException {
        String[] parts = message.trim().split(":", 2);
        if (parts.length == 2) {
            String type = parts[0];
            String content = parts[1];

            if ("register".equalsIgnoreCase(type)) {
                ApiGateway.registeredComponents.put(content, client.getRemoteAddress().toString());
                System.out.println("[Gateway] Registrado: " + content);
            } else if ("heartbeat".equalsIgnoreCase(type)) {
                ApiGateway.lastHeartbeats.put(content, System.currentTimeMillis());
                System.out.println("[Gateway] Heartbeat de " + content);
            } else {
                System.out.println("[Gateway] Requisição desconhecida: " + message);
            }
        } else {
            System.out.println("[Gateway] Mensagem inválida: " + message);
        }
    }

    private void startHeartbeatMonitor() {
        while (true) {
            long now = System.currentTimeMillis();
            clientLastSeen.forEach((client, lastSeen) -> {
                if (now - lastSeen > HEARTBEAT_TIMEOUT) {
                    try {
                        System.out.println("[Gateway] Timeout do cliente: " + client.getRemoteAddress());
                        client.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    clientLastSeen.remove(client);
                }
            });
            try {
                Thread.sleep(HEARTBEAT_CHECK_INTERVAL);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void processQueue() {
        while (true) {
            try {
                Runnable task = requestQueue.take();
                task.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}

