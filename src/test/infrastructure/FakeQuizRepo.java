package infrastructure;

import application.port.QuizRepository;
import application.QuizSession;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FakeQuizRepo implements QuizRepository {

    private final Map<Long, QuizSession> quizSessions = new HashMap<>();

    @Override
    public void saveQuizSession(QuizSession quizSession) {
        if (quizSession == null) {
            throw new IllegalArgumentException("QuizSession cannot be null");
        }
        quizSessions.put(quizSession.telegramId(), quizSession);
    }

    @Override
    public void updateQuizSession(QuizSession quizSession) {
        if (quizSession == null) {
            throw new IllegalArgumentException("QuizSession cannot be null");
        }
        if (!quizSessions.containsKey(quizSession.telegramId())) {
            throw new IllegalArgumentException("QuizSession does not exist");
        }
        quizSessions.put(quizSession.telegramId(), quizSession);
    }

    @Override
    public Optional<QuizSession> findQuizSessionByTelegramId(long telegramId) {
        if (telegramId <= 0) {
            throw new IllegalArgumentException("Telegram id must be positive");
        }
        return Optional.ofNullable(quizSessions.get(telegramId));
    }

    /**
     * Специальный метод для юнит-тестов.
     * Позволяет проверить, сколько всего сессий сейчас находится в репозитории.
     */
    public int getSessionsCount() {
        return quizSessions.size();
    }

    /**
     * Специальный метод для юнит-тестов.
     * Позволяет получить прямой доступ к карте сессий для глубоких проверок.
     */
    public Map<Long, QuizSession> getQuizSessions() {
        return quizSessions;
    }
}
