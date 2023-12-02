package com.eturn.eturn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.swing.*;

@SpringBootApplication
public class EturnApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(EturnApplication.class, args);

	}

}
