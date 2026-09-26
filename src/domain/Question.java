package domain;

import java.util.List;

/**
 * @param correctAnswerIndex номер в списке
 */
public record Question(int id, String text, List<String> answers, int correctAnswerIndex) {
    public Question(int id, String text, List<String> answers, int correctAnswerIndex) {
        this.id = id;
        this.text = text;
        this.answers = List.copyOf(answers);
        this.correctAnswerIndex = correctAnswerIndex;
    }

    @Override
    public List<String> answers() {
        return List.copyOf(answers);
    }

    public boolean isCorrectAnswer(int ans) {
        if (ans < 0 || ans >= answers.size()) {
            throw new IllegalArgumentException("Answer index is out of range");
        }

        return ans == correctAnswerIndex;
    }

}