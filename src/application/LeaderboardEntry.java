package application;

/** LeaderboardEntry - Контейнер для передачи данных из port 2 app 2 pres
 *
 * @param place - место пользователя в списке
 * @param score - счет пользователя
 * @param telegramId - id пользователя
 */

public record LeaderboardEntry(int place, int score, long telegramId) {

}
