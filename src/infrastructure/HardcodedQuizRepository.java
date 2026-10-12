package infrastructure;

import application.port.QuizRepository;
import application.QuizSession;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HardcodedQuizRepository implements QuizRepository{
    /// Ключи - telegram ID пользователей.
    /// Для каждого пользователя хранится не более одной сессии.
    private final Map<Long, QuizSession> quizSessions = new HashMap<>();

    /** Сохраняет новую игровую сессию.
     * @param quizSession - Новая игровая сессия
     * @throws IllegalArgumentException если QuizSession == null
     * Новая сессия заменяет предыдущую сессию того же пользователя.
     */
    @Override
    public void saveQuizSession(QuizSession quizSession){
        if (quizSession == null) {
            throw new IllegalArgumentException("quizSession cannot be null");
        }
        quizSessions.put(quizSession.getTelegramId(), quizSession);
    }

    /** Сохраняет данные о текущей или завершенной сессии в поле quizSessions
     * @param quizSession - существующая игровая сессия, которую нужно обновить
     */
    @Override
    public void updateQuizSession(QuizSession quizSession){
        if (quizSession == null) {
            throw new IllegalArgumentException("quizSession cannot be null");
        }
        if (!quizSessions.containsKey(quizSession.getTelegramId())){
            throw new IllegalArgumentException("quizSession does not exist");
        }

        quizSessions.put(quizSession.getTelegramId(), quizSession);
    }

    /** Возвращает текущую квиз-сессию юзера (их <=1 для каждого юзера) по тг id юзера
     * @param telegramId - тг id юзера
     * @return сессию, если у пользователя с таким id она есть,
     * ничего, если у пользователя нет сессии.
     */
    @Override
    public Optional<QuizSession> findQuizSessionByTelegramId(long telegramId){
        return Optional.ofNullable(quizSessions.get(telegramId));
    }
}