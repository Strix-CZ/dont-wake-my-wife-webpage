package online.temer.alarm.server;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpServerExchange;

public class Server {

    private final Undertow server;

    public Server(int port, String host) {
        server = createServer(port, host);
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop();
    }

    private Undertow createServer(int port, String host) {
        return Undertow.builder()
                .addHttpListener(port, host)
                .setHandler(Handlers.path().addExactPath("/checkin", this::handleCheckIn))
                .build();
    }

    private void handleCheckIn(HttpServerExchange httpServerExchange) {

    }


}
