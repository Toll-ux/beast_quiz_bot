package application;

import application.port.QuestionRepository;
import application.port.QuizRepository;
import application.port.UserRepository;
import domain.User;
import domain.Question;
import domain.Quiz;

import java.util.List;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

/**StartQuiz - отвечает за шаги (1) - (3) ReadMe (стартует сессию, получает вопросы для квиза)*/
public class StartQuiz
{
    /**userRepository - интерфейс репозитория юзера */
    private final UserRepository userRepository;
    /**questionRepository - интерфейс репозитория вопросов*/
    private final QuestionRepository questionRepository;
    /**quizRepository - интерфейс репозитория квизов*/
    private final QuizRepository quizRepository;
    /**clock - часы*/
    private final Clock clock;

    public StartQuiz(UserRepository userRepository, QuestionRepository questionRepository, QuizRepository quizRepository, Clock clock)
    {
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
        this.quizRepository = quizRepository;
        this.clock = clock;
    }
    /**
     *
     * @param telegramId - id юзера
     * @param questionsCount - количество вопросов
     * @return true, если создали новый квиз и соответствующую сессию, иначе - false.
     */
    public boolean execute(long telegramId, int questionsCount)
    {
        if (telegramId <= 0)
            throw new IllegalArgumentException("Telegram id must be positive");

        // Проверяем, есть ли юзер в репозитории. Если нет - создаем
        Optional<User> maybeUser = userRepository.findUserByTelegramId(telegramId);
        if (maybeUser.isEmpty())
        {
            User newUser = new User(telegramId);
            userRepository.saveUserInfo(newUser);
        }

        // Проверяем, есть ли у юзера незаконченный квиз. Если есть, новый не создаем
        Optional<QuizSession> maybeLastQuizSession = quizRepository.findQuizSessionByTelegramId(telegramId);
        if (maybeLastQuizSession.isPresent())
        {
            QuizSession lastQuizSession = maybeLastQuizSession.get();
            Quiz lastQuiz = lastQuizSession.quiz();
            // Если последний начатый квиз не закончен, используем его
            if (!(lastQuiz.isAborted() || lastQuiz.isFinished()))
            {
                // Если квиз не начат, начнем и обновим сессию
                if (lastQuiz.isDoingNothing()){
                    lastQuiz.start();
                    quizRepository.updateQuizSession(lastQuizSession);
                }
                // Новый квиз не создавали, так что возвращаем false;
                return false;
            }
        }

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
        return true;
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
