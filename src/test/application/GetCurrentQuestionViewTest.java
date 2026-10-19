package application;

import application.port.*;
import domain.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

public class GetCurrentQuestionViewTest
{

    final FakeQuizRepo quizRepo = new FakeQuizRepo();
    final FakeQuestionRepo questionRepo = new FakeQuestionRepo();

    final Clock clock = Clock.fixed(
            Instant.parse("2026-10-14T12:15:00Z"),
            ZoneId.of("UTC")
    );

    final long telegramId = 6522304678L;
    final GetCurrentQuestionView getQuestion = new GetCurrentQuestionView(quizRepo);
    final int questionCount = 1;

    @DisplayName("Инициализация класса GetCurrentQuestionView")
    @Test
    void getCurrentQuestionView() { assertDoesNotThrow( () -> { new GetCurrentQuestionView(quizRepo); }); }

    @DisplayName("execute: получил telegramId = 0")
    @Test
    void executeTgIdEqualsZero()
    {
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () ->
                getQuestion.execute(0L));
        Assertions.assertEquals("Telegram id must be positive", thrown.getMessage());
    }

    @DisplayName("execute: получил telegramId < 0")
    @Test
    void executeTgIdBelowZero()
    {
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () ->
                getQuestion.execute(-10L));
        Assertions.assertEquals("Telegram id must be positive", thrown.getMessage());
    }

    @DisplayName("execute: нет активной квиз-сессии")
    @Test
    void executeNoActiveQuizSession()
    {
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () ->
                getQuestion.execute(1L));

        String expectedMessage = "No active quiz for user with tg id: " + 1L;
        assertEquals(expectedMessage, thrown.getMessage());
    }

    @DisplayName("execute: для tg id Optional возвращает Empty")
    @Test
    void executeEmptyGetAfterTgId()
    {
        quizRepo.getEmptyForTelegramId = true;

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () ->
                getQuestion.execute(telegramId));

        String expectedMessage = "No active quiz for user with tg id: " + telegramId;
        assertEquals(expectedMessage, thrown.getMessage());
    }

    @DisplayName("execute: возвращает обзор текущего (первого) вопроса")
    @Test
    void executeReturnsCurrentQuestionView()
    {
        StartQuiz sq = new StartQuiz(questionRepo, quizRepo, clock);
        sq.execute(telegramId, questionCount);

        QuestionView view = getQuestion.execute(telegramId);

        assertNotNull(view, "Обзор первого вопроса не должен быть Null");

        QuizSession session = quizRepo.findQuizSessionByTelegramId(telegramId).orElseThrow();
        Question expected = session.quiz().getQuestions().get(session.quiz().getCurrentQuestionIndex());

        QuestionView expectedView = QuestionView.from(expected);
        assertEquals(expectedView, view, "Обзор должен соответсвовать текущему вопросу квиза");
    }

    @DisplayName("execute: возвращает обзор вопроса по текущему (не первому) индексу")
    @Test
    void executeReturnsQuestionViewAtCurrentIndex()
    {
        StartQuiz sq = new StartQuiz(questionRepo, quizRepo, clock);
        sq.execute(telegramId, 3);

        // продвигаем сессию к последнему вопросу
        AnswerQuestion aq = new AnswerQuestion(quizRepo);
        aq.execute(telegramId, 1, questionRepo.getAnswer(1));
        aq.execute(telegramId, 2, questionRepo.getAnswer(2));

        QuizSession session = quizRepo.findQuizSessionByTelegramId(telegramId).orElseThrow();
        int currentIndex = session.quiz().getCurrentQuestionIndex();
        assertTrue(currentIndex > 0, "Индекс текущего вопроса должен быть больше 0 для проверки сценария");

        QuestionView view = getQuestion.execute(telegramId);
        Question expected = session.quiz().getQuestions().get(session.quiz().getCurrentQuestionIndex());

        QuestionView expectedView = QuestionView.from(expected);

        assertEquals(expectedView, view, "Должен вернуться обзор вопроса по текущему (не первому) индексу");
    }

    @DisplayName("execute: повторный вызов не изменяет состояние сессии")
    @Test
    void executeDoesNotChangeQuizSession()
    {
        StartQuiz sq = new StartQuiz(questionRepo, quizRepo, clock);
        sq.execute(telegramId, questionCount);

        QuizSession before = quizRepo.findQuizSessionByTelegramId(telegramId).orElseThrow();
        int indexBefore = before.quiz().getCurrentQuestionIndex();
        int scoreBefore = before.quiz().getScore();

        QuestionView first = getQuestion.execute(telegramId);
        QuestionView second = getQuestion.execute(telegramId);

        assertEquals(first, second, "Повторный вызов должен вернуть тот же обзор");

        QuizSession after = quizRepo.findQuizSessionByTelegramId(telegramId).orElseThrow();

        // Проверяем currentQuestionIndex и score в quiz, т.к. эти поля изменяемы. Остальные константные
        assertEquals(indexBefore, after.quiz().getCurrentQuestionIndex(), "Индекс вопроса не должен меняться");
        assertEquals(scoreBefore, after.quiz().getScore(), "Счет не должен меняться");
    }
}
