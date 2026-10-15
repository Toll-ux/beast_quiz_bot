package application;

import application.port.*;
import domain.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Set;
import java.util.Optional;


public class AnswerQuestionTest {
    
    final FakeQuestionRepo generalQuestionRepo = new FakeQuestionRepo();
    final FakeQuizRepo generalQuizRepo = new FakeQuizRepo();

    final Clock clock = Clock.fixed(
            Instant.parse("2026-10-14T12:15:00Z"),
            ZoneId.of("UTC")
    );

    final long telegramId = 20;
    final int questionsCount = 1;

    @DisplayName("Инициализация класса AnswerQuestion")
    @Test
    void answerQuestion(){
        assertDoesNotThrow(() -> { new AnswerQuestion(generalQuizRepo); });
    }

    @DisplayName("execute: пользователь дал верный ответ")
    @Test
    void execute(){
        FakeQuestionRepo questionRepoCurrent = new FakeQuestionRepo();
        FakeQuizRepo quizRepoCurrent = new FakeQuizRepo();

        StartQuiz sq = new StartQuiz(questionRepoCurrent, quizRepoCurrent, clock);
        sq.execute(telegramId, questionsCount);

        AnswerQuestion aq = new AnswerQuestion(quizRepoCurrent);
        AnswerQuestionResult aqRes = aq.execute(telegramId, 1, Set.of(1));


        assertTrue(aqRes.correct(), "Ответ должен быть верным");
        assertEquals(Set.of(1), aqRes.correctAnswers());
        assertTrue(aqRes.currentScore() > 0, "Очки за правильный ответ должны быть больше 0");
        assertTrue(aqRes.finished(), "Квиз должен быть завершен, так как это был последний вопрос");
        assertNull(aqRes.nextQuestionView(), "NextQuestionView должен быть null для финального вопроса");

        Optional<QuizSession> updatedSessionOpt = quizRepoCurrent.findQuizSessionByTelegramId(telegramId);
        assertTrue(updatedSessionOpt.isPresent());
        assertEquals(aqRes.currentScore(), updatedSessionOpt.get().quiz().getScore());
    }

    @DisplayName("execute: пользователь дал неверный ответ")
    @Test
    void executeWithIncorrectAnswer(){
        FakeQuestionRepo questionRepoCurrent = new FakeQuestionRepo();
        FakeQuizRepo quizRepoCurrent = new FakeQuizRepo();

        StartQuiz sq = new StartQuiz(questionRepoCurrent, quizRepoCurrent, clock);
        sq.execute(telegramId, questionsCount);

        AnswerQuestion aq = new AnswerQuestion(quizRepoCurrent);
        AnswerQuestionResult aqRes = aq.execute(telegramId, 1, Set.of(3));

        assertFalse(aqRes.correct(), "Ответ должен быть неверным");

        assertEquals(Set.of(1), aqRes.correctAnswers(), "Бот должен вернуть Set.of(1) как правильный ответ");


        assertEquals(0, aqRes.currentScore(), "Очки за неверный ответ начисляться не должны");

        assertTrue(aqRes.finished(), "Квиз должен быть завершен, даже если ответ неверный");
        assertNull(aqRes.nextQuestionView(), "Следующего вопроса быть не должно");

        Optional<QuizSession> updatedSessionOpt = quizRepoCurrent.findQuizSessionByTelegramId(telegramId);
        assertTrue(updatedSessionOpt.isPresent());
        assertEquals(0, updatedSessionOpt.get().quiz().getScore(), "В базе данных у квиза должно быть 0 очков");
    }


    @DisplayName("execute: нет активного квиза")
    @Test
    void executeNoActiveQuiz(){
        FakeQuizRepo quizRepoCurrent = new FakeQuizRepo();

        AnswerQuestion aq = new AnswerQuestion(quizRepoCurrent);

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            aq.execute(telegramId, 1, Set.of(3));
        });


        String expectedMessage = "No active quiz for user with tg id: " + telegramId;
        assertEquals(expectedMessage, thrown.getMessage());
    }


    @DisplayName("execute: для tg id Optional возвращает Empty")
    @Test
    void executeEmptyGetAfterTgId(){
        FakeQuizRepo quizRepoCurrent = new FakeQuizRepo();
        quizRepoCurrent.getEmptyForTelegramId = true;
        AnswerQuestion aq = new AnswerQuestion(quizRepoCurrent);

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            aq.execute(telegramId, 1, Set.of(3));
        });

        String expectedMessage = "No active quiz for user with tg id: " + telegramId;
        assertEquals(expectedMessage, thrown.getMessage());
    }

    @DisplayName("execute: ответ не на тот вопрос (нарушение последовательности)")
    @Test
    void executeWithWrongQuestionIdThrowsException() {
        FakeQuestionRepo questionRepoCurrent = new FakeQuestionRepo();
        FakeQuizRepo quizRepoCurrent = new FakeQuizRepo();

        StartQuiz sq = new StartQuiz(questionRepoCurrent, quizRepoCurrent, clock);
        sq.execute(telegramId, questionsCount);


        int scoreBefore = quizRepoCurrent.findQuizSessionByTelegramId(telegramId).orElseThrow().quiz().getScore();

        AnswerQuestion aq = new AnswerQuestion(quizRepoCurrent);

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            aq.execute(telegramId, 2, Set.of(1));
        });

        String expectedMessage = "Answered the wrong question. Expected 1, got 2";
        assertEquals(expectedMessage, thrown.getMessage());

        QuizSession sessionAfter = quizRepoCurrent.findQuizSessionByTelegramId(telegramId).orElseThrow();
        assertEquals(0, sessionAfter.quiz().getScore(), "Счет не должен измениться при ошибке ID вопроса");
        assertEquals(0, sessionAfter.quiz().getCurrentQuestionIndex(), "Индекс вопроса не должен сдвинуться");
    }

}
