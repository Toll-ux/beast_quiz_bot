import application.*;
import application.port.QuestionRepository;
import application.port.QuizRepository;
import application.port.UserRepository;
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
    public static final QuestionRepository questionRepository = new HardcodedQuestionRepository();
    public static final QuizRepository quizRepository = new HardcodedQuizRepository();
    public static final UserRepository userRepository = new HardcodedUserRepository();
    public static final Scanner scanner = new Scanner(System.in);
    public static final QuizApplicationService QAS = new QuizApplicationService(questionRepository, quizRepository, userRepository,  Clock.systemDefaultZone());

    public static void playQuiz(long telegramId){
        IO.println("Please enter the desired quiz length (currently support 1-4 questions): ");

        int questionCount = scanner.nextInt();
        scanner.nextLine();
        if ((questionCount > 4) || (questionCount < 1)){
            IO.println("Come on, bro... let's assume you want 4 questions...");
            questionCount = 4;
        }

        if (!QAS.start(telegramId, questionCount)){
            IO.println("Something goes terrible wrong!!!!!");
        }

        while (true){
            QuestionView currentQuestionView = QAS.presentQuestion(telegramId);

            IO.println(currentQuestionView.text());

            List<String> answers = currentQuestionView.answers();
            for (int i = 0; i < answers.size(); i++) {
                System.out.println((i + 1) + ": " + answers.get(i));
            }


            IO.println("Please enter numbers representing your answer (possibly multiple choice) in one single line");
            String line = scanner.nextLine();

            Set<Integer> numbers = Arrays.stream(line.trim().split("\\s+"))
                    .map(Integer::parseInt).map(n -> n - 1)
                    .collect(Collectors.toSet());


            int questionId = currentQuestionView.questionId();

            AnswerQuestionResult result = QAS.answer(telegramId, questionId, numbers);

            if(result.correct()){
                IO.println("Good job, you answered correctly!");
            } else {
                IO.println("You are objectively wrong, the correct answers were: ");
                result.correctAnswers().stream().map(n -> n + 1).forEach(System.out::println);
            }


            if(result.finished()){
                break;
            }
        }
        FinishQuizResult finishedQuiz = QAS.finish(telegramId);
        IO.println("Nicely done! Your total score was: " + finishedQuiz.quizScore());

    }

    public static void viewLeaderboard(long telegramId){
        List<LeaderboardEntry> leaderBoard;
        try{
            leaderBoard = QAS.getLeaderBoard(telegramId);
        } catch (IllegalStateException e) {

            IO.println("Apparently " + e +".\n" + "That means you can be first in our database!");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("---------------------------\n");
        sb.append("     This is our scoreboard\n");

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

    public static void viewHelp(){
        IO.println("Help me");
        //TODO::дописать, что может бот
    }

    public static void main(String[] args) {
        IO.println("Hello and welcome to our Quiz-bot!");

        //IO.println("Enter your Telegram ID:");
        //long telegramId = Long.parseLong(scanner.nextLine().trim());
        final long telegramId = 1;

        //IO.println("Enter your nickname:");
        //String nickname = scanner.nextLine().trim();
        final String nickname = "Alex";

        User user = new User(telegramId);
        user.setNickname(nickname);
        userRepository.saveUserInfo(user);

        while (true) {
            IO.println("""
                Choose an action:
                p — play quiz
                l — show leaderboard
                q - quit
                h - help
                """);

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
