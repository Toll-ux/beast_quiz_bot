import application.*;
import infrastructure.HardcodedQuestionRepository;
import infrastructure.HardcodedQuizRepository;
import infrastructure.HardcodedUserRepository;
import domain.User;

import java.time.Clock;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;


public class TestRun
{
    private final Scanner scanner;
    private final QuizApplicationService QAS;

    public TestRun(Scanner scanner, QuizApplicationService QAS) {
        this.scanner = scanner;
        this.QAS = QAS;
    }

    /** playQuiz - функция для игры в квиз
     *
     * @param telegramId - тг id юзера
     */
    public void playQuiz(long telegramId){
        IO.println("Please enter the desired quiz length (currently support 1-4 questions) or 'q' to abort: ");

        // ввод количества вопросов
        String lengthInput = scanner.nextLine().trim();
        if (lengthInput.equalsIgnoreCase("q")) {
            IO.println("Quiz aborted by the user.");
            return;
        }

        int questionCount;
        // Исключение, если пользователь ввел что-то кроме чисел
        try {
            questionCount = Integer.parseInt(lengthInput);
        } catch (NumberFormatException e) {
            IO.println("Invalid input. Let's assume you want 4 questions...");
            questionCount = 4;
        }

        // Проверка на адекватность questionCount
        if ((questionCount > 4) || (questionCount < 1)){
            IO.println("Come on, bro... let's assume you want 4 questions...");
            questionCount = 4;
        }

        // создаем новый квиз или используем уже имеющийся
        QAS.start(telegramId, questionCount);

        // Цикл ответов на вопросы в квизе
        while (true){
            // текущий вопрос
            QuestionView currentQuestionView = QAS.presentQuestion(telegramId);

            IO.println(currentQuestionView.text());

            // печать ответов
            List<String> answers = currentQuestionView.answers();
            for (int i = 0; i < answers.size(); i++) {
                System.out.println((i + 1) + ": " + answers.get(i));
            }


            IO.println("Please enter numbers representing your answer (possibly multiple choice) in one single line");
            IO.println("Press 'q' to abort the quiz.");
            // ввод ответа
            String line = scanner.nextLine();

            // если там q - выход
            if (line.trim().equalsIgnoreCase("q")) {
                QAS.abort(telegramId);
                IO.println("Quiz aborted by the user.");
                break;
            }

            Set<Integer> numbers;
            // адекватность ввода ответа
            try {
                numbers = Arrays.stream(line.trim().split("\\s+"))
                        .map(Integer::parseInt).map(n -> n - 1)
                        .collect(Collectors.toSet());
            } catch (NumberFormatException e){
                IO.println("I think. I’ll overlook the typo");
                continue;
            }

            int questionId = currentQuestionView.questionId();

            // проверка правильности ответа
            AnswerQuestionResult result = QAS.answer(telegramId, questionId, numbers);

            // вывод итогов
            if(result.correct()){
                IO.println("Good job, you answered correctly!");
            } else {
                IO.println("You are objectively wrong, the correct answers were: ");
                result.correctAnswers().stream().map(n -> n + 1).forEach(System.out::println);
            }

            // если квиз закончен - выход из цикла
            if(result.finished()){
                break;
            }
        }
        // Квиз закончен запись счета в репозитории и др.
        FinishQuizResult finishedQuiz = QAS.finish(telegramId);
        IO.println("Nicely done! Your total score was: " + finishedQuiz.quizScore());

    }

    /** viewLeaderboard - функция, чтобы посмотреть квиз
     *
     * @param telegramId - тг id юзера
     */
    public void viewLeaderboard(long telegramId){
        List<LeaderboardEntry> leaderBoard;

        // проверка на то, что в репозитории никого нет
        try {
            leaderBoard = QAS.getLeaderBoard(telegramId);
        } catch (IllegalStateException e) {
            IO.println("Apparently " + e +".\n" + "That means you can be first in our repository!");
            return;
        }

        // разметка
        StringBuilder sb = new StringBuilder();
        sb.append("---------------------------\n");
        sb.append("This is our winners and you\n");

        for (LeaderboardEntry entry : leaderBoard) {
            sb.append(String.format("%-3s %-15s — %d points\n",
                    entry.place() + ".",
                    entry.nickname(),
                    entry.score()
            ));
        }
        sb.append("---------------------------\n");

        IO.println(sb.toString());
    }

    /**viewHelp - функция чтобы посмотреть возможности бота*/
    public void viewHelp(){
        IO.println("""
                Бот может все;
                """);
    }

    /** main - инициализация экземпляра, запуск*/
    public static void main(String[] args) {
        // создание экземпляра
        Scanner scanner = new Scanner(System.in);
        QuizApplicationService QAS = new QuizApplicationService(
                new HardcodedQuestionRepository(),
                new HardcodedQuizRepository(),
                new HardcodedUserRepository(),
                Clock.systemDefaultZone()
        );
        TestRun runner = new TestRun(scanner, QAS);
        runner.run();
    }

    /** run -  */
    public void run() {
        IO.println("Hello and welcome to our Quiz-bot!");

        // ввод tg id, сейчас он захардкожен
        //IO.println("Enter your Telegram ID:");
        //long telegramId = Long.parseLong(scanner.nextLine().trim());
        long telegramId = 1;

        // ввод nickname он нужен для красивого вывода leaderboard сейчас захардкожен
        //IO.println("Enter your nickname:");
        //String nickname = scanner.nextLine().trim();
        String nickname = "Alex";

        // создание юзера
        User user = new User(telegramId);
        user.setNickname(nickname);

        // цикл с игрой
        while(true) {
            IO.println("""
                Choose an action:
                p — play quiz
                l — show leaderboard
                q - quit
                h - help""");

            //ввод команд, ошибки нет, так как есть default
            String command = scanner.nextLine().trim().toLowerCase();

            switch (command) {
                case "p" -> playQuiz(telegramId);
                case "l" -> viewLeaderboard(telegramId);
                case "h" -> viewHelp();
                case "q" -> {
                    IO.println("So long, losers, whom I've always hated! © Bender Bending Rodríguez");
                    return;
                }
                default -> IO.println("Unknown command. Please enter p, s, h, or q.");
            }
        }
    }
}
