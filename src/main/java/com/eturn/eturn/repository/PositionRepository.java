package com.eturn.eturn.repository;

import com.eturn.eturn.entity.Position;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {

    Optional<Position> findFirstByUserAndTurnOrderByNumberDesc(User user,Turn turn);
    List<Position> getPositionByTurn(Turn turn);
    Page<Position> findAllByTurn(Turn turn,Pageable pageable);

    Optional<Position> findTopByTurnOrderByNumberDesc(Turn turn); //findLast
    Optional<Position> findFirstByTurnOrderByNumber(Turn turn); //findLast

    Optional<Position> findFirstByTurn(Turn turn);

    Optional<Position> findTopByTurnAndUser(Turn turn, User user);

    long countByTurn(Turn turn);

}
