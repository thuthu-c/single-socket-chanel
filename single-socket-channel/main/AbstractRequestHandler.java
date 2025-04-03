package main;

import java.io.IOException;
import java.nio.channels.SocketChannel;

abstract class AbstractRequestHandler implements RequestHandler {
    protected RequestHandler nextHandler;

    @Override
    public void setNextHandler(RequestHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    protected void forwardRequest(SocketChannel clientChannel, String request) throws IOException {
        if (nextHandler != null) {
            nextHandler.handleRequest(clientChannel, request);
        }
    }
}
