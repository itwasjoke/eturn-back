package com.eturn.eturn.repository;

import com.eturn.eturn.entity.Turn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface TurnRepository extends JpaRepository<Turn, Long> {
    List<Turn> findByIdUser(Long id_user);
    void deleteByIdUser(Long id_user);

    boolean existsByIdUser(Long id_user);

}
