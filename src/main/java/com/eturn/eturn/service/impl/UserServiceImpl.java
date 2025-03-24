package com.eturn.eturn.service.impl;

import com.eturn.eturn.dto.UserDTO;
import com.eturn.eturn.dto.mapper.UserMapper;
import com.eturn.eturn.entity.Group;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.exception.user.NotFoundUserException;
import com.eturn.eturn.repository.UserRepository;
import com.eturn.eturn.service.TurnService;
import com.eturn.eturn.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.eturn.eturn.enums.Role.ADMIN;
import static com.eturn.eturn.enums.Role.STUDENT;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LogManager.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private TurnService turnService;

    @Value("${eturn.defaults.username}")
    private String username;

    public UserServiceImpl(
            UserRepository userRepository,
            UserMapper userMapper
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Autowired
    public void setTurnService(TurnService turnService){
        this.turnService = turnService;
    }

    /**
     * Получение профиля
     * @param login имя пользователя
     * @return DTO с профилем
     */
    @Override
    public UserDTO getUserDTOFromLogin(String login) {
        Optional<User> u = userRepository.findUserByLogin(login);
        if (u.isEmpty()) {
            throw new NotFoundUserException("No user in database on getUser method (UserServiceImpl.java");
        }
        User user = u.get();
        // Все данные о пользователе
        String group = null;
        String faculty = null;
        String role = null;
        int countAvailable = 5;
        int countTurns =
                turnService.getCountTurnsOfUser(user);

        // ввод нужных данных в зависимости от статуса
        switch (user.getRole()) {
            case STUDENT -> {
                role = "Студент";
                group = user.getGroup().getNumber();
                faculty = user.getGroup().getFaculty().getName();
                countAvailable = 5 - countTurns;
            }
            case EMPLOYEE -> {
                role = "Сотрудник";
                countAvailable = 50 - countTurns;
            }
        }
        countAvailable = Math.max(countAvailable, 0);
        return userMapper.userToUserDTO(
                user,
                faculty,
                group,
                role,
                countAvailable,
                user.getFeedback() != null
        );
    }

    /**
     * Получение пользователя
     * @param id по ID
     * @return Опциональный пользователь
     */
    @Override
    public Optional<User> getOptionalUserFromId(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Получение пользователя
     * @param login по ETU ID
     * @return Опциональный пользователь
     */
    @Override
    public Optional<User> getOptionalUserFromLogin(String login) {
        return userRepository.findUserByLogin(login);
    }

    /**
     * Создание пользователя
     * @param user тело пользователя
     * @return пользователь
     */
    @Override
    public User createUser(User user) {
        if (userRepository.existsByLogin(user.getLogin())){
            throw new UsernameNotFoundException("Такой пользователь уже существует");
        }
        if (user.getRole() == ADMIN && !user.getLogin().equals(username)){
            throw new UsernameNotFoundException("Нет прав для создания");
        }
        logger.info("user created");
        return userRepository.save(user);
    }

    /**
     * Получение пользователя
     * @param login имя
     * @return пользователь
     */
    @Override
    public User getUserFromLogin(String login) {
        Optional<User> u = userRepository.findUserByLogin(login);
        if (u.isPresent()){
            return u.get();
        }
        else throw new NotFoundUserException("Auth error on findByLogin method (UserServiceImpl.java");
    }

    /**
     * Получение деталей о пользователе
     * @return пользователь по имени
     */
    @Override
    public UserDetailsService userDetailsService() {
        return this::getUserFromLogin;
    }

    /**
     * Сохранение данных о пользователе
     * @param user тело пользователя
     * @return пользователь
     */
    @Override
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Получение пользователей группы
     * @param groupId по ID группы
     * @return список пользователей
     */
    @Override
    public List<User> getGroupUsers(long groupId) {
        return userRepository.getAllByGroup_Id(groupId);
    }

    @Override
    public void deleteUsersWithGroups(Group group) {
        userRepository.deleteAllByGroupAndRole(group, STUDENT);
    }

    /**
     * Проверка на существование
     * @param login по логину
     * @return пользователь
     */
    @Override
    public boolean isUserExist(String login) {
        return userRepository.existsByLogin(login);
    }

    /**
     * Статистика по недавним зарегистрированным пользователям
     * @return список имен
     */
    @Override
    public List<String> getStatisticForRecentUsers() {
        Pageable paging = PageRequest.of(0, 20, Sort.by("id").descending());
        Page<User> users = userRepository.findAll(paging);
        return users.stream().map(User::getName).toList();
    }

}
