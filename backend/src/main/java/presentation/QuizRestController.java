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

    QuizRestController(QuizApplicationService QAS){
        this.QAS = QAS;
    }

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
        StartQuizResponse response = new StartQuizResponse(
                QAS.start(
                        request.telegramId(),
                        request.questionCount()));
        return ResponseEntity.ok(response);
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
        FinishQuizResult abortResult = QAS.abort(request.telegramId());
        AbortQuizResponse response = new AbortQuizResponse(
                abortResult.sessionId(),
                abortResult.quizScore(),
                abortResult.userTotalScore());
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
        FinishQuizResult finishResult = QAS.finish(request.telegramId());
        FinishQuizResponse response = new FinishQuizResponse(
                finishResult.sessionId(),
                finishResult.quizScore(),
                finishResult.userTotalScore());
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
        AnswerQuestionResult ansResult = QAS.answer(
                request.telegramId(),
                request.questionId(),
                request.chosen());

        AnswerQuestionResponse response = new AnswerQuestionResponse(
                ansResult.correct(),
                ansResult.correctAnswers(),
                ansResult.currentScore(),
                ansResult.finished());
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
        QuestionView currentQuestion = QAS.presentQuestion(request.telegramId());
        GetCurrentQuestionViewResponse response = new GetCurrentQuestionViewResponse(
                currentQuestion.questionId(),
                currentQuestion.text(),
                currentQuestion.answers());
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
        List<LeaderboardEntry> leaderboard = QAS.getLeaderBoard(request.telegramId());
        GetLeaderboardResponse response = new GetLeaderboardResponse(leaderboard.stream()
                .map(player -> new LeaderboardResponseEntry(
                        player.place(),
                        player.score(),
                        player.nickname()))
                .toList()
        );
        return ResponseEntity.ok(response);
    }

}
