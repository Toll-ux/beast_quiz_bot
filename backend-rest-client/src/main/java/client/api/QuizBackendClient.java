package client.api;

import client.api.dto.response.*;

import java.util.Set;


public interface QuizBackendClient {

    StartQuizResponse startQuiz(
            long telegramId,
            int questionCount
    );

    AnswerQuestionResponse answerQuestion(
            long telegramId,
            int questionId,
            Set<Integer> chosen
    );

    GetLeaderboardResponse getLeaderboard(long telegramId);

    GetCurrentQuestionViewResponse getCurrentQuestion(long telegramId);

    AbortQuizResponse abortQuiz(long telegramId);

    FinishQuizResponse finishQuiz(long telegramId);
}
