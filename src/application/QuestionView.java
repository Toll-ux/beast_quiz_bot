package application;

import domain.Question;
import java.util.List;


/**
 * 
 * QuestionView - обертка вопроса уровня application, убирающая правильный ответ на вопрос 
 * (чтобы можно было спокойно отправлять его юзеру)
 * @param questionId - id вопроса
 * @param text текст вопроса
 * @param answers список всех ответов на вопрос, которые предоставляются юзеру на выбор
 */
public record QuestionView(int questionId, String text, List<String> answers)
{
    public static QuestionView from(Question q)
    {
        return new QuestionView(q.id(), q.text(), q.answers());
    }
}