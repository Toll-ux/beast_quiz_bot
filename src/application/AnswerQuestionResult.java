package application;

import java.util.Set;

/**
 * 
 * AnswerQuestionResult - результат ответа на вопрос
 * @param correct - правильно ли ответили на вопрос
 * @param correctAnswers - все правильные ответы на вопрос
 * @param currentScore - текущий счет за квиз
 * @param finished - завершен ли квиз
 */

public record AnswerQuestionResult(
    boolean correct,
    Set<Integer> correctAnswers,
    int currentScore,
    boolean finished
) {}