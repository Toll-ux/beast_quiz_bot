package application;

/** LeaderboardEntry - Контейнер для передачи данных из port 2 app 2 pres
 *
 * @param place - место пользователя в списке
 * @param score - счет пользователя
 * @param nickname - ник пользователя
 */

public record LeaderboardEntryNicknames (int place, int score, String nickname){}
