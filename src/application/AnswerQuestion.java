package application;

import application.port.QuizRepository;
import domain.AnswerResult;
import domain.Question;
import domain.Quiz;

import java.util.Set;

/**
 * 
 * AnswerQuestion - класс, в котором осуществляется проверка ответа клиента на корректность и предоставление следующего вопроса
 */

public class AnswerQuestion
{   /* Интерфейс репозитория, в котором хранятся данные о квизах*/
    private final QuizRepository quizRepository;
    /* Конструктор, создает объект AnswerQuestion с конкретным интерфейсом репозитория квизов*/
    public AnswerQuestion(QuizRepository quizRepository)
    {
        this.quizRepository = quizRepository;
    }
    /**
     * execute - метод, который закрепляет и проверяет ответ клиента на текущий вопрос текущей квиз сессии юзера
     * @param telegramId
     * @param questionId
     * @param selectedAnswers
     * @return возвращает по текущему вопросу: 1) правильный ли ответ, 2) список правильных ответов;
     * по квизу: 3) текущее число очков, набранное за квиз, 4) обзор следующего вопроса, 5) завершен ли квиз.
     * 
     * @throws IllegalStateException если у юзера нет активного квиза, 
     *                               если вопрос, на который ожидался ответ не совпадает с переданным методу 
     */
    public AnswerQuestionResult execute(long telegramId, int questionId, Set<Integer> selectedAnswers)
    {
        /* Ищем текущую сессию */
        QuizSession session = quizRepository.findQuizSessionByTelegramId(telegramId)
            .orElseThrow( () -> new IllegalStateException("No active quiz for user with tg id: " + telegramId) );

        /* Берем квиз из текущей сессии */
        Quiz quiz = session.quiz();

        /* Берем текущий вопрос из квиза */
        Question currentQuestion = quiz.getQuestions().get(quiz.getCurrentQuestionIndex());

        /* Сравниваем переданный questionId с id текущего вопроса в квизе */
        if (currentQuestion.id() != questionId)
        {   /* Исключение, если id не совпадают */
            throw new IllegalStateException( "Answered the wrong question. Expected " + currentQuestion.id() 
            + ", got " + questionId );
        }

        /* Закрепляем ответ юзера (передаем ответ) */
        AnswerResult answerResult = quiz.submitAnswer(selectedAnswers);

        /* Создаем переменные обзора следующего вопроса и завершен ли квиз */
        QuestionView nextQuestionView = null;
        boolean finished = false;

        /* Смотрим, был ли текущий вопрос последним */
        int nextIndex = quiz.getCurrentQuestionIndex() + 1;
        if (nextIndex >= quiz.getQuestions().size())
        {
            quiz.finish();
            finished = true;
        } else {
            quiz.nextQuestion();
            nextQuestionView = QuestionView.from(quiz.getQuestions().get(quiz.getCurrentQuestionIndex()));
        }

        /* Обновляем состояние сессии юзера */
        quizRepository.updateQuizSession(session);

        return new AnswerQuestionResult(
            answerResult.correct(),
            answerResult.correctAnswers(),
            quiz.getScore(),
            nextQuestionView,
            finished
        );
    }
}