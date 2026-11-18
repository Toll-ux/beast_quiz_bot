package best_quizbot.application.port;

import best_quizbot.domain.Question;

import java.util.List;


/*
Интерфейсы для работы с бекендом в best_quizbot.application, их имплементация в best_quizbot.infrastructure
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

