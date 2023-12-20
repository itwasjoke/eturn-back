package com.eturn.eturn.service;

import com.eturn.eturn.dto.CourseDTO;
import com.eturn.eturn.entity.Course;
import org.springframework.stereotype.Service;

import java.util.List;


public interface CourseService {
    Course getOneCourse(Long id);

    Long createCourse(CourseDTO dto);

    List<CourseDTO> getAllList();
}
