package main.java.presentation.dto.response;

public record FinishQuizResponse(
        long sessionId,
        int quizScore,
        int userTotalScore
) {}