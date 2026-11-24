package best_quizbot.presentation.dto.request;

public record StartQuizRequest(
        long telegramId,
        int questionCount
) {}