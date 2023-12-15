package com.eturn.eturn.service.impl;

import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.enums.RoleEnum;
import com.eturn.eturn.exception.NotFoundException;
import com.eturn.eturn.repository.UserRepository;
import com.eturn.eturn.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public RoleEnum checkRoot(Long id) {
        User user = userRepository.getReferenceById(id);
        return user.getRoleEnum();
    }

    @Override
    public User getUser(Long id) {
        Optional<User> u = userRepository.findById(id);
        if (u.isPresent()){
            return u.get();
        }
        else{
            throw new NotFoundException("Пользователя не существует");
        }
//        if (userRepository.existsById(id)){
//            return userRepository.getReferenceById(id);
//        }
//        else{
//            throw new NotFoundException("Пользователя не существует");
//        }
    }

    @Override
    public User getUserFrom(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()){
            return user.get();
        }
        else{
            throw new NotFoundException("Пользователя не существует");
        }
    }

    @Override
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public Set<Turn> getUserTurns(Long id) {
        User user = userRepository.getReferenceById(id);
        return user.getTurns();
    }
}
