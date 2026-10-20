import application.*;
import infrastructure.HardcodedQuestionRepository;
import infrastructure.HardcodedQuizRepository;
import infrastructure.HardcodedUserRepository;
import domain.User;

import java.io.PrintStream;
import java.time.Clock;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;


public class TestRun
{
    private final Scanner scanner;
    private final PrintStream out;
    private final QuizApplicationService QAS;
    HardcodedUserRepository userRepo;

    public TestRun(Scanner scanner, PrintStream out, QuizApplicationService QAS, HardcodedUserRepository userRepo) {
        this.scanner = scanner;
        this.out = out;
        this.QAS = QAS;
        this.userRepo = userRepo;
    }

    /** playQuiz - функция для игры в квиз
     *
     * @param telegramId - тг id юзера
     */
    public void playQuiz(long telegramId){
        out.println("Please enter the desired quiz length (currently support 1-4 questions) or 'q' to abort: ");

        // ввод количества вопросов
        String lengthInput = scanner.nextLine().trim();
        if (lengthInput.equalsIgnoreCase("q")) {
            out.println("Quiz aborted by the user.");
            return;
        }

        int questionCount;
        // Исключение, если пользователь ввел что-то кроме чисел
        try {
            questionCount = Integer.parseInt(lengthInput);
        } catch (NumberFormatException e) {
            out.println("Invalid input. Let's assume you want 4 questions...");
            questionCount = 4;
        }

        // Проверка на адекватность questionCount
        if ((questionCount > 4) || (questionCount < 1)){
            out.println("Come on, bro... let's assume you want 4 questions...");
            questionCount = 4;
        }

        // создаем новый квиз или используем уже имеющийся
        QAS.start(telegramId, questionCount);

        // Цикл ответов на вопросы в квизе
        while (true){
            // текущий вопрос
            QuestionView currentQuestionView = QAS.presentQuestion(telegramId);

            out.println(currentQuestionView.text());

            // печать ответов
            List<String> answers = currentQuestionView.answers();
            for (int i = 0; i < answers.size(); i++) {
                out.println((i + 1) + ": " + answers.get(i));
            }


            out.println("Please enter numbers representing your answer (possibly multiple choice) in one single line");
            out.println("Press 'q' to abort the quiz.");
            // ввод ответа
            String line = scanner.nextLine();

            // если там q - выход
            if (line.trim().equalsIgnoreCase("q")) {
                QAS.abort(telegramId);
                out.println("Quiz aborted by the user.");
                return;
            }

            Set<Integer> numbers;
            // адекватность ввода ответа
            try {
                numbers = Arrays.stream(line.trim().split("\\s+"))
                        .map(Integer::parseInt).map(n -> n - 1)
                        .collect(Collectors.toSet());
            } catch (NumberFormatException e){
                out.println("I think. I’ll overlook the typo");
                continue;
            }

            int questionId = currentQuestionView.questionId();

            // проверка правильности ответа
            AnswerQuestionResult result = QAS.answer(telegramId, questionId, numbers);

            // вывод итогов
            if(result.correct()){
                out.println("Good job, you answered correctly!");
            } else {
                out.println("You are objectively wrong, the correct answers were: ");
                result.correctAnswers().stream().map(n -> n + 1).forEach(out::println);
            }

            // если квиз закончен - выход из цикла
            if(result.finished()){
                break;
            }
        }
        // Квиз закончен запись счета в репозитории и др.
        FinishQuizResult finishedQuiz = QAS.finish(telegramId);
        out.println("Nicely done! Your total score was: " + finishedQuiz.quizScore());
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
            out.println("Apparently " + e +".\n" + "That means you can be first in our repository!");
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

        out.println(sb.toString());
    }

    /**viewHelp - функция чтобы посмотреть возможности бота*/
    public void viewHelp(){
        out.println("""
                Бот может все;
                """);
        // TODO ::  viewHelp - функция чтобы посмотреть возможности бота
    }

    /** main - инициализация экземпляра, запуск*/
    public static void main(String[] args) {
        // создание экземпляра
        HardcodedUserRepository userRepo = new HardcodedUserRepository();
        Scanner scanner = new Scanner(System.in);
        QuizApplicationService QAS = new QuizApplicationService(
                new HardcodedQuestionRepository(),
                new HardcodedQuizRepository(),
                userRepo,
                Clock.systemDefaultZone()
        );

        TestRun runner = new TestRun(scanner, System.out, QAS, userRepo);
        runner.run();
    }

    /** run -  */
    public void run() {
        out.println("Hello and welcome to our Quiz-bot!");

        // ввод tg id, сейчас он захардкожен
        //out.println("Enter your Telegram ID:");
        //long telegramId = Long.parseLong(scanner.nextLine().trim());
        long telegramId = 1;

        // ввод nickname он нужен для красивого вывода leaderboard сейчас захардкожен
        //out.println("Enter your nickname:");
        //String nickname = scanner.nextLine().trim();
        String nickname = "Alex";

        // создание юзера
        User user = new User(telegramId);
        user.setNickname(nickname);
        userRepo.saveUserInfo(user);
        // цикл с игрой
        while(true) {
            out.println("""
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
                    out.println("So long, losers, whom I've always hated! © Bender Bending Rodríguez");
                    return;
                }
                default -> out.println("Unknown command. Please enter p, s, h, or q.");
            }
        }
    }
}
