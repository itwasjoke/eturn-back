package com.eturn.eturn.service.impl;

import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.enums.RoleEnum;
import com.eturn.eturn.repository.UserRepository;
import com.eturn.eturn.service.UserService;

import java.util.List;
import java.util.UUID;

public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    @Override
    public RoleEnum checkRoot(Long id) {
        User user = userRepository.getReferenceById(id);
        return user.getRoleEnum();
    }

    @Override
    public User getUser(Long id) {
        return userRepository.getReferenceById(id);
    }

    @Override
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public List<Turn> getUserTurns(Long id) {
        User user = userRepository.getReferenceById(id);
        return user.getTurnList();
    }
}
