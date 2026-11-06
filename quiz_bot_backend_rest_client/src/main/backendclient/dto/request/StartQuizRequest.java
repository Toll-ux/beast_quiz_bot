package backendclient.dto.request;

public record StartQuizRequest(
        long telegramId,
        int questionCount
) {}