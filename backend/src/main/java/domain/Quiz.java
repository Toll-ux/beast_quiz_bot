package domain;

import java.util.List;
import java.util.Set;

/**<h6>Класс отвечает за статус квиза</h6>
 * <h7>Это защита от начала квиза после того как он закончен и отправки ответа в рандомный момент и тп.</h7>
 * Порядок только такой:
 *  <ul>
 *     <li>NOT_STARTED -> IN_PROGRESS</li>
 *     <li>IN_PROGRESS -> FINISHED</li>
 *  <ul>
 *  Статус прервано добавлен
 */
enum QuizStatus {
    NOT_STARTED,
    IN_PROGRESS,
    FINISHED,
    ABORTED
}

/**Domain - Квиз*/
public class Quiz {
    /**Массив вопросов для пользователя*/
    private final List<Question> questions;
    /** Индекс в массиве вопросов, на котором сейчас пользователь*/
    private int currentQuestionIndex;
    private int score;
    private QuizStatus status;


    /** Конструктор создает квиз
     *
     * @param questions список подготовленных вопросов
     *
     * @throws IllegalArgumentException если список вопросов пустой
     * @throws NullPointerException если список null
     */
    public Quiz(List<Question> questions) {
        if (questions == null) throw new NullPointerException("Question list can not be null");
        if (questions.isEmpty()) throw new IllegalArgumentException("Question list is empty");

        this.questions = List.copyOf(questions);
        this.status = QuizStatus.NOT_STARTED;
        this.currentQuestionIndex = 0;
        this.score = 0;
    }

    /**Передается копия списка вопросов, чтобы не изменить оригинал*/
    public List<Question> getQuestions() {
        return  List.copyOf(questions);
    }

    public int getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }

    public int getScore() {
        return score;
    }

    /**Функция меняет статус квиза с NOT_STARTED на IN_PROGRESS
     * @throws IllegalStateException если квиз не в NOT_STARTED
     */
    public void start() {
        if (status != QuizStatus.NOT_STARTED) {
            throw new IllegalStateException("Quiz cannot be started");
        }

        this.status = QuizStatus.IN_PROGRESS;
    }

    /**Функция меняет статус квиза с IN_PROGRESS на FINISHED
     * @throws IllegalStateException если квиз не в IN_PROGRESS
     */
    public void finish() {
        if (status != QuizStatus.IN_PROGRESS) {
            throw new IllegalStateException("Quiz is not in progress");
        }

        status = QuizStatus.FINISHED;
    }


    /**Функция меняет статус квиза с IN_PROGRESS на ABORTED
     * @throws IllegalStateException если квиз не в IN_PROGRESS
     */
    public void abort(){
        if (status != QuizStatus.IN_PROGRESS)
            throw new IllegalStateException("Quiz is not in progress");

        status = QuizStatus.ABORTED;
    }

    /**Функция проверяет ответ на вопрос.
     * Она создает копию текущего вопроса и сверят у него ответ.
     * Прибавляет очки за каждый правильный ответ
     *
     * @param selectedAnswers индексы в списке вопросов
     * @throws IllegalStateException если статус квиза не IN_PROGRESS
     * @throws NullPointerException если переданный ответ null
     * @throws IllegalArgumentException если переданный ответ пустой
     * @return класс с правильным ответом и данными ответами
     */
    public AnswerResult submitAnswer(Set<Integer> selectedAnswers) {
        if (selectedAnswers == null)
            throw new NullPointerException("Selected Answers can not be null");

        if (selectedAnswers.isEmpty())
            throw new IllegalArgumentException("Selected answers must not be Empty");

        if (status != QuizStatus.IN_PROGRESS)
            throw new IllegalStateException("Quiz is not in progress");


        if (currentQuestionIndex > questions.size())
            throw new ArrayIndexOutOfBoundsException(
                    String.format("Question Index is out of range: %d > %d",currentQuestionIndex, questions.size()));


        Question question = questions.get(currentQuestionIndex);

        boolean correct = question.isCorrectAnswer(Set.copyOf(selectedAnswers));

        if (correct) {
            score += calcScore();
        }

        return new AnswerResult(
                correct,
                Set.copyOf(selectedAnswers),
                question.correctAnswerIndexes()
        );
    }

    /**Формула для подсчета очков*/
    private int calcScore(){
        return 10;
    }

    /**Функция инкрементирует индекс в массиве вопросов
     * @throws IllegalStateException если статус квиза не IN_PROGRESS
     *
     *@see submitAnswer WARNING решен, но надо иметь это ввиду
     * WARNING: инкрементирует, даже если массив закончился, возможен выход за границы массива
     */
    public void nextQuestion() {
        if (status != QuizStatus.IN_PROGRESS) {
            throw new IllegalStateException("Quiz is not in progress");
        }

        currentQuestionIndex++;
    }

    /**Функции показывающие состояние квиза, нужны для отладки*/
    public boolean isStarted(){
        return status == QuizStatus.IN_PROGRESS;
    }

    public boolean isFinished(){
        return status == QuizStatus.FINISHED;
    }

    public boolean isAborted(){
        return status == QuizStatus.ABORTED;
    }

    public boolean isDoingNothing(){
        return status == QuizStatus.NOT_STARTED;
    }

}
