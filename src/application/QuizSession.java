package application;

import domain.Quiz;
import java.time.LocalDateTime;

/**
 * QuizSession - связывает Quiz с конкретными номером сессии и ид юзера. 
 * @param sessionId - идентификатор сессии, уникальный для каждой сессии
 * @param telegramId - идентификатор tg юзера 
 * @param quiz - квиз, содержит текущее состояние квиза, список вопросов, агрегатный счет за сессию и индекс текущего вопроса
 * @param startDateTime - время начала квиза для отправки в БД и отсчета времени текущей сессии
 */

public class QuizSession 
{
    private final int sessionId;
    private final long telegramId;
    private final Quiz quiz;
    private final LocalDateTime startDateTime;

    public QuizSession(int sessionId, int telegramId, Quiz quiz, LocalDateTime startDateTime)
    {
        this.sessionId = sessionId;
        this.telegramId = telegramId;
        this.quiz = quiz;
        this.startDateTime = startDateTime;
    }

    /**
     * Геттеры
     */
    public int getSessionId()
    {
        return sessionId;
    }

    public int getTelegramId()
    {
        return telegramId;
    }

    public Quiz getQuiz()
    {
        return quiz;
    }

    public LocalDateTime getStartDateTime()
    {
        return startDateTime;
    }
}