package application.port;

import domain.Question;
import domain.Quiz;
import domain.User;

import java.util.List;
import java.util.Optional;

/*
Интерфейсы для работы с бекендом в application, их имплементация в infrastructure 
*/

/*
Application - интерфейс работы с БД вопросов
*/
public interface QuestionRepository
{
    /*
    Возвращает списов случайных неповторяющихся вопросов из базы жанных в количестве questionCount
    (Для начала стоит обеспечить, что вопросы в одном (возвращаемом этим методом) списке не повторялись, потом сделаем чтобы 
    в одной сессии не повторялись или для одного юзера в течение какого-либо времени) 
    */
    List<Question> findRandomQuestions(int questionsCount);
}

