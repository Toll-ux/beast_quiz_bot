package application;

import application.port.UserRepository;
import domain.User;

import java.util.*;

public class FakeUserRepo implements UserRepository {

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Optional<User> findUserByTelegramId(long telegramId) {
        if (telegramId <= 0) {
            throw new IllegalArgumentException("Telegram id must be positive");
        }

        return Optional.ofNullable(users.get(telegramId));
    }

    @Override
    public void saveUserInfo(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        if (users.containsKey(user.telegramId)) {
            throw new IllegalStateException("User with this id already exists");
        }

        users.put(user.telegramId, user);
    }

    @Override
    public void updateUserInfo(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        if (!users.containsKey(user.telegramId)) {
            throw new NoSuchElementException("User with this id does not exist");
        }

        users.put(user.telegramId, user);
    }

    @Override
    public List<LeaderboardEntry> findTopTen() {
        List<User> topUsers = users.values().stream()
                .sorted(Comparator.comparingInt(User::getTotalScore).reversed())
                .limit(10)
                .toList();

        List<LeaderboardEntry> result = new ArrayList<>();

        for (int i = 0; i < topUsers.size(); i++) {
            User user = topUsers.get(i);

            result.add(new LeaderboardEntry(
                    i + 1,
                    user.getTotalScore(),
                    user.getNickname()
            ));
        }

        return result;
    }

    @Override
    public List<LeaderboardEntry> findUserAndClosest(long telegramId) {
        if (telegramId <= 0) {
            throw new IllegalArgumentException("Telegram id must be positive");
        }

        List<Map.Entry<Long, User>> sorted = users.entrySet().stream()
                .sorted(
                        Comparator.comparingInt((Map.Entry<Long, User> entry) ->
                                        entry.getValue().getTotalScore()
                                )
                                .reversed()
                                .thenComparingLong(Map.Entry::getKey)
                )
                .toList();

        int userIndex = -1;

        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).getKey() == telegramId) {
                userIndex = i;
                break;
            }
        }

        if (userIndex == -1) {
            return List.of();
        }

        List<LeaderboardEntry> result = new ArrayList<>();

        if (userIndex > 0) {
            User above = sorted.get(userIndex - 1).getValue();

            result.add(new LeaderboardEntry(
                    userIndex,
                    above.getTotalScore(),
                    above.getNickname()
            ));
        }

        User current = sorted.get(userIndex).getValue();

        result.add(new LeaderboardEntry(
                userIndex + 1,
                current.getTotalScore(),
                current.getNickname()
        ));

        if (userIndex < sorted.size() - 1) {
            User below = sorted.get(userIndex + 1).getValue();

            result.add(new LeaderboardEntry(
                    userIndex + 2,
                    below.getTotalScore(),
                    below.getNickname()
            ));
        }

        return result;
    }

    // Вспомогательный метод для тестов
    public int getUsersCount() {
        return users.size();
    }

    // Вспомогательный метод для тестов
    public Map<Long, User> getUsers() {
        return users;
    }
}