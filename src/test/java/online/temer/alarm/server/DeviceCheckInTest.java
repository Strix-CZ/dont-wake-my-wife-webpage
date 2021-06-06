package online.temer.alarm.server;

import online.temer.alarm.db.DbTestExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@ExtendWith(DbTestExtension.class)
public class DeviceCheckInTest {

    private Server server;

    @BeforeEach
    void setUp() {
        server = new Server(8765, "localhost");
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    public void serverListens() throws IOException, InterruptedException {

        var request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8765/checkin"))
                .build();

        HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString()); // Throws in case of time-out
    }
}
