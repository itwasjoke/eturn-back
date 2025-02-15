package com.eturn.eturn.service.impl;

import com.eturn.eturn.dto.UserDTO;
import com.eturn.eturn.dto.mapper.UserMapper;
import com.eturn.eturn.entity.*;
import com.eturn.eturn.enums.Role;
import com.eturn.eturn.exception.user.NotFoundUserException;
import com.eturn.eturn.repository.UserRepository;
import com.eturn.eturn.service.TurnService;
import com.eturn.eturn.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Lazy;

import java.util.List;
import java.util.Optional;

import static com.eturn.eturn.enums.Role.ADMIN;
import static com.eturn.eturn.enums.Role.STUDENT;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LogManager.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final TurnService turnService;

    @Value("${eturn.defaults.username}")
    private String username;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, @Lazy TurnService turnService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.turnService = turnService;
    }

    @Override
    public UserDTO getUserDTOFromLogin(String login) {
        Optional<User> u = userRepository.findUserByLogin(login);
        if (u.isPresent()) {
            User user = u.get();
            String group = null;
            String faculty = null;
            if (user.getRole() == STUDENT) {
                group = user.getGroup().getNumber();
                faculty = user.getGroup().getFaculty().getName();
            }
            String role = null;
            Role R = user.getRole();
            int countTurns = turnService.getCountTurnsOfUser(user);
            switch (R) {
                case STUDENT -> role = "Студент";
                case EMPLOYEE -> role = "Сотрудник";
            }
            int countAvailable = 5;
            switch (R) {
                case STUDENT -> countAvailable = 5 - countTurns;
                case EMPLOYEE -> countAvailable = 50 - countTurns;
            }
            if (countAvailable < 0) {
                countAvailable = 0;
            }
            return userMapper.userToUserDTO(user, faculty, group, role, countAvailable);
        } else {
            throw new NotFoundUserException("No user in database on getUser method (UserServiceImpl.java");
        }
    }

    @Override
    public Optional<User> getOptionalUserFromId(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public User createUser(User user) {
        if (userRepository.existsByLogin(user.getLogin())){
            throw new UsernameNotFoundException("No user");
        }
        if (user.getRole() == ADMIN && !user.getLogin().equals(username)){
            throw new UsernameNotFoundException("No user");
        }
        logger.info("user created");
        return userRepository.save(user);
    }

    @Override
    public User getUserFromLogin(String login) {
        Optional<User> u = userRepository.findUserByLogin(login);
        if (u.isPresent()){
            return u.get();
        }
        else throw new NotFoundUserException("Auth error on findByLogin method (UserServiceImpl.java");
    }
    @Override
    public UserDetailsService userDetailsService() {
        return this::getUserFromLogin;
    }

    @Override
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public List<User> getGroupUsers(long groupId) {
        return userRepository.getAllByGroup_Id(groupId);
    }

    @Override
    public boolean isUserExist(String login) {
        return userRepository.existsByLogin(login);
    }

}
