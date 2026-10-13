package application.port;

import application.LeaderboardEntry;
import java.util.List;


/**Application - интерфейс работы с БД таблицы лидеров
 * Интерфейсы для работы с бекендом в application, их имплементация в infrastructure
*/



public interface LeaderboardRepository
{
    /** Ничего не возвращает. Сохраняет данные о набранных юзером очков в течение целого квиза */
    void updateHighScore(long telegramId, int score);

    /** Возвращает первые 10 игроков
     * @return список игроков по порядку 0 = 1-ый и тд.
     */
    List<LeaderboardEntry> findTopTen();

    /**Возвращает список игроков от 1 до 3
     * @return игрок 0, первый над юзером, 1 юзер, 2 первый после юзера
     */
    List<LeaderboardEntry> findUserAbdCloset(long telegramId);
}