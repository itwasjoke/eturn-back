package com.eturn.eturn.security;

import com.eturn.eturn.dto.FacultyDTO;
import com.eturn.eturn.dto.UserCreateDTO;
import com.eturn.eturn.dto.mapper.UserMapper;
import com.eturn.eturn.dto.parsing.DepartmentResponse;
import com.eturn.eturn.dto.parsing.FacultiesResponse;
import com.eturn.eturn.dto.parsing.GroupResponse;
import com.eturn.eturn.entity.Faculty;
import com.eturn.eturn.entity.Group;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.enums.RoleEnum;
import com.eturn.eturn.exception.group.NotFoundGroupException;
import com.eturn.eturn.exception.user.AuthPasswordException;
import com.eturn.eturn.exception.user.NotFoundUserException;
import com.eturn.eturn.service.FacultyService;
import com.eturn.eturn.service.GroupService;
import com.eturn.eturn.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class AuthenticationService {
    private final JwtService jwtService;
    @Value("${external.api.url}")
    private String externalApiUrl;
    private final RestTemplate restTemplate;
    private final UserService userService;
    private final GroupService groupService;
    private final FacultyService facultyService;

    // TODO Эти две переменные снизу будут не нужны, когда будут удалены тестировочные функции
    private final PasswordEncoder passwordEncoder;

    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(JwtService jwtService, RestTemplate restTemplate, UserService userService, GroupService groupService, FacultyService facultyService, PasswordEncoder passwordEncoder, UserMapper userMapper, AuthenticationManager authenticationManager) {
        this.jwtService = jwtService;
        this.restTemplate = restTemplate;
        this.userService = userService;
        this.groupService = groupService;
        this.facultyService = facultyService;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.authenticationManager = authenticationManager;
    }

    public void createFaculties(){
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String externalApiUrlGroups = "https://digital.etu.ru/api/mobile/groups";
        ResponseEntity<List<FacultiesResponse>> response = restTemplate.exchange(
                externalApiUrlGroups,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<FacultiesResponse>>() {}
        );

        if (!response.getStatusCode().is2xxSuccessful()){
            throw new NotFoundGroupException("network problem");
        }
        if (!response.getBody().isEmpty()){
            List<FacultiesResponse> faculties = response.getBody();
            for (FacultiesResponse faculty : faculties) {
                FacultyDTO facultyDTO = new FacultyDTO(faculty.getId(), faculty.getTitle());
                Faculty facultyCreated = facultyService.createFaculty(facultyDTO);
                for (DepartmentResponse department : faculty.getDepartmentResponses()) {
                    for (GroupResponse group : department.getGroupResponses()){
                        groupService.createOptionalGroup(group.getId(), group.getNumber(), group.getCourse(), facultyCreated);
                    }
                }
            }
        }
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
            Optional<User> user = userService.getUser(etuIdUser.getId());
            if (user.isPresent()) {
                currentUser = user.get();
            } else {
                User newUser = new User();
                newUser.setId(etuIdUser.getId());
                newUser.setName(etuIdUser.getFirstName() + " " + etuIdUser.getSecondName());
                newUser.setLogin("eturnLogin" + etuIdUser.getId().toString());
                newUser.setPassword("eturnPassword"+etuIdUser.getId().toString());
                if (etuIdUser.getEducations() != null) {
                    EtuIdEducation etuIdEducation = etuIdUser.getEducations().get(0);
                    EduGroups eduGroups = etuIdEducation.getEduGroups();
                    Optional<Group> group = groupService.getGroup(eduGroups.getName());
                    if (group.isPresent()) {
                        newUser.setGroup(group.get());
                    } else {
                        throw new NotFoundGroupException("no group exception");
                    }
                }
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
        User user = userMapper.userCreateDTOtoUser(userCreateDTO, r);
        String password = passwordEncoder.encode(userCreateDTO.password());
        user.setPassword(password);
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
