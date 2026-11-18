package best_quizbot.presentation.dto.response;

import java.util.List;

public record GetLeaderboardResponse(
        List<LeaderboardResponseEntry> leaderboard
) {}