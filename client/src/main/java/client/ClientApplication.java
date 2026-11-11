package client;

import client.api.BackendRestClient;
import client.dialog.DialogHandler;
import client.dialog.DialogResponse;

import java.util.Scanner;

/**
 * Создает хэндлер диалога для пользователя с его telegramId;
 * В бесконечном цикле смотрит ввод пользователя и отправляет его на обработку Хэндлеру;
 * Выводит на экран пришедший от Хэндлера ответ.
 * Обрывает цикл ожидания, если пользователь ввел q (exitRequested() == true)
 */
public class ClientApplication {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to our Quiz-bot!");
        System.out.print("Enter your Telegram ID: ");

        long telegramId = Long.parseLong(scanner.nextLine().trim());

        BackendRestClient backendRestClient =
                new BackendRestClient("http://localhost:8080");

        DialogHandler dialogHandler =
                new DialogHandler(backendRestClient, telegramId);

        System.out.println("""
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

            System.out.println(response.message());

            if (response.exitRequested()) {
                break;
            }
        }

        scanner.close();
    }
}