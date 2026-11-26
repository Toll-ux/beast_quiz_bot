package best_quizbot.infrastructure;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// Временный файл для проверки подключения к базе данных
// Примерный вывод, если все хорошо:
// Database connected: true
// Database: jdbc:sqlite:./data/quiz.db
@Component
public class DatabaseConnectionCheck implements CommandLineRunner {

    private final DataSource dataSource;

    public DatabaseConnectionCheck(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            System.out.println("Database connected: "
                    + connection.isValid(2));
            System.out.println("Database: "
                    + connection.getMetaData().getURL());
        }
    }
}