package client.dialog;

/**
 * Состояния диалога с пользователем.
 * MAIN_MENU - Пользователь открыл/запустил бот/не находится в режиме игры. Хэндлер ожидает команду.
 * WAITING_FOR_QUESTION_COUNT	Пользователь инициировал начало игры - хэндлер ждет количество вопросов перед началом.
 * ANSWERING_QUESTION	Пользователь отвечает на вопрос.
 */

public enum DialogState {
    MAIN_MENU,
    WAITING_FOR_QUESTION_COUNT,
    ANSWERING_QUESTION
}
