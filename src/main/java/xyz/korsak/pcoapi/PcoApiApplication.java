package xyz.korsak.pcoapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class PcoApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(PcoApiApplication.class, args);
	}
}
