package com.eturn.eturn.repository;

import com.eturn.eturn.entity.Position;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {

    Optional<Position> findFirstByUserAndTurn(User user,Turn turn);
    Page<Position> findAllByTurnOrderByIdAsc(Turn turn,Pageable pageable);

    boolean existsAllByTurnAndUser(Turn turn, User user);

    @Modifying
    @Query("delete from Position p where p.turn=:turn and p.number<=:number")
    void tryToDelete(@Param("turn") Turn turn, @Param("number") int number);
    Page<Position> findByTurnOrderByIdDesc(Turn turn, Pageable page);

    Optional<Position> findFirstByTurnOrderByIdAsc(Turn turn);

    Optional<Position> findTopByTurnAndUserOrderByIdAsc(Turn turn, User user);

    long countByTurn(Turn turn);

    void deletePositionsByUserAndTurn(User user, Turn turn);
    void deleteAllByTurnAndUser(Turn turn, User user);
    @Query(value = "select count(*) from Position p where p.turn = :t and p.number < :num")
    long countNumbersLeft(@Param("num") int num, @Param("t") Turn t);


    // TODO Сделать, чтобы он считал количество позиций по 2 минутам и удалял нужное количество позиций. Изменить запрос и добавить id до.

}
