package com.eturn.eturn.service;

import com.eturn.eturn.entity.Course;
import org.springframework.stereotype.Service;


public interface CourseService {
    Course getOneCourse(Long id);
}
