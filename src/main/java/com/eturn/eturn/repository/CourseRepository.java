package com.eturn.eturn.repository;

import com.eturn.eturn.entity.Course;
import com.eturn.eturn.enums.EduEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course,Long> {
    boolean existsByNumberAndEduEnum(int number, EduEnum eduEnum);
}
