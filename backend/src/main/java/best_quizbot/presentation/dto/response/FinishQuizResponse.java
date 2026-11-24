package best_quizbot.presentation.dto.response;

public record FinishQuizResponse(
        long sessionId,
        int quizScore,
        int userTotalScore
) {}