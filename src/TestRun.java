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
    public static final long telegramId = 1;

    public static final QuestionRepository questionRepository = new HardcodedQuestionRepository();
    public static final QuizRepository quizRepository = new HardcodedQuizRepository();
    public static final UserRepository userRepository = new HardcodedUserRepository();
    public static final Scanner scanner = new Scanner(System.in);
    public static final QuizApplicationService QAS = new QuizApplicationService(questionRepository, quizRepository, userRepository,  Clock.systemDefaultZone());

    public static void playQuiz(){
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

    public static void main(String[] args)
    {

        userRepository.saveUserInfo(new User(telegramId));

        // TODO ::в while предложить посмотреть таблицу лидеров, потом играть. или играть, потом таблицу лидеров
        IO.println("Hello and welcome to our Quiz-bot!");

        playQuiz();

        //LeaderBoard leaderBoard = new LeaderBoard(session, HardcodedUserRepository);
    }
}
