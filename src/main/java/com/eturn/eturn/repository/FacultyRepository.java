package com.eturn.eturn.repository;

import com.eturn.eturn.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty,Long> {
 boolean existsById(Long id);
 Faculty getFacultyById(Long id);
}
