package dsa.sheduler.dsa_sheduler_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DsaShedulerAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(DsaShedulerAppApplication.class, args);
	}

}
