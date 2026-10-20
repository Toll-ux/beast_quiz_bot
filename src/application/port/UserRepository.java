package application.port;

import application.LeaderboardEntry;
import domain.User;

import java.util.List;
import java.util.Optional;


/**Application - интерфейс работы с БД юзеров, в том числе и leaderboard
 * Интерфейсы для работы с бекендом в application, их имплементация в infrastructure
 */
public interface UserRepository
{
    /**Возвращает Юзера из БД по его тгИД*/
    Optional<User> findUserByTelegramId(long telegramId);

    /**Предполагает, что юзера нет в БД, и сохраняет его данные в БД*/
    void saveUserInfo(User user);

    /**Предполагает, что юзер уже есть в БД, новые данные юзера заменяют старые*/
    void updateUserInfo(User user);

    /** Возвращает первые 10 игроков
     * @return список игроков по порядку 0 = 1-ый и тд.
     * Места (int place) в LeaderboardEntry начинаются с 1
     */
    List<LeaderboardEntry> findTopTen();

    /**Возвращает список игроков от 1 до 3
     * @return игрок 0, первый над юзером, 1 юзер, 2 первый после юзера
     */
    List<LeaderboardEntry> findUserAndClosest(long telegramId);
}