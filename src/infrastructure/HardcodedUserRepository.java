package infrastructure;

import application.LeaderboardEntry;
import application.port.UserRepository;
import domain.User;

import java.util.*;

import static java.util.Comparator.comparingInt;

public class HardcodedUserRepository implements UserRepository {

    private final Map<Long, User> users = new HashMap<>();

    /** Возвращает Юзера из словаря по его tg ID
     * @param telegramId - tg ID юзера
     * @return User, если пользователь с таким tg id есть в словаре,
     * ничего, если его нет.
     */
    @Override
    public Optional<User> findUserByTelegramId(long telegramId) {
        if (telegramId <= 0) {
            throw new IllegalArgumentException("Telegram id must be positive");
        }
        return Optional.ofNullable(users.get(telegramId))
                .map(User::new); // копия создаётся только если пользователь найден
    }

    /** Предполагает, что юзера нет в БД, и добавляет его копию туда
     * @throws IllegalStateException если User с таким tg ID уже есть в БД
     * @throws IllegalArgumentException если user == null
     */
    @Override
    public void saveUserInfo(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (users.containsKey(user.telegramId)) {
            throw new IllegalStateException("User with this id already exists");
        }
        users.put(user.telegramId, new User(user));
    }

    /** Предполагает, что юзер уже есть в БД, новые данные (копия) юзера заменяют старые,
     * @throws NoSuchElementException если юзера с таким tg ID не существует
     * @throws IllegalArgumentException если user == null
     */
    @Override
    public void updateUserInfo(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (!users.containsKey(user.telegramId)){
            throw new NoSuchElementException("User with this id does not exist");
        }
        users.put(user.telegramId, new User(user));
    }

    /** Места начинаются с 1.
     * @return информацию о первых 10 пользователях с наибольшим количеством очков
     * в виде списка LeaderBoardEntry(int place, int score, string nickname)
     * если в БД меньше 10 пользователей - вернет все, сколько есть
     */
    @Override
    public List<LeaderboardEntry> findTopTen() {
        List<User> topUsers = users.values().stream()
                .sorted(comparingInt(User::getTotalScore).reversed())
                .limit(10)
                .toList();

        List<LeaderboardEntry> result = new ArrayList<>(topUsers.size());
        for (int i = 0; i < topUsers.size(); i++) {
            User u = topUsers.get(i);
            result.add(new LeaderboardEntry(i + 1, u.getTotalScore(), u.getNickname()));
        }
        return result;
    }

    /** @param telegramId - tg ID пользователя
     *  @return Информацию о положении пользователя в доске лидеров и
     *  одном пользователе ниже и выше него (если такие есть)
     *  в виде списка LeaderBoardEntry(int place, int score, string nickname)
     *  @throws IllegalArgumentException если telegramId <= 0
     *  если юзера с таким tg ID не существует - возвращает пустой список
     */
    @Override
    public List<LeaderboardEntry> findUserAndClosest(long telegramId){
        if (telegramId <= 0) {
            throw new IllegalArgumentException("Telegram id must be positive");
        }

        // 1. Сортирует всех игроков по убыванию счёта
        List<Map.Entry<Long, User>> sorted = users.entrySet().stream()
                .sorted(Comparator.comparingInt((Map.Entry<Long, User> u) -> u.getValue().getTotalScore())
                        .reversed()
                        .thenComparingLong(Map.Entry::getKey)).toList();

        // 2. Находит индекс пользователя
        int userIndex = -1;
        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).getKey() == telegramId) {
                userIndex = i;
                break;
            }
        }

        // 3. Если пользователя нет — возвращает пустой список
        if (userIndex == -1) {
            return List.of();
        }

        // 4. Собирает кусок: 1 выше, сам пользователь, 1 ниже
        // Учитывая случаи, если пользователь на 1 месте (нет никого выше)
        // или на последнем (нет никого ниже)
        List<LeaderboardEntry> result = new ArrayList<>();

        if (userIndex > 0) {
            Map.Entry<Long, User> above = sorted.get(userIndex - 1);
            result.add(new LeaderboardEntry(userIndex, above.getValue().getTotalScore(), above.getValue().getNickname()));
        }

        Map.Entry<Long, User> user = sorted.get(userIndex);
        result.add(new LeaderboardEntry(userIndex + 1, user.getValue().getTotalScore(), user.getValue().getNickname()));

        if (userIndex < sorted.size() - 1) {
            Map.Entry<Long, User> below = sorted.get(userIndex + 1);
            result.add(new LeaderboardEntry(userIndex + 2, below.getValue().getTotalScore(), below.getValue().getNickname()));
        }

        return result;
    }

    /**  метод для тестов
    */
    Map<Long, User> usersForTest() {
        return users;
    }
}