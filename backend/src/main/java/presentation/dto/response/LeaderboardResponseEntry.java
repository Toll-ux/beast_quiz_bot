package main.java.presentation.dto.response;

public record LeaderboardResponseEntry(
        int place,
        int score,
        String nickname
) {}