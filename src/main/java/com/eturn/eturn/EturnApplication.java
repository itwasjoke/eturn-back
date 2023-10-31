package com.eturn.eturn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.eturn.eturn.repo")
@EntityScan("com.eturn.eturn.domain")
public class EturnApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(EturnApplication.class, args);
	}

}
