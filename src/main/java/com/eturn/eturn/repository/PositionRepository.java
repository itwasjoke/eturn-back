package com.eturn.eturn.repository;

import com.eturn.eturn.entity.Position;
import com.eturn.eturn.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {

    Position findFirstByUserByOrderByCreatedAtDesc(User user);

}
