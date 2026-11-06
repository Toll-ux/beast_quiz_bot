package backendclient.dto.request;

public record AbortQuizResponse(
        long sessionId,
        int quizScore,
        int userTotalScore
) {}