package infrastructure;

import application.port.QuestionRepository;
import domain.Question;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

public class HardcodedQuestionRepository implements QuestionRepository{

    // id вопросов должны не повторяться
    private final List<Question> questions = List.of(
            new Question(
                    1,
                    "Какой язык программирования используется в проекте?",
                    List.of("Java", "Python", "C++", "JavaScript"),
                    Set.of(0)
            ),
            new Question(
                    2,
                    "Какой тип данных хранит целые числа в Java?",
                    List.of("String", "int", "boolean", "double"),
                    Set.of(1)
            ),
            new Question(
                    3,
                    "Какое ключевое слово используется для реализации интерфейса?",
                    List.of("extends", "implements", "instanceof", "import"),
                    Set.of(1)
            ),
            new Question(
                    4,
                    "Из перечисленных имен выберите имена участников проекта.",
                    List.of("Дима", "Андрей", "Саша", "Кира"),
                    Set.of(0,2,3)
            )
    );

    /** Возвращает случайно выбранные вопросы из доступного списка.
     * @param questionsCount - количество вопрос, которые нужно выбрать.
     * @return список случайно выбранных вопросов без повторений (в рамках 1 квиза)
     * @throws IllegalArgumentException если запрошенное количество вопросов
     * неположительное или превышает число доступных.
     */

    @Override
    public List<Question> findRandomQuestions(int questionsCount){
        if (questionsCount <= 0 )
        {
            throw new IllegalArgumentException("QuestionCount must be positive");
        }
        if (questionsCount > questions.size())
        {
            throw new IllegalArgumentException("More questions were requested than are available");
        }

        List<Question> shuffledQuestions = new ArrayList<>(questions);
        Collections.shuffle(shuffledQuestions);
        //Возвращаем независимый список, а не представление части исходного
        return new ArrayList<>(
                shuffledQuestions.subList(0, questionsCount)
        );
    }
}