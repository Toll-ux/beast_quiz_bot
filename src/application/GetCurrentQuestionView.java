package application;

import application.port.QuizRepository;
import domain.Question;
import domain.Quiz;

/**
 * 
 * GetCurrentQuestionView - класс, в котором осуществляется предоставление юзеру обзора текущего 
 *                          вопроса в квизе текущей квиз-сессии юзера
 */
public class GetCurrentQuestionView {

    /* Интерфейс репозитория, в котором хранятся данные о квизах*/
    private final QuizRepository quizRepository;

    /* Конструктор, создает объект GetCurrentQuestionView с конкретным интерфейсом репозитория квизов*/
    public GetCurrentQuestionView (QuizRepository quizRepository) 
    {
        this.quizRepository = quizRepository;
    }

    /**
     * execute - метод, возвращающий обзор текущего вопроса квиза из текущей сессии юзера
     * @param telegramId - tg ID юзера
     * @return обзор текущего вопроса в квизе активной сессии
     */
    public QuestionView execute(long telegramId)
    {
        /* Ищем текущую сессию */
        QuizSession session = quizRepository.findQuizSessionByTelegramId(telegramId)
            .orElseThrow( () -> new IllegalStateException("No active quiz for user with tg id: " + telegramId) );

        /* Берем квиз из текущей сессии */
        Quiz quiz = session.quiz();

        /* Берем текущий вопрос из квиза */
        Question currentQuestion = quiz.getQuestions().get(quiz.getCurrentQuestionIndex());
        
        /* Создаем обзор текущего вопроса */
        QuestionView currentQuestionView = QuestionView.from(currentQuestion);
        
        /* Возвращаем обзор на текущий вопрос */
        return currentQuestionView;
    }
}
