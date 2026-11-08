package client.api.dto.request;

public record StartQuizRequest(
        long telegramId,
        int questionCount
) {}