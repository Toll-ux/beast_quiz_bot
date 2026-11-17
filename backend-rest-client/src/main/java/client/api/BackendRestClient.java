package client.api;


import client.api.dto.request.*;
import client.api.dto.response.*;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.Set;


public class BackendRestClient implements QuizBackendClient
{
    private final RestClient restClient;

    /// Конструктор BackendRestClient, принимает на вход базовый URL-адрес
    public BackendRestClient(String baseUrl)
    {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    /**
     * Отправляет POST-запрос на backend, чтобы начать квиз юзера
     *
      * @param telegramId телеграм ИД юзера
     * @param questionCount количество вопросов в новом кваизе
     * @return StartQuizResponse, содержащий информацию об успешном начале квиза
     */
    public StartQuizResponse startQuiz(long telegramId, int questionCount) {
        StartQuizRequest request = new StartQuizRequest(telegramId, questionCount);

        /// Строим запрос
        return restClient.post()
                .uri("/api/start")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(StartQuizResponse.class);
    }


    /**
     * Отправляет POST-запрос на backend, содержащий ответы юзера на текущий вопрос квиза
     *
     * @param telegramId - телеграм ИД юзера
     * @param questionId - ожидаемый идентификатор вопроса
     * @param chosen - выбранные варианты ответов
     * @return AnswerQuestionResponse, содержащий инфо об успехе/неудаче юзера в ответе
     * и правильные варианты на отвеченный вопрос
     */
    public AnswerQuestionResponse answerQuestion(long telegramId, int questionId, Set<Integer> chosen) {
        AnswerQuestionRequest request = new AnswerQuestionRequest(telegramId, questionId, chosen);

        /// Строим запрос
        return restClient.post()
                .uri("/api/answer")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(AnswerQuestionResponse.class);
    }

    /**
     * Отправляет GET-запрос на backend с целью получить обзор следующего вопроса
     *
     * @param telegramId - телеграм ИД юзера
     * @return GetCurrentQuestionViewResponse, содержащий информацию о следующем вопросе (обзор вопроса)
     */
    public GetCurrentQuestionViewResponse getCurrentQuestion(long telegramId) {

        /// Строим запрос
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/current_question")
                        .queryParam("telegramId", telegramId)
                        .build())
                .retrieve()
                .body(GetCurrentQuestionViewResponse.class);
    }

    /**
     * Отправляет POST-запрос, требующий срочного (или аварийного) завершения квиза
     *
     * @param telegramId - телеграм ИД юзера
     * @return AbortQuizResponse, содержащий информацию о квизе - счет за квиз, обновленный счет юзера после квиза
     */
    public AbortQuizResponse abortQuiz(long telegramId) {
        AbortQuizRequest request = new AbortQuizRequest(telegramId);

        /// Строим запрос
        return restClient.post()
                .uri("/api/abort")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(AbortQuizResponse.class);
    }

    /**
     * Отправляет POST-запрос, требующий планового завершения квиза
     *
     * @param telegramId - телеграм ИД юзера
     * @return FinishQuizResponse, содержащий информацию о квизе - счет за квиз, обновленный счет юзера после квиза
     */
    public FinishQuizResponse finishQuiz(long telegramId) {
        FinishQuizRequest request = new FinishQuizRequest(telegramId);

        /// Строим запрос
        return restClient.post()
                .uri("/api/finish")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(FinishQuizResponse.class);
    }

    /**
     * Отправляет GET-запрос на backend с целью получения информации из таблицы лидеров
     *
     * @param telegramId - телеграм ИД юзера
     * @return GetLeaderboardResponse с информацией из таблицы лидеров (топ юзеров + положение юзера в таблице лидеров)
     */
    public GetLeaderboardResponse getLeaderboard(long telegramId) {

        /// Строим запрос
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/leaderboard")
                        .queryParam("telegramId", telegramId)
                        .build())
                .retrieve()
                .body(GetLeaderboardResponse.class);
    }
}