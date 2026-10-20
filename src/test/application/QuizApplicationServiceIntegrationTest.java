package application;

import application.port.QuestionRepository;
import application.port.QuizRepository;
import application.port.UserRepository;
import domain.Question;
import domain.User;
import infrastructure.HardcodedQuestionRepository;
import infrastructure.HardcodedQuizRepository;
import infrastructure.HardcodedUserRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class QuizApplicationServiceIntegrationTest {

    final long telegramId = 20;

    String username = "@test_user";
    String nickname = "Test_user";

    @DisplayName("Полный цикл: начать квиз, ответить и завершить")
    @Test
    void completeQuizLifecycle() {
        QuestionRepository questionRepo = new HardcodedQuestionRepository();
        QuizRepository quizRepo = new HardcodedQuizRepository();
        UserRepository userRepo = new HardcodedUserRepository();

        Clock clock = Clock.fixed(
                Instant.parse("2026-10-14T12:15:00Z"),
                ZoneId.of("UTC")
        );

        QuizApplicationService service = new QuizApplicationService(
                questionRepo,
                quizRepo,
                userRepo,
                clock
        );

        // 1. Создаём пользователя.
        User user = new User(
                telegramId,
                username,
                nickname,
                0
        );
        userRepo.saveUserInfo(user);

        // 2. Начинаем квиз из двух вопросов.
        assertTrue(service.start(telegramId, 2));

        QuizSession session = quizRepo
                .findQuizSessionByTelegramId(telegramId)
                .orElseThrow();

        assertEquals(2, session.quiz().getQuestions().size());

        // 3. Отвечаем на первый вопрос правильно.
        Question firstQuestion = session.quiz()
                .getQuestions()
                .get(0);

        AnswerQuestionResult firstResult = service.answer(
                telegramId,
                firstQuestion.id(),
                firstQuestion.correctAnswerIndexes()
        );

        assertTrue(firstResult.correct());
        assertFalse(firstResult.finished());

        // 4. Отвечаем на второй вопрос правильно.
        Question secondQuestion = session.quiz()
                .getQuestions()
                .get(1);

        AnswerQuestionResult secondResult = service.answer(
                telegramId,
                secondQuestion.id(),
                secondQuestion.correctAnswerIndexes()
        );

        assertTrue(secondResult.correct());
        assertTrue(secondResult.finished());

        // 5. Завершаем квиз.
        FinishQuizResult finishResult = service.finish(telegramId);

        assertTrue(session.quiz().isFinished());
        assertEquals(session.sessionId(), finishResult.sessionId());

        // 6. Проверяем, что результат отражён в пользователе.
        User savedUser = userRepo
                .findUserByTelegramId(telegramId)
                .orElseThrow();

        assertEquals(finishResult.userTotalScore(), savedUser.getTotalScore());

        // 7. Проверяем лидерборд.
        List<LeaderboardEntry> leaderboard = service.getLeaderBoard(telegramId);

        assertTrue(leaderboard.contains(
                new LeaderboardEntry(
                        1,
                        savedUser.getTotalScore(),
                        savedUser.getNickname()
                )
        ));
    }

    @DisplayName("Повторный старт не создаёт вторую активную сессию")
    @Test
    void repeatedStartDoesNotReplaceActiveQuiz() {
        QuestionRepository questionRepo = new HardcodedQuestionRepository();
        QuizRepository quizRepo = new HardcodedQuizRepository();
        UserRepository userRepo = new HardcodedUserRepository();

        Clock clock = Clock.fixed(
                Instant.parse("2026-10-14T12:15:00Z"),
                ZoneId.of("UTC")
        );

        QuizApplicationService service = new QuizApplicationService(
                questionRepo,
                quizRepo,
                userRepo,
                clock
        );

        assertTrue(service.start(telegramId, 2));

        QuizSession originalSession = quizRepo
                .findQuizSessionByTelegramId(telegramId)
                .orElseThrow();

        assertFalse(service.start(telegramId, 2));

        QuizSession currentSession = quizRepo
                .findQuizSessionByTelegramId(telegramId)
                .orElseThrow();

        assertEquals(originalSession.sessionId(), currentSession.sessionId());
    }

    @DisplayName("Нельзя получить вопрос без активного квиза")
    @Test
    void cannotPresentQuestionWithoutActiveQuiz() {
        QuizApplicationService service = new QuizApplicationService(
                new HardcodedQuestionRepository(),
                new HardcodedQuizRepository(),
                new HardcodedUserRepository(),
                Clock.systemUTC()
        );

        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> service.presentQuestion(telegramId)
        );

        assertEquals(
                "No active quiz for user with tg id: " + telegramId,
                thrown.getMessage()
        );
    }

    @DisplayName("Отмена квиза сохраняется в репозиториях")
    @Test
    void abortQuizIntegration() {
        QuestionRepository questionRepo = new HardcodedQuestionRepository();
        QuizRepository quizRepo = new HardcodedQuizRepository();
        UserRepository userRepo = new HardcodedUserRepository();

        Clock clock = Clock.fixed(
                Instant.parse("2026-10-14T12:15:00Z"),
                ZoneId.of("UTC")
        );

        QuizApplicationService service = new QuizApplicationService(
                questionRepo,
                quizRepo,
                userRepo,
                clock
        );

        User user = new User(telegramId, username, nickname, 0);
        userRepo.saveUserInfo(user);

        assertTrue(service.start(telegramId, 2));

        QuizSession session = quizRepo
                .findQuizSessionByTelegramId(telegramId)
                .orElseThrow();

        FinishQuizResult result = service.abort(telegramId);

        QuizSession savedSession = quizRepo
                .findQuizSessionByTelegramId(telegramId)
                .orElseThrow();

        User savedUser = userRepo
                .findUserByTelegramId(telegramId)
                .orElseThrow();

        assertTrue(savedSession.quiz().isAborted());
        assertEquals(session.sessionId(), result.sessionId());
        assertEquals(result.userTotalScore(), savedUser.getTotalScore());
    }
    @DisplayName("Нельзя ответить на вопрос после отмены квиза")
    @Test
    void cannotAnswerAfterAbortIntegration() {
        QuestionRepository questionRepo = new HardcodedQuestionRepository();
        QuizRepository quizRepo = new HardcodedQuizRepository();
        UserRepository userRepo = new HardcodedUserRepository();

        Clock clock = Clock.fixed(
                Instant.parse("2026-10-14T12:15:00Z"),
                ZoneId.of("UTC")
        );

        QuizApplicationService service = new QuizApplicationService(
                questionRepo,
                quizRepo,
                userRepo,
                clock
        );

        userRepo.saveUserInfo(
                new User(telegramId, username, nickname, 0)
        );

        assertTrue(service.start(telegramId, 2));

        QuizSession session = quizRepo
                .findQuizSessionByTelegramId(telegramId)
                .orElseThrow();

        Question question = session.quiz().getQuestions().get(0);

        service.abort(telegramId);

        assertThrows(
                IllegalStateException.class,
                () -> service.answer(
                        telegramId,
                        question.id(),
                        question.correctAnswerIndexes()
                )
        );

        QuizSession savedSession = quizRepo
                .findQuizSessionByTelegramId(telegramId)
                .orElseThrow();

        assertTrue(savedSession.quiz().isAborted());
    }

    @DisplayName("Нельзя завершить квиз без активной сессии")
    @Test
    void cannotFinishQuizWithoutActiveSession() {
        QuizApplicationService service = new QuizApplicationService(
                new HardcodedQuestionRepository(),
                new HardcodedQuizRepository(),
                new HardcodedUserRepository(),
                Clock.systemUTC()
        );

        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> service.finish(telegramId)
        );

        assertEquals(
                "No active quiz for user with tg id: " + telegramId,
                thrown.getMessage()
        );
    }

}