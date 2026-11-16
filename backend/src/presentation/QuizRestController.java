package presentation;

import application.*;
import application.port.QuestionRepository;
import application.port.QuizRepository;
import application.port.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import presentation.dto.request.*;
import presentation.dto.response.*;

import java.time.Clock;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * QuizRestController - REST контроллер для взаимодействия с квиз-сервисом
 */
@RestController
@RequestMapping("/api")
public class QuizRestController {

    private final QuizApplicationService QAS;

    public QuizRestController(QuizApplicationService qas) {
        this.QAS = qas;
    }

    public QuizRestController(
            QuestionRepository questionRepository,
            QuizRepository quizRepository,
            UserRepository userRepository,
            Clock clock) {
        this(new QuizApplicationService(questionRepository, quizRepository, userRepository, clock));
    }

    /**
     * Начало квиза
     *
     * @param request Запрос: { "userId": 12345, "questionCount": 2 }
     * @return Ответ: { "newQuizCreated": true }
     */
    @PostMapping("/start")
    public ResponseEntity<StartQuizResponse> start(@RequestBody StartQuizRequest request) {
        StartQuizResponse responseBody = new StartQuizResponse(
                QAS.start(request.userId(), request.questionCount())
        );
        return ResponseEntity.ok(responseBody);
    }

    /**
     * Принятие ответа пользователя
     *
     * @param request Запрос: { "userId": 12345, "questionId": 2, "chosen": [ 1, 2, 3] }
     * @return Ответ: { "correct": true, "correctAnswers": [1, 3, 4], "currentScore": 44, "finished": true }
     */
    @PostMapping("/answer")
    public ResponseEntity<AnswerQuestionResponse> answer(@RequestBody AnswerQuestionRequest request) {
        AnswerQuestionResult result = QAS.answer(request.userId(), request.questionId(), request.chosen());
        AnswerQuestionResponse responseBody = new AnswerQuestionResponse(
                result.correct(), result.correctAnswers(), result.currentScore(), result.finished()
        );
        return ResponseEntity.ok(responseBody);
    }

    /**
     * Завершение квиза
     *
     * @param request Запрос: { "userId": 12345 }
     * @return Ответ: { "sessionId": 3, "quizScore": 44, "userTotalScore": 34 }
     */
    @PostMapping("/finish")
    public ResponseEntity<FinishQuizResponse> finish(@RequestBody FinishQuizRequest request) {
        FinishQuizResult result = QAS.finish(request.userId());
        FinishQuizResponse responseBody = new FinishQuizResponse(
                result.sessionId(), result.quizScore(), result.userTotalScore()
        );
        return ResponseEntity.ok(responseBody);
    }

    /**
     * Прерывание квиза
     *
     * @param request Запрос: { "userId": 12345 }
     * @return Ответ: { "sessionId": 2, "quizScore": 40, "userTotalScore": 50 }
     */
    @PostMapping("/abort")
    public ResponseEntity<AbortQuizResponse> abort(@RequestBody AbortQuizRequest request) {
        FinishQuizResult result = QAS.abort(request.userId());
        AbortQuizResponse responseBody = new AbortQuizResponse(
                result.sessionId(), result.quizScore(), result.userTotalScore()
        );
        return ResponseEntity.ok(responseBody);
    }

    /**
     * Обзор текущего вопроса
     *
     * @param request Запрос: { "userId": 12345 }
     * @return Ответ: { "questionId": 2, "text": "...", "answers": ["...", "..."] }
     */
    @GetMapping("/current_question")
    public ResponseEntity<GetCurrentQuestionViewResponse> presentQuestion(@RequestBody GetCurrentQuestionViewRequest request) {
        QuestionView result = QAS.presentQuestion(request.userId());
        GetCurrentQuestionViewResponse responseBody = new GetCurrentQuestionViewResponse(
                result.questionId(), result.text(), result.answers()
        );
        return ResponseEntity.ok(responseBody);
    }

    /**
     * Возврат таблицы лидеров
     *
     * @param request Запрос: { "userId": 12345 }
     * @return Ответ: { "leaderboard": [ { "place": 1, "score": 120, "nickname": "Alice" } ] }
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<GetLeaderboardResponse> leaderboard(@RequestBody GetLeaderboardRequest request) {
        List<LeaderboardEntry> result = QAS.getLeaderBoard(request.userId());
        List<LeaderboardResponseEntry> leaderboardEntries = result.stream()
                .map(entry -> new LeaderboardResponseEntry(entry.place(), entry.score(), entry.nickname()))
                .collect(Collectors.toList());

        GetLeaderboardResponse responseBody = new GetLeaderboardResponse(leaderboardEntries);
        return ResponseEntity.ok(responseBody);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class, NoSuchElementException.class})
    public ResponseEntity<String> handleBadRequest(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
