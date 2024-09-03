package com.eturn.eturn;

import com.eturn.eturn.entity.User;
import com.eturn.eturn.enums.Role;
import com.eturn.eturn.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class EturnApplication {

	private final PasswordEncoder passwordEncoder;
	private final UserService userService;

	@Value("${eturn.defaults.username}")
	private String username;

	@Value("${eturn.defaults.password}")
	private String password;

	public EturnApplication(PasswordEncoder passwordEncoder, UserService userService) {
		this.passwordEncoder = passwordEncoder;
		this.userService = userService;
	}

	public static void main(String[] args) {
		SpringApplication.run(EturnApplication.class, args);
	}
	@Bean
	public CommandLineRunner CommandLineRunnerBean() {
		return (args) -> {
			if (!userService.isUserExist(username)) {
				User user = new User();
				user.setId(1L);
				user.setName("Admin");
				user.setPassword(passwordEncoder.encode(password));
				user.setLogin(username);
				user.setRole(Role.ADMIN);
				userService.createUser(user);
			}
		};
	}
}
