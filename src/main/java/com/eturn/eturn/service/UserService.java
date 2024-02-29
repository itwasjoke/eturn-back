package com.eturn.eturn.service;

import com.eturn.eturn.dto.TurnDTO;
import com.eturn.eturn.dto.UserCreateDTO;
import com.eturn.eturn.dto.UserDTO;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.enums.RoleEnum;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

public interface UserService {
    RoleEnum checkRoot(Long id);
    UserDTO getUser(String login);
    User getUserFrom(Long id);
    Long createUser(UserCreateDTO user);

    Set<Turn> getUserTurns(Long id);

    void updateUser(User user);

    User findByLogin(String login);

    Long loginUser(String username, String password);
//    void addTurn(Long userId, Long turnId);

    UserDetailsService userDetailsService();

    User getCurrentUser();


}
