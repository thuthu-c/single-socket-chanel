package components;

public class ServerContext {
    private final ServerStrategy strategy;
    private int port=5000;

    public ServerContext(ServerStrategy strategy, int port) {
        this.strategy = strategy;
        this.port = port;

    }

    public void startServer(int port) {
        System.out.println("Iniciando servidor na porta" + port +" ...");
        strategy.startServer(port);
    }
}
