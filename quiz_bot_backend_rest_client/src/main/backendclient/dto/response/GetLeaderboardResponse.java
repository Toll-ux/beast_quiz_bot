package backendclient.dto.response;

import java.util.List;

public record GetLeaderboardResponse(
        List<LeaderboardResponseEntry> leaderboard
) {}