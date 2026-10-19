package application;

import application.port.*;
import domain.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Set;
import java.util.Optional;


public class AnswerQuestionTest {
    
    final FakeQuestionRepo questionRepo = new FakeQuestionRepo();
    final FakeQuizRepo quizRepo = new FakeQuizRepo();

    final Clock clock = Clock.fixed(
            Instant.parse("2026-10-14T12:15:00Z"),
            ZoneId.of("UTC")
    );

    final long telegramId = 20;
    final int questionsCount = 1;
    final int questionId = 1;
    final Set<Integer> selectedAnswers = Set.of(1);

    @DisplayName("Инициализация класса AnswerQuestion")
    @Test
    void answerQuestion(){
        assertDoesNotThrow(() -> { new AnswerQuestion(quizRepo); });
    }

    @DisplayName("execute: получил tg id = 0")
    @Test
    void executeZeroTgId(){
        AnswerQuestion aq = new AnswerQuestion(quizRepo);
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () ->
                aq.execute(0, questionId, selectedAnswers));

        Assertions.assertEquals("Telegram id must be positive", thrown.getMessage());

    }

    @DisplayName("execute: получил tg id < 0")
    @Test
    void executeNegativeTgId(){
        AnswerQuestion aq = new AnswerQuestion(quizRepo);
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () ->
                aq.execute(-100, questionId, selectedAnswers));

        Assertions.assertEquals("Telegram id must be positive", thrown.getMessage());

    }

    @DisplayName("execute: получил question id = 0")
    @Test
    void executeZeroQuestionId(){
        StartQuiz sq = new StartQuiz(questionRepo, quizRepo, clock);
        sq.execute(telegramId, questionsCount);

        AnswerQuestion aq = new AnswerQuestion(quizRepo);
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () ->
                aq.execute(telegramId, 0, selectedAnswers));

        Assertions.assertEquals("Question id must be positive", thrown.getMessage());

    }

    @DisplayName("execute: получил question id < 0")
    @Test
    void executeNegativeQuestionId(){
        StartQuiz sq = new StartQuiz(questionRepo, quizRepo, clock);
        sq.execute(telegramId, questionsCount);

        AnswerQuestion aq = new AnswerQuestion(quizRepo);
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () ->
                aq.execute(telegramId, -100, selectedAnswers));

        Assertions.assertEquals("Question id must be positive", thrown.getMessage());

    }

    @DisplayName("execute: получил в selected answers пустое множество")
    @Test
    void executeEmptySelectedAnswers(){
        StartQuiz sq = new StartQuiz(questionRepo, quizRepo, clock);
        sq.execute(telegramId, questionsCount);

        AnswerQuestion aq = new AnswerQuestion(quizRepo);
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () ->
                aq.execute(telegramId, questionId, Set.of()));

        Assertions.assertEquals("selectedAnswers must not be Empty", thrown.getMessage());

    }

    @DisplayName("execute: получил selected answers null")
    @Test
    void executeNullSelectedAnswers(){
        StartQuiz sq = new StartQuiz(questionRepo, quizRepo, clock);
        sq.execute(telegramId, questionsCount);

        AnswerQuestion aq = new AnswerQuestion(quizRepo);
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () ->
                aq.execute(telegramId, questionId, null));

        Assertions.assertEquals("selectedAnswers must not be Empty", thrown.getMessage());

    }

    @DisplayName("execute: пользователь дал верный ответ на 1 вопрос")
    @Test
    void execute(){
        StartQuiz sq = new StartQuiz(questionRepo, quizRepo, clock);
        sq.execute(telegramId, questionsCount);

        AnswerQuestion aq = new AnswerQuestion(quizRepo);
        AnswerQuestionResult aqRes = aq.execute(telegramId, 1, Set.of(1));
        GetCurrentQuestionView gq = new GetCurrentQuestionView(quizRepo);

        assertTrue(aqRes.correct(), "Ответ должен быть верным");
        assertEquals(Set.of(1), aqRes.correctAnswers());
        assertTrue(aqRes.currentScore() > 0, "Очки за правильный ответ должны быть больше 0");
        assertTrue(aqRes.finished(), "Квиз должен быть завершен, так как это был последний вопрос");
        assertEquals(questionRepo.getQuestion(0), gq.execute(telegramId));

        Optional<QuizSession> updatedSessionOpt = quizRepo.findQuizSessionByTelegramId(telegramId);
        assertTrue(updatedSessionOpt.isPresent());
        assertEquals(aqRes.currentScore(), updatedSessionOpt.get().quiz().getScore());
    }

    @DisplayName("execute: пользователь дал неверный ответ на 1 вопрос")
    @Test
    void executeWithIncorrectAnswer(){
        GetCurrentQuestionView gq = new GetCurrentQuestionView(quizRepo);
        StartQuiz sq = new StartQuiz(questionRepo, quizRepo, clock);
        sq.execute(telegramId, questionsCount);

        AnswerQuestion aq = new AnswerQuestion(quizRepo);
        AnswerQuestionResult aqRes = aq.execute(telegramId, 1, Set.of(3));

        assertFalse(aqRes.correct(), "Ответ должен быть неверным");

        assertEquals(Set.of(1), aqRes.correctAnswers(), "Бот должен вернуть Set.of(1) как правильный ответ");


        assertEquals(0, aqRes.currentScore(), "Очки за неверный ответ начисляться не должны");

        assertTrue(aqRes.finished(), "Квиз должен быть завершен, даже если ответ неверный");
        assertEquals(questionRepo.getQuestion(0), gq.execute(telegramId));

        Optional<QuizSession> updatedSessionOpt = quizRepo.findQuizSessionByTelegramId(telegramId);
        assertTrue(updatedSessionOpt.isPresent());
        assertEquals(0, updatedSessionOpt.get().quiz().getScore(), "В базе данных у квиза должно быть 0 очков");
    }

    @DisplayName("execute: нет активного квиза")
    @Test
    void executeNoActiveQuiz(){
        AnswerQuestion aq = new AnswerQuestion(quizRepo);

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> aq.execute(
                telegramId, 1, Set.of(3)));

        String expectedMessage = "No active quiz for user with tg id: " + telegramId;
        assertEquals(expectedMessage, thrown.getMessage());
    }

    @DisplayName("execute: для tg id Optional возвращает Empty")
    @Test
    void executeEmptyGetAfterTgId(){
        quizRepo.getEmptyForTelegramId = true;
        AnswerQuestion aq = new AnswerQuestion(quizRepo);

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () ->
                aq.execute(telegramId, 1, Set.of(3)));

        String expectedMessage = "No active quiz for user with tg id: " + telegramId;
        assertEquals(expectedMessage, thrown.getMessage());
    }

    @DisplayName("execute: ответ не на тот вопрос (нарушение последовательности)")
    @Test
    void executeWithWrongQuestionIdThrowsException() {
        StartQuiz sq = new StartQuiz(questionRepo, quizRepo, clock);
        sq.execute(telegramId, questionsCount);
        AnswerQuestion aq = new AnswerQuestion(quizRepo);

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () ->
                aq.execute(telegramId, 2, Set.of(1)));

        String expectedMessage = "Answered the wrong question. Expected 1, got 2";
        assertEquals(expectedMessage, thrown.getMessage());

        QuizSession sessionAfter = quizRepo.findQuizSessionByTelegramId(telegramId).orElseThrow();
        assertEquals(0, sessionAfter.quiz().getScore(), "Счет не должен измениться при ошибке ID вопроса");
        assertEquals(0, sessionAfter.quiz().getCurrentQuestionIndex(), "Индекс вопроса не должен сдвинуться");
    }

    @DisplayName("execute: пользователь дал верный ответ 3 вопроса")
    @Test
    void executeTreeQuestions(){
        int totalQuestions = 3;

        StartQuiz sq = new StartQuiz(questionRepo, quizRepo, clock);
        sq.execute(telegramId, totalQuestions);

        GetCurrentQuestionView gq = new GetCurrentQuestionView(quizRepo);
        AnswerQuestion aq = new AnswerQuestion(quizRepo);
        AnswerQuestionResult aqRes = null;
        for (int i = 1; i <= totalQuestions; i++) {
            assertEquals(questionRepo.getQuestion(i-1).questionId(), gq.execute(telegramId).questionId());
            Set<Integer> rightAns = questionRepo.getAnswer(i);
            aqRes = aq.execute(telegramId, i, rightAns);

            assertTrue(aqRes.correct(), "Ответ должен быть верным");
            assertEquals(rightAns, aqRes.correctAnswers());
            assertTrue(aqRes.currentScore() > 0, "Очки за правильный ответ должны быть больше 0");


            Optional<QuizSession> updatedSessionOpt = quizRepo.findQuizSessionByTelegramId(telegramId);
            assertTrue(updatedSessionOpt.isPresent());
            assertEquals(aqRes.currentScore(), updatedSessionOpt.get().quiz().getScore());
        }
        assertTrue(aqRes.finished(), "Квиз должен быть завершен, так как это был последний вопрос");
    }
}
