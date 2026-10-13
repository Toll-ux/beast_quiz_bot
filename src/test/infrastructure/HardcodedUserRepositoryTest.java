package infrastructure;

import domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HardcodedUserRepositoryTest {

    static final int id = 1;
    static final long tgId = 123333;
    static final String username = "@qwer";
    static final String nickname = "alex123";
    static final int totalScore = 123;

    @DisplayName("Поиск пользователя, пользователь существует")
    @Test
    void findExistingUser() {
        HardcodedUserRepository repository =
                new HardcodedUserRepository();

        User user = new User(id, tgId);

        repository.saveUserInfo(user);

        assertEquals(
                user,
                repository.findUserByTelegramId(tgId).get()
        );
    }

    @DisplayName("Поиск пользователя, пользователя нет")
    @Test
    void findNonExistingUser() {
        HardcodedUserRepository repository =
                new HardcodedUserRepository();

        assertTrue(
                repository.findUserByTelegramId(tgId).isEmpty()
        );
    }

    @DisplayName("Ошибка поиска пользователя с отрицательным Telegram ID")
    @Test
    void findUserNegativeTelegramId() {
        HardcodedUserRepository repository =
                new HardcodedUserRepository();

        IllegalArgumentException thrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> repository.findUserByTelegramId(-1)
                );

        assertEquals(
                "Telegram id must be positive",
                thrown.getMessage()
        );
    }

    @DisplayName("Ошибка поиска пользователя с нулевым Telegram ID")
    @Test
    void findUserZeroTelegramId() {
        HardcodedUserRepository repository =
                new HardcodedUserRepository();

        IllegalArgumentException thrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> repository.findUserByTelegramId(0)
                );

        assertEquals(
                "Telegram id must be positive",
                thrown.getMessage()
        );
    }

    @DisplayName("Сохранение нового пользователя")
    @Test
    void saveNewUser() {
        HardcodedUserRepository repository =
                new HardcodedUserRepository();

        User user = new User(id, tgId);

        repository.saveUserInfo(user);

        assertEquals(
                user,
                repository.findUserByTelegramId(tgId).get()
        );
    }

    @DisplayName("Ошибка сохранения null пользователя")
    @Test
    void saveNullUser() {
        HardcodedUserRepository repository =
                new HardcodedUserRepository();

        IllegalArgumentException thrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> repository.saveUserInfo(null)
                );

        assertEquals(
                "user cannot be null",
                thrown.getMessage()
        );
    }

    @DisplayName("Новые данные пользователя заменяют старые")
    @Test
    void saveExistingUserUpdatesInformation() {
        HardcodedUserRepository repository =
                new HardcodedUserRepository();

        User oldUser = new User(id, tgId);
        User newUser = new User(id, tgId, username, nickname, totalScore);

        repository.saveUserInfo(oldUser);
        repository.saveUserInfo(newUser);

        assertEquals(
                newUser,
                repository.findUserByTelegramId(tgId).get()
        );

        assertNotEquals(
                oldUser,
                repository.findUserByTelegramId(tgId).get()
        );
    }
}