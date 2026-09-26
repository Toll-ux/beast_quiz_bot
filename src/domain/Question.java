package domain;

import java.util.List;

public class Question {
    private final int id;
    private final String text;
    private final List<String> answers;
    private final int correctAnswerIndex; // номер в списке

    public Question(int id, String text, List<String> answers, int correctAnswerIndex) {
        this.id = id;
        this.text = text;
        this.answers = List.copyOf(answers);
        this.correctAnswerIndex = correctAnswerIndex;
    }

    public int getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public List<String> getAnswers() {
        return List.copyOf(answers);
    }

    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    public boolean isCorrectAnswer(int ans){
        if(ans < 0 || ans >= answers.size()){
            throw new IllegalArgumentException("Answer index is out of range");
        }

        return ans == correctAnswerIndex;
    }

}