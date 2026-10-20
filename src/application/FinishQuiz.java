package application;

import application.port.QuizRepository;
import application.port.UserRepository;
import domain.Quiz;
import domain.User;

public class FinishQuiz
{
    /* Интерфейс репозиториев, в которых хранятся данные о квизах, юзерах и о лидерборде*/
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    /* Конструктор объекта FinishQuiz по конкретным интерфейсам репозиториев */
    public FinishQuiz(QuizRepository quizRepository,
        UserRepository userRepository)
    {
        this.quizRepository = quizRepository;
        this.userRepository = userRepository;
    }

    /**
     * finish - метод, который завершает квиз-сессию юзера со статусом квиза finished
     * @param telegramId - тг id юзера
     * @return сохраняет информацию в leaderboard- и user- репозитории
     */
    public FinishQuizResult finish(long telegramId)
    {
        if (telegramId <= 0)
            throw new IllegalArgumentException("Telegram id must be positive");

        QuizSession session = quizRepository.findQuizSessionByTelegramId(telegramId)
            .orElseThrow( () -> new IllegalStateException("No active quiz for user with tg id: " + telegramId) );
        
        Quiz quiz = session.quiz();

        if (quiz.getScore() >= 0)
        {
            try {
                quiz.finish();
            } catch (IllegalStateException ignored){
                // Если повторно завершаем квиз, ничего не сохраняем в репозиториях
                return new FinishQuizResult(
                        session.sessionId(),
                        0,
                        0
                );
            }
        } else {
            throw new IllegalStateException("Quiz score cannot be negative");
        }

        return persistAndReport(session, quiz);
    }

    /**
     * abort - метод, который завершает квиз-сессию юзера со статусом квиза abort
     * @param telegramId - тг id юзера
     * @return сохраняет информацию в leaderboard- и user- репозитории
     */
    public FinishQuizResult abort(long telegramId)
    {
        if (telegramId <= 0)
            throw new IllegalArgumentException("Telegram id must be positive");

        QuizSession session = quizRepository.findQuizSessionByTelegramId(telegramId)
            .orElseThrow( () -> new IllegalStateException("No active quiz for user with tg id: " + telegramId) );
        
        Quiz quiz = session.quiz();
        if (quiz.getScore() >= 0) {
            try {
                quiz.abort();
            } catch (IllegalStateException ignored) {
                // Если повторно абортим квиз, ничего не сохраняем в репозиториях
                return new FinishQuizResult(
                        session.sessionId(),
                        0,
                        0
                );
            }
        } else {
            throw new IllegalStateException("Quiz score cannot be negative");
        }

        return persistAndReport(session, quiz);
    }

    /**
     * persistAndReport - метод, отвечающий за отправку данных об очках юзера в репозитории
     * @param session - Квиз-сессия юзера
     * @param quiz - квиз
     * @return FinishQuizResult - id сессии, счет квиза, счет очков юзера
     */
    private FinishQuizResult persistAndReport(QuizSession session, Quiz quiz)
    {
        int score = quiz.getScore();

        // Ищем юзера
        User user = userRepository.findUserByTelegramId(session.telegramId())
            .orElseThrow(() -> new IllegalStateException("User not found"));
        
        // Сохраняем данные об очках юзера в userRepository
        user.updateTotalScore(score);
        userRepository.updateUserInfo(user);

        // Сохраняем результат сессии
        quizRepository.updateQuizSession(session);

        return new FinishQuizResult(
            session.sessionId(),
            score,
            user.getTotalScore()
        );
    }
}