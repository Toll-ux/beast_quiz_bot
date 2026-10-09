package domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {
    static final int id = 1;
    static final long tgId =123333;
    static final String username = "@qwer";
    static final String nickname = "alex123";
    static final int totalScore = 123;
    static final User uBaseReference = new User(id, tgId);
    static final User uAddReference = new User(id, tgId, username, nickname, totalScore);

    @DisplayName("Перегруженный конструктор работает")
    @Test
    void ConstructorDefaultValues() {
        assertEquals(id, uBaseReference.id);
        assertEquals(tgId, uBaseReference.telegramId);
        assertEquals("", uBaseReference.getNickname());
        assertEquals("", uBaseReference.getUsername());
        assertEquals(0, uBaseReference.getTotalScore());
        assertTrue(uBaseReference.isUserNormalPlayer());
    }

    @DisplayName("Конструктор работает")
    @Test
    void ConstructorSomeValues(){
        assertEquals(id, uAddReference.id);
        assertEquals(tgId, uAddReference.telegramId);
        assertEquals(username, uAddReference.getUsername());
        assertEquals(nickname, uAddReference.getNickname());
        assertEquals(totalScore, uAddReference.getTotalScore());
        assertTrue(uAddReference.isUserNormalPlayer());
    }

    @DisplayName("Конструктор инициализация id 0")
    @Test
    void ConstructorZeroId(){
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () ->
                new User(0, tgId));
        Assertions.assertEquals("User id must be positive", thrown.getMessage());
    }

    @DisplayName("Конструктор инициализация телеграм id 0")
    @Test
    void ConstructorZeroTgId(){
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () ->
                new User(id, 0));
        Assertions.assertEquals("Telegram id must be positive", thrown.getMessage());
    }

    @DisplayName("Конструктор инициализация id < 0")
    @Test
    void ConstructorNegativeId(){
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () ->
            new User(-100, tgId));

        Assertions.assertEquals("User id must be positive", thrown.getMessage());
    }

    @DisplayName("Конструктор инициализация телеграм id < 0")
    @Test
    void ConstructorNegativeTgId(){
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () ->
            new User(id, -100));

        Assertions.assertEquals("Telegram id must be positive", thrown.getMessage());
    }

    @DisplayName("Конструктор инициализация счет = 0")
    @Test
    void ConstructorZeroTotalScore(){
        Assertions.assertDoesNotThrow(() -> new User(id, tgId, username, nickname, 0));
    }

    @DisplayName("Конструктор инициализация счет < 0")
    @Test
    void ConstructorNegativeTotalScore(){
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () ->
            new User(id, tgId, username, nickname, -100));

        Assertions.assertEquals("Score cannot be negative", thrown.getMessage());
    }

    @DisplayName("Конструктор инициализация nickname null")
    @Test
    void ConstructorNullNickname(){
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () ->
                new User(id, tgId, username, null, totalScore));

        Assertions.assertEquals("Nickname must not be null", thrown.getMessage());
    }

    @DisplayName("Конструктор инициализация username null")
    @Test
    void ConstructorNullUsername(){
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () ->
                new User(id, tgId, null, nickname, totalScore));

        Assertions.assertEquals("Username must not be null", thrown.getMessage());
    }

    @DisplayName("Обновить счет задан отрицательный счет")
    @Test
    void updateTotalScoreNegativeInput(){
        IllegalArgumentException baseConstructor = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            uAddReference.updateTotalScore(-100);
        });
        Assertions.assertEquals("Score cannot be negative", baseConstructor.getMessage());

        IllegalArgumentException addedConstructor = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            uBaseReference.updateTotalScore(-100);
        });
        Assertions.assertEquals("Score cannot be negative", addedConstructor.getMessage());
    }

    @DisplayName("Обновить счет, задан счет больше предыдущего")
    @Test
    void updateTotalScoreBiggerRes () {
        int totalScoreNew = 12;
        User user = new User(id, tgId);
        user.updateTotalScore(totalScoreNew);
        assertEquals(totalScoreNew, user.getTotalScore());
    }

    @DisplayName("Обновить счет, задан счет меньше предыдущего")
    @Test
    void updateTotalScoreLessRes () {
        int totalScoreNew = 12;
        User user = new User(id, tgId, username, nickname, totalScore);
        user.updateTotalScore(totalScoreNew);
        Assertions.assertNotEquals(totalScoreNew, user.getTotalScore());
    }

    @DisplayName("Обновить счет, задан счет такой же")
    @Test
    void updateTotalScoreSameRes () {
        User user = new User(id, tgId, username, nickname, totalScore);
        user.updateTotalScore(totalScore);
        assertEquals(totalScore, user.getTotalScore());
    }

    @DisplayName("setUsername нормально работает")
    @Test
    void SetUsername() {
        User user = new User(id, tgId);
        user.setUsername(username);
        assertEquals(username, user.getUsername());
    }

    @DisplayName("setUsername получил пустую строчку")
    @Test
    void setEmptyUsername (){
        User user = new User(id, tgId);
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () ->
            user.setUsername(""));

        Assertions.assertEquals("Username must not be Blank", thrown.getMessage());
    }

    @DisplayName("setUsername получил null")
    @Test
    void setNullUsername (){
        User user = new User(id, tgId);
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () ->
                user.setUsername(null));

        Assertions.assertEquals("Username must not be Blank", thrown.getMessage());
    }

    @DisplayName("setUsername получил строку с @ не в начале")
    @Test
    void SetWrongAtSignUsername(){
        User user = new User(id, tgId);
        IllegalArgumentException atSignInEnd = Assertions.assertThrows(IllegalArgumentException.class, () ->
            user.setUsername("123@"));

        Assertions.assertEquals("Username must start with @", atSignInEnd.getMessage());

        IllegalArgumentException atSignInMiddle = Assertions.assertThrows(IllegalArgumentException.class, () ->
                user.setUsername("1@23"));

        Assertions.assertEquals("Username must start with @", atSignInMiddle.getMessage());
    }

    @DisplayName("setUsername получил строку без @")
    @Test
    void SetWrongUsername(){
        User user = new User(id, tgId);
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            user.setUsername("123");
        });

        Assertions.assertEquals("Username must start with @", thrown.getMessage());
    }

    @DisplayName("SetNickname нормально работает")
    @Test
    void SetNickname() {
        User user = new User(id, tgId);
        user.setNickname(nickname);
        assertEquals(nickname, user.getNickname());
    }

    @DisplayName("setNickname получил пустую строчку")
    @Test
    void SetEmptyNickname (){
        User user = new User(id, tgId);
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            user.setNickname("");
        });

        Assertions.assertEquals("Nickname must not be Blank", thrown.getMessage());
    }

    @DisplayName("setUsername получил null")
    @Test
    void setNullNickname(){
        User user = new User(id, tgId);
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () ->
                user.setNickname(null));

        Assertions.assertEquals("Nickname must not be Blank", thrown.getMessage());
    }

    @DisplayName("becomeAdmin нормальная работа")
    @Test
    void becomeAdmin(){
        User user = new User(id, tgId);
        Assertions.assertDoesNotThrow(user::becomeAdmin);
        assertTrue(user.isUserAdmin());
    }

    @DisplayName("becomeAdmin user уже админ")
    @Test
    void becomeAdminAlreadyAdmin(){
        User user = new User(id, tgId);
        user.becomeAdmin();
        assertTrue(user.isUserAdmin());
        IllegalStateException thrown = Assertions.assertThrows(IllegalStateException.class,user::becomeAdmin);
        Assertions.assertEquals("User already admin", thrown.getMessage());
    }

    @DisplayName("becomeAdmin нормальная работа")
    @Test
    void becomeNormalUser(){
        User user = new User(id, tgId);
        user.becomeAdmin();
        assertTrue(user.isUserAdmin());
        Assertions.assertDoesNotThrow(user::becomeNormalUser);
    }

    @DisplayName("becomeAdmin user уже админ")
    @Test
    void becomeNormalUserAlreadyNormalUser(){
        User user = new User(id, tgId);
        assertTrue(user.isUserNormalPlayer());
        IllegalStateException thrown = Assertions.assertThrows(IllegalStateException.class,user::becomeNormalUser);
        Assertions.assertEquals("User already normal user", thrown.getMessage());
    }
}