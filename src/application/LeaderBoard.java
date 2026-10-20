package application;

import application.port.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**Класс лидерборд - часть app, нужен, чтобы возвращать лидерборд*/
public class LeaderBoard {

    private final QuizSession quizSession;
    private final UserRepository userRepository;

    /**Конструктор класса
     *
     * @param quizSession сессия квиза
     * @param userRepository инфраструктура для лидерборда
     */
    public LeaderBoard(QuizSession quizSession, UserRepository userRepository){
        this.quizSession = quizSession;
        this.userRepository = userRepository;
    }

    /**Функция берет топ 10 игроков из лидерборда и 1 игрок выше юзера, юзера и 1 ниже юзера.
     * Их комбинирует и возвращает отсортированный список
     */
    public  List<LeaderboardEntry> getUsersPosition(){
        // топ 10
        List<LeaderboardEntry> topTen = userRepository.findTopTen();
        // не пустой ли он
        if (topTen.isEmpty())
            throw new IllegalStateException("Leaderboard data is missing or database is empty");
        // кусок у юзера
        List<LeaderboardEntry> usersSlice = userRepository.findUserAndClosest(quizSession.telegramId());
        // не пустой ли он
        if (usersSlice.isEmpty())
            throw new IllegalStateException("User slice data is missing or database is empty");

        //LinkedHashSet оставляет порядок юзеров и при добавлении исключает лишних, ка множество
        Set<LeaderboardEntry> combinedSet = new LinkedHashSet<>(topTen);
        combinedSet.addAll(usersSlice);
        return new ArrayList<>(combinedSet);
    }

}
