package presentation.dto.response;

import java.util.List;

public record AnswerQuestionResponse(
        boolean correct,
        List<Integer> correctAnswers,
        int currentScore,
        boolean finished
) {}