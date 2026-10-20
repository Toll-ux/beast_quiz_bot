package application;

import application.port.*;
import domain.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;


public class StartQuizTest {
    final FakeQuestionRepo questionRepo = new FakeQuestionRepo();
    final FakeUserRepo userRepo = new FakeUserRepo();
    final FakeQuizRepo quizRepo = new FakeQuizRepo();
    final Clock clock = Clock.fixed(
            Instant.parse("2026-10-14T12:15:00Z"),
            ZoneId.of("UTC")
    );

    private final List<Question> questions = List.of(new Question(1, "s", List.of("s"), Set.of(0)));

    final long telegramId = 20;
    int questionsCount = 2;


    @DisplayName("StartQuiz: инициализация класса StartQuiz")
    @Test
    void startQuisConstructor(){
        assertDoesNotThrow(() -> { new StartQuiz(userRepo, questionRepo, quizRepo, clock);});
    }

    @DisplayName("generateSessionId: обычная работа")
    @Test
    void generateSessionIdNormal(){
        StartQuiz sq = new StartQuiz(userRepo, questionRepo, quizRepo, clock);
        int id = (int) ((clock.millis() % Integer.MAX_VALUE) & 0x7FFFFFFF);
        assertEquals(id, sq.generateSessionId());
        assertTrue(sq.execute(telegramId, questionsCount));
        assertEquals(id, quizRepo.getQuizSessions().get(telegramId).sessionId());
        assertEquals(1, userRepo.getUsersCount());
    }

    @DisplayName("generateSessionId: переполнение Long")
    @Test
    void generateSessionId(){
        Clock badClock = Clock.fixed(Instant.ofEpochMilli(Long.MAX_VALUE + 1), ZoneId.systemDefault());
        StartQuiz sq = new StartQuiz(userRepo, questionRepo, quizRepo, badClock);

        assertTrue(sq.generateSessionId() > 0);
    }

    @DisplayName("execute: нормальная работа")
    @Test
    void execute(){
        StartQuiz sq = new StartQuiz(userRepo, questionRepo, quizRepo, clock);
        assertTrue(sq.execute(telegramId, questionsCount));

        assertEquals(sq.generateSessionId(), quizRepo.getQuizSessions().get(telegramId).sessionId());
        assertEquals(questionsCount, quizRepo.getQuizSessions().get(telegramId).quiz().getQuestions().size());
        assertEquals(1, userRepo.getUsersCount());
    }

    @DisplayName("execute: передали 0 вопросов")
    @Test
    void executeZeroQuestionCount(){
        StartQuiz sq = new StartQuiz(userRepo, questionRepo, quizRepo, clock);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> sq.execute(telegramId, 0));

        assertEquals("questionCount must be positive", thrown.getMessage());
        assertEquals(1, userRepo.getUsersCount());
    }

    @DisplayName("execute: передали отрицательное количество вопросов")
    @Test
    void executeNegativeQuestionCount(){
        StartQuiz sq = new StartQuiz(userRepo, questionRepo, quizRepo, clock);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> sq.execute(telegramId, -5));

        assertEquals("questionCount must be positive", thrown.getMessage());
    }

    @DisplayName("execute: передали 0 tg id")
    @Test
    void executeZeroTgId(){
        StartQuiz sq = new StartQuiz(userRepo, questionRepo, quizRepo, clock);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> sq.execute(0, questionsCount));

        assertEquals("Telegram id must be positive", thrown.getMessage());
    }

    @DisplayName("execute: передали отрицательный tg id")
    @Test
    void executeNegativeTgId(){
        StartQuiz sq = new StartQuiz(userRepo, questionRepo, quizRepo, clock);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> sq.execute(-10, questionsCount));

        assertEquals("Telegram id must be positive", thrown.getMessage());
    }

    @DisplayName("execute: принял от бд пустой список")
    @Test
    void executeNoQuestionInBD(){
        questionRepo.isOutputEmpty = true;
        StartQuiz sq = new StartQuiz(userRepo, questionRepo, quizRepo, clock);

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> sq.execute(telegramId, questionsCount));

        assertEquals("question list is empty", thrown.getMessage());
    }

    @DisplayName("execute: принял от бд null")
    @Test
    void executeNullFromBD(){
        questionRepo.isOutputNull = true;
        StartQuiz sq = new StartQuiz(userRepo, questionRepo, quizRepo, clock);

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> sq.execute(telegramId, questionsCount));

        assertEquals("question list is empty", thrown.getMessage());
    }

    @DisplayName("execute: успешно сохраняет сессию в репозиторий квизов")
    @Test
    void executeSavesSessionCorrectly() {
        StartQuiz sq = new StartQuiz(userRepo, questionRepo, quizRepo, clock);
        assertTrue(sq.execute(telegramId, questionsCount));

        Optional<QuizSession> savedSessionOpt = quizRepo.findQuizSessionByTelegramId(telegramId);
        assertTrue(savedSessionOpt.isPresent(), "Сессия должна быть сохранена в репозитории!");

        QuizSession savedSession = savedSessionOpt.get();
        assertEquals(sq.generateSessionId(), savedSession.sessionId());
        assertEquals(telegramId, savedSession.telegramId());
    }

    @DisplayName("execute: 2 раза перезаписывает юзера")
    @Test
    void executeSavesSessionTwoTimes() {
        StartQuiz sq = new StartQuiz(userRepo, questionRepo, quizRepo, clock);

        assertTrue(sq.execute(telegramId, questionsCount));

        Optional<QuizSession> savedSessionOpt1 = quizRepo.findQuizSessionByTelegramId(telegramId);
        assertTrue(savedSessionOpt1.isPresent(), "Первая сессия должна быть сохранена в репозитории");

        assertEquals(1, userRepo.getUsersCount());
        QuizSession session1 = savedSessionOpt1.get();
        assertEquals(sq.generateSessionId(), session1.sessionId());
        assertEquals(telegramId, session1.telegramId());

        assertFalse(sq.execute(telegramId, questionsCount), "Второй вызов не перезаписывает сессию");

        Optional<QuizSession> savedSessionOpt2 = quizRepo.findQuizSessionByTelegramId(telegramId);
        assertTrue(savedSessionOpt2.isPresent(), "Второй вызов не удалил сессию юзера");

        QuizSession session2 = savedSessionOpt2.get();
        assertEquals(sq.generateSessionId(), session2.sessionId());
        assertEquals(telegramId, session2.telegramId());

        assertEquals(1, quizRepo.getSessionsCount(), "Старая сессия должна была удалиться при перезаписи!");
    }

    @DisplayName("execute: у юзера есть незаконченная сессия")
    @Test
    void executeUserHasUnfinishedSession(){
        StartQuiz sq = new StartQuiz(userRepo, questionRepo, quizRepo, clock);

        assertTrue(sq.execute(telegramId, questionsCount));

        Optional<QuizSession> savedSessionOpt = quizRepo.findQuizSessionByTelegramId(telegramId);
        assertTrue(savedSessionOpt.isPresent(), "Первая сессия должна быть сохранена в репозитории");

        assertEquals(1, userRepo.getUsersCount());
        QuizSession session = savedSessionOpt.get();
        assertEquals(sq.generateSessionId(), session.sessionId());
        assertEquals(telegramId, session.telegramId());

        assertFalse(sq.execute(telegramId, questionsCount));
        assertEquals(1, quizRepo.getSessionsCount(), "Новая сессия не должна быть записана!");
    }

    @DisplayName("execute: у юзера есть незаконченная сессия, но она aborted")
    @Test
    void executeUserHasUnfinishedButAbortedSession(){
        StartQuiz sq = new StartQuiz(userRepo, questionRepo, quizRepo, clock);

        assertTrue(sq.execute(telegramId, questionsCount));

        Optional<QuizSession> savedSessionOpt = quizRepo.findQuizSessionByTelegramId(telegramId);
        assertTrue(savedSessionOpt.isPresent(), "Первая сессия должна быть сохранена в репозитории");
        assertEquals(1, userRepo.getUsersCount());

        QuizSession session = savedSessionOpt.get();
        assertEquals(sq.generateSessionId(), session.sessionId());
        assertEquals(telegramId, session.telegramId());

        session.quiz().abort();

        assertTrue(session.quiz().isAborted());
        assertTrue(sq.execute(telegramId, questionsCount));
        assertEquals(1, quizRepo.getSessionsCount(), "Новая сессия не должна быть записана!");
    }

    @DisplayName("execute: у юзера есть законченная сессия")
    @Test
    void executeUserHasFinishedSession(){
        StartQuiz sq = new StartQuiz(userRepo, questionRepo, quizRepo, clock);

        assertTrue(sq.execute(telegramId, questionsCount));

        Optional<QuizSession> savedSessionOpt = quizRepo.findQuizSessionByTelegramId(telegramId);
        assertTrue(savedSessionOpt.isPresent(), "Первая сессия должна быть сохранена в репозитории");

        QuizSession session = savedSessionOpt.get();
        assertEquals(1, userRepo.getUsersCount());
        assertEquals(sq.generateSessionId(), session.sessionId());
        assertEquals(telegramId, session.telegramId());

        session.quiz().finish();

        assertTrue(session.quiz().isFinished());

        assertTrue(sq.execute(telegramId, questionsCount));
        assertEquals(1, quizRepo.getSessionsCount(), "Новая сессия не должна быть записана!");
    }

    @DisplayName("execute: незаконченная сессия в isDoingNothing -> start() и update()")
    @Test
    void executeStartsQuizAndUpdatesSessionWhenQuizIsDoingNothing() {
        Quiz quiz = new Quiz(questions);
        LocalDateTime now = LocalDateTime.now(clock);
        quizRepo.saveQuizSession(new QuizSession(1, telegramId, quiz, now));
        QuizSession session = quizRepo.getQuizSessions().get(telegramId);
        assertTrue(session.quiz().isDoingNothing(),
                "Свежесозданный quiz должен быть в состоянии isDoingNothing");

        StartQuiz sq = new StartQuiz(userRepo, questionRepo, quizRepo, clock);

        // второй вызов должен запустить квиз и обновить сессию
        boolean result = sq.execute(telegramId, questionsCount);

        assertFalse(result, "Повторный вызов не создаёт новый квиз");
        assertEquals(1, quizRepo.getSessionsCount(), "Новая сессия не должна быть создана");
        assertFalse(session.quiz().isDoingNothing(),
                "Квиз должен был перейти в started-состояние");
    }

}
