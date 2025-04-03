package main;


import java.io.IOException;
import java.nio.channels.SocketChannel;

class TransformationHandler extends AbstractRequestHandler {
    @Override
    public void handleRequest(SocketChannel clientChannel, String request) throws IOException {
        System.out.println("[Pipeline] Transformação: " + request);
        forwardRequest(clientChannel, request.toUpperCase());
    }
}
