package domain;

import java.util.List;

enum QuizStatus {
    NOT_STARTED,
    IN_PROGRESS,
    FINISHED
}


public class Quiz {
    private final List<Question> questions;
    private int currentQuestionIndex;
    private int score;
    private QuizStatus status;


    Quiz(List<Question> questions) {
        this.questions = List.copyOf(questions);
        this.status = QuizStatus.NOT_STARTED;
        this.currentQuestionIndex = 0;
        this.score = 0;
    }


    public List<Question> getQuestions() {
        return  List.copyOf(questions);
    }

    public int getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }

    public int getScore() {
        return score;
    }

    public void start() {
        if (status != QuizStatus.NOT_STARTED) {
            throw new IllegalStateException("Quiz cannot be started");
        }

        this.status = QuizStatus.IN_PROGRESS;
    }

    public void finish() {
        if (status != QuizStatus.IN_PROGRESS) {
            throw new IllegalStateException("Quiz is not in progress");
        }

        status = QuizStatus.FINISHED;
    }

    public boolean submitAnswer(int answer) {
        if (status != QuizStatus.IN_PROGRESS) {
            throw new IllegalStateException("Quiz is not in progress");
        }

        Question question = questions.get(currentQuestionIndex);

        boolean correct = question.isCorrectAnswer(answer);

        if (correct) {
            score += 10;
        }

        return correct;
    }

    public void nextQuestion() {
        if (status != QuizStatus.IN_PROGRESS) {
            throw new IllegalStateException("Quiz is not in progress");
        }

        currentQuestionIndex++;
        //тут не знаю, можно здесь finish вызывать и менять статус или менять статус в app
    }
}
