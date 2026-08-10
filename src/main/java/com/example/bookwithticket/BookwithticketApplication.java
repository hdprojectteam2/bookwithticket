package com.example.bookwithticket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class BookwithticketApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookwithticketApplication.class, args);
	}

}

