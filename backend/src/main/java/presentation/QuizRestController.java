package main.java.presentation;


import main.java.application.*;
import main.java.application.port.QuestionRepository;
import main.java.application.port.QuizRepository;
import main.java.application.port.UserRepository;
import main.java.presentation.dto.request.*;
import main.java.presentation.dto.response.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.util.List;

@RestController
//@PostMapping(value = "/api")
public class QuizRestController {

    QuizApplicationService QAS;

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


    /** start
     *
     * @param {
     * 	"userId": 12345
     * 	"questionCount": 2
     * }
     * @return{
     * 	"newQuizCreated": true
     * }
     */
    @GetMapping("/start")
    public ResponseEntity<StartQuizResponse> start(@RequestBody StartQuizRequest request){
        StartQuizResponse response = new StartQuizResponse(
                QAS.start(
                        request.telegramId(),
                        request.questionCount()));
        return ResponseEntity.ok(response);
    }

    /**
     * abort
     *
     * @param{
     *    "userId": 12345
     *     }
     * @return
     * {
     * 	"sessionId": 2, Можно убрать?
     * 	"quizScore": 40,
     * 	"userTotalScore": 50
     * }
     */
    @GetMapping("/abort")
    public ResponseEntity<AbortQuizResponse> abort(@RequestBody AbortQuizRequest request){
        FinishQuizResult abortResult = QAS.abort(request.telegramId());
        AbortQuizResponse response = new AbortQuizResponse(
                abortResult.sessionId(),
                abortResult.quizScore(),
                abortResult.userTotalScore());
        return ResponseEntity.ok(response);
    }

    /**finish
     *
     * @param {
     * 	"userId": 12345
     * }
     * @retrun {
     * 	"sessionId": 3,
     * 	"quizScore":  44,
     * 	"userTotalScore": 34
     * }
     */
    @GetMapping("/finish")
    public ResponseEntity<FinishQuizResponse> finish(@RequestBody FinishQuizRequest request){
        FinishQuizResult finishResult = QAS.finish(request.telegramId());
        FinishQuizResponse response = new FinishQuizResponse(
                finishResult.sessionId(),
                finishResult.quizScore(),
                finishResult.userTotalScore());
        return ResponseEntity.ok(response);
    }

    /**answer
     *
     * @param {
     * 	"userId": 12345,
     * 	"questionId": 2,
     * 	"chosen": [ 1, 2, 3]
     * }
     * @return {
     * 	"correct": true,
     * 	"correctAnswers": [1, 3, 4],
     * 	"currentScore": 44,
     * 	"finished": true
     * }
     */
    @GetMapping("/answer")
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

    /**current_question
     *
     * @param {
     * 	"userId": 12345
     * }
     * @return  {  "questionId": 2,
     * 	"text": "Какой бог был богом грома в скандинавской мифологии?",
     * 	"answers": [
     * 		"Один",
     * 		"Тор",
     * 		"Локи",
     * 		"Фрейя"
     * 	]
     * }
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

    /** leaderboard
     *
     * @param {
     * 	"userId": 12345
     * }
     * @return{
     * "leaderboard": [
     * 	    {
     * 		"place": 1,
     * 		"score": 120,
     * 		"nickname": "Alice"
     *    }]
     * }
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
