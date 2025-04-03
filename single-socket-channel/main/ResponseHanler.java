package main;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

class ResponseHandler extends AbstractRequestHandler {
    @Override
    public void handleRequest(SocketChannel clientChannel, String response) throws IOException {
        System.out.println("[Pipeline] Resposta: " + response);
        ByteBuffer buffer = ByteBuffer.wrap(response.getBytes());
        clientChannel.write(buffer);
    }
}
