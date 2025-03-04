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
import com.eturn.eturn.exception.user.*;
import com.eturn.eturn.security.entity.*;
import com.eturn.eturn.security.jwt.JwtAuthenticationResponse;
import com.eturn.eturn.security.jwt.JwtService;
import com.eturn.eturn.service.FacultyService;
import com.eturn.eturn.service.GroupService;
import com.eturn.eturn.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.eturn.eturn.enums.Role.*;

@Service
public class AuthenticationService {
    private static final Logger logger = LogManager.getLogger(AuthenticationService.class);
    private final JwtService jwtService;
    @Value("${external.api.url}")
    private String externalApiUrl;

    @Value("${external.api.url_etu_id}")
    private String externalApiETUIDUrl;

    @Value("${external.api.url_profile}")
    private String externalApiProfile;

    @Value("${eturn.etu.secret}")
    private String etuIdSecret;
    @Value("${eturn.etu.client}")
    private String etuIdClientId;
    private final RestTemplate restTemplate;
    private final UserService userService;
    private final GroupService groupService;
    private final FacultyService facultyService;

    // TODO Эти две переменные снизу будут не нужны, когда будут удалены тестировочные функции
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(
            JwtService jwtService,
            RestTemplate restTemplate,
            UserService userService,
            GroupService groupService,
            FacultyService facultyService,
            PasswordEncoder passwordEncoder,
            UserMapper userMapper,
            AuthenticationManager authenticationManager
    ) {
        this.jwtService = jwtService;
        this.restTemplate = restTemplate;
        this.userService = userService;
        this.groupService = groupService;
        this.facultyService = facultyService;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.authenticationManager = authenticationManager;
    }

    @CacheEvict(value = "groups", allEntries = true)
    public void createFaculties(String username){

        // проверка прав
        User u = userService.getUserFromLogin(username);
        if (u.getRole() != ADMIN) {
            throw new AccessException("no admin access");
        }

        // получение групп из API ЛЭТИ
        List<FacultiesResponse> faculties = getGroupsByEtu();
        if (faculties == null){
            throw new NotFoundGroupException("network problem");
        }

        // создание групп
        for (FacultiesResponse faculty : faculties) {
            FacultyDTO facultyDTO = new FacultyDTO(
                    faculty.getId(),
                    faculty.getTitle()
            );
            Faculty facultyCreated =
                    facultyService.createFaculty(facultyDTO);
            for (DepartmentResponse dep : faculty.getDepartmentResponses()) {
                for (GroupResponse group : dep.getGroupResponses()) {
                    groupService.createOptionalGroup(
                            group.getId(),
                            group.getNumber(),
                            group.getCourse(),
                            facultyCreated
                    );
                }
            }
        }
    }

    /**
     * Ручная регистрация
     * @param userCreateDTO с помощью собственного тела
     * @param username имя текущего пользователя
     * @return токен
     */
    public JwtAuthenticationResponse signUp(
            UserCreateDTO userCreateDTO,
            String username
    ) {
        // Проверяем, имеет ли пользователь права администратора
        validateAdminAccess(username);

        // Создаем пользователя на основе DTO
        User newUser = createUserFromDTO(userCreateDTO);

        // Сохраняем пользователя в системе
        User createdUser = userService.createUser(newUser);

        // Генерируем JWT токен
        String jwt = "Bearer "
                + jwtService.generateToken(createdUser);
        return new JwtAuthenticationResponse(jwt);
    }

    /**
     * Авторизация через ETU ID
     * @param authData Данные с токеном
     * @return данные об успешной авторизации
     */
    public JwtAuthenticationResponse auth(AuthData authData) {
        // Получаем данные пользователя из ETU ID
        EtuIdUser etuIdUser = fetchEtuIdUser(authData.tokenETUID());

        // Проверяем, что данные пользователя получены
        if (etuIdUser == null) {
            throw new NotFoundUserException("No user in ETU ID");
        }

        // Обновляем или создаем пользователя в системе
        User currentUser = updateOrCreateUser(etuIdUser, authData);

        // Генерируем JWT токен
        String jwt = "Bearer " + jwtService.generateToken(currentUser);
        return new JwtAuthenticationResponse(jwt);
    }

    /**
     * Ручной вход
     * @param login по логину
     * @param password и паролю
     * @return токен авторизации
     */
    public JwtAuthenticationResponse signIn(
            String login,
            String password
    ) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        login,
                        password
                )
        );
        User user = userService.getUserFromLogin(login);
        String jwt = "Bearer " + jwtService.generateToken(user);
        return new JwtAuthenticationResponse(jwt);
    }

    //
    //
    //

    /**
     * GET запрос групп
     * @return список факультетов с группами
     */
    private List<FacultiesResponse> getGroupsByEtu(){
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String externalApiUrlGroups = "https://digital.etu.ru/api/mobile/groups";
        ResponseEntity<List<FacultiesResponse>> response = restTemplate.exchange(
                externalApiUrlGroups,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );

        // проверка ответа
        checkSuccessResponse(response);
        return response.getBody();
    }

    /**
     * Проверка успешного выполнения запроса
     * @param response выполненный запрос
     */
    private void checkSuccessResponse(
            ResponseEntity<List<FacultiesResponse>> response
    ){
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new NotFoundGroupException("network problem");
        }
    }

    /**
     * Получает данные пользователя из ETU ID.
     * @param tokenETUID Токен для авторизации в ETU ID.
     * @return Данные пользователя из ETU ID.
     * @throws AuthPasswordException Если не удалось получить данные.
     */
    private EtuIdUser fetchEtuIdUser(String tokenETUID) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + tokenETUID);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<EtuIdUser> response = restTemplate.exchange(
                externalApiUrl,
                HttpMethod.GET,
                entity,
                EtuIdUser.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new AuthPasswordException("No access from ETU ID");
        }

        return response.getBody();
    }

    /**
     * Обновляет или создает пользователя в системе на основе данных из ETU ID.
     * @param etuIdUser Данные пользователя из ETU ID.
     * @param authData  Данные для авторизации.
     * @return Обновленный или созданный пользователь.
     */
    private User updateOrCreateUser(
            EtuIdUser etuIdUser,
            AuthData authData
    ) {
        Optional<User> optionalUser = userService.getOptionalUserFromLogin(
                etuIdUser.getEtuId()
        );
        User user;

        if (optionalUser.isPresent()) {
            user = optionalUser.get();
            updateUserGroup(user, etuIdUser);
            updateUserNotificationToken(user, authData);
            user = userService.updateUser(user);
        } else {
            user = createNewUser(etuIdUser, authData);
        }

        return user;
    }

    /**
     * Обновляет группу пользователя на основе данных из ETU ID.
     * @param user     Пользователь, которого нужно обновить.
     * @param etuIdUser Данные пользователя из ETU ID.
     * @throws NotFoundGroupException Если группа не найдена.
     */
    private void updateUserGroup(
            User user,
            EtuIdUser etuIdUser
    ) {
        if (etuIdUser.getEducations() != null) {
            EtuIdEducation etuIdEducation =
                    etuIdUser.getEducations().get(0);
            EduGroups eduGroups =
                    etuIdEducation.getEduGroups();
            Optional<Group> group =
                    groupService.getGroup(
                            eduGroups.getName()
                    );

            if (group.isPresent()) {
                user.setGroup(group.get());
            } else {
                throw new NotFoundGroupException("No group exception");
            }
        }
    }

    /**
     * Обновляет токен уведомлений и тип приложения пользователя.
     * @param user     Пользователь, которого нужно обновить.
     * @param authData Данные для авторизации.
     */
    private void updateUserNotificationToken(
            User user,
            AuthData authData
    ) {
        if (authData.tokenNotify() != null) {
            try {
                user.setTokenNotification(
                        authData.tokenNotify()
                );
                user.setApplicationType(
                        ApplicationType.valueOf(authData.type())
                );
            } catch (IllegalArgumentException e) {
                logger.error("Cannot resolve type of application");
            }
        }
    }

    /**
     * Создает нового пользователя на основе данных из ETU ID.
     * @param etuIdUser Данные пользователя из ETU ID.
     * @param authData  Данные для авторизации.
     * @return Созданный пользователь.
     */
    private User createNewUser(
            EtuIdUser etuIdUser,
            AuthData authData
    ) {
        User newUser = new User();
        newUser.setName(
                etuIdUser.getFirstName() + " " + etuIdUser.getSecondName()
        );
        newUser.setLogin(
                etuIdUser.getEtuId()
        );
        newUser.setPassword(
                etuIdUser.getEtuId()+HashGenerator.generateUniqueCode()
        );

        updateUserGroup(newUser, etuIdUser);
        updateUserNotificationToken(
                newUser,
                authData
        );

        Role role = determineUserRole(
                etuIdUser.getPosition()
        );
        newUser.setRole(role);

        return userService.createUser(newUser);
    }

    /**
     * Определяет роль пользователя на основе его позиции в ETU ID.
     * @param position Позиция пользователя в ETU ID.
     * @return Роль пользователя.
     */
    private Role determineUserRole(String position) {
        return "Сотрудник".equals(position) ? EMPLOYEE : STUDENT;
    }

    /**
     * Проверяет, имеет ли пользователь права администратора.
     * @param username Логин пользователя.
     * @throws AccessException Если пользователь не является администратором.
     */
    private void validateAdminAccess(String username) {
        User userAdmin = userService.getUserFromLogin(username);
        if (userAdmin.getRole() != ADMIN) {
            throw new AccessException("No admin access");
        }
    }

    /**
     * Создает пользователя на основе DTO.
     * @param userCreateDTO DTO с данными для создания пользователя.
     * @return Созданный пользователь.
     */
    private User createUserFromDTO(
            UserCreateDTO userCreateDTO
    ) {
        // Определяем роль пользователя
        Role role = Role.valueOf(userCreateDTO.role());

        // Определяем тип приложения (если указан)
        ApplicationType appType = parseApplicationType(
                userCreateDTO.appType()
        );

        // Создаем пользователя
        User user = userMapper.userCreateDTOtoUser(
                userCreateDTO,
                role,
                appType
        );

        // Хэшируем пароль
        String hashedPassword = passwordEncoder.encode(
                userCreateDTO.password()
        );
        user.setPassword(hashedPassword);

        return user;
    }

    /**
     * Парсит тип приложения из строки.
     * @param appType Строка с типом приложения.
     * @return Тип приложения или null, если тип не поддерживается.
     */
    private ApplicationType parseApplicationType(String appType) {
        try {
            return ApplicationType.valueOf(appType);
        } catch (IllegalArgumentException e) {
            logger.warn("Unsupported application type: {}", appType);
            return null;
        }
    }

    public JwtAuthenticationResponse etuIdAuth(EtuIdCode etuIdCode) {
        // Создаем заголовки
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Создаем параметры для тела запроса
        MultiValueMap<String, String> bodyParams = new LinkedMultiValueMap<>();
        bodyParams.add("grant_type", "authorization_code");
        bodyParams.add("client_id", etuIdClientId);
        bodyParams.add("redirect_uri", "https://digital.etu.ru/eturn/processing-auth");
        bodyParams.add("client_secret", etuIdSecret);
        bodyParams.add("code_verifier", etuIdCode.codeVerifier());
        bodyParams.add("code", etuIdCode.code());

        // Создаем HttpEntity с заголовками и телом запроса
        HttpEntity<MultiValueMap<String, String>> entity
                = new HttpEntity<>(bodyParams, headers);
        ResponseEntity<EtuIdToken> response = restTemplate.exchange(
                externalApiETUIDUrl,
                HttpMethod.POST,
                entity,
                EtuIdToken.class
        );

        // Обрабатываем ответ
        if (response.getStatusCode() != HttpStatus.OK) {
           throw new OAuthRequestETUIDException("Request failed");
        }
        EtuIdToken token = response.getBody();
        if (token == null){
            throw new NoBodyETUIDException("Request failed with no body");
        }

        return auth(new AuthData(token.access_token(), null, null));
    }
}
