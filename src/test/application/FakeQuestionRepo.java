package application;

import application.port.QuestionRepository;
import domain.Question;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FakeQuestionRepo implements QuestionRepository {

    public final List<Question> questions = List.of(
            new Question(1, "Сколько планет в Солнечной системе?", List.of("7", "8", "9", "10"), Set.of(1)));

    public boolean isOutputNull = false;
    public boolean isOutputEmpty = false;

    @Override
    public List<Question> findRandomQuestions(int questionsCount) {
        if (isOutputNull) return null;
        if (isOutputEmpty) return List.of();

        if (questionsCount <= 0 )
        {
            throw new IllegalArgumentException("QuestionCount must be positive");
        }
        if (questionsCount > questions.size())
        {
            throw new IllegalArgumentException("More questions were requested than are available");
        }

        List<Question> shuffledQuestions = new ArrayList<>(questions);
        //Возвращаем независимый список, а не представление части исходного
        return new ArrayList<>(
                shuffledQuestions.subList(0, questionsCount)
        );
    }
}
