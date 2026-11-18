package best_quizbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

@SpringBootApplication
public class BackendApplicationStarter {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplicationStarter.class, args);

    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

}
