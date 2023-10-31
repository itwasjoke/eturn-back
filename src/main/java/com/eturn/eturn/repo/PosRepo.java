package com.eturn.eturn.repo;

import com.eturn.eturn.domain.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface PosRepo extends JpaRepository<Position,Long> {

    List<Position> findByIdTurn(Long id_turn);
    List<Position> findByIdUserAndIdTurn(Long id_user, Long id_turn);

    void deleteByIdTurn(Long idTurn);
    void deleteByIdUser(Long idUser);
    void deleteByIdUserAndIdTurn(Long id_user, Long id_turn);
}
