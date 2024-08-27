package com.eturn.eturn.service;

import com.eturn.eturn.dto.UserDTO;
import com.eturn.eturn.entity.Faculty;
import com.eturn.eturn.entity.Group;
import com.eturn.eturn.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserDTO getUser(String login);
    Optional<User> getUser(Long id);
    User createUser(User user);
    User findByLogin(String login);
    UserDetailsService userDetailsService();
    User updateUser(User user);
    List<User> getGroupUsers(long groupId);
}
