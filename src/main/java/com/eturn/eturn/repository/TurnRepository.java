package com.eturn.eturn.repository;

import com.eturn.eturn.entity.Turn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface TurnRepository extends JpaRepository<Turn, Long> {
    boolean existsTurnById(Long id);

    void deleteTurnById(Long id);
}
