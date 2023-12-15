package com.eturn.eturn.repository;

import com.eturn.eturn.entity.Position;
import com.eturn.eturn.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {

    Optional<Position> findFirstByUserOrderByNumberDesc(User user);

    // TODO Page<Position> findAllByUser(User user, Pageable pageable);
}
