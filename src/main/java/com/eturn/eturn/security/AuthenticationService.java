package com.eturn.eturn.security;

import com.eturn.eturn.dto.UserCreateDTO;
import com.eturn.eturn.entity.Faculty;
import com.eturn.eturn.entity.Group;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.enums.RoleEnum;
import com.eturn.eturn.exception.user.AuthPasswordException;
import com.eturn.eturn.exception.user.NotFoundUserException;
import com.eturn.eturn.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class AuthenticationService {
    private final JwtService jwtService;
    @Value("${external.api.url}")
    private String externalApiUrl;
    private final RestTemplate restTemplate;
    private final UserService userService;

    // TODO Эти две переменные снизу будут не нужны, когда будут удалены тестировочные функции
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(JwtService jwtService, RestTemplate restTemplate, UserService userService, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.jwtService = jwtService;
        this.restTemplate = restTemplate;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public JwtAuthenticationResponse auth(String token) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<EtuIdUser> response = restTemplate.exchange(
                externalApiUrl,
                HttpMethod.GET,
                entity,
                EtuIdUser.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new AuthPasswordException("no access from ETU ID");
        }
        EtuIdUser etuIdUser = response.getBody();
        User currentUser;
        if (etuIdUser != null) {
            Optional<User> user = userService.getUserFromOptional(etuIdUser.getId());
            if (user.isPresent()) {
                currentUser = user.get();
            } else {
                User newUser = new User();
                newUser.setId(etuIdUser.getId());
                newUser.setName(etuIdUser.getFio());
                newUser.setLogin("eturnLogin" + etuIdUser.getId().toString());
                newUser.setPassword("eturnPassword"+etuIdUser.getId().toString());
                EtuIdEducation etuIdEducation = etuIdUser.getEducations().get(0);
                EduGroups eduGroups = etuIdEducation.getEduGroups();
                FacultyResponse facultyResponse = eduGroups.getFacultyResponse();
                Faculty f = userService.getFacultyForUser(facultyResponse.getName());
                Group g = userService.getGroupForUser(eduGroups.getName(), f);
                newUser.setIdFaculty(f.getId());
                newUser.setIdGroup(g.getId());
                RoleEnum roleEnum;
                switch (etuIdUser.getPosition()) {
                    case "Учащийся":
                        roleEnum = RoleEnum.STUDENT;
                    case "Сотрудник":
                        roleEnum = RoleEnum.EMPLOYEE;
                    default:
                        roleEnum = RoleEnum.STUDENT;
                }
                newUser.setRoleEnum(roleEnum);
                currentUser = userService.createUser(newUser);
            }
        }
        else {
            throw new NotFoundUserException("no user in ETU ID");
        }
        var jwt = "Bearer " + jwtService.generateToken(currentUser);
        return new JwtAuthenticationResponse(jwt);
    }

    // TODO Удалить функцию, когда необходимость в тестировании пользователей отсутствует.
    public JwtAuthenticationResponse signUp(UserCreateDTO userCreateDTO) {

        RoleEnum r = RoleEnum.valueOf(userCreateDTO.role());
        String password = passwordEncoder.encode(userCreateDTO.password());
        User user = new User();
        user.setName(userCreateDTO.name());
        user.setPassword(password);
        user.setLogin(userCreateDTO.login());
        user.setIdFaculty(userCreateDTO.facultyId());
        user.setIdGroup(userCreateDTO.groupId());
        user.setRoleEnum(r);
        User newUser = userService.createUser(user);

        var jwt = "Bearer "+jwtService.generateToken(newUser);
        return new JwtAuthenticationResponse(jwt);
    }

    // TODO Удалить функцию, когда необходимость в тестировании пользователей отсутствует.
    public JwtAuthenticationResponse signIn(String login, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                login,
                password
        ));

        var user = userService.findByLogin(login);

        var jwt = "Bearer "+jwtService.generateToken(user);
        return new JwtAuthenticationResponse(jwt);
    }
}
