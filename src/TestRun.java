import application.AnswerQuestionResult;
import application.FinishQuizResult;
import application.QuestionView;
import application.QuizApplicationService;
import application.StartQuizResult;
import application.port.LeaderboardRepository;
import application.port.QuestionRepository;
import application.port.QuizRepository;
import application.port.UserRepository;
import infrastructure.HardcodedLeaderboardRepository;
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
    public static void main(String[] args)
    {
        LeaderboardRepository leaderboardRepository = new HardcodedLeaderboardRepository();
        QuestionRepository questionRepository = new HardcodedQuestionRepository();
        QuizRepository quizRepository = new HardcodedQuizRepository();
        UserRepository userRepository = new HardcodedUserRepository();
        Clock clock = Clock.systemDefaultZone();

        userRepository.saveUserInfo(new User(1, 1));

        QuizApplicationService QAS = new QuizApplicationService(questionRepository, quizRepository, userRepository, leaderboardRepository,  clock);

        Scanner scanner = new Scanner(System.in);
        IO.println("Hello and welcome to our Quiz-bot!");
        IO.println("Please enter the desired quiz length (currently support 1-4 questions): ");

        int questionCount = scanner.nextInt();
        scanner.nextLine();
        if ((questionCount > 4) || (questionCount < 1)){
            IO.println("Come on, bro... let's assume you want 4 questions...");
            questionCount = 4;
        }

        StartQuizResult startResult = QAS.start(1, questionCount);
        QuestionView currentQuestionView = startResult.firstQuestionView();

        while (true){
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

            AnswerQuestionResult result = QAS.answer(1, questionId, numbers);

            if(result.correct()){
                IO.println("Good job, you answered correctly!");
            } else {
                IO.println("You are objectively wrong, the correct answers were: ");
                result.correctAnswers().stream().map(n -> n + 1).forEach(System.out::println);
            }

            if(result.finished()){
                break;
            }

            currentQuestionView = result.nextQuestionView();
        }
        FinishQuizResult finishedQuiz = QAS.finish(1);
        IO.println("Nicely done! Your total score was: " + finishedQuiz.quizScore());
    }
}
