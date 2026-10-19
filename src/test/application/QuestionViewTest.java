package application;

import domain.Question;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


class QuestionViewTest {

    private QuestionView questionViewTest;
    private Question question;

    final int questionId = 10;
    final String text = "123";
    final List<String> answers = List.of("1", "2");
    final Set<Integer> answerIdx = Set.of(0);

    @DisplayName("Инициализация и подготовка данных в questionViewTest")
    @BeforeEach
    void prepareTestDataQuestionViewTest() {
        questionViewTest = new QuestionView(questionId, text, answers);
        assertNotNull(questionViewTest);
        assertEquals(questionId, questionViewTest.questionId());
        assertEquals(text, questionViewTest.text());
        assertEquals(answers.size(), questionViewTest.answers().size());
    }

    @DisplayName("Инициализация и подготовка данных в Question")
    @BeforeEach
    void prepareTestDataQuestion(){
        question = new Question(questionId, text, answers, answerIdx);
    }

    @DisplayName("Проверка конструктора копирования")
    @Test
    void copyConstructor(){
        QuestionView newQuestionView = QuestionView.from(question);
        assertEquals(questionViewTest.questionId(), newQuestionView.questionId());
        assertEquals(questionViewTest.answers(), newQuestionView.answers());
        assertEquals(questionViewTest.text(), newQuestionView.text());
    }

    @DisplayName("Проверка конструктора копирования передали null вместо question")
    @Test
    void copyConstructorFromNull(){
        assertThrows(NullPointerException.class, () -> QuestionView.from(null));
    }
}