package application;

import application.port.LeaderboardRepository;
import application.port.UserRepository;

import java.util.List;

public class GetLeaderboardTopTenNicknames {
    private final LeaderboardRepository leaderboardRepository;
    private final UserRepository userRepository;

    public GetLeaderboardTopTenNicknames(
        LeaderboardRepository leaderboardRepository, UserRepository userRepository
    )
    {
        this.leaderboardRepository = leaderboardRepository;
        this.userRepository = userRepository;
    }

    public List<LeaderboardEntryNicknames> execute(){
        List<LeaderboardEntry> topTen = leaderboardRepository.findTopTen();
        List<LeaderboardEntryNicknames> topTenNicknames = topTen.stream()
            .map(a -> new LeaderboardEntryNicknames(
                a.place(),
                a.score(),
                userRepository.findUserByTelegramId(a.telegramId())
                .orElseThrow(() -> new IllegalStateException("User not found")).getNickname()))
            .toList();
        return topTenNicknames;
        }
}
