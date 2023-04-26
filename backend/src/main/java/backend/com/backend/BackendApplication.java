package backend.com.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class BackendApplication{

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
/*수만 Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJVU0VSIl0sInVzZXJuYW1lIjoic29vbWFuMzM0QGdtYWlsLmNvbSIsInN1YiI6InNvb21hbjMzNEBnbWFpbC5jb20iLCJpYXQiOjE2ODI1MTc5OTMsImV4cCI6MTY4MjU2MTE5M30.sdvSXMiw1VgqPJA1BR8FKqrttLWS7NSPMFaguwgiabY*/