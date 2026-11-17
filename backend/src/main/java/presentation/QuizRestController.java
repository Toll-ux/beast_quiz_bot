package presentation;


import application.*;
import application.port.QuestionRepository;
import application.port.QuizRepository;
import application.port.UserRepository;
import org.springframework.web.bind.annotation.*;
import presentation.dto.request.*;
import presentation.dto.response.*;
import org.springframework.http.ResponseEntity;

import java.time.Clock;
import java.util.List;

@RestController
@RequestMapping("/api")
public class QuizRestController {

    private final QuizApplicationService QAS;

    /// Инициализация перегрузка Quiz Application Service
    QuizRestController(QuizApplicationService QAS){
        this.QAS = QAS;
    }

    /// Инициализация перегрузка Quiz Application Service
    QuizRestController(QuestionRepository questionRepository,
                       QuizRepository quizRepository,
                       UserRepository userRepository,
                       Clock clock){
        QAS = new QuizApplicationService(
                questionRepository,
                quizRepository,
                userRepository,
                clock);
    }


    /** /start - POST запрос
     *
     * @param request - StartQuizRequest
     *                {"userId": 12345, "questionCount": 2}
     *
     * @return response - ResponseEntity<StartQuizResponse>
     *     {"newQuizCreated": true}
     */
    @PostMapping("/start")
    public ResponseEntity<StartQuizResponse> start(@RequestBody StartQuizRequest request){
        boolean success;

        /// try catch для возможных ошибок, важно, что кроме IllegalArgumentException и IllegalStateException он не должен обрабатывать
        try {
            /// обработка QAS.start
            success = QAS.start(
                    request.userId(),
                    request.questionCount());
        } catch (IllegalArgumentException | IllegalStateException e) {
            /// исключение - всегда 400 (badRequest)
            return ResponseEntity.badRequest().build();
        }
        /// если catch не сработал, то 200 (ok) + json
        return ResponseEntity.ok(new StartQuizResponse(success));
    }

    /** /abort - POST запрос
     *
     * @param request - AbortQuizRequest
     *               {"userId": 12345}
     *
     * @return response - ResponseEntity<AbortQuizResponse>
     *     {"sessionId": 2, "quizScore": 40, "userTotalScore": 50}
     */
    @PostMapping("/abort")
    public ResponseEntity<AbortQuizResponse> abort(@RequestBody AbortQuizRequest request){
        FinishQuizResult abortResult;

        /// try catch для возможных ошибок, важно, что кроме IllegalArgumentException и IllegalStateException он не должен обрабатывать
        try {
            /// обработка QAS.abort
            abortResult = QAS.abort(request.userId());
        } catch (IllegalArgumentException | IllegalStateException e) {
            /// исключение - всегда 400 (badRequest)
            return ResponseEntity.badRequest().build();
        }
        /// переупаковываем данные
        AbortQuizResponse response = new AbortQuizResponse(
                abortResult.sessionId(),
                abortResult.quizScore(),
                abortResult.userTotalScore());
        /// если catch не сработал, то 200 (ok) + json
        return ResponseEntity.ok(response);
    }

    /** /finish - POST запрос
     *
     * @param request - FinishQuizRequest
     *                {"userId": 12345}
     *
     * @return response - ResponseEntity<FinishQuizResponse>
     *     {"sessionId": 3, "quizScore": 44, "userTotalScore": 34}
     */
    @PostMapping("/finish")
    public ResponseEntity<FinishQuizResponse> finish(@RequestBody FinishQuizRequest request){
        FinishQuizResult finishResult;
        /// try catch для возможных ошибок, важно, что кроме IllegalArgumentException и IllegalStateException он не должен обрабатывать
        try {
            finishResult = QAS.finish(request.userId());
        } catch (IllegalArgumentException | IllegalStateException e) {
            /// исключение - всегда 400 (badRequest)
            return ResponseEntity.badRequest().build();
        }
        /// переупаковываем данные
        FinishQuizResponse response = new FinishQuizResponse(
                finishResult.sessionId(),
                finishResult.quizScore(),
                finishResult.userTotalScore());
        /// если catch не сработал, то 200 (ok) + json
        return ResponseEntity.ok(response);
    }

    /** \answer - POST запрос
     *
     * @param request - AnswerQuestionRequest
     *                {	"userId": 12345, "questionId": 2, "chosen": [ 1, 2, 3] }
     *
     * @return response - ResponseEntity<AnswerQuestionResponse>
     *     {"correct": true, "correctAnswers": [1, 3, 4], "currentScore": 44, "finished": true}
     */
    @PostMapping("/answer")
    public ResponseEntity<AnswerQuestionResponse> answer(@RequestBody AnswerQuestionRequest request){
        AnswerQuestionResult ansResult;
        /// try catch для возможных ошибок, важно, что кроме IllegalArgumentException и IllegalStateException он не должен обрабатывать
        try {
            ansResult = QAS.answer(
                    request.userId(),
                    request.questionId(),
                    request.chosen());
        } catch (IllegalArgumentException | IllegalStateException e) {
            /// исключение - всегда 400 (badRequest)
            return ResponseEntity.badRequest().build();
        }
        /// переупаковываем данные
        AnswerQuestionResponse response = new AnswerQuestionResponse(
                ansResult.correct(),
                ansResult.correctAnswers(),
                ansResult.currentScore(),
                ansResult.finished());
        /// если catch не сработал, то 200 (ok) + json
        return ResponseEntity.ok(response);
    }

    /** current_question - GET запрос
     *
     * @param request - GetCurrentQuestionViewRequest
     *                {  "userId": 12345}
     *
     * @return - ResponseEntity<GetCurrentQuestionViewResponse>
     *     {  "questionId": 2, "text": "Какой бог был богом грома в скандинавской мифологии?",
     * 	        "answers": ["Один", "Тор", "Локи", "Фрейя"]
     * 	    }
     */
    @GetMapping("/current_question")
    public ResponseEntity<GetCurrentQuestionViewResponse> presentQuestion(@RequestBody GetCurrentQuestionViewRequest request){
        QuestionView currentQuestion;
        /// try catch для возможных ошибок, важно, что кроме IllegalArgumentException и IllegalStateException он не должен обрабатывать
        try {
            currentQuestion = QAS.presentQuestion(request.userId());
        } catch (IllegalArgumentException | IllegalStateException e) {
            /// исключение - всегда 400 (badRequest)
            return ResponseEntity.badRequest().build();
        }
        /// переупаковываем данные
        GetCurrentQuestionViewResponse response = new GetCurrentQuestionViewResponse(
                currentQuestion.questionId(),
                currentQuestion.text(),
                currentQuestion.answers());
        /// если catch не сработал, то 200 (ok) + json
        return ResponseEntity.ok(response);
    }

    /** leaderboard - GET запрос
     *
     * @param request - GetLeaderboardRequest
     *                {  "userId": 12345}
     *
     * @return response - ResponseEntity<GetLeaderboardResponse>
     *     { "leaderboard": [
     * 	    { "place": 1, "score": 120,"nickname": "Alice" }, ... ]}
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<GetLeaderboardResponse> leaderboard(@RequestBody GetLeaderboardRequest request){
        List<LeaderboardEntry> leaderboard;
        /// try catch для возможных ошибок, важно, что кроме IllegalArgumentException и IllegalStateException он не должен обрабатывать
        try {
            leaderboard = QAS.getLeaderBoard(request.userId());
        } catch (IllegalArgumentException | IllegalStateException e) {
            /// исключение - всегда 400 (badRequest)
            return ResponseEntity.badRequest().build();
        }
        /// переупаковываем данные
        GetLeaderboardResponse response = new GetLeaderboardResponse(leaderboard.stream()
                .map(player -> new LeaderboardResponseEntry(
                        player.place(),
                        player.score(),
                        player.nickname()))
                .toList()
        );
        /// если catch не сработал, то 200 (ok) + json
        return ResponseEntity.ok(response);
    }

}
