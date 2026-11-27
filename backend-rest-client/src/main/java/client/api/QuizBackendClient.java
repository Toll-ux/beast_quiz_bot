package client.api;

import client.api.dto.response.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Set;


public interface QuizBackendClient {

    StartQuizResponse startQuiz(
            long telegramId,
            int questionCount
    ) throws HttpClientErrorException;

    AnswerQuestionResponse answerQuestion(
            long telegramId,
            int questionId,
            Set<Integer> chosen
    ) throws HttpClientErrorException;

    GetLeaderboardResponse getLeaderboard(long telegramId) throws HttpClientErrorException;

    GetCurrentQuestionViewResponse getCurrentQuestion(long telegramId) throws HttpClientErrorException;

    AbortQuizResponse abortQuiz(long telegramId) throws HttpClientErrorException;

    FinishQuizResponse finishQuiz(long telegramId) throws HttpClientErrorException;
}
