package client.api.dto.response;

import java.util.List;

public record GetLeaderboardResponse(
        List<LeaderboardResponseEntry> leaderboard
) {}