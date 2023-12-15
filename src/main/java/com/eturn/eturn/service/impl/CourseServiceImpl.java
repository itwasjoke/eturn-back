package com.eturn.eturn.service.impl;

import com.eturn.eturn.entity.Course;
import com.eturn.eturn.repository.CourseRepository;
import com.eturn.eturn.service.CourseService;
import org.springframework.stereotype.Service;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    public CourseServiceImpl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    public Course getOneCourse(Long id) {
        return courseRepository.getReferenceById(id);
    }
}
