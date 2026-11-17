package domain;

import java.util.List;
import java.util.Set;

/** Domain - Вопрос
 *
 * @param id идентификатор вопроса
 * @param text текст вопроса
 * @param answers список ответов
 * @param correctAnswerIndexes номер правильного ответа в списке
 */
public record Question(int id, String text, List<String> answers, Set<Integer> correctAnswerIndexes) {

    /**
     * Создает вопрос и проверяет правильность ввода данных
     *
     * @throws IllegalArgumentException если данные вопроса некорректны
     */
    public Question(int id, String text, List<String> answers, Set<Integer> correctAnswerIndexes) {

        if (id <= 0) throw new IllegalArgumentException("Question id is must be positive");
        if (text == null || text.isBlank()) throw new IllegalArgumentException("Question text must not be blank");
        if (answers == null || answers.isEmpty()) throw new IllegalArgumentException("List of answers must not be empty");
        if (correctAnswerIndexes == null || correctAnswerIndexes.isEmpty()) throw new IllegalArgumentException("Set of correctAnswerIndexes must not be empty");
        for (int index : correctAnswerIndexes) {
            if (index < 0 || index >= answers.size()) {
                throw new IllegalArgumentException(
                        "Correct answer indexes are out of range"
                );
            }
        }


        this.id = id;
        this.text = text;
        this.answers = List.copyOf(answers);
        this.correctAnswerIndexes = Set.copyOf(correctAnswerIndexes);
    }

    /**Возвращается копия списка ответов, чтобы не изменить оригинал*/
    @Override
    public List<String> answers() {
        return List.copyOf(answers);
    }

    /**Копия множества, чтобы не изменить оригинал*/
    @Override
    public Set<Integer> correctAnswerIndexes(){
        return Set.copyOf(correctAnswerIndexes);
    }

    /**Проверяет, является ли данный индекс правильным ответом
     *
     * @param ansIndexes индекс выбранного ответа
     * @return true, если ответ правильный, иначе false
     * @throws IllegalArgumentException если индекс выходит за пределы списка answers
     */
    public boolean isCorrectAnswer(Set<Integer> ansIndexes) {
        if (ansIndexes == null || ansIndexes.isEmpty()) throw new IllegalArgumentException("Set of ansIndexes must not be empty");
        for (int index : ansIndexes) {
            if (index < 0 || index >= answers.size()) {
                throw new IllegalArgumentException(
                        "Correct answer index is out of range"
                );
            }
        }

        return correctAnswerIndexes.equals(ansIndexes);
    }
}