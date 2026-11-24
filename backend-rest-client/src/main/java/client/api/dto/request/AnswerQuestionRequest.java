package client.api.dto.request;

import java.util.Set;

public record AnswerQuestionRequest(
        long telegramId,
        int questionId,
        Set<Integer> chosen
) {}