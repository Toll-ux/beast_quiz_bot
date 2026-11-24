
package client;

import client.dialog.DialogHandler;
import client.dialog.DialogResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClientApplicationTest {

    private DialogHandler dialogHandler;
    private ByteArrayOutputStream outputStream;
    private PrintStream printStream;

    @BeforeEach
    void setUp() {
        dialogHandler = mock(DialogHandler.class);

        outputStream = new ByteArrayOutputStream();
        printStream = new PrintStream(outputStream, true, StandardCharsets.UTF_8);
    }

    private String getOutput() {
        return outputStream.toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("Client best_quizbot.application displays welcome message and menu")
    void testWelcomeMessageAndMenu() {

        String input = "12345\nq\n";

        when(dialogHandler.handleMessage("q"))
                .thenReturn(new DialogResponse("Goodbye!", true));

        ClientApplication.run(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                printStream,
                telegramId -> dialogHandler
        );

        String output = getOutput();

        assertTrue(output.contains("Welcome to our Quiz-bot!"));
        assertTrue(output.contains("Enter your Telegram ID:"));
        assertTrue(output.contains("Choose an action:"));
        assertTrue(output.contains("p — play quiz"));
        assertTrue(output.contains("l — show leaderboard"));
        assertTrue(output.contains("h — help"));
        assertTrue(output.contains("q — quit"));
    }

    @Test
    @DisplayName("Client best_quizbot.application passes Telegram ID to DialogHandler")
    void testTelegramIdIsPassedToDialogHandler() {

        String input = "12345\nq\n";

        AtomicLong receivedTelegramId = new AtomicLong(-1);

        when(dialogHandler.handleMessage("q"))
                .thenReturn(new DialogResponse("Goodbye!", true));

        ClientApplication.run(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                printStream,
                telegramId -> {
                    receivedTelegramId.set(telegramId);
                    return dialogHandler;
                }
        );

        assertEquals(12345L, receivedTelegramId.get());
    }

    @Test
    @DisplayName("Client best_quizbot.application trims whitespace from Telegram ID")
    void testTelegramIdIsTrimmed() {

        String input = "  12345  \nq\n";

        AtomicLong receivedTelegramId = new AtomicLong(-1);

        when(dialogHandler.handleMessage("q"))
                .thenReturn(new DialogResponse("Goodbye!", true));

        ClientApplication.run(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                printStream,
                telegramId -> {
                    receivedTelegramId.set(telegramId);
                    return dialogHandler;
                }
        );

        assertEquals(12345L, receivedTelegramId.get());
    }

    @Test
    @DisplayName("Client best_quizbot.application forwards user messages to DialogHandler")
    void testUserMessagesAreForwarded() {

        String input = "12345\np\nh\nq\n";

        when(dialogHandler.handleMessage("p"))
                .thenReturn(new DialogResponse("Starting quiz...", false));

        when(dialogHandler.handleMessage("h"))
                .thenReturn(new DialogResponse("Help message", false));

        when(dialogHandler.handleMessage("q"))
                .thenReturn(new DialogResponse("Goodbye!", true));

        ClientApplication.run(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                printStream,
                telegramId -> dialogHandler
        );

        verify(dialogHandler).handleMessage("p");
        verify(dialogHandler).handleMessage("h");
        verify(dialogHandler).handleMessage("q");

        verifyNoMoreInteractions(dialogHandler);
    }

    @Test
    @DisplayName("Client best_quizbot.application prints responses from DialogHandler")
    void testDialogResponsesArePrinted() {

        String input = "12345\np\nh\nq\n";

        when(dialogHandler.handleMessage("p"))
                .thenReturn(new DialogResponse("Starting quiz...", false));

        when(dialogHandler.handleMessage("h"))
                .thenReturn(new DialogResponse("Help message", false));

        when(dialogHandler.handleMessage("q"))
                .thenReturn(new DialogResponse("Goodbye!", true));

        ClientApplication.run(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                printStream,
                telegramId -> dialogHandler
        );

        String output = getOutput();

        assertTrue(output.contains("Starting quiz..."));
        assertTrue(output.contains("Help message"));
        assertTrue(output.contains("Goodbye!"));
    }

    @Test
    @DisplayName("Client best_quizbot.application continues until exit is requested")
    void testApplicationContinuesUntilExitRequested() {

        String input = "12345\np\nh\nq\n";

        when(dialogHandler.handleMessage("p"))
                .thenReturn(new DialogResponse("Quiz started", false));

        when(dialogHandler.handleMessage("h"))
                .thenReturn(new DialogResponse("Help", false));

        when(dialogHandler.handleMessage("q"))
                .thenReturn(new DialogResponse("Goodbye", true));

        ClientApplication.run(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                printStream,
                telegramId -> dialogHandler
        );

        verify(dialogHandler, times(1)).handleMessage("p");
        verify(dialogHandler, times(1)).handleMessage("h");
        verify(dialogHandler, times(1)).handleMessage("q");
    }

    @Test
    @DisplayName("Client best_quizbot.application stops processing messages after exit is requested")
    void testApplicationStopsAfterExitRequested() {

        String input = "12345\nq\np\nh\n";

        when(dialogHandler.handleMessage("q"))
                .thenReturn(new DialogResponse("Goodbye!", true));

        ClientApplication.run(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                printStream,
                telegramId -> dialogHandler
        );

        verify(dialogHandler, times(1)).handleMessage("q");

        verifyNoMoreInteractions(dialogHandler);
    }

    @Test
    @DisplayName("Client best_quizbot.application creates DialogHandler only once")
    void testDialogHandlerIsCreatedOnlyOnce() {

        String input = "12345\np\nh\nq\n";

        AtomicLong factoryCalls = new AtomicLong(0);

        when(dialogHandler.handleMessage("p"))
                .thenReturn(new DialogResponse("Quiz started", false));

        when(dialogHandler.handleMessage("h"))
                .thenReturn(new DialogResponse("Help", false));

        when(dialogHandler.handleMessage("q"))
                .thenReturn(new DialogResponse("Goodbye!", true));

        ClientApplication.run(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                printStream,
                telegramId -> {
                    factoryCalls.incrementAndGet();
                    return dialogHandler;
                }
        );

        assertEquals(1, factoryCalls.get());
    }

    @Test
    @DisplayName("Client best_quizbot.application throws exception when Telegram ID is invalid")
    void testInvalidTelegramId() {

        String input = "invalid_id\n";

        assertThrows(NumberFormatException.class, () ->
                ClientApplication.run(
                        new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                        printStream,
                        telegramId -> dialogHandler
                )
        );

        verifyNoInteractions(dialogHandler);
    }
}
