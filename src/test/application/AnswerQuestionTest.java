package application;

import application.port.*;
import domain.*;
import infrastructure.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

class FakeQuestionRepo4AnswerQuestions implements QuestionRepository {

    public final List<Question> questions = List.of(
            new Question(1, "Сколько планет в Солнечной системе?", List.of("7", "8", "9", "10"), Set.of(1)),
            new Question(2, "Какой тип данных хранит целые числа в Java?", List.of("String", "int", "boolean", "double"), Set.of(1)));

    public boolean isOutputNull = false;
    public boolean isOutputEmpty = false;

    @Override
    public List<Question> findRandomQuestions(int questionsCount) {
        if (questionsCount <= 0) {
            throw new IllegalArgumentException("QuestionCount must be positive");
        }
        if (isOutputNull) return null;
        if (isOutputEmpty) return List.of();

        if (questionsCount > questions.size()) {
            throw new IllegalArgumentException("More questions were requested than are available");
        }

        List<Question> shuffledQuestions = new ArrayList<>(questions);
        return new ArrayList<>(shuffledQuestions.subList(0, questionsCount));
    }
}

class FakeQuizRepo4AnswerQuestions implements QuizRepository {

    public boolean getEmptyForTelegramId = false;

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

    // ИСПРАВЛЕНО: Добавлен недостающий метод для поиска сессии, чтобы удовлетворить интерфейс QuizRepository
    @Override
    public Optional<QuizSession> findQuizSessionByTelegramId(long telegramId) {
        if (getEmptyForTelegramId)
            return Optional.empty();

        if (telegramId <= 0) {
            throw new IllegalArgumentException("Telegram id must be positive");
        }
        return Optional.ofNullable(quizSessions.get(telegramId));
    }
}

public class AnswerQuestionTest {

    // Используем интерфейсы для полиморфизма
    final QuestionRepository generalQuestionRepo = new FakeQuestionRepo4AnswerQuestions();
    final QuizRepository generalQuizRepo = new FakeQuizRepo4AnswerQuestions();

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
        FakeQuestionRepo4AnswerQuestions questionRepoCurrent = new FakeQuestionRepo4AnswerQuestions();
        FakeQuizRepo4AnswerQuestions quizRepoCurrent = new FakeQuizRepo4AnswerQuestions();

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
        FakeQuestionRepo4AnswerQuestions questionRepoCurrent = new FakeQuestionRepo4AnswerQuestions();
        FakeQuizRepo4AnswerQuestions quizRepoCurrent = new FakeQuizRepo4AnswerQuestions();

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
        FakeQuizRepo4AnswerQuestions quizRepoCurrent = new FakeQuizRepo4AnswerQuestions();

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
        FakeQuizRepo4AnswerQuestions quizRepoCurrent = new FakeQuizRepo4AnswerQuestions();
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
        FakeQuestionRepo4AnswerQuestions questionRepoCurrent = new FakeQuestionRepo4AnswerQuestions();
        FakeQuizRepo4AnswerQuestions quizRepoCurrent = new FakeQuizRepo4AnswerQuestions();

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
