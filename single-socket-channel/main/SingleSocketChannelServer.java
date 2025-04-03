package main;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.*;

public class SingleSocketChannelServer {
    private static final int PORT = 6000;
    private static final int BUFFER_SIZE = 1024;
    private static final int HEARTBEAT_INTERVAL = 5000; // 5 segundos
    private static final int HEARTBEAT_TIMEOUT = 15000; // 15 segundos

    private Selector selector;
    private ExecutorService threadPool;
    private ConcurrentHashMap<SocketChannel, Long> clientLastResponse;
    private BlockingQueue<Runnable> eventQueue;
    private RequestHandler requestPipeline;

    public SingleSocketChannelServer() throws IOException {
        selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(PORT));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        threadPool = Executors.newFixedThreadPool(4); // Leaders and Followers
        clientLastResponse = new ConcurrentHashMap<>();
        eventQueue = new LinkedBlockingQueue<>(); // Singular Update Queue
        startHeartbeatMonitor();
        startEventProcessor();
        setupRequestPipeline();
    }

    public void start() throws IOException {
        System.out.println("Servidor rodando na porta " + PORT);
        while (true) {
            selector.select();
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                if (key.isAcceptable()) {
                    acceptConnection((ServerSocketChannel) key.channel());
                } else if (key.isReadable()) {
                    enqueueRequest(key);
                }
            }
        }
    }

    private void acceptConnection(ServerSocketChannel serverChannel) throws IOException {
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
        clientLastResponse.put(clientChannel, System.currentTimeMillis());
        System.out.println("Nova conexão aceita: " + clientChannel.getRemoteAddress());
    }

    private void enqueueRequest(SelectionKey key) {
        eventQueue.offer(() -> processRequestPipeline(key));
    }

    private void startEventProcessor() {
        Thread eventThread = new Thread(() -> {
            while (true) {
                try {
                    Runnable task = eventQueue.take();
                    task.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        eventThread.start();
    }

    private void processRequestPipeline(SelectionKey key) {
        threadPool.submit(() -> {
            try {
                SocketChannel clientChannel = (SocketChannel) key.channel();
                ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                int bytesRead = clientChannel.read(buffer);
                if (bytesRead == -1) {
                    clientLastResponse.remove(clientChannel);
                    clientChannel.close();
                    System.out.println("Conexão encerrada");
                    return;
                }
                buffer.flip();
                String request = new String(buffer.array(), 0, bytesRead);
                System.out.println("Recebido: " + request);
                clientLastResponse.put(clientChannel, System.currentTimeMillis());

                requestPipeline.handleRequest(clientChannel, request);

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void setupRequestPipeline() {
        RequestHandler validationHandler = new ValidationHandler();
        RequestHandler transformationHandler = new TransformationHandler();
        RequestHandler responseHandler = new ResponseHandler();

        validationHandler.setNextHandler(transformationHandler);
        transformationHandler.setNextHandler(responseHandler);

        this.requestPipeline = validationHandler;
    }

    private void startHeartbeatMonitor() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();
            for (Map.Entry<SocketChannel, Long> entry : clientLastResponse.entrySet()) {
                if (currentTime - entry.getValue() > HEARTBEAT_TIMEOUT) {
                    try {
                        System.out.println("Cliente desconectado por timeout: " + entry.getKey().getRemoteAddress());
                        entry.getKey().close();
                        clientLastResponse.remove(entry.getKey());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS);
    }

    public static void main(String[] args) {
        try {
            new SingleSocketChannelServer().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}