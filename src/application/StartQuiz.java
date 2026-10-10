package application;

import application.port.QuestionRepository;
import application.port.QuizRepository;
import domain.Question;
import domain.Quiz;

import java.util.List;
import java.time.Clock;

/**
 * 
 * StartQuiz - отвечает за шаги (1) - (3) ReadMe (стартует сессию, получает вопросы для квиза)
 * @param questionRepository - интерфейс репозитория вопросов
 * @param quizRepository - интерфейс репозитория квизов
 * @param clock - 
 */

public class StartQuiz
{
    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;
    private final Clock clock;

    public StartQuiz(QuestionRepository questionRepository, QuizRepository quizRepository, Clock clock)
    {
        this.questionRepository = questionRepository;
        this.quizRepository = quizRepository;
        this.clock = clock;
    }
    /**
     * 
     * @param telegramId - id юзера
     * @param questionsCount - количество вопросов
     * @return id сессии и количество найденных вопросов
     */
    public StartQuizResult execute(long telegramId, int questionsCount)
    {
        if (questionsCount <=0)
        {
            throw new IllegalArgumentException("questionCount must be positive");            
        }
        // ищем вопросы
        List<Question> questions = questionRepository.findRandomQuestions(questionsCount);
        if (questions.isEmpty()) {
            throw new IllegalStateException("question list is empty");
        }

        // создаем квиз
        Quiz quiz = new Quiz(questions);
        quiz.start();

        // квиз + id юзера + id сессии -> сессия
        int sessionId = generateSessionId();
        LocalDateTime now = LocalDateTime.now(clock);
        QuizSession quizSession = new QuizSession(sessionId, telegramId, quiz, now);

        // сохраняем сессию
        quizRepository.saveQuizSession(quizSession);
        return new StartQuizResult(sessionId, questions.size());
    }
    /**
     * 
     * @return ид сессии
     * само по себе ид сессии не уникально, образует ключ (для поиска QuizSession) только в паре с telegramId
     */
    public int generateSessionId()
    {
        return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
    }
}
