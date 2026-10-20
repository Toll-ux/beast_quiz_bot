import application.QuizApplicationService;
import infrastructure.HardcodedQuestionRepository;
import infrastructure.HardcodedQuizRepository;
import infrastructure.HardcodedUserRepository;
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

    @BeforeEach
    void initializeQAS() {
        QAS = new QuizApplicationService(
                new HardcodedQuestionRepository(),
                new HardcodedQuizRepository(),
                new HardcodedUserRepository(),
                Clock.systemDefaultZone()
        );

        out = new ByteArrayOutputStream();
        testOut = new PrintStream(out);
    }

    // 1. Input Validation & Resilience
    @DisplayName("playQuiz: не числовой ввод количества вопросов")
    @Test
    void playQuizNonNumStringInCountQuestions() {
        String input = "sdffdg\nq";

        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        Scanner scanner = new Scanner(in);

        // testOut и out берутся из вашего @BeforeEach
        TestRun runner = new TestRun(scanner, testOut, QAS);
        runner.playQuiz(telegramId);

        // Забираем всё, что попало в поток вывода
        String consoleOutput = out.toString();

        // Проверяем, что программа отреагировала на ошибку и выставила 4 вопроса
        assertTrue(consoleOutput.contains("Invalid input. Let's assume you want 4 questions..."),
                "Ожидался текст-предупреждение о неверном вводе, но вывелось:\n" + consoleOutput);

        // Также можно проверить, что сработал корректный выход из квиза по 'q'
        assertTrue(consoleOutput.contains("Quiz aborted by the user."));
    }
}
/*
    @DisplayName("playQuiz: числовой ввод вопросов, но числа < 0")
    @Test
    void playQuizNumLowerThanZero(){
        String input = "-1";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        Scanner scanner = new Scanner(in);

        TestRun runner = new TestRun(scanner,testOut, QAS);
        assertDoesNotThrow(() -> runner.playQuiz(telegramId));
    }

    @DisplayName("playQuiz: числовой ввод вопросов, но числа 0")
    @Test
    void playQuizNumLowerZero(){
        String input = "0";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        Scanner scanner = new Scanner(in);

        TestRun runner = new TestRun(scanner,testOut, QAS);
        assertDoesNotThrow(() -> runner.playQuiz(telegramId));
    }

    @DisplayName("playQuiz: числовой ввод вопросов, но числа > 4")
    @Test
    void playQuizNumHigherThanFour(){
        String input = "100";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        Scanner scanner = new Scanner(in);

        TestRun runner = new TestRun(scanner,testOut, QAS);
        assertDoesNotThrow(() -> runner.playQuiz(telegramId));
    }

    @DisplayName("playQuiz: числовой ввод вопросов, проверка на выход")
    @Test
    void playQuizAbort() {
        String input = "q\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        Scanner scanner = new Scanner(in);

        TestRun runner = new TestRun(scanner,testOut, QAS);
        assertDoesNotThrow(() -> runner.playQuiz(telegramId));
    }

    @DisplayName("playQuiz: не числовой ввод ответа, проверка на второй шанс")
    @Test
    void playQuizNonNumStringInAnswers() {
        String input = "1\n apsfjs\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        Scanner scanner = new Scanner(in);

        TestRun runner = new TestRun(scanner,testOut, QAS);
        assertDoesNotThrow(() -> runner.playQuiz(telegramId));
    }

    // TODO::Исправить это. Сделать как ошибку и пропустить вопрос
    @DisplayName("playQuiz: числовой ввод ответа, проверка zero")
    @Test
    void playQuizNumZero() {
        String input = "1\n 0\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        Scanner scanner = new Scanner(in);

        TestRun runner = new TestRun(scanner,testOut,QAS);
        assertDoesNotThrow(() -> runner.playQuiz(telegramId));
    }

    // TODO::Исправить это. Сделать как ошибку и пропустить вопрос
    @DisplayName("playQuiz: числовой ввод ответа, проверка отрицательные числа")
    @Test
    void playQuizNumBelowZero() {
        String input = "1\n -10\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        Scanner scanner = new Scanner(in);

        TestRun runner = new TestRun(scanner,testOut, QAS);
        assertDoesNotThrow(() -> runner.playQuiz(telegramId));
    }

    // TODO::Исправить это. Сделать как ошибку и пропустить вопрос
    @DisplayName("playQuiz: числовой ввод ответа, проверка больше верхней границы")
    @Test
    void playQuizNumUpperThanRange() {
        String input = "1\n 1000\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        Scanner scanner = new Scanner(in);

        TestRun runner = new TestRun(scanner,testOut, QAS);
        assertDoesNotThrow(() -> runner.playQuiz(telegramId));
    }

    //TODO :: playQuizNumAnswers
    @DisplayName("playQuiz: числовой ввод ответа, проверка неправильность ответа")
    @Test
    void playQuizNumAnswers() {
        String input = "1\n 1\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        Scanner scanner = new Scanner(in);

        TestRun runner = new TestRun(scanner,testOut, QAS);
        assertDoesNotThrow(() -> runner.playQuiz(telegramId));
    }

    @DisplayName("playQuiz: ввод ответов, проверка на выход")
    @Test
    void playQuizAnswersAbort() {
        String input = "1\n q\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        Scanner scanner = new Scanner(in);

        TestRun runner = new TestRun(scanner,testOut, QAS);
        assertDoesNotThrow(() -> runner.playQuiz(telegramId));
    }

    @DisplayName("playQuiz: передан tg id < 0")
    @Test
    void playQuizNegativeTgId() {
        String input = "1\n 1\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        Scanner scanner = new Scanner(in);

        TestRun runner = new TestRun(scanner,testOut, QAS);
        assertDoesNotThrow(() -> runner.playQuiz(-100));
    }

    @DisplayName("playQuiz: передан tg id = 0")
    @Test
    void playQuizZeroTgId() {
        String input = "1\n 1\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        Scanner scanner = new Scanner(in);

        TestRun runner = new TestRun(scanner,testOut, QAS);
        assertDoesNotThrow(() -> runner.playQuiz(0));
    }

    @DisplayName("viewHelp: что-то выводит")
    @Test
    void viewHelp(){
        String input = "\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        Scanner scanner = new Scanner(in);

        TestRun runner = new TestRun(scanner,testOut, QAS);
        assertDoesNotThrow(() -> runner.viewHelp());
    }

}
*/