package client.api.dto.response;

public record LeaderboardResponseEntry(
        int place,
        int score,
        String nickname
) {}