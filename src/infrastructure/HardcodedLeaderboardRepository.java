package infrastructure;

import application.LeaderboardEntry;
import application.port.LeaderboardRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HardcodedLeaderboardRepository implements LeaderboardRepository {
    /// Ключи -  telegram ID пользователей.
    /// Для каждого пользователя хранится его totalScore.
    private final Map<Long, Integer> scores = new HashMap<>();

    /** Сохраняет данные об общем количестве очков у пользователя
     * @param telegramId - tg ID пользователя
     * @param score - totalScore пользователя, который нужно переписать/записать
     * @throws IllegalArgumentException если telegramId <= 0 или score < 0
     */
    @Override
    public void saveUserTotalScore(long telegramId, int score) {
        if (telegramId <= 0) {
            throw new IllegalArgumentException("Telegram id must be positive");
        }
        if (score < 0) {
            throw new IllegalArgumentException("Score cannot be negative");
        }

        // upsert: если пользователя нет — добавит, если есть — обновит только если новый счёт больше
        // для защиты рекорда
        scores.merge(telegramId, score, Math::max);
    }

    /**
     * Места начинаются с 1.
     * @return информацию о первых 10 пользователях с наибольшим количеством очков
     */
    @Override
    public List<LeaderboardEntry> findTopTen() {
        List<Map.Entry<Long, Integer>> sorted = scores.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(10)
                .toList();

        List<LeaderboardEntry> result = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            Map.Entry<Long, Integer> entry = sorted.get(i);
            result.add(new LeaderboardEntry(i + 1, entry.getValue(), entry.getKey()));
        }
        return result;
    }

    /**
     * @param telegramId - tg ID пользователя
     * @return Информацию о положении пользователя в доске лидеров и
     * одном пользователе ниже и выше него (если такие есть)
     * @throws IllegalArgumentException если telegramId <= 0
     */
    @Override
    public List<LeaderboardEntry> findUserAndClosest(long telegramId) {
        if (telegramId <= 0) {
            throw new IllegalArgumentException("Telegram id must be positive");
        }
        // 1. Сортирует всех игроков по убыванию счёта
        List<Map.Entry<Long, Integer>> sorted = scores.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .toList();

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
            Map.Entry<Long, Integer> above = sorted.get(userIndex - 1);
            result.add(new LeaderboardEntry(userIndex, above.getValue(), above.getKey()));
        }

        Map.Entry<Long, Integer> user = sorted.get(userIndex);
        result.add(new LeaderboardEntry(userIndex + 1, user.getValue(), user.getKey()));

        if (userIndex < sorted.size() - 1) {
            Map.Entry<Long, Integer> below = sorted.get(userIndex + 1);
            result.add(new LeaderboardEntry(userIndex + 2, below.getValue(), below.getKey()));
        }

        return result;
    }
}
