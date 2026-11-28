package client.api;


import client.api.dto.request.*;
import client.api.dto.response.*;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
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
    @Override
    public StartQuizResponse startQuiz(long telegramId, int questionCount) throws HttpClientErrorException {
        StartQuizRequest request = new StartQuizRequest(telegramId, questionCount);

        /// Строим запрос
        return restClient.post()
                .uri("/api/start")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .onStatus(status -> status.value() != 200, (req, res) -> {
                    throw HttpClientErrorException.create(
                            res.getStatusCode(),
                            "Invalid response for start quiz request: " + res.getStatusCode()
                                    + " - " + res.getStatusText(),
                            res.getHeaders(),
                            null,
                            null
                    );
                })
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
    @Override
    public AnswerQuestionResponse answerQuestion(long telegramId, int questionId, Set<Integer> chosen) throws HttpClientErrorException {
        AnswerQuestionRequest request = new AnswerQuestionRequest(telegramId, questionId, chosen);

        /// Строим запрос
        return restClient.post()
                .uri("/api/answer")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .onStatus(status -> status.value() != 200, (req, res) -> {
                    throw  HttpClientErrorException.create(
                            res.getStatusCode(),
                            "Invalid response for answer question request: " + res.getStatusCode()
                                    + " - " + res.getStatusText(),
                            res.getHeaders(),
                            null,
                            null
                    );
                })
                .body(AnswerQuestionResponse.class);
    }

    /**
     * Отправляет GET-запрос на backend с целью получить обзор следующего вопроса
     *
     * @param telegramId - телеграм ИД юзера
     * @return GetCurrentQuestionViewResponse, содержащий информацию о следующем вопросе (обзор вопроса)
     */
    @Override
    public GetCurrentQuestionViewResponse getCurrentQuestion(long telegramId) throws HttpClientErrorException {

        /// Строим запрос
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/current_question")
                        .queryParam("telegramId", telegramId)
                        .build())
                .retrieve()
                .onStatus(status -> status.value() != 200, (req, res) -> {
                    throw  HttpClientErrorException.create(
                            res.getStatusCode(),
                            "Invalid response for getCurrentQuestion request: " + res.getStatusCode()
                                    + " - " + res.getStatusText(),
                            res.getHeaders(),
                            null,
                            null
                    );
                })
                .body(GetCurrentQuestionViewResponse.class);
    }

    /**
     * Отправляет POST-запрос, требующий срочного (или аварийного) завершения квиза
     *
     * @param telegramId - телеграм ИД юзера
     * @return AbortQuizResponse, содержащий информацию о квизе - счет за квиз, обновленный счет юзера после квиза
     */
    @Override
    public AbortQuizResponse abortQuiz(long telegramId) throws HttpClientErrorException {
        AbortQuizRequest request = new AbortQuizRequest(telegramId);

        /// Строим запрос
        return restClient.post()
                .uri("/api/abort")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .onStatus(status -> status.value() != 200, (req, res) -> {
                    throw  HttpClientErrorException.create(
                            res.getStatusCode(),
                            "Invalid response for abort quiz request: " + res.getStatusCode()
                                    + " - " + res.getStatusText(),
                            res.getHeaders(),
                            null,
                            null
                    );
                })
                .body(AbortQuizResponse.class);
    }

    /**
     * Отправляет POST-запрос, требующий планового завершения квиза
     *
     * @param telegramId - телеграм ИД юзера
     * @return FinishQuizResponse, содержащий информацию о квизе - счет за квиз, обновленный счет юзера после квиза
     */
    @Override
    public FinishQuizResponse finishQuiz(long telegramId) throws HttpClientErrorException {
        FinishQuizRequest request = new FinishQuizRequest(telegramId);

        /// Строим запрос
        return restClient.post()
                .uri("/api/finish")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .onStatus(status -> status.value() != 200, (req, res) -> {
                    throw  HttpClientErrorException.create(
                            res.getStatusCode(),
                            "Invalid response for finish quiz request: " + res.getStatusCode()
                                    + " - " + res.getStatusText(),
                            res.getHeaders(),
                            null,
                            null
                    );
                })
                .body(FinishQuizResponse.class);
    }

    /**
     * Отправляет GET-запрос на backend с целью получения информации из таблицы лидеров
     *
     * @param telegramId - телеграм ИД юзера
     * @return GetLeaderboardResponse с информацией из таблицы лидеров (топ юзеров + положение юзера в таблице лидеров)
     */
    @Override
    public GetLeaderboardResponse getLeaderboard(long telegramId) throws HttpClientErrorException {

        /// Строим запрос
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/leaderboard")
                        .queryParam("telegramId", telegramId)
                        .build())
                .retrieve()
                .onStatus(status -> status.value() != 200, (req, res) -> {
                    throw  HttpClientErrorException.create(
                            res.getStatusCode(),
                            "Invalid response for leaderboard request: " + res.getStatusCode()
                                    + " - " + res.getStatusText(),
                            res.getHeaders(),
                            null,
                            null
                    );
                })
                .body(GetLeaderboardResponse.class);
    }
}