package domain;

/**Статус пользователя - задел на будущее*/
enum UserPermissions{
    ADMIN,
    NORMAL_PLAYER
}

/**Domain - Юзер*/
public class User {
    /**Свой идентификатор пользователя*/
    public final int id;
    /**Идентификатор тг: 10 значное число или меньше, уникальное для любого пользователя и бота*/
    public final long telegramId;
    /**Имя пользователя (имя с @)
     *  может быть null*/
    private String username;
    /**Ник пользователя (без @)
     * Может быть null*/
    private String nickname;
    /**Счет за все партии*/
    private int totalScore;
    /**Статус пользователя*/
    private UserPermissions permissions;

    /** Перегруженный конструктор создает объект и проверяет корректность telegramId и id.
     * Можно на этих полях или на всех
     *
     * @throws IllegalArgumentException если id, telegramId, или счет неверные
     * @throws NullPointerException если username или nickname null
     */
    public User(int id, long telegramId) {
        this(id, telegramId, "", "", 0);
    }


    public User(int id, long telegramId, String username, String nickname, int totalScore) {
        if (id <= 0) throw new IllegalArgumentException("User id must be positive");
        if (telegramId <= 0) throw new IllegalArgumentException("Telegram id must be positive");
        if (totalScore < 0)
            throw new IllegalArgumentException("Score cannot be negative");
        if (username == null) throw new IllegalArgumentException("Username must not be null");
        if (nickname == null) throw new IllegalArgumentException("Nickname must not be null");

        this.id = id;
        this.telegramId = telegramId;
        this.username = username;
        this.nickname = nickname;
        this.totalScore = totalScore;
        this.permissions = UserPermissions.NORMAL_PLAYER;
    }

    public String getUsername() {
        return username;
    }

    public String getNickname() {
        return nickname;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setUsername(String username) {
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Username must not be Blank");

        if (!username.startsWith("@"))
            throw new IllegalArgumentException("Username must start with @");

        this.username = username;
    }

    public void setNickname(String nickname) {
        if (nickname == null || nickname.isBlank())
            throw new IllegalArgumentException("Nickname must not be Blank");

        this.nickname = nickname;
    }

    public void updateTotalScore(int score) {
        if (score < 0) {
            throw new IllegalArgumentException("Score cannot be negative");
        }

        this.totalScore = Math.max(this.totalScore, score);
    }

    /**Функция делает юзера админом. Без проверок
     * @throws IllegalStateException если уже админ
     */
    public void becomeAdmin(){
        if (permissions == UserPermissions.ADMIN)
            throw new IllegalStateException("User already admin");

        permissions = UserPermissions.ADMIN;
    }

    /**Функция делает юзера обычным пользователем
     * @throws IllegalStateException если уже NORMAL_PLAYER
     */
    public void becomeNormalUser(){
        if (permissions == UserPermissions.NORMAL_PLAYER)
            throw new IllegalStateException("User already normal user");

        permissions = UserPermissions.NORMAL_PLAYER;
    }

    public boolean isUserAdmin(){
        return permissions == UserPermissions.ADMIN;
    }

    public boolean isUserNormalPlayer(){
        return permissions == UserPermissions.NORMAL_PLAYER;
    }
}