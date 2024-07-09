package com.eturn.eturn.repository;

import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import jakarta.persistence.SqlResultSetMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface TurnRepository extends JpaRepository<Turn, Long> {
    boolean existsTurnById(Long id);
    @Query(name = "getMemberOutTurns")
    List<Object[]> resultsMemberOut(@Param("userId") Long userId, @Param("groupId") Long groupId, @Param("facultyId") Long facultyId, @Param("turnType") String turnType);
    @Query(name = "getMemberInTurns")
    List<Object[]> resultsMemberIn(@Param("userId") Long userId, @Param("turnType") String turnType);
    void deleteTurnById(Long id);
}
