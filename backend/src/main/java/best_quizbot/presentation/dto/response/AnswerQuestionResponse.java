package best_quizbot.presentation.dto.response;

import java.util.Set;

public record AnswerQuestionResponse(
        boolean correct,
        Set<Integer> correctAnswers,
        int currentScore,
        boolean finished
) {}