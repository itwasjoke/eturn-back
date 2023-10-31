package com.eturn.eturn.repo;

import com.eturn.eturn.domain.Turn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface TrnRepo extends JpaRepository<Turn, Long> {
    List<Turn> findByIdUser(Long id_user);
    void deleteByIdUser(Long id_user);

    boolean existsByIdUser(Long id_user);

}
