package main;

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
        userRepository.saveUserInfo(user);

        out = new ByteArrayOutputStream();
        testOut = new PrintStream(out);
    }

    /** создания main TestRun с заданным вводом */
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
       assertThrows(IllegalArgumentException.class, () -> runner.playQuiz(0));
    }

    @DisplayName("viewHelp: что-то выводит")
    @Test
    void viewHelp() {
        String input = "\n";
        TestRun runner = makeRunner(input);
        assertDoesNotThrow(runner::viewHelp);
    }


    @DisplayName("run: выводит приветствие и меню")
    @Test
    void runPrintsGreetingAndMenu() {
        makeRunner("q\n").run();
        String consoleOutput = out.toString();

        assertTrue(consoleOutput.contains("Hello and welcome to our Quiz-bot!"),
                "Нет приветствия. Вывод:\n" + consoleOutput);
        assertTrue(consoleOutput.contains("Choose an action:"),
                "Нет меню. Вывод:\n" + consoleOutput);
        assertTrue(consoleOutput.contains("p — play quiz"));
        assertTrue(consoleOutput.contains("l — show leaderboard"));
        assertTrue(consoleOutput.contains("q - quit"));
        assertTrue(consoleOutput.contains("h - help"));
    }


    @DisplayName("run: 'q' завершает цикл и выводит прощание")
    @Test
    void runQuitPrintsFarewellAndReturns() {
        assertDoesNotThrow(() -> makeRunner("q\n").run());

        String consoleOutput = out.toString();
        assertTrue(consoleOutput.contains("So long, losers, whom I've always hated!"),
                "Нет прощания. Вывод:\n" + consoleOutput);
    }

    @DisplayName("run: 'Q' в верхнем регистре тоже работает")
    @Test
    void runQuitUpperCase() {
        assertDoesNotThrow(() -> makeRunner("Q\n").run());
        assertTrue(out.toString().contains("So long, losers"));
    }

    @DisplayName("run: 'q' с пробелами/табами вокруг распознаётся")
    @Test
    void runQuitWithWhitespace() {
        assertDoesNotThrow(() -> makeRunner("  q  \n").run());
        assertTrue(out.toString().contains("So long, losers"));
    }


    @DisplayName("run: неизвестная команда выводит предупреждение и продолжает цикл")
    @Test
    void runUnknownCommand() {
        // "x" -> default, затем "q" -> выход
        assertDoesNotThrow(() -> makeRunner("x\nq\n").run());
        String consoleOutput = out.toString();

        assertTrue(consoleOutput.contains("Unknown command. Please enter p, s, h, or q."),
                "Нет предупреждения о неизвестной команде. Вывод:\n" + consoleOutput);
        assertTrue(consoleOutput.contains("So long, losers"));
    }

    @DisplayName("run: пустая строка трактуется как неизвестная команда")
    @Test
    void runEmptyLineAsUnknownCommand() {
        assertDoesNotThrow(() -> makeRunner("\nq\n").run());
        assertTrue(out.toString().contains("Unknown command. Please enter p, s, h, or q."));
    }

    @DisplayName("run: несколько неизвестных команд подряд")
    @Test
    void runMultipleUnknownCommands() {
        assertDoesNotThrow(() -> makeRunner("abc\nxyz\n123\nq\n").run());
        String consoleOutput = out.toString();

        // три предупреждения
        int count = consoleOutput.split("Unknown command", -1).length - 1;
        assertEquals(3, count,
                "Ожидалось 3 предупреждения, найдено " + count + ". Вывод:\n" + consoleOutput);
    }


    @DisplayName("run: 'h' выводит справку и не падает")
    @Test
    void runHelp() {
        assertDoesNotThrow(() -> makeRunner("h\nq\n").run());
        String consoleOutput = out.toString();

        assertTrue(consoleOutput.contains("help me"),
                "Нет текста справки. Вывод:\n" + consoleOutput);
        assertTrue(consoleOutput.contains("Choose an action:"));
    }


    @DisplayName("run: 'p' запускает playQuiz и выходит через 'q'")
    @Test
    void runPlayQuizAndAbort() {
        // q на длину квиза = выходим сразу
        assertDoesNotThrow(() -> makeRunner("p\nq\nq\n").run());

        String consoleOutput = out.toString();
        assertTrue(consoleOutput.contains("Please enter the desired quiz length"),
                "playQuiz не запустился. Вывод:\n" + consoleOutput);
        assertTrue(consoleOutput.contains("Quiz aborted by the user."),
                "Квиз не прервался. Вывод:\n" + consoleOutput);
        // после прерывания квиза снова показано меню и затем выход по 'q'
        assertTrue(consoleOutput.contains("So long, losers"));
    }

    @DisplayName("run: 'p' + нечисловая длина квиза → 4 вопроса → выход из квиза по 'q'")
    @Test
    void runPlayQuizNonNumericLength() {
        // длина: "abc" -> 4 вопроса; затем q -> прерывание квиза; затем q -> выход
        assertDoesNotThrow(() -> makeRunner("p\nabc\nq\nq\n").run());

        String consoleOutput = out.toString();
        assertTrue(consoleOutput.contains("Invalid input. Let's assume you want 4 questions..."));
        assertTrue(consoleOutput.contains("Quiz aborted by the user."));
    }

    @DisplayName("run: 'p' затем 'q' для выхода из квиза — программа возвращается в главное меню")
    @Test
    void runPlayQuizThenBackToMenu() {
        assertDoesNotThrow(() -> makeRunner("p\nq\nq\n").run());

        String consoleOutput = out.toString();
        // меню должно встретиться минимум дважды: до p и после выхода из квиза
        int menuCount = consoleOutput.split("Choose an action:", -1).length - 1;
        assertTrue(menuCount >= 2,
                "После выхода из квиза меню не показалось повторно. Вывод:\n" + consoleOutput);
    }


    @DisplayName("run: 'l' выводит лидерборд, если в нём есть юзер")
    @Test
    void runLeaderboard() {
        // Юзер уже зарегистрирован в @BeforeEach -> leaderboard не пуст
        assertDoesNotThrow(() -> makeRunner("l\nq\n").run());
        String consoleOutput = out.toString();

        assertTrue(consoleOutput.contains("This is our winners and you"),
                "Лидерборд не вывелся. Вывод:\n" + consoleOutput);
    }

    @DisplayName("run: 'l' при пустом лидерборде — исключение ловится в run()")
    @Test
    void runLeaderboardEmptyThrows() {
        HardcodedUserRepository emptyRepo = new HardcodedUserRepository();
        QuizApplicationService emptyQAS = new QuizApplicationService(
                new HardcodedQuestionRepository(),
                new HardcodedQuizRepository(),
                emptyRepo,
                Clock.systemDefaultZone()
        );

        ByteArrayInputStream in = new ByteArrayInputStream("l\nq\n".getBytes());
        Scanner scanner = new Scanner(in);
        TestRun runner = new TestRun(scanner, testOut, emptyQAS);

        assertDoesNotThrow(runner::run);
    }


    @DisplayName("run:  h -> l -> x -> q — все команды отрабатывают по порядку")
    @Test
    void runMixedCommands() {
        assertDoesNotThrow(() -> makeRunner("h\nl\nx\nq\n").run());
        String consoleOutput = out.toString();

        assertTrue(consoleOutput.contains("help me"));
        assertTrue(consoleOutput.contains("This is our winners and you"));
        assertTrue(consoleOutput.contains("Unknown command"));
        assertTrue(consoleOutput.contains("So long, losers"));
    }

    @DisplayName("run: после 'p' и выхода из квиза можно снова запустить 'p'")
    @Test
    void runPlayQuizTwice() {
        assertDoesNotThrow(() -> makeRunner("p\nq\np\nq\nq\n").run());

        String consoleOutput = out.toString();
        int starts = consoleOutput.split("Please enter the desired quiz length", -1).length - 1;
        assertEquals(2, starts,
                "Ожидалось 2 запуска playQuiz, найдено " + starts + ". Вывод:\n" + consoleOutput);
    }

    @DisplayName("run: ввод заканчивается без 'q' → NoSuchElementException")
    @Test
    void runNoQuitThrowsNoSuchElement() {
        assertThrows(java.util.NoSuchElementException.class,
                () -> makeRunner("h\n").run());
    }

    @Test
    @DisplayName("main: инициализирует зависимости и запускает run (виден вывод приветствия)")
    void mainInitializesAndRuns() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();

        try {
            System.setIn(new ByteArrayInputStream("q\n".getBytes()));
            System.setOut(new PrintStream(captured, true));

            TestRun.main(new String[]{});

            System.out.flush();
            String output = captured.toString();

            assertTrue(output.contains("Hello and welcome to our Quiz-bot!"),
                    "run() не был вызван. Вывод:\n" + output);
            assertTrue(output.contains("Choose an action:"),
                    "Меню не напечатано. Вывод:\n" + output);
            assertTrue(output.contains("So long, losers"));
        } finally {
            System.setOut(originalOut);
        }
    }
}