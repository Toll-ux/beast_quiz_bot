package client.api;

/// Здесь отредачить
import client.api.dto.request.*;
import client.api.dto.response.*;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.Set;


public class BackendRestClient
{
    private final RestClient restClient;

    /* Конструктор BackendRestClient, принимает на вход базовый URL-адрес */
    public BackendRestClient(String baseUrl)
    {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public StartQuizResponse startQuiz(long telegramId, int questionCount) {
        StartQuizRequest request = new StartQuizRequest(telegramId, questionCount);

        return restClient.post()
                .uri("/api/start")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(StartQuizResponse.class);
    }

    public AnswerQuestionResponse answerQuestion(long telegramId, int questionId, Set<Integer> chosen) {
        AnswerQuestionRequest request = new AnswerQuestionRequest(telegramId, questionId, chosen);

        return restClient.post()
                .uri("/api/answer")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(AnswerQuestionResponse.class);
    }

    public GetLeaderboardResponse getLeaderboard(long telegramId) {
        return restClient.get()
                .uri("/api/leaderboard")
                .retrieve()
                .body(GetLeaderboardResponse.class);
    }

    public GetCurrentQuestionViewResponse getCurrentQuestion(long telegramId) {
        return restClient.get()
                .uri("/api/current_question")
                .retrieve()
                .body(GetCurrentQuestionViewResponse.class);
    }

    public AbortQuizResponse abortQuiz(long telegramId) {
        AbortQuizRequest request = new AbortQuizRequest(telegramId);

        return restClient.post()
                .uri("/api/abort")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(AbortQuizResponse.class);
    }

    public FinishQuizResponse finishQuiz(long telegramId) {
        FinishQuizRequest request = new FinishQuizRequest(telegramId);

        return restClient.post()
                .uri("/api/finish")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(FinishQuizResponse.class);
    }

}