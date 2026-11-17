package client.api.dto.request;

public record StartQuizRequest(
        long userId,
        int questionCount
) {}