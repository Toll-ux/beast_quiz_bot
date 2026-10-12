package application;

/**
 * 
 * FinishQuizResult - результат завершения квиза
 * @param sessionId - id сессии
 * @param quizScore - кол-во очков, набранных за квиз
 * @param userTotalScore - количество очков юзера после квиза
 */

public record FinishQuizResult(
    int sessionId,
    int quizScore,
    int userTotalScore
) {}