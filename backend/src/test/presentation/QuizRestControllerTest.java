package presentation;

import application.FakeQuestionRepo;
import application.FakeQuizRepo;
import application.FakeUserRepo;

import com.fasterxml.jackson.databind.ObjectMapper;
import domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import presentation.dto.request.*;
import presentation.dto.response.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


class QuizRestControllerTest {


    private FakeUserRepo userRepo;
    private FakeQuestionRepo questionRepo;
    private FakeQuizRepo quizRepo;
    private Clock clock;
    private QuizRestController controller;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;



    @BeforeEach
    void setUp() {
        userRepo = new FakeUserRepo();
        questionRepo = new FakeQuestionRepo();
        quizRepo = new FakeQuizRepo();
        clock = Clock.fixed(Instant.parse("2026-11-08T12:00:00Z"), ZoneId.of("UTC"));

        userRepo.saveUserInfo(new User(12345, "@Alice", "Alice", 100));
        userRepo.saveUserInfo(new User(67890, "@Bob", "Bob", 200));

        controller = new QuizRestController(questionRepo, quizRepo, userRepo, clock);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("POST /api/start successfully creates new quiz session")
    void testStartQuizMockMvc() throws Exception {
        StartQuizRequest request = new StartQuizRequest(12345, 2);

        mockMvc.perform(post("/api/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.newQuizCreated").value(true));
    }

    @Test
    @DisplayName("POST /api/start with invalid count returns 400 Bad Request")
    void testStartQuizInvalidCount() throws Exception {
        StartQuizRequest request = new StartQuizRequest(12345, 0);

        mockMvc.perform(post("/api/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/start with negative userId returns 400 Bad Request")
    void testStartQuizNegativeUserId() throws Exception {
        StartQuizRequest request = new StartQuizRequest(-5, 2);

        mockMvc.perform(post("/api/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/answer successfully records user answer")
    void testAnswerQuestionMockMvc() throws Exception {
        controller.start(new StartQuizRequest(12345, 2));

        AnswerQuestionRequest request = new AnswerQuestionRequest(12345, 1, Set.of(1));

        mockMvc.perform(post("/api/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.correct").value(true))
                .andExpect(jsonPath("$.currentScore").isNumber())
                .andExpect(jsonPath("$.finished").value(false));
    }

    @Test
    @DisplayName("POST /api/answer with empty chosen answers returns 400 Bad Request")
    void testAnswerQuestionEmptyChosen() throws Exception {
        controller.start(new StartQuizRequest(12345, 2));

        AnswerQuestionRequest request = new AnswerQuestionRequest(12345, 1, Set.of());

        mockMvc.perform(post("/api/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/finish successfully finishes quiz session")
    void testFinishQuizMockMvc() throws Exception {
        controller.start(new StartQuizRequest(12345, 2));
        controller.answer(new AnswerQuestionRequest(12345, 1, Set.of(1)));

        FinishQuizRequest request = new FinishQuizRequest(12345);

        mockMvc.perform(post("/api/finish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.sessionId").isNumber())
                .andExpect(jsonPath("$.quizScore").isNumber())
                .andExpect(jsonPath("$.userTotalScore").isNumber());
    }

    @Test
    @DisplayName("POST /api/finish with non-existing active quiz returns 400 Bad Request")
    void testFinishQuizNoActiveQuiz() throws Exception {
        FinishQuizRequest request = new FinishQuizRequest(12345);

        mockMvc.perform(post("/api/finish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/abort successfully aborts quiz session")
    void testAbortQuizMockMvc() throws Exception {
        controller.start(new StartQuizRequest(12345, 2));

        AbortQuizRequest request = new AbortQuizRequest(12345);

        mockMvc.perform(post("/api/abort")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.sessionId").isNumber())
                .andExpect(jsonPath("$.quizScore").isNumber())
                .andExpect(jsonPath("$.userTotalScore").isNumber());
    }

    @Test
    @DisplayName("POST /api/abort with non-existing active quiz returns 400 Bad Request")
    void testAbortQuizNoActiveQuiz() throws Exception {
        AbortQuizRequest request = new AbortQuizRequest(12345);

        mockMvc.perform(post("/api/abort")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/current_question returns active question view")
    void testCurrentQuestionMockMvc() throws Exception {
        controller.start(new StartQuizRequest(12345, 2));

        GetCurrentQuestionViewRequest request = new GetCurrentQuestionViewRequest(12345);

        mockMvc.perform(get("/api/current_question")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.questionId").value(1))
                .andExpect(jsonPath("$.text").isNotEmpty())
                .andExpect(jsonPath("$.answers").isArray());
    }

    @Test
    @DisplayName("GET /api/current_question when no active quiz returns 400 Bad Request")
    void testCurrentQuestionNoActiveQuiz() throws Exception {
        GetCurrentQuestionViewRequest request = new GetCurrentQuestionViewRequest(12345);

        mockMvc.perform(get("/api/current_question")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/leaderboard returns leaderboard entries")
    void testLeaderboardMockMvc() throws Exception {
        GetLeaderboardRequest request = new GetLeaderboardRequest(12345);

        mockMvc.perform(get("/api/leaderboard")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.leaderboard").isArray());
    }

    @Test
    @DisplayName("Direct method calls on QuizRestController verify correct DTO returns")
    void testDirectControllerInvocations() {
        ResponseEntity<StartQuizResponse> startResponse = controller.start(new StartQuizRequest(12345, 2));
        assertEquals(HttpStatus.OK, startResponse.getStatusCode());
        assertNotNull(startResponse.getBody());
        assertTrue(startResponse.getBody().newQuizCreated());

        ResponseEntity<GetCurrentQuestionViewResponse> questionResponse = controller.presentQuestion(new GetCurrentQuestionViewRequest(12345));
        assertEquals(HttpStatus.OK, questionResponse.getStatusCode());
        assertNotNull(questionResponse.getBody());
        assertEquals(1, questionResponse.getBody().questionId());

        ResponseEntity<AnswerQuestionResponse> answerResponse = controller.answer(new AnswerQuestionRequest(12345, 1, Set.of(1)));
        assertEquals(HttpStatus.OK, answerResponse.getStatusCode());
        assertNotNull(answerResponse.getBody());
        assertTrue(answerResponse.getBody().correct());

        ResponseEntity<FinishQuizResponse> finishResponse = controller.finish(new FinishQuizRequest(12345));
        assertEquals(HttpStatus.OK, finishResponse.getStatusCode());
        assertNotNull(finishResponse.getBody());

        ResponseEntity<GetLeaderboardResponse> leaderboardResponse = controller.leaderboard(new GetLeaderboardRequest(12345));
        assertEquals(HttpStatus.OK, leaderboardResponse.getStatusCode());
        assertNotNull(leaderboardResponse.getBody());
        assertFalse(leaderboardResponse.getBody().leaderboard().isEmpty());
    }

    @Test
    @DisplayName("Multiple users have independent dialogs and quiz sessions")
    void testIndependentUserDialogs() {
        // User A starts quiz
        ResponseEntity<StartQuizResponse> startA = controller.start(new StartQuizRequest(12345, 2));
        assertTrue(startA.getBody().newQuizCreated());

        // User B starts quiz
        ResponseEntity<StartQuizResponse> startB = controller.start(new StartQuizRequest(67890, 1));
        assertTrue(startB.getBody().newQuizCreated());

        // User A gets current question
        ResponseEntity<GetCurrentQuestionViewResponse> qA = controller.presentQuestion(new GetCurrentQuestionViewRequest(12345));
        assertEquals(1, qA.getBody().questionId());

        // User B answers question
        ResponseEntity<AnswerQuestionResponse> ansB = controller.answer(new AnswerQuestionRequest(67890, 1, Set.of(1)));
        assertTrue(ansB.getBody().finished());

        // User A answers question (session of User A is still not finished)
        ResponseEntity<AnswerQuestionResponse> ansA = controller.answer(new AnswerQuestionRequest(12345, 1, Set.of(1)));
        assertFalse(ansA.getBody().finished());

        // User B finishes quiz
        ResponseEntity<FinishQuizResponse> finishB = controller.finish(new FinishQuizRequest(67890));
        assertNotNull(finishB.getBody());

        // User A finishes quiz independently
        ResponseEntity<FinishQuizResponse> finishA = controller.finish(new FinishQuizRequest(12345));
        assertNotNull(finishA.getBody());
    }
}