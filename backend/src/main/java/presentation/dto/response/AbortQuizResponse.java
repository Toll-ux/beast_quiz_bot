package presentation.dto.response;

public record AbortQuizResponse(
        long sessionId,
        int quizScore,
        int userTotalScore
) {}