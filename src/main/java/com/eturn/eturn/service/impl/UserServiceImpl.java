package com.eturn.eturn.service.impl;

import com.eturn.eturn.dto.GroupDTO;
import com.eturn.eturn.dto.UserDTO;
import com.eturn.eturn.dto.mapper.UserMapper;
import com.eturn.eturn.entity.*;
import com.eturn.eturn.enums.RoleEnum;
import com.eturn.eturn.exception.user.AuthPasswordException;
import com.eturn.eturn.exception.user.LocalNotFoundUserException;
import com.eturn.eturn.exception.user.NotFoundUserException;
import com.eturn.eturn.repository.UserRepository;
import com.eturn.eturn.service.FacultyService;
import com.eturn.eturn.service.GroupService;
import com.eturn.eturn.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserDTO getUser(String login) {
        Optional<User> u = userRepository.findUserByLogin(login);
        if (u.isPresent()) {
            User user = u.get();
            String group = null;
            String faculty = null;
            if (user.getRoleEnum() == RoleEnum.STUDENT) {
                group = user.getGroup().getNumber();
                faculty = user.getGroup().getFaculty().getName();
            }
            String role = null;
            RoleEnum R = user.getRoleEnum();
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
    public User getUserFrom(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            return user.get();
        } else {
            throw new LocalNotFoundUserException("No user in database on getUserMethod");
        }
    }

    @Override
    public Optional<User> getUser(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public User createUser(User user) {
        if (userRepository.existsByLogin(user.getLogin())){
            throw new RuntimeException("This user already exists");
        }
        return userRepository.save(user);
    }

    @Override
    public User findByLogin(String login) {
        Optional<User> u = userRepository.findUserByLogin(login);
        if (u.isPresent()){
            return u.get();
        }
        else throw new NotFoundUserException("Auth error on findByLogin method (UserServiceImpl.java");
    }



    @Override
    public UserDetailsService userDetailsService() {
        return this::findByLogin;
    }

}
