package application;

import application.port.*;

import java.util.List;
import java.util.Set;
import java.time.Clock;

/**
 * 
 * QuizApplicationService - один общий класс для всего сервиса application
 */

public class QuizApplicationService
{

    // Объекты StartQuiz, AnswerQuestion, FinishQuiz, LeaderBoard созданные по определенному набору интерфейсов репозиториев
    private final StartQuiz startQuiz;
    private final GetCurrentQuestionView getCurrentQuestionView;
    private final AnswerQuestion answerQuestion;
    private final FinishQuiz finishQuiz;
    private final LeaderBoard leaderBoard;

    // Конструктор объекта QuizApplicationService на основе связки интерфейсов репозиториев вопросов, квизов, юзеров и лидерборда
    public QuizApplicationService(
        QuestionRepository questionRepository,
        QuizRepository quizRepository,
        UserRepository userRepository,
        Clock clock
    ) {
        
        this.startQuiz = new StartQuiz(questionRepository, quizRepository, clock);
        this.getCurrentQuestionView = new GetCurrentQuestionView(quizRepository);
        this.answerQuestion = new AnswerQuestion(quizRepository);
        this.finishQuiz = new FinishQuiz(quizRepository, userRepository);
        this.leaderBoard = new LeaderBoard(userRepository);
    }

    /** Метод, начинающий квиз */
    public boolean start(long telegramId, int count)
    {
        return startQuiz.execute(telegramId, count);
    }

    /** Метод, возвращающий обзор текущего вопроса */
    public QuestionView presentQuestion(long telegramId)
    {
        return getCurrentQuestionView.execute(telegramId);
    }

    /** Метод, закрепляющий ответ юзера на 1 текущий вопрос */
    public AnswerQuestionResult answer(long telegramId, int questionId, Set<Integer> chosen)
    {
        return answerQuestion.execute(telegramId, questionId, chosen);
    }

    /** Метод, завершающий квиз в статусе finished */
    public FinishQuizResult finish(long telegramId)
    {
        return finishQuiz.finish(telegramId);
    }

    /** Метод, завершающий квиз в статусе aborted */
    public FinishQuizResult abort(long telegramId)
    {
        return finishQuiz.abort(telegramId);
    }

    /** Метод, после квиз давать лидерборд */
    public List<LeaderboardEntry> getLeaderBoard(long telegramId)
    {
        return leaderBoard.getUsersPosition(telegramId);
    }
}