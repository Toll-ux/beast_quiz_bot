package client.api.dto.response;

public record FinishQuizResponse(
        long sessionId,
        int quizScore,
        int userTotalScore
) {}