package application;

import application.port.QuestionRepository;
import application.port.QuizRepository;
import domain.Question;
import domain.Quiz;

import java.util.List;
import java.time.Clock;
import java.time.LocalDateTime;

/**StartQuiz - отвечает за шаги (1) - (3) ReadMe (стартует сессию, получает вопросы для квиза)*/
public class StartQuiz
{
    /**questionRepository - интерфейс репозитория вопросов*/
    private final QuestionRepository questionRepository;
    /**quizRepository - интерфейс репозитория квизов*/
    private final QuizRepository quizRepository;
    /**clock - часы*/
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
     * @return id сессии, обзор первого вопроса и количество найденных вопросов
     */
    public StartQuizResult execute(long telegramId, int questionsCount)
    {
        if (telegramId <= 0)
            throw new IllegalArgumentException("Telegram id must be positive");



        if (questionsCount <=0)
        {
            throw new IllegalArgumentException("questionCount must be positive");
        }
        // ищем вопросы
        List<Question> questions = questionRepository.findRandomQuestions(questionsCount);
        if (questions == null || questions.isEmpty()) {
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

        QuestionView firstQuestionView = QuestionView.from(quiz.getQuestions().getFirst());
        return new StartQuizResult(sessionId, questions.size(), firstQuestionView);
    }
    /**
     *
     * @return ид сессии
     * само по себе ид сессии не уникально, образует ключ (для поиска QuizSession) только в паре с telegramId
     */
    public int generateSessionId()
    {
        return (int) ((clock.millis() % Integer.MAX_VALUE) & 0x7FFFFFFF);
    }
}
