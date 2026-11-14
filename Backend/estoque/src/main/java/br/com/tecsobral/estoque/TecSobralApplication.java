package br.com.tecsobral.estoque;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TecSobralApplication {

	public static void main(String[] args) {
		SpringApplication.run(TecSobralApplication.class, args);
	}

}
