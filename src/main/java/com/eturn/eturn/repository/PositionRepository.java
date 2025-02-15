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

    Optional<Position> findFirstByUserAndTurnOrderByIdAsc(User user, Turn turn);
    Optional<Position> findFirstByUserAndTurnOrderByIdDesc(User user, Turn turn);
    Page<Position> findAllByTurnOrderByIdAsc(Turn turn, Pageable pageable);
    Page<Position> findAllByTurn_IdOrderByIdAsc(Long id, Pageable pageable);
    Optional<Position> findFirstByTurnOrderByIdDesc(Turn turn);
    boolean existsAllByTurnAndUser(Turn turn, User user);
    @Query(name = "getPositionForDelete")
    Position resultsPositionDelete(@Param("turn") Long turn, @Param("count") int count);
    @Query(name = "getPositionForDeleteOverdueElements")
    Position resultsPositionDeleteOverdueElements(@Param("turn") Long turn, @Param("count") int count);
    Optional<Position> findFirstByTurnOrderByIdAsc(Turn turn);
    Optional<Position> findTopByTurnAndUserOrderByIdAsc(Turn turn, User user);
    long countByTurn(Turn turn);
    void deletePositionsByUserAndTurn(User user, Turn turn);
    void deleteAllByTurnAndUser(Turn turn, User user);
    @Query(value = "select count(*) from Position p where p.turn = :t and p.id < :id")
    long countIdLeft(@Param("id") long id, @Param("t") Turn t);
    int countAllByTurn(Turn turn);
    int countAllByTurnAndIdBetween(Turn turn, long id1, long id2);
    void deleteByTurnAndIdLessThanEqual(Turn turn, long id);
    Optional<Position> findFirstByTurnAndUserAndIdGreaterThanOrderByIdDesc(Turn turn, User user, long Id);
    Optional<Position> findFirstByTurnAndIdGreaterThanOrderByIdAsc(Turn turn, long id);
    Optional<Position> findFirstByTurnAndIdLessThanOrderByIdDesc(Turn turn, long id);
}
