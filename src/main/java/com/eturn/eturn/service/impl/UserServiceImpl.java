package com.eturn.eturn.service.impl;

import com.eturn.eturn.dto.UserDTO;
import com.eturn.eturn.dto.mapper.UserMapper;
import com.eturn.eturn.entity.*;
import com.eturn.eturn.enums.Role;
import com.eturn.eturn.exception.user.NotFoundUserException;
import com.eturn.eturn.repository.UserRepository;
import com.eturn.eturn.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LogManager.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Value("${eturn.defaults.username}")
    private String username;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserDTO getUserDTOFromLogin(String login) {
        Optional<User> u = userRepository.findUserByLogin(login);
        if (u.isPresent()) {
            User user = u.get();
            String group = null;
            String faculty = null;
            if (user.getRole() == Role.STUDENT) {
                group = user.getGroup().getNumber();
                faculty = user.getGroup().getFaculty().getName();
            }
            String role = null;
            Role R = user.getRole();
            switch (R) {
                case STUDENT -> role = "Студент";
                case EMPLOYEE -> role = "Сотрудник";
            }
            return userMapper.userToUserDTO(user, faculty, group, role);
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
        if (user.getRole() == Role.ADMIN && !user.getLogin().equals(username)){
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

}
