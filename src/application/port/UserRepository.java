package application.port;

import domain.User;
import java.util.Optional;

/*
Интерфейсы для работы с бекендом в application, их имплементация в infrastructure 
*/

/*
Application - интерфейс работы с БД юзеров
*/
public interface UserRepository
{
    /*
    Возвращает Юзера из БД по его тгИД
    */
    Optional<User> findUserByTelegramId(long telegramId);
    /*
    Отправляет данные Юзера в БД
    */
    void saveUserInfo(User user);
}