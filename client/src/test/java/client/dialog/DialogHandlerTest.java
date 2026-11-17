
package client.dialog;

import client.api.BackendRestClient;
import client.api.dto.response.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DialogHandlerTest {

    private static final long TELEGRAM_ID = 12345L;

    private BackendRestClient backendRestClient;
    private DialogHandler dialogHandler;

    @BeforeEach
    void setUp() {
        backendRestClient = mock(BackendRestClient.class);
        dialogHandler = new DialogHandler(backendRestClient, TELEGRAM_ID);
    }

    // -------------------- MAIN MENU --------------------

    @DisplayName("Ответ на null сообщение")
    @Test
    void handleMessage_nullMessage_returnsRequestToEnterMessage() {
        DialogResponse response = dialogHandler.handleMessage(null);

        assertEquals("Please enter a message.", response.message());
        assertFalse(response.exitRequested());

        verifyNoInteractions(backendRestClient);
    }

    @DisplayName("Ответ на ввод неизвестной команды")
    @Test
    void handleMessage_unknownCommand_returnsErrorMessage() {
        DialogResponse response = dialogHandler.handleMessage("x");

        assertEquals(
                "Unknown command. Please enter p, l, h, or q.",
                response.message()
        );
        assertFalse(response.exitRequested());

        verifyNoInteractions(backendRestClient);
    }

    @DisplayName("Правильная работа команды q")
    @Test
    void handleMessage_quitCommand_returnsGoodbyeAndRequestsExit() {
        DialogResponse response = dialogHandler.handleMessage("q");

        assertEquals("Goodbye!", response.message());
        assertTrue(response.exitRequested());

        verifyNoInteractions(backendRestClient);
    }

    @DisplayName("Правильная работа команды h")
    @Test
    void handleMessage_helpCommand_returnsHelpInformation() {
        DialogResponse response = dialogHandler.handleMessage("h");

        assertTrue(response.message().contains("Available commands:"));
        assertTrue(response.message().contains("play quiz"));
        assertFalse(response.exitRequested());

        verifyNoInteractions(backendRestClient);
    }

    @DisplayName("Правильная обработка команды с лишними пробелами и верхним регистром")
    @Test
    void handleMessage_commandIsCaseInsensitiveAndTrimmed() {
        DialogResponse response = dialogHandler.handleMessage("  H  ");

        assertTrue(response.message().contains("Available commands:"));
        assertFalse(response.exitRequested());

        verifyNoInteractions(backendRestClient);
    }

    @DisplayName("Смена состояния на WAITING_FOR_QUESTION_COUNT при вводе команды p")
    @Test
    void handleMessage_playCommand_switchesToQuestionCountState() {
        DialogResponse response = dialogHandler.handleMessage("p");

        assertEquals(
                "Please enter the desired quiz length:",
                response.message()
        );
        assertFalse(response.exitRequested());

        verifyNoInteractions(backendRestClient);
    }

    // -------------------- QUESTION COUNT --------------------

    @DisplayName("Ввод отличный от числа при WAITING_FOR_QUESTION_COUNT")
    @Test
    void handleQuestionCount_nonNumericInput_returnsError() {
        dialogHandler.handleMessage("p");

        DialogResponse response = dialogHandler.handleMessage("abc");

        assertEquals(
                "Please enter a valid number from 1 to 4.",
                response.message()
        );

        verifyNoInteractions(backendRestClient);
    }

    @DisplayName("Ввод 0 при WAITING_FOR_QUESTION_COUNT")
    @Test
    void handleQuestionCount_zero_returnsError() {
        dialogHandler.handleMessage("p");

        DialogResponse response = dialogHandler.handleMessage("0");

        assertEquals(
                "Please enter a number from 1 to 4.",
                response.message()
        );

        verifyNoInteractions(backendRestClient);
    }

    @DisplayName("Ввод числа вопросов большего чем в базе вопросов (сейчас -4)")
    @Test
    void handleQuestionCount_numberGreaterThanFour_returnsError() {
        dialogHandler.handleMessage("p");

        DialogResponse response = dialogHandler.handleMessage("5");

        assertEquals(
                "Please enter a number from 1 to 4.",
                response.message()
        );

        verifyNoInteractions(backendRestClient);
    }

    @DisplayName("Начинает квиз и показывает первый вопрос")
    @Test
    void handleQuestionCount_validInput_startsQuizAndShowsFirstQuestion() {
        GetCurrentQuestionViewResponse question = createQuestion();

        when(backendRestClient.getCurrentQuestion(TELEGRAM_ID))
                .thenReturn(question);

        dialogHandler.handleMessage("p");
        DialogResponse response = dialogHandler.handleMessage("2");

        assertTrue(response.message().contains("What is Java?"));
        assertTrue(response.message().contains("1: Programming language"));
        assertTrue(response.message().contains("2: Coffee"));
        assertTrue(response.message().contains("3: Operating system"));
        assertTrue(response.message().contains(
                "Enter the number(s) of your answer."
        ));

        verify(backendRestClient).startQuiz(TELEGRAM_ID, 2);
        verify(backendRestClient).getCurrentQuestion(TELEGRAM_ID);
    }

    // -------------------- ANSWERS --------------------

    @DisplayName("Ввод отличных от чисел ответов на вопрос")
    @Test
    void handleAnswer_nonNumericInput_returnsError() {
        startQuizAndShowQuestion();

        DialogResponse response = dialogHandler.handleMessage("abc");

        assertEquals(
                "Invalid answer. Please enter the answer numbers.",
                response.message()
        );

        verify(backendRestClient, never())
                .answerQuestion(anyLong(), anyInt(), anySet());
    }

    @DisplayName("Номер ответа выходит за допустимый диапазон вариантов ответа")
    @Test
    void handleAnswer_answerOutsideRange_returnsError() {
        startQuizAndShowQuestion();

        DialogResponse response = dialogHandler.handleMessage("4");

        assertEquals(
                "Invalid answer. Please enter the answer numbers.",
                response.message()
        );

        verify(backendRestClient, never())
                .answerQuestion(anyLong(), anyInt(), anySet());
    }

    @DisplayName("Пустой ввод ответов на вопрос")
    @Test
    void handleAnswer_emptyInput_returnsError() {
        startQuizAndShowQuestion();

        DialogResponse response = dialogHandler.handleMessage("");

        assertEquals(
                "Invalid answer. Please enter the answer numbers.",
                response.message()
        );

        verify(backendRestClient, never())
                .answerQuestion(anyLong(), anyInt(), anySet());
    }

    @DisplayName("Вывод при праввильном ответе на вопрос")
    @Test
    void handleAnswer_correctAnswer_showsSuccessAndNextQuestion() {
        GetCurrentQuestionViewResponse firstQuestion = createQuestion();
        GetCurrentQuestionViewResponse secondQuestion =
                new GetCurrentQuestionViewResponse(
                        200L,
                        "What is Python?",
                        List.of("Programming language", "Animal")
                );

        when(backendRestClient.getCurrentQuestion(TELEGRAM_ID))
                .thenReturn(firstQuestion, secondQuestion);

        AnswerQuestionResponse answerResult =
                new AnswerQuestionResponse(true, List.of(0), 0, false);

        when(backendRestClient.answerQuestion(
                eq(TELEGRAM_ID),
                eq(100),
                eq(Set.of(0))
        )).thenReturn(answerResult);

        dialogHandler.handleMessage("p");
        dialogHandler.handleMessage("2");

        DialogResponse response = dialogHandler.handleMessage("1");

        assertTrue(response.message().contains(
                "Good job, you answered correctly!"
        ));
        assertTrue(response.message().contains("What is Python?"));

        verify(backendRestClient).answerQuestion(
                TELEGRAM_ID, 100, Set.of(0)
        );
        verify(backendRestClient, times(2))
                .getCurrentQuestion(TELEGRAM_ID);
    }

    @DisplayName("Вывод при неправильном ответе на вопрос")
    @Test
    void handleAnswer_incorrectAnswer_showsCorrectAnswersAndNextQuestion() {
        GetCurrentQuestionViewResponse firstQuestion = createQuestion();
        GetCurrentQuestionViewResponse secondQuestion =
                new GetCurrentQuestionViewResponse(
                        200L,
                        "What is Python?",
                        List.of("Programming language", "Animal")
                );

        when(backendRestClient.getCurrentQuestion(TELEGRAM_ID))
                .thenReturn(firstQuestion, secondQuestion);

        AnswerQuestionResponse answerResult =
                new AnswerQuestionResponse(false, List.of(1), 0, false);

        when(backendRestClient.answerQuestion(
                eq(TELEGRAM_ID),
                eq(100),
                eq(Set.of(1))
        )).thenReturn(answerResult);

        dialogHandler.handleMessage("p");
        dialogHandler.handleMessage("2");

        DialogResponse response = dialogHandler.handleMessage("2");

        assertTrue(response.message().contains(
                "Incorrect! The correct answers were: 2"
        ));
        assertTrue(response.message().contains("What is Python?"));
    }

    @DisplayName("Правильная обработка ответа из нескольких вариантов")
    @Test
    void handleAnswer_multipleAnswers_areParsedAndSentToBackend() {
        when(backendRestClient.getCurrentQuestion(TELEGRAM_ID))
                .thenReturn(createQuestion());

        when(backendRestClient.answerQuestion(
                eq(TELEGRAM_ID),
                eq(100),
                eq(Set.of(0, 1))
        )).thenReturn(
                new AnswerQuestionResponse(true, List.of(0, 1), 0, false)
        );

        // Показываем вопрос
        dialogHandler.handleMessage("p");
        dialogHandler.handleMessage("2");

        // Ответ из двух вариантов
        when(backendRestClient.getCurrentQuestion(TELEGRAM_ID))
                .thenReturn(createQuestion());

        dialogHandler.handleMessage("1, 2");

        verify(backendRestClient).answerQuestion(
                TELEGRAM_ID, 100, Set.of(0, 1)
        );
    }

    @DisplayName("Вывод при завершении квиза")
    @Test
    void handleAnswer_finishedQuiz_showsFinalScoreAndReturnsToMenu() {
        when(backendRestClient.getCurrentQuestion(TELEGRAM_ID))
                .thenReturn(createQuestion());

        when(backendRestClient.answerQuestion(
                eq(TELEGRAM_ID),
                eq(100),
                eq(Set.of(0))
        )).thenReturn(
                new AnswerQuestionResponse(true, List.of(0), 0, true)
        );

        when(backendRestClient.finishQuiz(TELEGRAM_ID))
                .thenReturn(new FinishQuizResponse(10, 10, 50));

        dialogHandler.handleMessage("p");
        dialogHandler.handleMessage("2");

        DialogResponse response = dialogHandler.handleMessage("1");

        assertTrue(response.message().contains("Quiz completed!"));
        assertTrue(response.message().contains("Your quiz score: 10"));
        assertTrue(response.message().contains("Your total score: 50"));
        assertTrue(response.message().contains("Choose an action:"));

        verify(backendRestClient).finishQuiz(TELEGRAM_ID);

        // Проверяем, что после завершения квиза снова доступно главное меню
        DialogResponse menuResponse = dialogHandler.handleMessage("q");

        assertTrue(menuResponse.exitRequested());
        assertEquals("Goodbye!", menuResponse.message());
    }

    // -------------------- LEADERBOARD --------------------

    @DisplayName("Вывод доски лидеров: в репозитории еще нет пользователей")
    @Test
    void showLeaderboard_emptyLeaderboard_returnsFirstPlaceMessage() {
        when(backendRestClient.getLeaderboard(TELEGRAM_ID))
                .thenReturn(new GetLeaderboardResponse(List.of()));

        DialogResponse response = dialogHandler.handleMessage("l");

        assertEquals(
                "Apparently you can be first in our repository!",
                response.message()
        );
    }

    @DisplayName("Вывод доски лидеров: в репозитории есть пользователи")
    @Test
    void showLeaderboard_nonEmptyLeaderboard_showsPlayers() {
        List<LeaderboardResponseEntry> entries = List.of(
                new LeaderboardResponseEntry(1, 100, "Alice"),
                new LeaderboardResponseEntry(2, 80, "Bob" )
        );

        when(backendRestClient.getLeaderboard(TELEGRAM_ID))
                .thenReturn(new GetLeaderboardResponse(entries));

        DialogResponse response = dialogHandler.handleMessage("l");

        assertTrue(response.message().contains("Alice"));
        assertTrue(response.message().contains("100 points"));
        assertTrue(response.message().contains("Bob"));
        assertTrue(response.message().contains("80 points"));
    }

    // -------------------- TEST HELPERS --------------------

    private GetCurrentQuestionViewResponse createQuestion() {
        return new GetCurrentQuestionViewResponse(
                100L,
                "What is Java?",
                List.of(
                        "Programming language",
                        "Coffee",
                        "Operating system"
                )
        );
    }

    private void startQuizAndShowQuestion() {
        when(backendRestClient.getCurrentQuestion(TELEGRAM_ID))
                .thenReturn(createQuestion());

        dialogHandler.handleMessage("p");
        dialogHandler.handleMessage("2");
    }
}
