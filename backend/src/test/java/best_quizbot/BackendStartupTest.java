package best_quizbot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BackendStartupTest {
    @Test
    void startsAndAcceptsQuizRequests() throws Exception {
        try (var context = SpringApplication.run(BackendApplicationStarter.class, "--server.port=0")) {
            int port = ((ServletWebServerApplicationContext) context).getWebServer().getPort();
            HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/start"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{\"telegramId\":12345,\"questionCount\":1}"))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());

            HttpRequest questionRequest = HttpRequest.newBuilder(
                    URI.create("http://localhost:" + port + "/api/current_question?telegramId=12345"))
                    .GET()
                    .build();
            assertEquals(200, HttpClient.newHttpClient().send(questionRequest, HttpResponse.BodyHandlers.ofString()).statusCode());
        }
    }
}