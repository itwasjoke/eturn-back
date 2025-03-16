package com.eturn.eturn.repository;

import com.eturn.eturn.entity.Counter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CounterRepository extends JpaRepository<Counter, Long> {
    Optional<Counter> findCounterById(String id);
}
