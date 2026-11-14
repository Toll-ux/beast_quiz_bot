package infrastructure;

import application.LeaderboardEntry;
import domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class HardcodedUserRepositoryTest {

    static final long tgId = 123333;
    static final String username = "@qwer";
    static final String nickname = "alex123";
    static final int totalScore = 123;

    @DisplayName("Поиск пользователя, пользователь существует")
    @Test
    void findExistingUser() {
        HardcodedUserRepository repository =
                new HardcodedUserRepository();

        User user = new User(tgId, username, nickname, totalScore);

        repository.usersForTest().put(user.telegramId, user);

        Optional<User> found = repository.findUserByTelegramId(tgId);

        assertTrue(found.isPresent());
        assertEquals(tgId, found.get().telegramId);
        assertEquals(username, found.get().getUsername());
        assertEquals(nickname, found.get().getNickname());
        assertEquals(totalScore, found.get().getTotalScore());
        assertTrue(found.get().isUserNormalPlayer());
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

    @DisplayName("Изменение Nickname найденного пользователя не меняет его Nickname в HashMap")
    @Test
    void changeFoundUsersNickname() {
        HardcodedUserRepository repository =
                new HardcodedUserRepository();

        User user = new User(tgId, username, nickname, totalScore);

        repository.usersForTest().put(user.telegramId, user);

        User found = repository.findUserByTelegramId(tgId).get();

        found.setNickname("new nickname");

        User inMap = repository.findUserByTelegramId(tgId).get();

        assertNotEquals(found.getNickname(), inMap.getNickname());
    }

    @DisplayName("Изменение Username найденного пользователя не меняет его Username в HashMap")
    @Test
    void changeFoundUsersUsername() {
        HardcodedUserRepository repository =
                new HardcodedUserRepository();

        User user = new User(tgId, username, nickname, totalScore);

        repository.usersForTest().put(user.telegramId, user);

        User found = repository.findUserByTelegramId(tgId).get();

        found.setUsername("@newUsername");

        User inMap = repository.findUserByTelegramId(tgId).get();

        assertNotEquals(found.getUsername(), inMap.getUsername());
    }

    @DisplayName("Изменение totalScore найденного пользователя не меняет его totalScore в HashMap")
    @Test
    void changeFoundUsersTotalScore() {
        HardcodedUserRepository repository =
                new HardcodedUserRepository();

        User user = new User(tgId, username, nickname, totalScore);

        repository.usersForTest().put(user.telegramId, user);

        User found = repository.findUserByTelegramId(tgId).get();

        found.updateTotalScore(totalScore + 10);

        User inMap = repository.findUserByTelegramId(tgId).get();

        assertNotEquals(found.getTotalScore(), inMap.getTotalScore());
    }

    @DisplayName("Изменение permission найденного пользователя не меняет его permission в HashMap")
    @Test
    void changeFoundUsersPermission() {
        HardcodedUserRepository repository =
                new HardcodedUserRepository();

        User user = new User(tgId, username, nickname, totalScore);

        repository.usersForTest().put(user.telegramId, user);

        User found = repository.findUserByTelegramId(tgId).get();

        found.becomeAdmin();

        User inMap = repository.findUserByTelegramId(tgId).get();

        assertNotEquals(found.isUserAdmin(), inMap.isUserAdmin());
        assertNotEquals(found.isUserNormalPlayer(), inMap.isUserNormalPlayer());
    }

    @DisplayName("Сохранение нового пользователя")
    @Test
    void saveNewUser() {
        HardcodedUserRepository repository =
                new HardcodedUserRepository();

        User user = new User(tgId, username, nickname, totalScore);

        repository.saveUserInfo(user);

        Map<Long, User> users = repository.usersForTest();

        assertTrue(users.containsKey(user.telegramId));
        User found = users.get(user.telegramId);
        assertEquals(tgId, found.telegramId);
        assertEquals(username, found.getUsername());
        assertEquals(nickname, found.getNickname());
        assertEquals(totalScore, found.getTotalScore());
        assertTrue(found.isUserNormalPlayer());
    }

    @DisplayName("Поиск сохраненного пользователя по tgID выдает того же пользователя")
    @Test
    void saveThenFind_ReturnSameUser() {
        HardcodedUserRepository repository =
                new HardcodedUserRepository();
        User user = new User(tgId, username, nickname, totalScore);

        repository.saveUserInfo(user);

        Optional<User> found = repository.findUserByTelegramId(tgId);

        assertTrue(found.isPresent());
        assertEquals(tgId, found.get().telegramId);
        assertEquals(username, found.get().getUsername());
        assertEquals(nickname, found.get().getNickname());
        assertEquals(totalScore, found.get().getTotalScore());
        assertTrue(found.get().isUserNormalPlayer());
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
                "User cannot be null",
                thrown.getMessage()
        );
    }

    @DisplayName("Ошибка существования пользователя с таким ID")
    @Test
    void saveExistingUser() {
        HardcodedUserRepository repository =
                new HardcodedUserRepository();

        User user = new User(tgId, username, nickname, totalScore);

        repository.usersForTest().put(user.telegramId, user);

        IllegalStateException thrown =
                assertThrows(
                        IllegalStateException.class,
                        () -> repository.saveUserInfo(user)
                );

        assertEquals(
                "User with this id already exists",
                thrown.getMessage()
        );
    }

    @DisplayName("Изменение Nickname сохраненного пользователя не меняет Nickname сохраненной версии")
    @Test

    void changeSavedNickname(){
        HardcodedUserRepository repository =
                new HardcodedUserRepository();

        User user = new User(tgId, username, nickname, totalScore);

        repository.saveUserInfo(user);

        user.setNickname("new nickname");

        User saved = repository.usersForTest().get(user.telegramId);

        assertNotEquals(user.getNickname(), saved.getNickname());
    }

    @DisplayName("Изменение Username сохраненного пользователя не меняет Username сохраненной версии")
    @Test

    void changeSavedUsername(){
        HardcodedUserRepository repository =
                new HardcodedUserRepository();

        User user = new User(tgId, username, nickname, totalScore);

        repository.saveUserInfo(user);

        user.setUsername("@newUsername");

        User saved = repository.usersForTest().get(user.telegramId);

        assertNotEquals(user.getUsername(), saved.getUsername());
    }

    @DisplayName("Изменение totalScore сохраненного пользователя не меняет totalScore сохраненной версии")
    @Test

    void changeSavedTotalScore(){
        HardcodedUserRepository repository =
                new HardcodedUserRepository();

        User user = new User(tgId, username, nickname, totalScore);

        repository.saveUserInfo(user);

        user.updateTotalScore(totalScore + 10);

        User saved = repository.usersForTest().get(user.telegramId);

        assertNotEquals(user.getTotalScore(), saved.getTotalScore());
    }

    @DisplayName("Изменение permission сохраненного пользователя не меняет permission сохраненной версии")
    @Test

    void changeSavedPermission(){
        HardcodedUserRepository repository =
                new HardcodedUserRepository();

        User user = new User(tgId, username, nickname, totalScore);

        repository.saveUserInfo(user);

        user.becomeAdmin();

        User saved = repository.usersForTest().get(user.telegramId);

        assertNotEquals(user.isUserNormalPlayer(), saved.isUserNormalPlayer());
        assertNotEquals(user.isUserAdmin(), saved.isUserAdmin());

    }

    @DisplayName("Новые данные пользователя заменяют старые при обновлении")
    @Test
    void saveExistingUserUpdatesInformation() {
        HardcodedUserRepository repository =
                new HardcodedUserRepository();

        User old = new User(tgId, username, nickname, totalScore);
        repository.saveUserInfo(old);

        User updated = new User(tgId, "", "", totalScore + 10);
        updated.becomeAdmin();
        repository.updateUserInfo(updated);

        User stored = repository.usersForTest().get(tgId);
        assertEquals(tgId, stored.telegramId);
        assertEquals("", stored.getNickname());
        assertEquals("", stored.getUsername());
        assertEquals(totalScore + 10, stored.getTotalScore());
        assertEquals(stored.isUserNormalPlayer(), updated.isUserNormalPlayer());
    }

    @DisplayName("Update не мутирует исходный объект вместо замены в map")
    @Test
    void updateUserInfo_shouldNotMutatePassedObject() {
        HardcodedUserRepository repository =
                new HardcodedUserRepository();

        User old = new User(tgId, username, nickname, totalScore);
        repository.saveUserInfo(old);

        User updated = new User(tgId, "", "", totalScore + 10);
        repository.updateUserInfo(updated);

        // переданный объект не должен измениться
        assertEquals("", updated.getUsername());
        assertEquals(totalScore + 10, updated.getTotalScore());
        // а вот старый объект не должен мутировать
        assertEquals(username, old.getUsername());
        assertEquals(totalScore, old.getTotalScore());
    }

    @DisplayName("Ошибка обновления данных null пользователя")
    @Test
    void updateNullUser() {
        HardcodedUserRepository repository =
                new HardcodedUserRepository();

        IllegalArgumentException thrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> repository.updateUserInfo(null)
                );

        assertEquals(
                "User cannot be null",
                thrown.getMessage()
        );
    }

    @DisplayName("Ошибка обновления данных пользователя, которого нет")
    @Test
    void updateNonExistingUser() {
        HardcodedUserRepository repository =
                new HardcodedUserRepository();

        User user = new User(tgId);

        NoSuchElementException thrown =
                assertThrows(
                        NoSuchElementException.class,
                        () -> repository.updateUserInfo(user)
                );

        assertEquals(
                "User with this id does not exist",
                thrown.getMessage()
        );
    }

    @DisplayName("Таблица лидеров сортируется по убыванию очков")
    @Test
    void findTopTenSortsByScoreDescending() {
        HardcodedUserRepository repository =
                new HardcodedUserRepository();

        repository.saveUserInfo(new User(11, "", "Alice", 100));
        repository.saveUserInfo(new User(22, "", "Bob", 300));
        repository.saveUserInfo(new User(33, "", "Robin", 200));

        List<LeaderboardEntry> result =
                repository.findTopTen();

        assertEquals(300, result.get(0).score());
        assertEquals(200, result.get(1).score());
        assertEquals(100, result.get(2).score());
    }

    @DisplayName("Места в таблице начинаются с первого")
    @Test
    void findTopTenRanksStartFromOne() {
        HardcodedUserRepository repository =
                new HardcodedUserRepository();

        repository.saveUserInfo(new User(11, "", "Alice", 100));
        repository.saveUserInfo(new User(22, "", "Bob", 300));

        List<LeaderboardEntry> result =
                repository.findTopTen();

        assertEquals(1
                , result.get(0).place());
        assertEquals(2, result.get(1).place());
    }
    @DisplayName("Таблица лидеров ограничена десятью игроками")
    @Test
    void findTopTenReturnsMaximumTenPlayers() {
        HardcodedUserRepository repository =
                new HardcodedUserRepository();

        for (long i = 1; i <= 15; i++) {
            repository.saveUserInfo(new User(i,"","", (int) i * 10));
        }

        List<LeaderboardEntry> result =
                repository.findTopTen();

        assertEquals(
                10,
                result.size()
        );
    }

    @DisplayName("Поиск ближайших игроков существующего пользователя")
    @Test
    void findUserAndClosestExistingUser() {
        HardcodedUserRepository repository =
                new HardcodedUserRepository();

        repository.saveUserInfo(new User(11, "", "Alice", 100));
        repository.saveUserInfo(new User(33, "", "Robin", 200));
        repository.saveUserInfo(new User(22, "", "Bob", 300));

        List<LeaderboardEntry> result =
                repository.findUserAndClosest(33);

        assertEquals(
                3,
                result.size()
        );

        assertEquals(
                "Bob",
                result.get(0).nickname()
        );

        assertEquals(
                "Robin",
                result.get(1).nickname()
        );

        assertEquals(
                "Alice",
                result.get(2).nickname()
        );
    }

    @DisplayName("Поиск ближайших игроков отсутствующего пользователя возвращает пустой список")
    @Test
    void findUserAndClosestNonExistingUser() {
        HardcodedUserRepository repository =
                new HardcodedUserRepository();

        assertTrue(
                repository.findUserAndClosest(tgId)
                        .isEmpty()
        );
    }

    @DisplayName("Ошибка поиска ближайших игроков пользователя с отрицательным Telegram ID")
    @Test
    void findUserAndClosestNegativeTelegramId() {
        HardcodedUserRepository repository =
                new HardcodedUserRepository();

        IllegalArgumentException thrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> repository.findUserAndClosest(-1)
                );

        assertEquals(
                "Telegram id must be positive",
                thrown.getMessage()
        );
    }

    @DisplayName("Ошибка поиска ближайших игроков пользователя с нулевым Telegram ID")
    @Test
    void findUserAndClosestZeroTelegramId() {
        HardcodedUserRepository repository =
                new HardcodedUserRepository();

        IllegalArgumentException thrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> repository.findUserAndClosest(0)
                );

        assertEquals(
                "Telegram id must be positive",
                thrown.getMessage()
        );
    }

    @DisplayName("Поиск ближайших игроков пользователя на 1 месте дает его и пользователя на 2 месте")
    @Test
    void findUserAndClosestFirstUser() {
        HardcodedUserRepository repository =
                new HardcodedUserRepository();

        repository.saveUserInfo(new User(11, "", "Alice", 100));
        repository.saveUserInfo(new User(33, "", "Robin", 200));
        repository.saveUserInfo(new User(22, "", "Bob", 300));

        List<LeaderboardEntry> result =
                repository.findUserAndClosest(22);

        assertEquals(
                2,
                result.size()
        );

        assertEquals(
                "Bob",
                result.get(0).nickname()
        );

        assertEquals(
                "Robin",
                result.get(1).nickname()
        );

    }

    @DisplayName("Поиск ближайших игроков пользователя на последнем месте дает его и пользователя на предпоследнем месте")
    @Test
    void findUserAndClosestLastUser() {
        HardcodedUserRepository repository =
                new HardcodedUserRepository();

        repository.saveUserInfo(new User(11, "", "Alice", 100));
        repository.saveUserInfo(new User(33, "", "Robin", 200));
        repository.saveUserInfo(new User(22, "", "Bob", 300));

        List<LeaderboardEntry> result =
                repository.findUserAndClosest(11);

        assertEquals(
                2,
                result.size()
        );

        assertEquals(
                "Robin",
                result.get(0).nickname()
        );

        assertEquals(
                "Alice",
                result.get(1).nickname()
        );

    }

}