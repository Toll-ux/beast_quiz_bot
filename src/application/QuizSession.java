package application;

import domain.Quiz;
import java.time.LocalDateTime;

/**
 * QuizSession - связывает Quiz с конкретными номером сессии и ид юзера.
 *
 * @param sessionId     sessionId - идентификатор сессии, уникальный для каждой сессии
 * @param telegramId    telegramId - идентификатор tg юзера
 * @param quiz          quiz - квиз, содержит текущее состояние квиза, список вопросов, агрегатный счет за сессию и индекс текущего вопроса
 * @param startDateTime startDateTime - время начала квиза для отправки в БД и отсчета времени текущей сессии
 */

public record QuizSession(int sessionId, long telegramId, Quiz quiz, LocalDateTime startDateTime) {

}