package application.port;

/*
Интерфейсы для работы с бекендом в application, их имплементация в infrastructure 
*/

/*
Application - интерфейс работы с БД таблицы лидеров
*/

public interface LeaderboardRepository
{
    /* Ничего не возвращает. Сохраняет данные о набранных юзером очков в течение целого квиза */
    void updateHighScore(long telegramId, int score);
}