package main;

import java.io.IOException;
import java.nio.channels.SocketChannel;

interface RequestHandler {
    void setNextHandler(RequestHandler nextHandler);
    void handleRequest(SocketChannel clientChannel, String request) throws IOException;
}
