package com.eturn.eturn.repository;

import com.eturn.eturn.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    void deleteUserById(Long idUser);
    Optional<User> findUserByLogin(String login);

    boolean existsByLogin(String login);
}
