package application.port;

import application.QuizSession;

import java.util.Optional;
/*
Интерфейсы для работы с бекендом в application, их имплементация в infrastructure 
*/

/*
Application - интерфейс работы с БД квизов
*/
public interface QuizRepository
{
    /*
    Ничего не вовращает. Отправляет данные о новой сессии в базу данных
    */
    void saveQuizSession(QuizSession quizSession);
    /*
    Ничего не вовращает. Отправляет данные о текущей, или завершенной сессии в базу данных
    */
    void updateQuizSession(QuizSession quizSession);
    /*
    Возвращает текущую квиз-сессию юзера (их <=1 для каждого юзера) по тг id юзера
    */
    Optional<QuizSession> findQuizSessionByTelegramId(long telegramId);
}