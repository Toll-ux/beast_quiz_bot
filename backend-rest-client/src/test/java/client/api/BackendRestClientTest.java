package client.api;

import client.api.dto.request.AnswerQuestionRequest;
import client.api.dto.response.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class BackendRestClientTest {

    private MockWebServer server;
    private BackendRestClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();

        client = new BackendRestClient(
                server.url("/").toString()
        );
    }
    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    private MockResponse jsonResponse(Object body)
            throws JsonProcessingException {

        return new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(mapper.writeValueAsString(body));
    }

    private RecordedRequest takeRequest(
            String method,
            String path
    ) throws InterruptedException {

        RecordedRequest request = server.takeRequest();

        assertEquals(method, request.getMethod());
        assertEquals(
                path,
                request.getRequestUrl().encodedPath()
        );

        return request;
    }

    private JsonNode readBody(RecordedRequest request) throws IOException {
        return mapper.readTree(
                request.getBody().readUtf8()
        );
    }

    // StartQuiz тест
    @Test
    void startQuiz_shouldSendPostRequest() throws Exception {

        server.enqueue(jsonResponse(new StartQuizResponse(true)));

        StartQuizResponse response = client.startQuiz(4135413L, 10);

        assertNotNull(response);

        RecordedRequest request = takeRequest("POST", "/api/start");

        assertTrue(
                request.getHeader("Content-Type")
                        .contains("application/json")
        );

        JsonNode body = readBody(request);

        assertEquals(4135413L, body.get("telegramId").asLong());
        assertEquals(10, body.get("questionCount").asInt());
    }

    // AnswerQuestion тест
    @Test
    void answerQuestion_shouldSendCorrectRequest()
            throws Exception {

        server.enqueue(jsonResponse(
                new AnswerQuestionResponse(
                        true,
                        List.of(1, 3, 5),
                        44,
                        true
                )
        ));

        AnswerQuestionResponse response =
                client.answerQuestion(
                        12345L,
                        2,
                        Set.of(1, 3)
                );

        assertNotNull(response);

        RecordedRequest request =
                takeRequest("POST", "/api/answer");

        JsonNode body = readBody(request);

        assertEquals(12345L, body.get("telegramId").asLong());
        assertEquals(2, body.get("questionId").asInt());

        Set<Integer> chosen = new HashSet<>();

        body.get("chosen").forEach(
                value -> chosen.add(value.asInt())
        );

        assertEquals(Set.of(1, 3), chosen);
    }

    // GetLeaderboard

    @Test
    void getLeaderboard_shouldSendGetRequest()
            throws Exception {

        server.enqueue(jsonResponse(
           new GetLeaderboardResponse(
                   List.of(
                           new LeaderboardResponseEntry(1, 120, "Alice")
                   )
           )
        ));

        GetLeaderboardResponse response =
                client.getLeaderboard(12345L);

        assertNotNull(response);

        RecordedRequest request =
                takeRequest("GET", "/api/leaderboard");

        assertEquals(
                "12345",
                request.getRequestUrl()
                        .queryParameter("telegramId")
        );

        assertEquals(0, request.getBody().size());
    }

    // GetCurrentQuestion
    @Test
    void getCurrentQuestion_shouldSendGetRequest()
            throws Exception {

        server.enqueue(jsonResponse(
           new GetCurrentQuestionViewResponse(
                   2,
                   "Who was the norse god of thunder?",
                   List.of(
                           "Odin",
                           "Thor",
                           "Loki",
                           "Me :D"
                   )
           )
        ));

        GetCurrentQuestionViewResponse response =
                client.getCurrentQuestion(12345L);

        assertNotNull(response);

        RecordedRequest request =
                takeRequest("GET", "/api/current_question");

        assertEquals(
                "12345",
                request.getRequestUrl()
                        .queryParameter("telegramId")
        );

        assertEquals(0, request.getBody().size());
    }

    // AbortQuiz
    @Test
    void abortQuiz_shouldSendPostRequest()
            throws Exception {

        server.enqueue(jsonResponse(
           new AbortQuizResponse(
                   2,
                   40,
                   50
           )
        ));

        AbortQuizResponse response =
                client.abortQuiz(12345L);

        assertNotNull(response);

        RecordedRequest request =
                takeRequest("POST", "/api/abort");

        JsonNode body = readBody(request);

        assertEquals(12345L, body.get("telegramId").asLong());
    }

    @Test
    void finishQuiz_shouldSendPostRequest()
            throws Exception {

        server.enqueue(jsonResponse(
                new FinishQuizResponse(
                        3,
                        44,
                        34
                )
        ));

        FinishQuizResponse response =
                client.finishQuiz(12345L);

        assertNotNull(response);

        RecordedRequest request =
                takeRequest("POST", "/api/finish");

        JsonNode body = readBody(request);

        assertEquals(12345L, body.get("telegramId").asLong());
    }

    // Error handling
    @Test
    void startQuiz_shouldThrowExceptionOnBadRequest() {

        server.enqueue(
                new MockResponse()
                        .setResponseCode(400)
        );

        assertThrows(
                HttpClientErrorException.BadRequest.class,
                () -> client.startQuiz(12345L, 10)
        );
    }
}
