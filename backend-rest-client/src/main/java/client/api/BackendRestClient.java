package client.api;

/// Здесь отредачить
import client.api.dto.request.AbortQuizRequest;
import client.api.dto.response.AbortQuizResponse;
import client.api.dto.response.GetLeaderboardResponse;
import org.springframework.http.MediaType;
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

    public AbortQuizResponse abortQuiz(AbortQuizRequest request) {
        return restClient.post()
                .uri("/api/abort")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(AbortQuizResponse.class);
    }
}