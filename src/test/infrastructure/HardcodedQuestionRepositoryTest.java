package infrastructure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import domain.Question;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HardcodedQuestionRepositoryTest {

    @DisplayName("Получение необходимого количества вопросов")
    @Test
    void findRandomQuestions() {
        HardcodedQuestionRepository repository =
                new HardcodedQuestionRepository();

        List<Question> questions =
                repository.findRandomQuestions(2);

        assertEquals(2, questions.size());
    }

    // ! Менять тест при изменении количества доступных вопросов в списке
    @DisplayName("Получение всех доступных вопросов")
    @Test
    void findAllQuestions() {
        HardcodedQuestionRepository repository =
                new HardcodedQuestionRepository();

        List<Question> questions =
                repository.findRandomQuestions(4);

        assertEquals(4, questions.size());
    }

    @DisplayName("Ошибка при запросе нулевого количества вопросов")
    @Test
    void findRandomQuestionsZeroInput() {
        HardcodedQuestionRepository repository =
                new HardcodedQuestionRepository();

        IllegalArgumentException thrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> repository.findRandomQuestions(0)
                );

        assertEquals(
                "QuestionCount must be positive",
                thrown.getMessage()
        );
    }

    @DisplayName("Ошибка при запросе отрицательного количества вопросов")
    @Test
    void findRandomQuestionsNegativeInput() {
        HardcodedQuestionRepository repository =
                new HardcodedQuestionRepository();

        IllegalArgumentException thrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> repository.findRandomQuestions(-1)
                );

        assertEquals(
                "QuestionCount must be positive",
                thrown.getMessage()
        );
    }

    @DisplayName("Ошибка при запросе большего количества вопросов, чем есть")
    @Test
    void findRandomQuestionsMoreThanAvailable() {
        HardcodedQuestionRepository repository =
                new HardcodedQuestionRepository();

        IllegalArgumentException thrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> repository.findRandomQuestions(5)
                );

        assertEquals(
                "More questions were requested than are available",
                thrown.getMessage()
        );
    }

    @DisplayName("Полученные вопросы не повторяются")
    @Test
    void findRandomQuestionsWithoutDuplicates() {
        HardcodedQuestionRepository repository =
                new HardcodedQuestionRepository();

        List<Question> questions =
                repository.findRandomQuestions(4);

        long uniqueQuestionsCount = questions.stream()
                .map(Question::id)
                .distinct()
                .count();

        assertEquals(
                questions.size(),
                uniqueQuestionsCount
        );
    }
}