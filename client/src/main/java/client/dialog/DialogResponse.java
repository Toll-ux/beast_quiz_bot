package client.dialog;

/**
 * Класс для передачи ответов от DialogHandler клиенту
 * @param message - сообщение, которое увидит пользователь
 * @param exitRequested - указывает, запросил ли пользователь завершение диалога (ввел q)
 */
public record DialogResponse(
        String message,
        boolean exitRequested
) {}
