import application.QuizApplicationService;
import infrastructure.HardcodedQuestionRepository;
import infrastructure.HardcodedQuizRepository;
import infrastructure.HardcodedUserRepository;
import domain.User;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Clock;
import java.util.Scanner;

class TestRunTest {
    private final long telegramId = 1;
    QuizApplicationService QAS;
    ByteArrayOutputStream out;
    PrintStream testOut;
    HardcodedUserRepository userRepository;

    @BeforeEach
    void initializeQAS() {
        userRepository = new HardcodedUserRepository();
        QAS = new QuizApplicationService(
                new HardcodedQuestionRepository(),
                new HardcodedQuizRepository(),
                userRepository,
                Clock.systemDefaultZone()
        );

        // регистрируем юзера, иначе finish/abort бросит "User not found"
        User user = new User(telegramId);
        user.setNickname("Alex");

        out = new ByteArrayOutputStream();
        testOut = new PrintStream(out);
    }

    /** создания TestRun с заданным вводом */
    private TestRun makeRunner(String input) {
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        Scanner scanner = new Scanner(in);
        return new TestRun(scanner, testOut, QAS);
    }


    @DisplayName("playQuiz: не числовой ввод количества вопросов")
    @Test
    void playQuizNonNumStringInCountQuestions() {
        String input = "sdffdg\nq\n";   // <-- добавляем \n после q
        TestRun runner = makeRunner(input);
        runner.playQuiz(telegramId);

        String consoleOutput = out.toString();
        assertTrue(consoleOutput.contains("Invalid input. Let's assume you want 4 questions..."),
                "Ожидался текст-предупреждение о неверном вводе, но вывелось:\n" + consoleOutput);
        assertTrue(consoleOutput.contains("Quiz aborted by the user."));
    }

    @DisplayName("playQuiz: числовой ввод вопросов, но числа < 0")
    @Test
    void playQuizNumLowerThanZero() {
        String input = "-1\nq\n";   // <-- добавляем \n
        TestRun runner = makeRunner(input);
        assertDoesNotThrow(() -> runner.playQuiz(telegramId));
    }

    @DisplayName("playQuiz: числовой ввод вопросов, но числа 0")
    @Test
    void playQuizNumLowerZero() {
        String input = "0\nq\n";
        TestRun runner = makeRunner(input);
        assertDoesNotThrow(() -> runner.playQuiz(telegramId));
    }

    @DisplayName("playQuiz: числовой ввод вопросов, но числа > 4")
    @Test
    void playQuizNumHigherThanFour() {
        String input = "100\nq\n";
        TestRun runner = makeRunner(input);
        assertDoesNotThrow(() -> runner.playQuiz(telegramId));
    }

    @DisplayName("playQuiz: числовой ввод вопросов, проверка на выход")
    @Test
    void playQuizAbort() {
        String input = "q\n";
        TestRun runner = makeRunner(input);
        assertDoesNotThrow(() -> runner.playQuiz(telegramId));
    }

    @DisplayName("playQuiz: не числовой ввод ответа, проверка на второй шанс")
    @Test
    void playQuizNonNumStringInAnswers() {
        String input = "1\n apsfjs\n q\n";   // <-- добавляем q, чтобы выйти
        TestRun runner = makeRunner(input);
        assertDoesNotThrow(() -> runner.playQuiz(telegramId));
    }


    @DisplayName("playQuiz: числовой ввод ответа, проверка неправильность ответа")
    @Test
    void playQuizNumAnswers() {
        String input = "1\n 1\n q\n";
        TestRun runner = makeRunner(input);
        assertDoesNotThrow(() -> runner.playQuiz(telegramId));
    }

    @DisplayName("playQuiz: ввод ответов, проверка на выход")
    @Test
    void playQuizAnswersAbort() {
        String input = "1\n q\n";
        TestRun runner = makeRunner(input);
        assertDoesNotThrow(() -> runner.playQuiz(telegramId));
    }

    @DisplayName("playQuiz: передан tg id < 0")
    @Test
    void playQuizNegativeTgId() {
        String input = "1\n 1\n q\n";
        TestRun runner = makeRunner(input);
        assertThrows(IllegalArgumentException.class,
                () -> runner.playQuiz(-100));
    }

    @DisplayName("playQuiz: передан tg id = 0")
    @Test
    void playQuizZeroTgId() {
        String input = "1\n 1\n q\n";
        TestRun runner = makeRunner(input);
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> runner.playQuiz(0));
    }

    @DisplayName("viewHelp: что-то выводит")
    @Test
    void viewHelp() {
        String input = "\n";
        TestRun runner = makeRunner(input);
        assertDoesNotThrow(() -> runner.viewHelp());
    }


}