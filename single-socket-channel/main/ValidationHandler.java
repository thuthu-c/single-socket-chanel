package main;

import java.io.IOException;
import java.nio.channels.SocketChannel;

class ValidationHandler extends AbstractRequestHandler {
    @Override
    public void handleRequest(SocketChannel clientChannel, String request) throws IOException {
        System.out.println("[Pipeline] Validação: " + request);
        forwardRequest(clientChannel, request.trim());
    }
}
