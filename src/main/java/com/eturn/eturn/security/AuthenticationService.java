package com.eturn.eturn.security;

import com.eturn.eturn.dto.AuthData;
import com.eturn.eturn.dto.FacultyDTO;
import com.eturn.eturn.dto.UserCreateDTO;
import com.eturn.eturn.dto.mapper.UserMapper;
import com.eturn.eturn.dto.parsing.DepartmentResponse;
import com.eturn.eturn.dto.parsing.FacultiesResponse;
import com.eturn.eturn.dto.parsing.GroupResponse;
import com.eturn.eturn.entity.Faculty;
import com.eturn.eturn.entity.Group;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.enums.ApplicationType;
import com.eturn.eturn.enums.Role;
import com.eturn.eturn.exception.group.NotFoundGroupException;
import com.eturn.eturn.exception.user.AccessException;
import com.eturn.eturn.exception.user.AuthPasswordException;
import com.eturn.eturn.exception.user.NotFoundUserException;
import com.eturn.eturn.security.entity.EduGroups;
import com.eturn.eturn.security.entity.EtuIdEducation;
import com.eturn.eturn.security.entity.EtuIdUser;
import com.eturn.eturn.security.jwt.JwtAuthenticationResponse;
import com.eturn.eturn.security.jwt.JwtService;
import com.eturn.eturn.service.FacultyService;
import com.eturn.eturn.service.GroupService;
import com.eturn.eturn.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import java.util.Optional;

@Service
public class AuthenticationService {
    private static final Logger logger = LogManager.getLogger(AuthenticationService.class);
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

    public void createFaculties(String username){
        User u = userService.findByLogin(username);
        if (u.getRole() == Role.ADMIN) {
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String externalApiUrlGroups = "https://digital.etu.ru/api/mobile/groups";
            ResponseEntity<List<FacultiesResponse>> response = restTemplate.exchange(
                    externalApiUrlGroups,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<FacultiesResponse>>() {
                    }
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new NotFoundGroupException("network problem");
            }
            if (!response.getBody().isEmpty()) {
                List<FacultiesResponse> faculties = response.getBody();
                for (FacultiesResponse faculty : faculties) {
                    FacultyDTO facultyDTO = new FacultyDTO(faculty.getId(), faculty.getTitle());
                    Faculty facultyCreated = facultyService.createFaculty(facultyDTO);
                    for (DepartmentResponse department : faculty.getDepartmentResponses()) {
                        for (GroupResponse group : department.getGroupResponses()) {
                            groupService.createOptionalGroup(group.getId(), group.getNumber(), group.getCourse(), facultyCreated);
                        }
                    }
                }
            }
        } else {
            throw new AccessException("no admin access");
        }
    }

    public JwtAuthenticationResponse auth(AuthData authData) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authData.tokenETUID());
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
            EtuIdEducation etuIdEducation = etuIdUser.getEducations().get(0);
            EduGroups eduGroups = etuIdEducation.getEduGroups();
            if (user.isPresent()) {
                currentUser = user.get();
                if (etuIdUser.getEducations() != null) {
                    Optional<Group> group = groupService.getGroup(eduGroups.getName());
                    if (group.isPresent()) {
                        currentUser.setGroup(group.get());
                    } else {
                        throw new NotFoundGroupException("no group exception");
                    }
                }
                if (authData.tokenNotify()!=null) {
                    try {
                        currentUser.setTokenNotification(authData.tokenNotify());
                        currentUser.setApplicationType(ApplicationType.valueOf(authData.type()));
                    } catch (IllegalArgumentException e) {
                        logger.error("cannot resolve type of application");
                    }
                }
                currentUser = userService.updateUser(currentUser);
            } else {
                User newUser = new User();
                newUser.setId(etuIdUser.getId());
                if (authData.tokenNotify()!=null) {
                    try {
                        newUser.setTokenNotification(authData.tokenNotify());
                        newUser.setApplicationType(ApplicationType.valueOf(authData.type()));
                    } catch (IllegalArgumentException e) {
                        logger.error("cannot resolve type of application");
                    }
                }
                newUser.setName(etuIdUser.getFirstName() + " " + etuIdUser.getSecondName());
                newUser.setLogin("eturnLogin" + etuIdUser.getId().toString());
                newUser.setPassword("eturnPassword"+etuIdUser.getId().toString());
                if (etuIdUser.getEducations() != null) {
                    Optional<Group> group = groupService.getGroup(eduGroups.getName());
                    if (group.isPresent()) {
                        newUser.setGroup(group.get());
                    } else {
                        throw new NotFoundGroupException("no group exception");
                    }
                }
                Role role;
                switch (etuIdUser.getPosition()) {
                    case "Сотрудник":
                        role = Role.EMPLOYEE;
                        break;
                    default:
                        role = Role.STUDENT;
                }
                newUser.setRole(role);
                currentUser = userService.createUser(newUser);
            }
        }
        else {
            throw new NotFoundUserException("no user in ETU ID");
        }
        String jwt = "Bearer " + jwtService.generateToken(currentUser);
        return new JwtAuthenticationResponse(jwt);
    }
    public JwtAuthenticationResponse signUp(UserCreateDTO userCreateDTO, String username) {
        User userAdmin = userService.findByLogin(username);
        if (userAdmin.getRole() == Role.ADMIN) {
            User user;
            Role r = Role.valueOf(userCreateDTO.role());
            if (userCreateDTO.appType().equals("IOS") || userCreateDTO.appType().equals("ANDROID") || userCreateDTO.appType().equals("RUSTORE")) {
                ApplicationType a = ApplicationType.valueOf(userCreateDTO.appType());
                user = userMapper.userCreateDTOtoUser(userCreateDTO, r, a);
            } else {
                user = userMapper.userCreateDTOtoUser(userCreateDTO, r, null);
            }
            String password = passwordEncoder.encode(userCreateDTO.password());
            user.setPassword(password);
            User newUser = userService.createUser(user);

            var jwt = "Bearer " + jwtService.generateToken(newUser);
            return new JwtAuthenticationResponse(jwt);
        } else {
            throw new AccessException("no admin access");
        }
    }
    public JwtAuthenticationResponse signIn(String login, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                login,
                password
        ));
        User user = userService.findByLogin(login);
        String jwt = "Bearer "+jwtService.generateToken(user);
        return new JwtAuthenticationResponse(jwt);
    }
}
