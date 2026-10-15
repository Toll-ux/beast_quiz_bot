package application;

/**
 * 
 * StartQuizResult - результат начала квиза 
 * @param sessionId - id новой квиз-сессии
 * @param questionsCount - количество вопросов в новом квизе
 * @param firstQuestionView - первый вопрос
 */
public record StartQuizResult(int sessionId, int questionsCount, QuestionView firstQuestionView) {}