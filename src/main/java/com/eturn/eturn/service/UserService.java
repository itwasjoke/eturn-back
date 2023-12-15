package com.eturn.eturn.service;

import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.enums.RoleEnum;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

public interface UserService {
    RoleEnum checkRoot(Long id);
    User getUser(Long id);
    User getUserFrom(Long id);
    User createUser(User user);

    Set<Turn> getUserTurns(Long id);
}
