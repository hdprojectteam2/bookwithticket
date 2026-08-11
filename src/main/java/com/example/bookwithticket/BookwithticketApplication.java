package com.example.bookwithticket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BookwithticketApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookwithticketApplication.class, args);
	}

}
