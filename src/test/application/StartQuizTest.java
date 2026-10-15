package application;

import application.port.*;
import domain.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;


public class StartQuizTest {
    final FakeQuestionRepo questionRepo = new FakeQuestionRepo();
    final FakeQuizRepo quizRepo = new FakeQuizRepo();
    final Clock clock = Clock.fixed(
            Instant.parse("2026-10-14T12:15:00Z"),
            ZoneId.of("UTC")
    );

    final long telegramId = 20;
    int questionsCount = 1;


    @DisplayName("Инициализация класса StartQuiz")
    @Test
    void startQuisConstructor(){
        assertDoesNotThrow(() -> { new StartQuiz( questionRepo, quizRepo, clock);});
    }

    @DisplayName("execute нормальная работа")
    @Test
    void execute(){
        FakeQuestionRepo questionRepoCurrent = new FakeQuestionRepo();
        StartQuiz sq = new StartQuiz(questionRepoCurrent, quizRepo, clock);
        StartQuizResult result = sq.execute(telegramId, questionsCount);

        assertEquals(questionsCount, result.questionsCount());
        Question expectedQuestion = questionRepoCurrent.questions.getFirst();
        QuestionView expectedView = QuestionView.from(expectedQuestion);
        assertEquals(expectedView, result.firstQuestionView());
        assertEquals(sq.generateSessionId(),result.sessionId());
    }

    @DisplayName("execute передали 0 вопросов")
    @Test
    void executeZeroQuestionCount(){
        StartQuiz sq = new StartQuiz(questionRepo, quizRepo, clock);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            sq.execute(telegramId, 0);
        });

        assertEquals("questionCount must be positive", thrown.getMessage());
    }

    @DisplayName("execute передали отрицательное количество вопросов")
    @Test
    void executeNegativeQuestionCount(){
        StartQuiz sq = new StartQuiz(questionRepo, quizRepo, clock);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            sq.execute(telegramId, -5);
        });

        assertEquals("questionCount must be positive", thrown.getMessage());
    }

    @DisplayName("execute передали 0 tg id")
    @Test
    void executeZeroTgId(){
        StartQuiz sq = new StartQuiz(questionRepo, quizRepo, clock);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            sq.execute(0, questionsCount);
        });

        assertEquals("Telegram id must be positive", thrown.getMessage());
    }

    @DisplayName("execute передали отрицательный tg id")
    @Test
    void executeNegativeTgId(){
        StartQuiz sq = new StartQuiz(questionRepo, quizRepo, clock);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            sq.execute(-10, questionsCount);
        });

        assertEquals("Telegram id must be positive", thrown.getMessage());
    }

    @DisplayName("execute вопросов в базе данных нет")
    @Test
    void executeNoQuestionInBD(){
        FakeQuestionRepo questionRepoCurrent = new FakeQuestionRepo();
        questionRepoCurrent.isOutputEmpty = true;
        StartQuiz sq = new StartQuiz(questionRepoCurrent, quizRepo, clock);

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            StartQuizResult result = sq.execute(telegramId, questionsCount);
        });

        assertEquals("question list is empty", thrown.getMessage());
    }

    @DisplayName("execute принял от бд null")
    @Test
    void executeNullFromBD(){
        FakeQuestionRepo questionRepoCurrent = new FakeQuestionRepo();
        questionRepoCurrent.isOutputNull = true;
        StartQuiz sq = new StartQuiz(questionRepoCurrent, quizRepo, clock);

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            StartQuizResult result = sq.execute(telegramId, questionsCount);
        });

        assertEquals("question list is empty", thrown.getMessage());
    }

    @DisplayName("execute успешно сохраняет сессию в репозиторий квизов")
    @Test
    void executeSavesSessionCorrectly() {
        FakeQuestionRepo questionRepoCurrent = new FakeQuestionRepo();
        FakeQuizRepo quizRepoCurrent = new FakeQuizRepo();

        StartQuiz sq = new StartQuiz(questionRepoCurrent, quizRepoCurrent, clock);
        StartQuizResult result = sq.execute(telegramId, questionsCount);

        Optional<QuizSession> savedSessionOpt = quizRepoCurrent.findQuizSessionByTelegramId(telegramId);
        assertTrue(savedSessionOpt.isPresent(), "Сессия должна быть сохранена в репозитории!");

        QuizSession savedSession = savedSessionOpt.get();
        assertEquals(result.sessionId(), savedSession.sessionId());
        assertEquals(telegramId, savedSession.telegramId());
    }


    @DisplayName("execute 2 раза перезаписывает юзера")
    @Test
    void executeSavesSessionTwoTimes() {
        FakeQuestionRepo questionRepoCurrent = new FakeQuestionRepo();
        FakeQuizRepo quizRepoCurrent = new FakeQuizRepo();

        StartQuiz sq = new StartQuiz(questionRepoCurrent, quizRepoCurrent, clock);

        StartQuizResult result1 = sq.execute(telegramId, questionsCount);

        Optional<QuizSession> savedSessionOpt1 = quizRepoCurrent.findQuizSessionByTelegramId(telegramId);
        assertTrue(savedSessionOpt1.isPresent(), "Первая сессия должна быть сохранена в репозитории");

        QuizSession session1 = savedSessionOpt1.get();
        assertEquals(result1.sessionId(), session1.sessionId());
        assertEquals(telegramId, session1.telegramId());

        StartQuizResult result2 = sq.execute(telegramId, questionsCount);

        Optional<QuizSession> savedSessionOpt2 = quizRepoCurrent.findQuizSessionByTelegramId(telegramId);
        assertTrue(savedSessionOpt2.isPresent(), "Вторая сессия должна успешно перезаписать первую");

        QuizSession session2 = savedSessionOpt2.get();
        assertEquals(result2.sessionId(), session2.sessionId());
        assertEquals(telegramId, session2.telegramId());

        assertEquals(1, quizRepoCurrent.getSessionsCount(), "Старая сессия должна была удалиться при перезаписи!");
    }

}
