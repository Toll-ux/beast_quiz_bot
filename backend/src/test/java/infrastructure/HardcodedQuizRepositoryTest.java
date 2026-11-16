package infrastructure;

import application.QuizSession;
import domain.Question;
import domain.Quiz;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class HardcodedQuizRepositoryTest {

    static final long tgId = 123333;
    private final List<Question> questions = List.of(
            new Question(
                    1,
                    "Какой язык программирования используется в проекте?",
                    List.of("Java", "Python", "C++", "JavaScript"),
                    Set.of(0)
            )
    );
    final Quiz quiz = new Quiz(questions);
    LocalDateTime now = LocalDateTime.now();


    @DisplayName("Сохранение новой QuizSession")
    @Test
    void saveQuizSession() {
        HardcodedQuizRepository repository =
                new HardcodedQuizRepository();

        QuizSession session = createQuizSession();

        repository.saveQuizSession(session);

        assertEquals(
                session,
                repository.findQuizSessionByTelegramId(tgId)
                        .get()
        );
    }

    @DisplayName("Ошибка сохранения null QuizSession")
    @Test
    void saveNullQuizSession() {
        HardcodedQuizRepository repository =
                new HardcodedQuizRepository();

        IllegalArgumentException thrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> repository.saveQuizSession(null)
                );

        assertEquals(
                "QuizSession cannot be null",
                thrown.getMessage()
        );
    }

    @DisplayName("Новая сессия заменяет старую для того же пользователя")
    @Test
    void saveQuizSessionReplacesOldSession() {
        HardcodedQuizRepository repository =
                new HardcodedQuizRepository();

        QuizSession oldSession =
                createQuizSession();

        QuizSession newSession =
                createQuizSession();

        repository.saveQuizSession(oldSession);
        repository.saveQuizSession(newSession);

        assertEquals(
                newSession,
                repository.findQuizSessionByTelegramId(tgId)
                        .get()
        );
    }

    @DisplayName("Обновление существующей QuizSession")
    @Test
    void updateExistingQuizSession() {
        HardcodedQuizRepository repository =
                new HardcodedQuizRepository();

        QuizSession oldSession =
                createQuizSession();

        QuizSession updatedSession =
                createQuizSession();

        repository.saveQuizSession(oldSession);
        repository.updateQuizSession(updatedSession);

        assertEquals(
                updatedSession,
                repository.findQuizSessionByTelegramId(tgId)
                        .get()
        );
    }

    @DisplayName("Ошибка обновления null QuizSession")
    @Test
    void updateNullQuizSession() {
        HardcodedQuizRepository repository =
                new HardcodedQuizRepository();

        IllegalArgumentException thrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> repository.updateQuizSession(null)
                );

        assertEquals(
                "QuizSession cannot be null",
                thrown.getMessage()
        );
    }

    @DisplayName("Ошибка обновления несуществующей QuizSession")
    @Test
    void updateNonExistingQuizSession() {
        HardcodedQuizRepository repository =
                new HardcodedQuizRepository();

        QuizSession session =
                createQuizSession();

        IllegalArgumentException thrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> repository.updateQuizSession(session)
                );

        assertEquals(
                "QuizSession does not exist",
                thrown.getMessage()
        );
    }

    @DisplayName("Поиск существующей QuizSession")
    @Test
    void findExistingQuizSession() {
        HardcodedQuizRepository repository =
                new HardcodedQuizRepository();

        QuizSession session =
                createQuizSession();

        repository.saveQuizSession(session);

        assertTrue(
                repository.findQuizSessionByTelegramId(tgId)
                        .isPresent()
        );

        assertEquals(
                session,
                repository.findQuizSessionByTelegramId(tgId)
                        .get()
        );
    }

    @DisplayName("Поиск несуществующей QuizSession")
    @Test
    void findNonExistingQuizSession() {
        HardcodedQuizRepository repository =
                new HardcodedQuizRepository();

        assertTrue(
                repository.findQuizSessionByTelegramId(tgId)
                        .isEmpty()
        );
    }

    @DisplayName("Ошибка поиска QuizSession с отрицательным Telegram ID")
    @Test
    void findQuizSessionNegativeTelegramId() {
        HardcodedQuizRepository repository =
                new HardcodedQuizRepository();

        IllegalArgumentException thrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> repository.findQuizSessionByTelegramId(-1)
                );

        assertEquals(
                "Telegram id must be positive",
                thrown.getMessage()
        );
    }

    @DisplayName("Ошибка поиска QuizSession с нулевым Telegram ID")
    @Test
    void findQuizSessionZeroTelegramId() {
        HardcodedQuizRepository repository =
                new HardcodedQuizRepository();

        IllegalArgumentException thrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> repository.findQuizSessionByTelegramId(0)
                );

        assertEquals(
                "Telegram id must be positive",
                thrown.getMessage()
        );
    }

    private QuizSession createQuizSession() {
        return new QuizSession(
                1,
                HardcodedQuizRepositoryTest.tgId,
                quiz,
                now

        );
    }
}