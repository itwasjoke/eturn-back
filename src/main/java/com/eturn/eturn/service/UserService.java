package com.eturn.eturn.service;

import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.enums.RoleEnum;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface UserService {
    RoleEnum checkRoot(Long id);
    User getUser(Long id);
    User createUser(User user);

    List<Turn> getUserTurns(Long id);
}
