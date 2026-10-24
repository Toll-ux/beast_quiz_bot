package domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AnswerResultTest {

    boolean correct = true;
    Set<Integer> selectedAnswers = Set.of(1,2);
    Set<Integer> correctAnswers = Set.of(1,2);

    AnswerResult ansRes;

    @DisplayName("Инициализация класса")
    @BeforeEach
    void initialization(){
        ansRes = new AnswerResult(correct, selectedAnswers, correctAnswers);
        assertNotNull(ansRes);
        assertEquals(correct, ansRes.correct());
        assertEquals(selectedAnswers, ansRes.selectedAnswers());
        assertEquals(correctAnswers, ansRes.correctAnswers());
    }

    @DisplayName("selectedAnswers: мутация исходного сета не меняет результат")
    @Test
    void mutatingOriginalSelectedAnswersDoesNotAffectResult() {
        Set<Integer> mutableSelected = new HashSet<>(Set.of(1, 2));
        AnswerResult result = new AnswerResult(true, mutableSelected, Set.of(1, 2));
        Set<Integer> before = result.selectedAnswers();
        assertEquals(Set.of(1, 2), before);
        mutableSelected.add(99);
        assertEquals(Set.of(1, 2, 99), result.selectedAnswers(),
                "Аксессор копирует поле в момент вызова — ожидаем актуальное состояние");
    }

    @DisplayName("correctAnswers: мутация исходного сета не меняет уже полученный результат")
    @Test
    void mutatingOriginalCorrectAnswersDoesNotAffectObtainedResult() {
        Set<Integer> mutableCorrect = new HashSet<>(Set.of(1, 2));
        AnswerResult result = new AnswerResult(true, Set.of(1, 2), mutableCorrect);
        Set<Integer> snapshot = result.correctAnswers();
        assertEquals(Set.of(1, 2), snapshot);

        mutableCorrect.add(99);

        assertEquals(Set.of(1, 2), snapshot,
                "Ранее полученный сет не должен меняться после мутации оригинала");
    }


    @DisplayName("selectedAnswers: возвращённый сет неизменяем")
    @Test
    void selectedAnswersReturnsImmutableSet() {
        Set<Integer> result = ansRes.selectedAnswers();
        assertThrows(UnsupportedOperationException.class,
                () -> result.add(42),
                "Set.copyOf возвращает неизменяемый сет — add должен падать");
        assertThrows(UnsupportedOperationException.class,
                () -> result.remove(1));
        assertThrows(UnsupportedOperationException.class,
                () -> result.clear());
    }

    @DisplayName("correctAnswers: возвращённый сет неизменяем")
    @Test
    void correctAnswersReturnsImmutableSet() {
        Set<Integer> result = ansRes.correctAnswers();
        assertThrows(UnsupportedOperationException.class,
                () -> result.add(42),
                "Set.copyOf возвращает неизменяемый сет — add должен падать");
        assertThrows(UnsupportedOperationException.class,
                () -> result.remove(1));
        assertThrows(UnsupportedOperationException.class,
                () -> result.clear());
    }
}