package infrastructure;

import application.port.UserRepository;
import domain.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HardcodedUserRepository implements UserRepository {

    private final Map<Long, User> users = new HashMap<>();

    /** Возвращает Юзера из словаря по его tg ID
     * @param telegramId - tg ID юзера
     * @return User, если пользователь с таким tg id есть в словаре,
     * ничего, если его нет.
     */
    @Override
    public Optional<User> findUserByTelegramId(long telegramId) {
        return Optional.ofNullable(users.get(telegramId));
    }

    /** Сохраняет данные юзера в словарь, новые данные заменяют старые, если
     * юзер с таким tg ID уже существует
     * @throws IllegalArgumentException если user == null
     */
    @Override
    public void saveUserInfo(User user) {
        if (user == null) {
            throw new IllegalArgumentException("user cannot be null");
        }
        users.put(user.telegramId, user);
    }
}