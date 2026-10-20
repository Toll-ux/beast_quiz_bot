package application;

import domain.User;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LeaderBoardTest {

    private final FakeUserRepo userRepo = new FakeUserRepo();
    private final LeaderBoard leaderBoard = new LeaderBoard(userRepo);

    @DisplayName("getUsersPosition: нормальная работа")
    @Test
    void getUsersPositionNormal() {
        User user1 = new User(10, "@alice", "Alice", 100);
        User user2 = new User(20, "@bob", "Bob", 80);
        User user3 = new User(30, "@charlie", "Charlie", 60);

        userRepo.saveUserInfo(user1);
        userRepo.saveUserInfo(user2);
        userRepo.saveUserInfo(user3);

        List<LeaderboardEntry> result =
                leaderBoard.getUsersPosition(20);

        assertNotNull(result);
        assertFalse(result.isEmpty());

        assertTrue(result.contains(
                new LeaderboardEntry(1, 100, "Alice")
        ));
        assertTrue(result.contains(
                new LeaderboardEntry(2, 80, "Bob")
        ));
        assertTrue(result.contains(
                new LeaderboardEntry(3, 60, "Charlie")
        ));
    }

    @DisplayName("getUsersPosition: пустая база данных")
    @Test
    void getUsersPositionEmptyDatabase() {
        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> leaderBoard.getUsersPosition(20)
        );

        assertEquals(
                "Leaderboard data is missing or database is empty",
                thrown.getMessage()
        );
    }

    @DisplayName("getUsersPosition: пользователь не найден")
    @Test
    void getUsersPositionUserNotFound() {
        userRepo.saveUserInfo(
                new User(10, "@alice", "Alice", 100)
        );

        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> leaderBoard.getUsersPosition(20)
        );

        assertEquals(
                "User slice data is missing or database is empty",
                thrown.getMessage()
        );
    }

    @DisplayName("getUsersPosition: список содержит записи топ-10 и ближайших пользователей")
    @Test
    void getUsersPositionCombinesTopTenAndUserSlice() {
        userRepo.saveUserInfo(
                new User(10, "@alice", "Alice", 100)
        );
        userRepo.saveUserInfo(
                new User(20, "@bob", "Bob", 80)
        );
        userRepo.saveUserInfo(
                new User(30, "@charlie", "Charlie", 60)
        );

        List<LeaderboardEntry> result =
                leaderBoard.getUsersPosition(20);

        // Топ-10 уже содержит всех трёх пользователей.
        // Повторные записи из списка ближайших позиций не должны дублироваться.
        assertEquals(3, result.size());

        assertEquals(
                List.of(
                        new LeaderboardEntry(1, 100, "Alice"),
                        new LeaderboardEntry(2, 80, "Bob"),
                        new LeaderboardEntry(3, 60, "Charlie")
                ),
                result
        );
    }

    @DisplayName("getUsersPosition: пользователь на первом месте")
    @Test
    void getUsersPositionUserOnFirstPlace() {
        userRepo.saveUserInfo(
                new User(10, "@alice", "Alice", 100)
        );
        userRepo.saveUserInfo(
                new User(20, "@bob", "Bob", 80)
        );

        List<LeaderboardEntry> result =
                leaderBoard.getUsersPosition(10);

        assertTrue(result.contains(
                new LeaderboardEntry(1, 100, "Alice")
        ));
        assertTrue(result.contains(
                new LeaderboardEntry(2, 80, "Bob")
        ));

        assertEquals(2, result.size());
    }

    @DisplayName("getUsersPosition: пользователь на последнем месте")
    @Test
    void getUsersPositionUserOnLastPlace() {
        userRepo.saveUserInfo(
                new User(10, "@alice", "Alice", 100)
        );
        userRepo.saveUserInfo(
                new User(20, "@bob", "Bob", 80)
        );

        List<LeaderboardEntry> result =
                leaderBoard.getUsersPosition(20);

        assertTrue(result.contains(
                new LeaderboardEntry(1, 100, "Alice")
        ));
        assertTrue(result.contains(
                new LeaderboardEntry(2, 80, "Bob")
        ));

        assertEquals(2, result.size());
    }

    @DisplayName("getUsersPosition: пользователь вне топ-10")
    @Test
    void getUsersPositionUserOutsideTopTen() {
        for (int i = 1; i <= 15; i++) {
            userRepo.saveUserInfo(
                    new User(
                            i,
                            "@user" + i,
                            "User" + i,
                            (16 - i) * 10
                    )
            );
        }

        List<LeaderboardEntry> result =
                leaderBoard.getUsersPosition(12);

        // Топ-10 плюс пользователь на 12-м месте
        // и его ближайшие соседи.
        assertEquals(13, result.size());

        assertTrue(result.contains(
                new LeaderboardEntry(10, 60, "User10")
        ));
        assertTrue(result.contains(
                new LeaderboardEntry(11, 50, "User11")
        ));
        assertTrue(result.contains(
                new LeaderboardEntry(12, 40, "User12")
        ));
        assertTrue(result.contains(
                new LeaderboardEntry(13, 30, "User13")
        ));
    }

    @DisplayName("getUsersPosition: при объединении нет дубликатов")
    @Test
    void getUsersPositionDoesNotDuplicateEntries() {
        userRepo.saveUserInfo(
                new User(10, "@alice", "Alice", 100)
        );
        userRepo.saveUserInfo(
                new User(20, "@bob", "Bob", 80)
        );
        userRepo.saveUserInfo(
                new User(30, "@charlie", "Charlie", 60)
        );

        List<LeaderboardEntry> result =
                leaderBoard.getUsersPosition(20);

        assertEquals(
                result.size(),
                result.stream().distinct().count()
        );
    }

    @DisplayName("getUsersPosition: одинаковые очки")
    @Test
    void getUsersPositionEqualScores() {
        userRepo.saveUserInfo(
                new User(30, "@charlie", "Charlie", 100)
        );
        userRepo.saveUserInfo(
                new User(10, "@alice", "Alice", 100)
        );
        userRepo.saveUserInfo(
                new User(20, "@bob", "Bob", 50)
        );

        List<LeaderboardEntry> result =
                leaderBoard.getUsersPosition(30);

        assertEquals(
                List.of(
                        new LeaderboardEntry(1, 100, "Alice"),
                        new LeaderboardEntry(2, 100, "Charlie"),
                        new LeaderboardEntry(3, 50, "Bob")
                ),
                result
        );
    }
}