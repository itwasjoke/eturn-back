package com.eturn.eturn.repository;

import com.eturn.eturn.entity.Faculty;
import com.eturn.eturn.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty,Long> {
 Optional<Faculty> getFacultyByName(String name);
 Faculty getFacultyById(Long id);
}
