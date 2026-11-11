package client.dialog;

import client.api.BackendRestClient;
import client.api.dto.response.*;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 */
public class DialogHandler {

    private final BackendRestClient backendRestClient;
    private final long telegramId;

    /// При создании устанавливает состояние ожидания команды от пользователя
    private DialogState state = DialogState.MAIN_MENU;

    private long currentQuestionId;

    public DialogHandler(
            BackendRestClient backendRestClient,
            long telegramId
    ) {
        this.backendRestClient = backendRestClient;
        this.telegramId = telegramId;
    }

    /**
     * Метод принятия сообщения, проверки наличия ввода и отправки сообщения на дальнейшую обработку
     * в зависимости от состояния диалога пользователя
     * @param message - сообщение от пользователя
     * @return вызов один из методов-хэндлеров, зависимых от состояния диалога, если сообщение непустое
     * @return ответ-просьбу ввода сообщения, если сообщение пустое
     */
    public DialogResponse handleMessage(String message) {
        /// Если пользователь отправил пустую строку - просит отправить сообщение повторно
        if (message == null) {
            return new DialogResponse(
                    "Please enter a message.",
                    false
            );
        }

        String input = message.trim();

        return switch (state) {
            case MAIN_MENU -> handleMainMenu(input);
            case WAITING_FOR_QUESTION_COUNT ->
                    handleQuestionCount(input);
            case ANSWERING_QUESTION ->
                    handleAnswer(input);
        };
    }

    /**
     * Метод обработки сообщения в состоянии MAIN_MENU,
     * когда игрок не находится в игре или ее инициации (вводе количества вопросов)
     * @param input - отформатированное сообщение пользователя - отсутствуют пробелы
     * @return зависит от выбора команды:
     * p - ответ-просьба ввести количество вопросов для квиза и перевод диалога в состояние WAITING_FOR_QUESTION_COUNT
     * l - вызов метода showLeaderboard
     * h - вызов метода showHelp
     * q - ответ-прощание и завершение диалога (exitRequested = true)
     * default - ответ-сообщение о неизвестной команде и просьба повторного ввода.
     */
    private DialogResponse handleMainMenu(String input) {
        return switch (input.toLowerCase()) {
            case "p" -> {
                state = DialogState.WAITING_FOR_QUESTION_COUNT;
                yield new DialogResponse(
                        "Please enter the desired quiz length:",
                        false
                );
            }

            case "l" -> showLeaderboard();

            case "h" -> showHelp();

            case "q" -> new DialogResponse(
                    "Goodbye!",
                    true
            );

            default -> new DialogResponse(
                    "Unknown command. Please enter p, l, h, or q.",
                    false
            );
        };
    }

    /**
     * Метод обработки сообщения в состоянии WAITING_FOR_QUESTION_COUNT,
     * когда пользователь инициировал старт квиза и хэндлер ждет, сколько вопросов он хочет пройти.
     * @param input - отформатированное сообщение пользователя - отсутствуют пробелы
     * @return
     * если пользователь ввел число меньше 1 или больше количества имеющихся в базе вопросов(сейчас - 4):
     * ответ-просьба ввести число от 1 до 4;
     * если пользователь ввел не число:
     * ответ-просьба ввести число от 1 до 4;
     *
     * После получения подходящего запроса переводит диалог в режим ANSWERING_QUESTION,
     * вызывает метод получения вопроса, чтобы пользователю сразу вывелся первый вопрос
     */
    private DialogResponse handleQuestionCount(String input) {
        int questionCount;
        try {
            questionCount = Integer.parseInt(input);

            // На будущее: questionCount должен сверяться с количеством вопросов в базе, т.е. 4 это временное число
            if (questionCount < 1 || questionCount > 4) {
                return new DialogResponse(
                        "Please enter a number from 1 to 4.",
                        false
                );
            }

        } catch (NumberFormatException e) {
            return new DialogResponse(
                    "Please enter a valid number from 1 to 4.",
                    false
            );
        }

        /// На запрос о начале квиза приходит ответ, создан ли новый квиз или используется существующий незавершенный
        /// Пока никак не используется
        StartQuizResponse startResponse =
                backendRestClient.startQuiz(telegramId, questionCount);

        /// Переход в режим ответа на вопрос - после удачного начала пользователю сразу показывают первый.
        state = DialogState.ANSWERING_QUESTION;

        return getCurrentQuestionMessage();
    }

    /**
     * Метод, формирующий презентацию вопроса пользователю.
     * @return ответ с вопросом и вариантами ответов на него + просьбой ввести номера ответов.
     */
    private DialogResponse getCurrentQuestionMessage() {
        GetCurrentQuestionViewResponse question =
                backendRestClient.getCurrentQuestion(telegramId);

        currentQuestionId = question.questionId();

        /// Класс, который позволяет эффективно работать с изменяемыми строками
        StringBuilder message = new StringBuilder();

        /// Формирование текста вопроса
        message.append(question.text()).append("\n\n");

        for (int i = 0; i < question.answers().size(); i++) {
            message.append(i + 1)
                    .append(": ")
                    .append(question.answers().get(i))
                    .append("\n");
        }

        message.append(
                "\nEnter the number(s) of your answer. "
//                        + "For multiple answers, separate numbers with spaces."
        );

        return new DialogResponse(message.toString(), false);
    }

    /**
     * Метод обработки сообщения в состоянии ANSWERING_QUESTION,
     * когда пользователь видит вопрос и ожидается ввод ответа на него.
     * Отправляет сообщение на обработку методу parseAnswer, который преобразует их в множество цифр.
     * @param input - отформатированное сообщение пользователя - отсутствуют пробелы
     * @return
     * если parseAnswers не увидел похожую на набор ответов (цифр) строку:
     * ответ-просьба ввести ответ из цифр;
     * если пользователь ввел некоторый набор вариантов ответа:
     * ответ-результат(правильный/неправильный ответ),
     * если вопрос в квизе был последним, результаты за весь пройденный квиз, переводит диалог в состояние MAIN_MENU,
     * иначе возвращает обзор следующего вопроса.
     */
    private DialogResponse handleAnswer(String input) {
        Set<Integer> chosenAnswers;

        try {
            chosenAnswers = parseAnswers(input);

            if (chosenAnswers.isEmpty()) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException e) {
            return new DialogResponse(
                    "Invalid answer. Please enter the answer numbers.",
                    false
            );
        }

        AnswerQuestionResponse result =
                backendRestClient.answerQuestion(
                        telegramId,
                        currentQuestionId,
                        chosenAnswers
                );

        /// Формирование текста результата ответа на вопрос
        StringBuilder message = new StringBuilder();

        if (result.correct()) {
            message.append("Good job, you answered correctly!\n");
        } else {
            message.append("Incorrect! The correct answers were: ")
                    .append(
                            result.correctAnswers().stream()
                                    .map(n -> n + 1)
                                    .map(String::valueOf)
                                    .collect(Collectors.joining(", "))
                    )
                    .append("\n");
        }

        /// Завершение квиза, если вопрос был последний
        if (result.finished()) {
            FinishQuizResponse finishResult =
                    backendRestClient.finishQuiz(telegramId);

            state = DialogState.MAIN_MENU;

            message.append("\nQuiz completed!")
                    .append("\nYour quiz score: ")
                    .append(finishResult.quizScore())
                    .append("\nYour total score: ")
                    .append(finishResult.userTotalScore())
                    .append("\n\n");

            message.append("Choose an action: p, l, h, or q.");

            return new DialogResponse(message.toString(), false);
        }

        /// Запрос следующего вопроса, если нынешний не был последним

        message.append("\n");

        DialogResponse nextQuestion = getCurrentQuestionMessage();
        message.append(nextQuestion.message());

        return new DialogResponse(message.toString(), false);
    }

    /** В РЕДАКЦИИ
     * Функция трансформирует введеный пользователем ответ на вопрос во множество цифр
     */
    private Set<Integer> parseAnswers(String input) {
        Set<Integer> result = new HashSet<>();

        String[] parts = input.split("[,\\s]+");

        for (String part : parts) {
            int number = Integer.parseInt(part);

            if (number < 1 || number > 4) {
                throw new NumberFormatException();
            }

            result.add(number - 1); // ?? Точно???
        }

        return result;
    }

    /**
     * метод вывода доски лидеров пользователю.
     * @return
     * если в базе больше нет пользователей - ответ-сообщение что пользователь может быть первым, без вывода доски лидеров;
     * иначе ответ-сообщение с содержимым доски лидеров.
     */
    private DialogResponse showLeaderboard() {
        GetLeaderboardResponse response =
                backendRestClient.getLeaderboard(telegramId);

        if (response.leaderboard().isEmpty()) {
            return new DialogResponse(
                    "Apparently you can be first in our repository!",
                    false
            );
        }

        StringBuilder message = new StringBuilder();
        message.append("---------------------------\n");
        message.append("This is our winners and you\n");

        for (LeaderboardResponseEntry entry : response.leaderboard()) {
            message.append(String.format(
                    "%-3s %-15s — %d points%n",
                    entry.place() + ".",
                    entry.nickname(),
                    entry.score()
            ));
        }

        message.append("---------------------------");

        return new DialogResponse(message.toString(), false);
    }

    /**
     * метод объясняет пользователю функции квиз-бота.
     * @return ответ с краткой сводкой и доступными командами.
     */
    private DialogResponse showHelp() {
        return new DialogResponse(
                "Hello and welcome to our quiz bot! You can choose how many questions " +
                        "you'd like to answer and get points " +
                        "for each correct answer. " +
                        "The points summarise in the leaderboard, " +
                        "where you can see other's players and your own rating!\n\n"

                + "Available commands:\n"
                        + "p — play quiz\n"
                        + "l — show leaderboard\n"
                        + "h — help\n"
                        + "q — quit",
                false
        );
    }
}
