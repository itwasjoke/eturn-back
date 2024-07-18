package com.eturn.eturn.repository;

import com.eturn.eturn.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty,Long> {
 boolean existsById(Long id);
 boolean existsByName(String name);

 Faculty getFacultyById(Long id);
 Optional<Faculty> getFacultyByName(String name);
}
