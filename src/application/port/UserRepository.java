package application.port;

import domain.User;

/*
Application - интерфейс работы с БД юзеров
*/
public interface UserRepository
{
    /*
    Возвращает Юзера из БД по его тгИД
    */
    User findUserByTelegramId(long telegramId);
    /*
    Отправляет данные Юзера в БД
    */
    void saveUserInfo(User user);
}