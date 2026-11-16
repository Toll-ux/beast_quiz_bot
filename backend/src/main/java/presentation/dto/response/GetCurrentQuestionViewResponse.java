package presentation.dto.response;

import java.util.List;

public record GetCurrentQuestionViewResponse(
        long questionId,
        String text,
        List<String> answers
) {}