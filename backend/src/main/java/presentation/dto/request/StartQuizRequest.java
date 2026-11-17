package presentation.dto.request;

public record StartQuizRequest(
        long userId,
        int questionCount
) {}