package infrastructure;

import application.LeaderboardEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HardcodedLeaderboardRepositoryTest {

    static final long tgId = 123333;
    static final int totalScore = 123;

    @DisplayName("Сохранение нового рекорда пользователя")
    @Test
    void updateHighScore() {
        HardcodedLeaderboardRepository repository =
                new HardcodedLeaderboardRepository();

        repository.updateHighScore(tgId, totalScore);

        List<LeaderboardEntry> result =
                repository.findTopTen();

        assertEquals(1, result.size());

        assertEquals(
                totalScore,
                result.get(0).score()
        );

        assertEquals(
                tgId,
                result.get(0).telegramId()
        );
    }

    @DisplayName("Новый меньший результат не заменяет старый рекорд")
    @Test
    void updateHighScoreDoesNotLowerRecord() {
        HardcodedLeaderboardRepository repository =
                new HardcodedLeaderboardRepository();

        repository.updateHighScore(tgId, 100);
        repository.updateHighScore(tgId, 50);

        LeaderboardEntry entry =
                repository.findTopTen().get(0);

        assertEquals(
                100,
                entry.score()
        );
    }

    @DisplayName("Новый больший результат заменяет старый рекорд")
    @Test
    void updateHighScoreUpdatesHigherScore() {
        HardcodedLeaderboardRepository repository =
                new HardcodedLeaderboardRepository();

        repository.updateHighScore(tgId, 100);
        repository.updateHighScore(tgId, 150);

        LeaderboardEntry entry =
                repository.findTopTen().get(0);

        assertEquals(
                150,
                entry.score()
        );
    }

    @DisplayName("Ошибка сохранения с отрицательным Telegram ID")
    @Test
    void updateHighScoreNegativeTelegramId() {
        HardcodedLeaderboardRepository repository =
                new HardcodedLeaderboardRepository();

        IllegalArgumentException thrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> repository.updateHighScore(-1, 100)
                );

        assertEquals(
                "Telegram id must be positive",
                thrown.getMessage()
        );
    }

    @DisplayName("Ошибка сохранения с нулевым Telegram ID")
    @Test
    void updateHighScoreZeroTelegramId() {
        HardcodedLeaderboardRepository repository =
                new HardcodedLeaderboardRepository();

        IllegalArgumentException thrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> repository.updateHighScore(0, 100)
                );

        assertEquals(
                "Telegram id must be positive",
                thrown.getMessage()
        );
    }

    @DisplayName("Ошибка сохранения отрицательного количества очков")
    @Test
    void updateHighScoreNegativeScore() {
        HardcodedLeaderboardRepository repository =
                new HardcodedLeaderboardRepository();

        IllegalArgumentException thrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> repository.updateHighScore(tgId, -1)
                );

        assertEquals(
                "Score cannot be negative",
                thrown.getMessage()
        );
    }

    @DisplayName("Таблица лидеров сортируется по убыванию очков")
    @Test
    void findTopTenSortsByScoreDescending() {
        HardcodedLeaderboardRepository repository =
                new HardcodedLeaderboardRepository();

        repository.updateHighScore(11, 100);
        repository.updateHighScore(22, 300);
        repository.updateHighScore(33, 200);

        List<LeaderboardEntry> result =
                repository.findTopTen();

        assertEquals(300, result.get(0).score());
        assertEquals(200, result.get(1).score());
        assertEquals(100, result.get(2).score());
    }

    @DisplayName("Места в таблице начинаются с первого")
    @Test
    void findTopTenRanksStartFromOne() {
        HardcodedLeaderboardRepository repository =
                new HardcodedLeaderboardRepository();

        repository.updateHighScore(11, 100);
        repository.updateHighScore(22, 200);

        List<LeaderboardEntry> result =
                repository.findTopTen();

        assertEquals(1


                , result.get(0).place());
        assertEquals(2, result.get(1).place());
    }

    @DisplayName("Таблица лидеров ограничена десятью игроками")
    @Test
    void findTopTenReturnsMaximumTenPlayers() {
        HardcodedLeaderboardRepository repository =
                new HardcodedLeaderboardRepository();

        for (long i = 1; i <= 15; i++) {
            repository.updateHighScore(i, (int) i * 10);
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
        HardcodedLeaderboardRepository repository =
                new HardcodedLeaderboardRepository();

        repository.updateHighScore(11, 300);
        repository.updateHighScore(22, 200);
        repository.updateHighScore(33, 100);

        List<LeaderboardEntry> result =
                repository.findUserAndClosest(22);

        assertEquals(
                3,
                result.size()
        );

        assertEquals(
                11,
                result.get(0).telegramId()
        );

        assertEquals(
                22,
                result.get(1).telegramId()
        );

        assertEquals(
                33,
                result.get(2).telegramId()
        );
    }

    @DisplayName("Поиск ближайших игроков отсутствующего пользователя возвращает пустой список")
    @Test
    void findUserAndClosestNonExistingUser() {
        HardcodedLeaderboardRepository repository =
                new HardcodedLeaderboardRepository();

        assertTrue(
                repository.findUserAndClosest(tgId)
                        .isEmpty()
        );
    }

    @DisplayName("Ошибка поиска с отрицательным Telegram ID")
    @Test
    void findUserAndClosestNegativeTelegramId() {
        HardcodedLeaderboardRepository repository =
                new HardcodedLeaderboardRepository();

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

    @DisplayName("Ошибка поиска с нулевым Telegram ID")
    @Test
    void findUserAndClosestZeroTelegramId() {
        HardcodedLeaderboardRepository repository =
                new HardcodedLeaderboardRepository();

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
}