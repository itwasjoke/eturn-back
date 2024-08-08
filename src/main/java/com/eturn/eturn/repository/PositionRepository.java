package com.eturn.eturn.repository;

import com.eturn.eturn.entity.Position;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {

    Optional<Position> findFirstByUserAndTurnOrderByNumberAsc(User user, Turn turn);
    Optional<Position> findFirstByUserAndTurnOrderByNumberDesc(User user, Turn turn);
    Page<Position> findAllByTurnOrderByNumberAsc(Turn turn, Pageable pageable);
    Optional<Position> findFirstByTurnOrderByIdDesc(Turn turn);

    boolean existsAllByTurnAndUser(Turn turn, User user);

    @Query(name = "getPositionForDelete")
    Position resultsPositionDelete(@Param("turn") Long turn, @Param("count") int count);

    @Query(name = "getPositionForDeleteOverdueElements")
    Position resultsPositionDeleteOverdueElements(@Param("turn") Long turn, @Param("count") int count);

    Optional<Position> findFirstByTurnOrderByNumberAsc(Turn turn);
    Optional<Position> findTopByTurnAndUserOrderByNumberAsc(Turn turn, User user);
    long countByTurn(Turn turn);
    void deletePositionsByUserAndTurn(User user, Turn turn);
    void deleteAllByTurnAndUser(Turn turn, User user);
    @Query(value = "select count(*) from Position p where p.turn = :t and p.number < :num")
    long countNumbersLeft(@Param("num") int num, @Param("t") Turn t);
    int countAllByTurn(Turn turn);
    int countAllByNumberBetween(int number1, int number2);
    void deleteByTurnAndNumberLessThanEqual(Turn turn, int number);
    Optional<Position> findFirstByTurnAndUserAndNumberGreaterThanOrderByNumberDesc(Turn turn, User user, int number);
    Optional<Position> findFirstByTurnAndNumberGreaterThan(Turn turn, int number);
    Optional<Position> findFirstByTurnAndNumberLessThanOrderByNumberDesc(Turn turn, int number);

    // TODO Сделать, чтобы он считал количество позиций по 2 минутам и удалял нужное количество позиций. Изменить запрос и добавить id до.

}
