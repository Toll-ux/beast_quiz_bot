package application;

/**
 * 
 * StartQuizResult - результат начала квиза 
 * @param sessionId - id новой квиз-сессии
 * @param firstQuestionView - обзор первого вопроса
 * @param questionsCount - количество вопросов в новом квизе
 */
public record StartQuizResult(int sessionId, QuestionView firstQuestionView, int questionsCount) {}