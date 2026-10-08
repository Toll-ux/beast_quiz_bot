package domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

class QuestionTest {
    /**Референсные переменные*/
    static final int id = 1;
    static final String text = "2+2 = ?";
    static final List<String> answers = List.of("1", "No", "14", "4");
    static final Set<Integer> correctAnswerIndexes = Set.of(3);
    static final Question qReference = new Question(id, text, answers, correctAnswerIndexes);

    /**Переменные для проверки вопроса с одним ответом*/
    static int idOne = 2;
    static String textOne = "3+3 = ?";
    static List<String> answersOne = List.of("6", "9", "4", "4");
    static Set<Integer> correctAnswerIndexesOne = Set.of(0);
    static Question questionWithOneAnswer = new Question(idOne, textOne, answersOne, correctAnswerIndexesOne);

    /**Переменные для проверки вопроса с двумя ответами. Остальные случаи по индукции*/
    static int idTwo = 3;
    static String textTwo = "3*3 = ?";
    static List<String> answersTwo = List.of("9", "No", "18/2", "4");
    static Set<Integer> correctAnswerIndexesTwo = Set.of(0, 2);
    static Question questionWithTwoAnswers = new Question(idTwo, textTwo, answersTwo, correctAnswerIndexesTwo);

    @DisplayName("Работа конструктора")
    @Test
    void generalAccessPermission(){
        Question qt = new Question(id, text, answers, correctAnswerIndexes);

        assertEquals(id, qt.id());
        assertEquals(text, qt.text());
        assertEquals(answers, qt.answers());
        assertEquals(correctAnswerIndexes, qt.correctAnswerIndexes());
    }

    @DisplayName("Конструктор получил id = 0")
    @Test
    void constructorZeroId(){
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Question(0, text, answers, correctAnswerIndexes);
        });

        Assertions.assertEquals("Question id is must be positive", thrown.getMessage());
    }

    @DisplayName("Конструктор получил отрицательный id")
    @Test
    void constructorNegativeId(){
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Question(-1000, text, answers, correctAnswerIndexes);
        });

        Assertions.assertEquals("Question id is must be positive", thrown.getMessage());
    }

    @DisplayName("Конструктор получил пустую строку вопроса")
    @Test
    void constructorNoQuestion(){
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Question(id, "", answers, correctAnswerIndexes);
        });

        Assertions.assertEquals("Question text must not be blank", thrown.getMessage());
    }

    @DisplayName("Конструктор получил null строку вопроса")
    @Test
    void constructorNullQuestion(){
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Question(id, null, answers, correctAnswerIndexes);
        });

        Assertions.assertEquals("Question text must not be blank", thrown.getMessage());
    }

    @DisplayName("Конструктор получил пустой список ответов")
    @Test
    void constructorNoAnswers(){
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Question(id, text, List.of(), correctAnswerIndexes);
        });

        Assertions.assertEquals("List of answers must not be empty", thrown.getMessage());
    }

    @DisplayName("Конструктор получил null вместо списка ответов")
    @Test
    void constructorNullAnswers(){
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Question(id, text, null, correctAnswerIndexes);
        });

        Assertions.assertEquals("List of answers must not be empty", thrown.getMessage());
    }

    @DisplayName("Конструктор получил пустое множество правильных ответов")
    @Test
    void constructorNoCorrectAnswerIndexes(){
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Question(id, text, answers, Set.of());
        });

        Assertions.assertEquals("Set of correctAnswerIndexes must not be empty", thrown.getMessage());
    }

    @DisplayName("Конструктор получил пустое null вместо множества правильных ответов")
    @Test
    void constructorNullCorrectAnswerIndexes(){
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Question(id, text, answers, null);
        });

        Assertions.assertEquals("Set of correctAnswerIndexes must not be empty", thrown.getMessage());
    }

    @DisplayName("Конструктор получил множество ответов, но есть индекс за пределами списка")
    @Test
    void constructorCorrectAnswerIndexesBiggerThanList(){
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Question(id, text, answers, Set.of(1, 2, 4));
        });

        Assertions.assertEquals("Correct answer indexes are out of range", thrown.getMessage());
    }

    @DisplayName("Конструктор получил множество ответов, но есть отрицательный индекс")
    @Test
    void constructorCorrectAnswerIndexesNegative(){
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Question(id, text, answers, Set.of(1, 2, -3));
        });

        Assertions.assertEquals("Correct answer indexes are out of range", thrown.getMessage());
    }

    @DisplayName("Множество индексов в классе постоянное, проверка изменением множества, которым инициализировался класс")
    @Test
    void initialisedCorrectAnswerIndexesChange() {
        Set<Integer> changeableCorAnsIdx  = new CopyOnWriteArraySet<>(correctAnswerIndexes);
        Question qt = new Question(id, text, answers, changeableCorAnsIdx);

        changeableCorAnsIdx.clear();
        assertEquals(correctAnswerIndexes, qt.correctAnswerIndexes());
    }

    @DisplayName("Множество индексов в классе постоянное, проверка изменением множества, которое получили по get запросу")
    @Test
    void getCorrectAnswerIndexesChange(){
        Set<Integer> newCorrectAnswerIndexes = qReference.correctAnswerIndexes();
        Assertions.assertThrows(UnsupportedOperationException.class, newCorrectAnswerIndexes::clear);
    }

    @DisplayName("Список ответов в классе постоянный, проверка изменением списка, которым инициализировался класс")
    @Test
    void initialisedAnswersChange(){
        List<String> newAnswers = new ArrayList<>(answers);
        Question qt = new Question(id, text, newAnswers, correctAnswerIndexes);
        newAnswers.clear();
        assertEquals(answers, qt.answers());
    }

    @DisplayName("Список индексов в классе постоянный, проверка изменением списка, который получили по get запросу")
    @Test
    void getAnswersChange(){
        List<String> newAnswers = qReference.answers();
        Assertions.assertThrows(UnsupportedOperationException.class, newAnswers::clear);
    }

    @DisplayName("Проверка на правильность ответа на вход пустое множество")
    @Test
    void isCorrectAnswerEmptyInput(){
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            qReference.isCorrectAnswer(Set.of());
        });

        Assertions.assertEquals("Set of ansIndexes must not be empty", thrown.getMessage());
    }

    @DisplayName("Проверка на правильность ответа  на вход null")
    @Test
    void isCorrectAnswerNull(){
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            qReference.isCorrectAnswer(null);
        });

        Assertions.assertEquals("Set of ansIndexes must not be empty", thrown.getMessage());
    }

    @DisplayName("Проверка на правильность ответа на вход множество с индексом > длины списка")
    @Test
    void isCorrectAnswerIndexBiggerThanListOfAnswers(){
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            qReference.isCorrectAnswer(Set.of(answers.size() + 2));
        });

        Assertions.assertEquals("Correct answer index is out of range", thrown.getMessage());
    }

    @DisplayName("Проверка на правильность ответа на вход множество с индексом < 0")
    @Test
    void testIsCorrectAnswerIndexNegative(){
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            qReference.isCorrectAnswer(Set.of(-12));
        });

        Assertions.assertEquals("Correct answer index is out of range", thrown.getMessage());
    }


    @DisplayName("Проверка на правильность ответа для set 1 элемент, неправильный ответ, частично правильный ответ")
    @Test
    void isCorrectAnswerOneRightAnswers(){
        assertTrue(questionWithOneAnswer.isCorrectAnswer(correctAnswerIndexesOne));
        assertFalse(questionWithOneAnswer.isCorrectAnswer(correctAnswerIndexesTwo));
        assertFalse(questionWithOneAnswer.isCorrectAnswer(correctAnswerIndexes));
    }

    @DisplayName("Проверка на правильность ответа для set 2 элемента, неправильный ответ, частично правильный ответ")
    @Test
    void isCorrectAnswerTwoRightAnswers(){
        assertTrue(questionWithTwoAnswers.isCorrectAnswer(correctAnswerIndexesTwo));
        assertFalse(questionWithTwoAnswers.isCorrectAnswer(correctAnswerIndexesOne));
        assertFalse(questionWithTwoAnswers.isCorrectAnswer(correctAnswerIndexes));
    }

}