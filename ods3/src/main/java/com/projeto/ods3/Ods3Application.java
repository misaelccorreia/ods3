package com.projeto.ods3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

// sem usuario em memoria gerado pelo Boot: a autenticacao e toda via JWT
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class Ods3Application {

	public static void main(String[] args) {
		SpringApplication.run(Ods3Application.class, args);
	}

}
