package side.project.collec;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class CollecApplication {

	public static void main(String[] args) {
		SpringApplication.run(CollecApplication.class, args);
	}

}
