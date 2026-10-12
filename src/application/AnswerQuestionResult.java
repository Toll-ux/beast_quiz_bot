package application;

import java.util.Set;

/**
 * 
 * AnswerQuestionResult - результат ответа на вопрос
 * @param correct - правильно ли ответили на вопрос
 * @param correctAnswers - все правильные ответы на вопрос
 * @param currentScore - текущий счет за квиз
 * @param nextQuestionView - обзор следующего вопроса
 * @param finished - завершен ли квиз
 */

public record AnswerQuestionResult(
    boolean correct,
    Set<Integer> correctAnswers,
    int currentScore,
    QuestionView nextQuestionView,
    boolean finished
) {}