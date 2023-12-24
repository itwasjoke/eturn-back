package com.eturn.eturn.repository;

import com.eturn.eturn.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    void deleteUserById(Long idUser);
    User findUserByLogin(String login);
}
