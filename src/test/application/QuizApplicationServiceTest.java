package application;

import application.port.*;
import domain.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;


//Проверяет публичные методы сервисов, которые они используют внутри себя
public class QuizApplicationServiceTest {
    final FakeQuestionRepo questionRepo = new FakeQuestionRepo();
    final FakeQuizRepo quizRepo = new FakeQuizRepo();
    final FakeUserRepo userRepo = new FakeUserRepo();

    final Clock clock = Clock.fixed(
            Instant.parse("2026-10-14T12:15:00Z"),
            ZoneId.of("UTC")
    );

    final long telegramId = 20;
    int questionsCount = 2;

    String username = "@test_user";
    String nickname = "Test_user";

    private QuizApplicationService createService() {
        return new QuizApplicationService(
                questionRepo,
                quizRepo,
                userRepo,
                clock
        );
    }

    /**
     * Вспомогательный метод: начинает квиз и возвращает его сессию.
     */
    private QuizSession startQuiz(QuizApplicationService service) {
        service.start(telegramId, questionsCount);

        return quizRepo.findQuizSessionByTelegramId(telegramId).orElseThrow();
    }

    @DisplayName("Инициализация класса")
    @Test
    void quizApplicationServiceConstructor() {
        assertDoesNotThrow(() -> {
            new QuizApplicationService(
                    questionRepo,
                    quizRepo,
                    userRepo,
                    clock
            );
        });
    }

    @DisplayName("start: нормальная работа")
    @Test
    void startNormal() {
        QuizApplicationService service = createService();

        assertTrue(service.start(telegramId, questionsCount));

        Optional<QuizSession> savedSessionOpt =
                quizRepo.findQuizSessionByTelegramId(telegramId);

        assertTrue(savedSessionOpt.isPresent(),
                "Сессия должна быть сохранена в репозитории");

        QuizSession savedSession = savedSessionOpt.get();

        assertEquals(telegramId, savedSession.telegramId());
        assertEquals(questionsCount, savedSession.quiz().getQuestions().size());
    }

    @DisplayName("start: передали 0 вопросов")
    @Test
    void startZeroQuestionCount() {
        QuizApplicationService service = createService();

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> service.start(telegramId, 0)
        );

        assertEquals("questionCount must be positive",
                thrown.getMessage()
        );

    }

    @DisplayName("start: у пользователя уже есть незавершенный quiz")
    @Test
    void startUserHasUnfinishedQuiz() {
        QuizApplicationService service = createService();

        assertTrue(service.start(telegramId, questionsCount));
        assertFalse(service.start(telegramId, questionsCount));

        assertEquals(1,
                quizRepo.getSessionsCount(),
                "Не должна создаваться вторая сессия");
    }

    @DisplayName("presentQuestion: нормальная работа")
    @Test
    void presentQuestionNormal() {
        QuizApplicationService service = createService();
        QuizSession session = startQuiz(service);
        Quiz quiz = session.quiz();

        QuestionView questionView = service.presentQuestion(telegramId);

        Question currentQuestion = quiz.getQuestions().get(quiz.getCurrentQuestionIndex());

        assertEquals(currentQuestion.id(), questionView.questionId());
        assertEquals(currentQuestion.text(), questionView.text());
        assertEquals(currentQuestion.answers(), questionView.answers());
    }

    @DisplayName("presentQuestion: нет активного квиза")
    @Test
    void presentQuestionWithoutActiveQuiz() {
        QuizApplicationService service = createService();
        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> service.answer(telegramId, 1, Set.of(0))
        );

        assertEquals(
                "No active quiz for user with tg id: " + telegramId,
                thrown.getMessage()
        );
    }

    @DisplayName("answer: правильный ответ на вопрос")
    @Test
    void answerCorrectAnswer() {
        QuizApplicationService service = createService();
        QuizSession session = startQuiz(service);

        Question currentQuestion = session.quiz()
                .getQuestions()
                .getFirst();

        Set<Integer> correctAnswers =
                currentQuestion.correctAnswerIndexes();

        AnswerQuestionResult result = service.answer(
                telegramId,
                currentQuestion.id(),
                correctAnswers
        );

        assertTrue(result.correct());
        assertEquals(correctAnswers, result.correctAnswers());
        assertFalse(result.finished());
    }

    @DisplayName("answer: передан отрицательный ID вопроса")
    @Test
    void answerNegativeQuestionId() {
        QuizApplicationService service = createService();
        startQuiz(service);

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> service.answer(telegramId, -1, Set.of(0))
        );

        assertTrue(thrown.getMessage().startsWith("Question id must be positive"));
    }

    @DisplayName("answer: передан отрицательный telegram ID")
    @Test
    void answerNegativeTelegramId() {
        QuizApplicationService service = createService();
        startQuiz(service);

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> service.answer(-1, 1, Set.of(0))
        );

        assertTrue(thrown.getMessage().startsWith("Telegram id must be positive"));
    }
    @DisplayName("answer: передан нулевой telegram ID")
    @Test
    void answerZeroTelegramId() {
        QuizApplicationService service = createService();
        startQuiz(service);

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> service.answer(0, 1, Set.of(0))
        );

        assertTrue(thrown.getMessage().startsWith("Telegram id must be positive"));
    }

    @DisplayName("answer: передан пустой список ответов")
    @Test
    void answerEmptySelectedAnswers(){
        QuizApplicationService service = createService();
        startQuiz(service);

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> service.answer(telegramId, 1, Set.of())
        );

        assertTrue(thrown.getMessage().startsWith("selectedAnswers must not be Empty"));
    }

    @DisplayName("answer: передан null список ответов")
    @Test
    void answerNullSelectedAnswers(){
        QuizApplicationService service = createService();
        startQuiz(service);

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> service.answer(telegramId, 1, null)
        );

        assertTrue(thrown.getMessage().startsWith("selectedAnswers must not be Empty"));
    }

    @DisplayName("answer: нет активного квиза")
    @Test
    void answerWithoutActiveQuiz() {
        QuizApplicationService service = createService();

        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> service.answer(telegramId, 1, Set.of(0))
        );

        assertEquals(
                "No active quiz for user with tg id: " + telegramId,
                thrown.getMessage()
        );
    }

    @DisplayName("answer: переданный questionId не совпадает с текущим вопросом в квизе")
    @Test
    void answerWrongQuestionID(){
        QuizApplicationService service = createService();
        QuizSession session = startQuiz(service);

        // Отвечаем на первый вопрос.
        Question firstQuestion = session.quiz()
                .getQuestions()
                .getFirst();

        service.answer(
                telegramId,
                firstQuestion.id(),
                firstQuestion.correctAnswerIndexes()
        );

        //Снова пытаемся ответить на первый вопрос
        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> service.answer(telegramId, firstQuestion.id(), Set.of(0))
        );

        assertEquals(
                "Answered the wrong question. Expected " + session.quiz()
                        .getQuestions().get(1).id()
                        + ", got " + firstQuestion.id() ,
                thrown.getMessage()
        );

    }


    @DisplayName("answer: ответ на последний вопрос завершает квиз")
    @Test
    void answerLastQuestion() {
        QuizApplicationService service = createService();
        QuizSession session = startQuiz(service);

        // Отвечаем на первый вопрос.
        Question firstQuestion = session.quiz()
                .getQuestions()
                .getFirst();

        service.answer(
                telegramId,
                firstQuestion.id(),
                firstQuestion.correctAnswerIndexes()
        );

        // Получаем следующий вопрос.
        Question secondQuestion = session.quiz()
                .getQuestions()
                .get(1);

        AnswerQuestionResult result = service.answer(
                telegramId,
                secondQuestion.id(),
                secondQuestion.correctAnswerIndexes()
        );

        assertTrue(result.finished());

    }

    @DisplayName("finish: успешное завершение квиза")
    @Test
    void finishQuiz() {
        QuizApplicationService service = createService();

        // Подготавливаем пользователя.
        User user = new User(
                telegramId,
                username,
                nickname,
                0
        );
        userRepo.saveUserInfo(user);

        QuizSession session = startQuiz(service);

        FinishQuizResult result = service.finish(telegramId);

        assertEquals(session.sessionId(), result.sessionId());
        assertEquals(0, result.quizScore());
        assertEquals(0, result.userTotalScore());
        assertTrue(session.quiz().isFinished());
    }

    @DisplayName("finish: нет активного квиза")
    @Test
    void finishWithoutActiveQuiz() {
        QuizApplicationService service = createService();

        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> service.finish(telegramId)
        );

        assertEquals(
                "No active quiz for user with tg id: " + telegramId,
                thrown.getMessage()
        );
    }

    @DisplayName("abort: успешное прерывание квиза")
    @Test
    void abortQuiz() {
        QuizApplicationService service = createService();

        User user = new User(
                telegramId,
                username,
                nickname,
                0
        );
        userRepo.saveUserInfo(user);

        QuizSession session = startQuiz(service);

        FinishQuizResult result = service.abort(telegramId);

        assertEquals(session.sessionId(), result.sessionId());
        assertEquals(0, result.quizScore());
        assertEquals(0, result.userTotalScore());
        assertTrue(session.quiz().isAborted());
    }

    @DisplayName("abort: нет активного квиза")
    @Test
    void abortWithoutActiveQuiz() {
        QuizApplicationService service = createService();

        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> service.abort(telegramId)
        );

        assertEquals(
                "No active quiz for user with tg id: " + telegramId,
                thrown.getMessage()
        );
    }

    @DisplayName("getLeaderBoard: возвращает непустой список LeaderboardEntry")
    @Test
    void getLeaderBoardNormal() {
        QuizApplicationService service = createService();

        User user = new User(telegramId, username, nickname, 100);
        userRepo.saveUserInfo(user);

        List<LeaderboardEntry> result = service.getLeaderBoard(telegramId);

        assertNotNull(result);
        assertFalse(result.isEmpty());

        assertTrue(result.contains(
                new LeaderboardEntry(1, 100, nickname)
        ));
    }

    @DisplayName("getLeaderBoard: пустая база данных")
    @Test
    void getLeaderBoardEmptyDatabase() {
        QuizApplicationService service = createService();

        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> service.getLeaderBoard(telegramId)
        );

        assertEquals(
                "Leaderboard data is missing or database is empty",
                thrown.getMessage()
        );
    }

    @DisplayName("getLeaderBoard: пользователь отсутствует в базе данных")
    @Test
    void getLeaderBoardUserNotFound() {
        QuizApplicationService service = createService();

        // Топ 10 не пуст, но искомого пользователя нет.
        userRepo.saveUserInfo(
                new User(30, "@other_user", "OtherUser", 50)
        );

        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> service.getLeaderBoard(telegramId)
        );

        assertEquals(
                "User slice data is missing or database is empty",
                thrown.getMessage()
        );
    }

    @DisplayName("finish: Завершение квиза идемпотентно — повторный finish() не падает, возвращает идентичный результат и не начисляет очки пользователю второй раз, ни в ответе, ни в базе данных.")
    @Test
    void finishQuizTwice() {
        User user = new User(telegramId, username, nickname, 0);
        userRepo.saveUserInfo(user);

        QuizApplicationService service = createService();
        QuizSession session = startQuiz(service);

        FinishQuizResult firstResult = service.finish(telegramId);

        assertTrue(session.quiz().isFinished());

        FinishQuizResult secondResult =
                assertDoesNotThrow(() -> service.finish(telegramId));

        assertEquals(firstResult.sessionId(), secondResult.sessionId());
        assertEquals(firstResult.quizScore(), secondResult.quizScore());
        assertEquals(firstResult.userTotalScore(), secondResult.userTotalScore());

        assertTrue(session.quiz().isFinished());

        User savedUser =
                userRepo.findUserByTelegramId(telegramId).orElseThrow();

        assertEquals(firstResult.userTotalScore(), savedUser.getTotalScore());
    }

    @DisplayName("abort: Отмена квиза идемпотентна")
    @Test
    void abortQuizTwice() {
        User user = new User(telegramId, username, nickname, 0);
        userRepo.saveUserInfo(user);

        QuizApplicationService service = createService();
        QuizSession session = startQuiz(service);

        FinishQuizResult firstResult = service.abort(telegramId);

        assertTrue(session.quiz().isAborted());

        FinishQuizResult secondResult =
                assertDoesNotThrow(() -> service.abort(telegramId));

        assertEquals(firstResult.sessionId(), secondResult.sessionId());
        assertEquals(firstResult.quizScore(), secondResult.quizScore());
        assertEquals(firstResult.userTotalScore(), secondResult.userTotalScore());

        assertTrue(session.quiz().isAborted());

        User savedUser =
                userRepo.findUserByTelegramId(telegramId).orElseThrow();

        assertEquals(firstResult.userTotalScore(), savedUser.getTotalScore());
    }

    @DisplayName("abort: нельзя ответить после отмены квиза")
    @Test
    void cannotAnswerAfterAbort() {
        User user = new User(telegramId, username, nickname, 0);
        userRepo.saveUserInfo(user);

        QuizApplicationService service = createService();
        QuizSession session = startQuiz(service);

        Question question = session.quiz().getQuestions().getFirst();

        service.abort(telegramId);

        assertThrows(
                IllegalStateException.class,
                () -> service.answer(
                        telegramId,
                        question.id(),
                        question.correctAnswerIndexes()
                )
        );

        assertTrue(session.quiz().isAborted());
    }

    @DisplayName("finish после abort: не падает и не начисляет очки повторно")
    @Test
    void finishAfterAbort() {
        User user = new User(telegramId, username, nickname, 0);
        userRepo.saveUserInfo(user);

        QuizApplicationService service = createService();
        QuizSession session = startQuiz(service);

        FinishQuizResult abortResult = service.abort(telegramId);

        FinishQuizResult finishResult =
                assertDoesNotThrow(() -> service.finish(telegramId));

        assertEquals(abortResult.sessionId(), finishResult.sessionId());
        assertTrue(session.quiz().isAborted());

        User savedUser =
                userRepo.findUserByTelegramId(telegramId).orElseThrow();

        assertEquals(finishResult.userTotalScore(), savedUser.getTotalScore());
    }
}