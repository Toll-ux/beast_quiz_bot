package client;

import client.dialog.DialogHandler;
import client.dialog.DialogResponse;
import config.ClientConfig;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.function.Function;

/**
 * Создает хэндлер диалога для пользователя с его telegramId;
 * В бесконечном цикле смотрит ввод пользователя и отправляет его на обработку Хэндлеру;
 * Выводит на экран пришедший от Хэндлера ответ.
 * Обрывает цикл ожидания, если пользователь ввел q (exitRequested() == true)
 */
public class ClientApplication {

    public static void main(String[] args) {
        run(System.in, System.out, telegramId ->
                new DialogHandler(
                        ClientConfig.createBackendRestClient(),
                        telegramId
                )
        );
    }

    static void run(
            InputStream inputStream,
            PrintStream outputStream,
            Function<Long, DialogHandler> handlerFactory
    ) {
        try (Scanner scanner = new Scanner(inputStream)) {

            outputStream.println("Welcome to our Quiz-bot!");
            outputStream.print("Enter your Telegram ID: ");

            long telegramId = Long.parseLong(scanner.nextLine().trim());

            DialogHandler dialogHandler = handlerFactory.apply(telegramId);

            outputStream.println("""
                    Choose an action:
                    p — play quiz
                    l — show leaderboard
                    h — help
                    q — quit
                    """);

            while (true) {
                String input = scanner.nextLine();

                DialogResponse response =
                        dialogHandler.handleMessage(input);

                outputStream.println(response.message());

                if (response.exitRequested()) {
                    break;
                }
            }
        }
    }
}