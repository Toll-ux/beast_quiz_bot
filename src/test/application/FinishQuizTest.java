package application;

import application.port.*;
import domain.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/// FinishQuizTest - implements unit-tests for FinishQuiz class
public class FinishQuizTest {

    private final List<Question> questions = List.of(new Question(1, "s", List.of("s"), Set.of(0)));

    private FakeQuizRepoSpy quizRepo;
    private FakeUserRepoSpy userRepo;
    private FinishQuiz finishQuiz;

    private final Clock clock = Clock.fixed(
            Instant.parse("2026-10-14T12:15:00Z"),
            ZoneId.of("UTC")
    );

    private static final long telegramId = 12L;

    /* Перед каждым тестом обновляем репозитории и finishQuiz */
    @BeforeEach
    void setUp() {
        quizRepo = new FakeQuizRepoSpy();
        userRepo = new FakeUserRepoSpy();
        finishQuiz = new FinishQuiz(quizRepo, userRepo);
    }

    @DisplayName("Инициализация класса FinishQuiz")
    @Test
    void finishQuiz() { assertDoesNotThrow( () -> { new FinishQuiz(quizRepo, userRepo); }); }

    /* Тесты для finish() */
    @Nested
    class Finish
    {

        @DisplayName("finish: получил tg id <= 0")
        @ParameterizedTest
        @ValueSource(longs = {0L, -1L, Long.MIN_VALUE})
        void throwsWhenTgIdIsNotPositive(long invalidId)
        {
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                    finishQuiz.finish(invalidId));
            Assertions.assertEquals("Telegram id must be positive", thrown.getMessage());
        }

        @DisplayName("finish: успешное завершение квиза")
        @Test
        void finishHappyPath()
        {
            /* создаем и начинаем квиз */
            SpyQuiz quiz = new SpyQuiz(questions);
            quiz.start();
            /* счет квиза > 0 */
            quiz.overriddenScore = 10;
            assertEquals(10, quiz.getScore(), "Test setup invalid: Quiz should start with score >0");
            /* сохраняем сессию до завершения квиза */
            LocalDateTime now = LocalDateTime.now(clock);
            quizRepo.addSession(new QuizSession(1, telegramId, quiz, now));

            /* сохраняем данные юзера */
            User user = new User(telegramId);
            user.updateTotalScore(100);
            userRepo.saveUserInfo(user);

            finishQuiz.finish(telegramId);
            assertTrue(quiz.wasFinishCalled, "quiz.finish() должен быть вызван!");
            assertTrue(quiz.isFinished(), "Квиз должен быть в статусе FINISHED");
            assertTrue(quizRepo.wasSessionUpdated, "Сессия должна быть обновлена");
            assertTrue(userRepo.wasUserUpdated, "Информация юзера должна быть обновлена");
        }

        @DisplayName("finish: нулевой счет валиден")
        @Test
        void finishZeroScoreIsValid()
        {
            /* создали новый квиз, score должен быть 0 по умолчанию */
            SpyQuiz quiz = new SpyQuiz(questions);
            quiz.start();
            assertEquals(0, quiz.getScore(), "Test setup invalid: Quiz should start with score 0");

            /* сохраняем сессию до завершения квиза */
            LocalDateTime now = LocalDateTime.now(clock);
            quizRepo.addSession(new QuizSession(1, telegramId, quiz, now));

            /* сохраняем данные юзера */
            User user = new User(telegramId);
            user.updateTotalScore(10);
            userRepo.saveUserInfo(user);

            finishQuiz.finish(telegramId);

            assertTrue(quiz.wasFinishCalled, "quiz.finish() должен быть вызван!");
            assertTrue(quizRepo.wasSessionUpdated, "Сессия должна быть обновлена");
        }

        @DisplayName("finish: квиз уже завершен (данные не сохраняются)")
        @Test
        void doesNotSaveWhenQuizAlreadyFinished()
        {
            SpyQuiz quiz = new SpyQuiz(questions);
            quiz.overriddenScore = 10;
            quiz.throwOnFinish = true;
            /* сохраняем сессию до завершения квиза */
            LocalDateTime now = LocalDateTime.now(clock);
            quizRepo.addSession(new QuizSession(1, telegramId, quiz, now));

            User user = new User(telegramId);
            user.updateTotalScore(10);
            /* сохраняем данные юзера */
            userRepo.saveUserInfo(user);

            FinishQuizResult result = assertDoesNotThrow(() -> finishQuiz.finish(telegramId));

            assertNotNull(result);
            assertFalse(quizRepo.wasSessionUpdated, "Сессия не должна сохраниться, если квиз уже завершен ранее");
        }

        @DisplayName("finish: нет активной сессии")
        @Test
        void throwsWhenNoActiveQuizSession()
        {
            quizRepo.getEmptyForTelegramId = true;
            IllegalStateException thrown = assertThrows(IllegalStateException.class, () ->
                    finishQuiz.finish(telegramId));

            String expectedMessage = "No active quiz for user with tg id: " + telegramId;
            assertEquals(expectedMessage, thrown.getMessage());
        }

        @DisplayName("finish: throws когда счет <0, не завершает квиз")
        @Test
        void throwsWhenScoreNegativeAndDoesNotFinish()
        {
            SpyQuiz quiz = new SpyQuiz(questions);
            quiz.overriddenScore = -10;
            /* сохраняем сессию до завершения квиза */
            LocalDateTime now = LocalDateTime.now(clock);
            quizRepo.addSession(new QuizSession(1, telegramId, quiz, now));

            IllegalStateException thrown = assertThrows(IllegalStateException.class, () ->
                    finishQuiz.finish(telegramId));

            assertEquals("Quiz score cannot be negative", thrown.getMessage());
            assertFalse(quiz.wasFinishCalled);
        }
    }

    /* Тесты для abort() */
    @Nested
    class Abort
    {
        @DisplayName("abort: получил tg id <= 0")
        @ParameterizedTest
        @ValueSource(longs = {0L, -1L, Long.MIN_VALUE})
        void throwsWhenTgIdIsNotPositive(long invalidId)
        {
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                    finishQuiz.abort(invalidId));
            Assertions.assertEquals("Telegram id must be positive", thrown.getMessage());
        }

        @DisplayName("abort: нет активной сессии")
        @Test
        void throwsWhenNoActiveQuizSession()
        {
            quizRepo.getEmptyForTelegramId = true;
            IllegalStateException thrown = assertThrows(IllegalStateException.class, () ->
                    finishQuiz.abort(telegramId));

            String expectedMessage = "No active quiz for user with tg id: " + telegramId;
            assertEquals(expectedMessage, thrown.getMessage());
        }

        @DisplayName("abort: квиз уже покинут (данные не сохраняются)")
        @Test
        void doesNotSaveWhenQuizAlreadyAborted()
        {
            SpyQuiz quiz = new SpyQuiz(questions);
            quiz.overriddenScore = 10;
            quiz.throwOnFinish = true;
            /* сохраняем сессию до завершения квиза */
            LocalDateTime now = LocalDateTime.now(clock);
            quizRepo.addSession(new QuizSession(1, telegramId, quiz, now));

            /* сохраняем данные юзера */
            User user = new User(telegramId);
            user.updateTotalScore(10);
            userRepo.saveUserInfo(user);

            FinishQuizResult result = assertDoesNotThrow(() -> finishQuiz.abort(telegramId));

            assertNotNull(result);
            assertFalse(quizRepo.wasSessionUpdated, "Сессия не должна сохраниться, если квиз уже завершен ранее");
        }

        @DisplayName("abort: апдейтит данные юзера, если счет неотрицателен")
        @Test
        void abortUpdatesUserWhenScoreIsPositive()
        {
            /* создаем новый квиз */
            SpyQuiz quiz = new SpyQuiz(questions);
            quiz.start();
            quiz.overriddenScore = 10;
            assertEquals(10, quiz.getScore(), "Test setup invalid: Quiz should start with score >0");
            /* сохраняем сессию до завершения квиза */
            LocalDateTime now = LocalDateTime.now(clock);
            quizRepo.addSession(new QuizSession(1, telegramId, quiz, now));

            /* сохраняем данные юзера */
            User user = new User(telegramId);
            user.updateTotalScore(100);
            userRepo.saveUserInfo(user);

            finishQuiz.abort(telegramId);
            assertTrue(quiz.wasAbortCalled, "quiz.abort() должен быть вызван!");
            assertTrue(quiz.isAborted(), "Квиз должен быть в статусе ABORTED");
            assertTrue(quizRepo.wasSessionUpdated, "Сессия должна быть обновлена");
            assertTrue(userRepo.wasUserUpdated, "Информация юзера должна быть обновлена");
        }

        @DisplayName("abort: throws когда счет <0, не покидает квиз")
        @Test
        void throwsWhenScoreNegativeAndDoesNotAbort()
        {
            SpyQuiz quiz = new SpyQuiz(questions);
            quiz.overriddenScore = -10;
            /* сохраняем сессию до завершения квиза */
            LocalDateTime now = LocalDateTime.now(clock);
            quizRepo.addSession(new QuizSession(1, telegramId, quiz, now));

            IllegalStateException thrown = assertThrows(IllegalStateException.class, () ->
                    finishQuiz.abort(telegramId));

            assertEquals("Quiz score cannot be negative", thrown.getMessage());
            assertFalse(quiz.wasAbortCalled);
        }
    }

    @Nested
    class PersistAndReport
    {
        @DisplayName("persistAndReport: не сохраняет данные, если юзер не нашелся")
        @Test
        void throwsWithoutUpdatingWhenUserNotFound()
        {
            SpyQuiz quiz = new SpyQuiz(questions);
            quiz.start();
            quiz.overriddenScore = 10;
            /* сохраняем сессию до завершения квиза */
            LocalDateTime now = LocalDateTime.now(clock);
            quizRepo.addSession(new QuizSession(1, telegramId, quiz, now));
            /* МЫ НЕ СОХРАНИЛИ ДАННЫЕ ЮЗЕРА */

            IllegalStateException thrown = assertThrows(IllegalStateException.class,
                    () -> finishQuiz.abort(telegramId));

            assertTrue(thrown.getMessage().contains("User not found"));

            assertFalse(quizRepo.wasSessionUpdated, "Сессия НЕ должна обновляться, если юзера нет в БД");
            assertFalse(userRepo.wasUserUpdated);
        }

        @DisplayName("persistAndReport: распространяет исключение, если не получается обновить данные")
        @Test
        void propagatesExceptionWhenSessionUpdateFails()
        {
            SpyQuiz quiz = new SpyQuiz(questions);
            quiz.start();
            quiz.overriddenScore = 10;
            /* сохраняем сессию до завершения квиза */
            LocalDateTime now = LocalDateTime.now(clock);
            quizRepo.addSession(new QuizSession(1, telegramId, quiz, now));
            quizRepo.throwOnUpdate = true;

            /* сохраняем данные юзера */
            User user = new User(telegramId);
            user.updateTotalScore(100);

            assertThrows(RuntimeException.class, () -> finishQuiz.finish(telegramId));
            assertFalse(userRepo.wasUserUpdated, "Данные юзера НЕ должны сохраняться, когда НЕ можем сохранить сессию");
        }
    }

    static class SpyQuiz extends Quiz
    {
        boolean wasFinishCalled = false;
        boolean wasAbortCalled = false;
        boolean throwOnFinish = false;
        boolean throwOnAbort = false;
        int overriddenScore = 0;
        /**
         * Конструктор создает квиз
         *
         * @param questions список подготовленных вопросов
         * @throws IllegalArgumentException если список вопросов пустой
         * @throws NullPointerException     если список null
         */
        public SpyQuiz(List<Question> questions)
        {
            super(questions);
        }

        @Override
        public int getScore() {
            return overriddenScore;
        }

        @Override
        public void finish()
        {
            wasFinishCalled = true;
            if (throwOnFinish) throw new IllegalStateException("Already finished");
            super.finish();
        }

        @Override
        public void abort()
        {
            wasAbortCalled = true;
            if (throwOnAbort) throw new IllegalStateException("Already aborted");
            super.abort();
        }
    }

    static class FakeQuizRepoSpy extends FakeQuizRepo {
        private QuizSession session;
        boolean wasQueried = false;
        boolean wasSessionUpdated = false;
        boolean throwOnUpdate = false;

        void addSession(QuizSession session) {
            this.session = session;
        }

        @Override
        public Optional<QuizSession> findQuizSessionByTelegramId(long id) {
            wasQueried = true;
            if (getEmptyForTelegramId)
                return Optional.empty();

            if (telegramId <= 0) {
                throw new IllegalArgumentException("Telegram id must be positive");
            }
            return Optional.ofNullable(session);
        }

        @Override
        public void updateQuizSession(QuizSession session) {
            if (throwOnUpdate) throw new RuntimeException("DB down");
            this.session = session;
            this.wasSessionUpdated = true;
        }
    }

    static class FakeUserRepoSpy extends FakeUserRepo
    {
        boolean wasUserUpdated = false;

        @Override
        public void updateUserInfo(User user) {
            this.wasUserUpdated = true;
        }
    }
}
