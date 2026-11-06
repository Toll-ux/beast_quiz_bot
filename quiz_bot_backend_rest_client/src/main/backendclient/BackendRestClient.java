package backendclient;

import backendclient.dto.response;

import org.springframework.web.client.RestClient;

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

    public GetLeaderboardResponse getLeaderboard() {
        return restClient.get()
                .uri("/api/leaderboard")
                .retrieve()
                .body(GetLeaderboardResponse.class);
    }

    public AbortQuizResponse abortQuiz() {
        return restClient.post()
                .uri("/api/abort")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(AbortQuizResponse.class);
    }
}