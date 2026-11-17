package presentation.dto.request;

public record StartQuizRequest(
        long telegramId,
        int questionCount
) {}