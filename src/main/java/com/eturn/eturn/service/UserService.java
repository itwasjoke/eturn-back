package com.eturn.eturn.service;

import com.eturn.eturn.dto.UserDTO;
import com.eturn.eturn.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserDTO getUserDTOFromLogin(String login);
    Optional<User> getOptionalUserFromId(Long id);
    User createUser(User user);
    User getUserFromLogin(String login);
    UserDetailsService userDetailsService();
    User updateUser(User user);
    List<User> getGroupUsers(long groupId);

    boolean isUserExist(String login);
}
