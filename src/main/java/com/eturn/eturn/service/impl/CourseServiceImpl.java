package com.eturn.eturn.service.impl;

import com.eturn.eturn.entity.Course;
import com.eturn.eturn.repository.CourseRepository;
import com.eturn.eturn.service.CourseService;

public class CourseServiceImpl implements CourseService {

    private CourseRepository courseRepository;
    @Override
    public Course getOneCourse(Long id) {
        return courseRepository.getReferenceById(id);
    }
}
