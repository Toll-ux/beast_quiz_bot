package domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class QuizTest {

    static final List<Question> qtsReference = List.of(new Question(1,"2+2 = ?", List.of("1", "No", "14", "4"), Set.of(3)),
            new Question(2, "3+3 = ?",  List.of("6", "12/2", "No", "Why"), Set.of(1,2))
    );

    static final int initializedQuestionIndex = 0;
    static final int initializedScore = 0;

    @DisplayName("Конструктор, действительно, что-то инициализирует")
    @Test
    void  constructor(){
        Quiz quiz = new Quiz(qtsReference);
        assertEquals(initializedQuestionIndex, quiz.getCurrentQuestionIndex());
        assertEquals(initializedScore, quiz.getScore());
        assertEquals(qtsReference, quiz.getQuestions());
    }

    @DisplayName("Ошибка при инициализации пустым списком")
    @Test
    void ConstructorEmptyInput(){
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Quiz(List.of());
        });
        assertEquals("Question list is empty", thrown.getMessage());
    }

    @DisplayName("Ошибка при инициализации null")
    @Test
    void constructorNullSafety(){
        NullPointerException thrown = Assertions.assertThrows(NullPointerException.class, () -> {
            new Quiz(null);
        });
        assertEquals("Question list can not be null", thrown.getMessage());
    }

    @DisplayName("Список вопросов в классе постоянный, проверка изменением списка, которым инициализировался класс")
    @Test
    void initialisedQuestionsChange() {
        List<Question> qts = new ArrayList<>(qtsReference);
        Quiz quiz = new Quiz(qts);
        qts.clear();
        assertEquals(qtsReference, quiz.getQuestions());
    }

    @DisplayName("Список вопросов в классе постоянный, проверка изменением списка, который получили по get запросу")
    @Test
    void getQuestionsChange() {
        Quiz quiz = new Quiz(qtsReference);
        List<Question> qts = quiz.getQuestions();
        Assertions.assertThrows(UnsupportedOperationException.class, qts::clear);
    }

    @DisplayName("Старт нормальная работа")
    @Test
    void start(){
        Quiz quiz = new Quiz(qtsReference);
        quiz.start();
        assertTrue(quiz.isStarted());
    }

    @DisplayName("Старт после старта")
    @Test
    void doubleStart() {
        Quiz quiz = new Quiz(qtsReference);
        quiz.start();
        IllegalStateException thrown = Assertions.assertThrows(IllegalStateException.class, quiz::start);
        assertEquals("Quiz cannot be started", thrown.getMessage());
    }

    @DisplayName("Старт после финиша")
    @Test
    void startAfterFinish() {
        Quiz quiz = new Quiz(qtsReference);
        quiz.start();
        quiz.finish();
        IllegalStateException thrown = Assertions.assertThrows(IllegalStateException.class, quiz::start);
        assertEquals("Quiz cannot be started", thrown.getMessage());
    }

    @DisplayName("Старт после аборта")
    @Test
    void startAfterAbort(){
        Quiz quiz = new Quiz(qtsReference);
        quiz.start();
        quiz.abort();
        IllegalStateException thrown = Assertions.assertThrows(IllegalStateException.class, quiz::start);
        assertEquals("Quiz cannot be started", thrown.getMessage());
    }

    @DisplayName("Финиш нормальная работа")
    @Test
    void finish() {
        Quiz quiz = new Quiz(qtsReference);
        quiz.start();
        quiz.finish();
        assertTrue(quiz.isFinished());
    }

    @DisplayName("Финиш до старта")
    @Test
    void finishBeforeStart(){
        Quiz quiz = new Quiz(qtsReference);

        IllegalStateException thrown = Assertions.assertThrows(IllegalStateException.class, quiz::finish);
        assertEquals("Quiz is not in progress", thrown.getMessage());
    }

    @DisplayName("Финиш после финиша")
    @Test
    void finishAfterFinish(){
        Quiz quiz = new Quiz(qtsReference);

        quiz.start();
        quiz.finish();
        IllegalStateException thrown = Assertions.assertThrows(IllegalStateException.class, quiz::finish);
        assertEquals("Quiz is not in progress", thrown.getMessage());
    }

    @DisplayName("Финиш после аборта")
    @Test
    void finishAfterAbort(){
        Quiz quiz = new Quiz(qtsReference);
        quiz.start();
        quiz.abort();
        IllegalStateException thrown = Assertions.assertThrows(IllegalStateException.class, quiz::finish);
        assertEquals("Quiz is not in progress", thrown.getMessage());
    }

    @DisplayName("Аборт нормальная работа")
    @Test
    void abort() {
        Quiz quiz = new Quiz(qtsReference);
        quiz.start();
        quiz.abort();
        assertTrue(quiz.isAborted());
    }

    @DisplayName("Аборт до старта")
    @Test
    void abortBeforeStart(){
        Quiz quiz = new Quiz(qtsReference);
        IllegalStateException thrown = Assertions.assertThrows(IllegalStateException.class, quiz::abort);
        assertEquals("Quiz is not in progress", thrown.getMessage());
    }

    @DisplayName("Аборт после финиша")
    @Test
    void abortAfterFinish(){
        Quiz quiz = new Quiz(qtsReference);
        quiz.start();
        quiz.finish();
        IllegalStateException thrown3 = Assertions.assertThrows(IllegalStateException.class, quiz::abort);
        assertEquals("Quiz is not in progress", thrown3.getMessage());
    }

    @DisplayName("Аборт после финиша")
    @Test
    void abortAfterAbort(){
        Quiz quiz = new Quiz(qtsReference);
        quiz.start();
        quiz.abort();
        IllegalStateException thrown3 = Assertions.assertThrows(IllegalStateException.class, quiz::abort);
        assertEquals("Quiz is not in progress", thrown3.getMessage());
    }

    @DisplayName("Следующий вопрос нормальная работа")
    @Test
    void nextQuestion() {
        Quiz quiz = new Quiz(qtsReference);
        quiz.start();
        int qtsIdxBefore = quiz.getCurrentQuestionIndex();
        quiz.nextQuestion();
        int qtsIdxAfter = quiz.getCurrentQuestionIndex();
        assertEquals(qtsIdxBefore, qtsIdxAfter-1);
    }

    @DisplayName("Следующий вопрос выход за массив вопросов")
    @Test
    void nextQuestionOutOfRange(){
        Set<Integer> ans = Set.of(0);
        Quiz quiz = new Quiz(qtsReference);
        assertEquals(initializedQuestionIndex, quiz.getCurrentQuestionIndex());
        int size = quiz.getQuestions().size();
        quiz.start();
        for (int i = 0; i < size; i++){
            assertDoesNotThrow(() -> {quiz.submitAnswer(ans);});
            quiz.nextQuestion();
        }
        quiz.nextQuestion();
        IndexOutOfBoundsException thrown = Assertions.assertThrows(IndexOutOfBoundsException.class, () -> {
            quiz.submitAnswer(ans);
        });
        assertEquals(String.format("Question Index is out of range: %d > %d", quiz.getCurrentQuestionIndex(), size),
                thrown.getMessage());
    }

    @DisplayName("Следующий вопрос, но квиз не начался")
    @Test
    void nextQuestionQuisIsNotStarted(){
        Quiz quiz = new Quiz(qtsReference);
        assertFalse(quiz.isStarted());
        IllegalStateException thrown = Assertions.assertThrows(IllegalStateException.class, quiz::nextQuestion);
        assertEquals("Quiz is not in progress", thrown.getMessage());
    }

    @DisplayName("Следующий вопрос, но квиз прервался")
    @Test
    void nextQuestionQuisIsAborted(){
        Quiz quiz = new Quiz(qtsReference);
        quiz.start();
        quiz.abort();
        assertTrue(quiz.isAborted());
        IllegalStateException thrown = Assertions.assertThrows(IllegalStateException.class, quiz::nextQuestion);
        assertEquals("Quiz is not in progress", thrown.getMessage());
    }

    @DisplayName("Следующий вопрос, но квиз закончился")
    @Test
    void nextQuestionQuizIsFinished(){
        Quiz quiz = new Quiz(qtsReference);
        quiz.start();
        quiz.finish();
        IllegalStateException thrown2 = Assertions.assertThrows(IllegalStateException.class, quiz::nextQuestion);
        assertEquals("Quiz is not in progress", thrown2.getMessage());
    }


    @DisplayName("Отправить ответ нормальная работа")
    @Test
    void submitAnswer() {
        Quiz quiz = new Quiz(qtsReference);
        assertEquals(initializedQuestionIndex, quiz.getCurrentQuestionIndex());
        int size = quiz.getQuestions().size();
        quiz.start();
        for (int i = 0; i < size; i++){
            int idx = quiz.getCurrentQuestionIndex();
            assertTrue(quiz.submitAnswer(qtsReference.get(idx).correctAnswerIndexes()).correct());
            quiz.nextQuestion();
        }
    }

    @DisplayName("Отправить ответ, квиз не в процессе")
    @Test
    void submitAnswerQuizIsNotInProgres(){
        Set<Integer> ans = Set.of(0);
        Quiz quiz = new Quiz(qtsReference);

        IllegalStateException thrown = Assertions.assertThrows(IllegalStateException.class, () -> {
            quiz.submitAnswer(ans);
        });
        assertEquals("Quiz is not in progress", thrown.getMessage());
    }

    @DisplayName("Отправить пустой ответ")
    @Test
    void submitAnswerEmpty(){
        Quiz quiz = new Quiz(qtsReference);
        quiz.start();
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            quiz.submitAnswer(Set.of());
        });
        assertEquals("Selected answers must not be Empty", thrown.getMessage());
    }

    @DisplayName("Отправить ответ null")
    @Test
    void submitAnswerNullSafety(){
        Quiz quiz = new Quiz(qtsReference);
        quiz.start();
        NullPointerException thrown = Assertions.assertThrows(NullPointerException.class, () -> {
            quiz.submitAnswer(null);
        });
        assertEquals("Selected Answers can not be null", thrown.getMessage());
    }

}