package application.port;

import application.QuizSession;


/*
Application - интерфейс работы с БД квизов
*/
public interface QuizRepository
{
    /*
    Ничего не вовращает. Отправляет данные о новой, текущей, или завершенной сессии в базу данных
    */
    void saveQuizSession(QuizSession quizSession);
}