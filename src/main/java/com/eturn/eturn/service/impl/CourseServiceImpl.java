package com.eturn.eturn.service.impl;

import com.eturn.eturn.dto.CourseDTO;
import com.eturn.eturn.dto.mapper.CourseListMapper;
import com.eturn.eturn.dto.mapper.CourseMapper;
import com.eturn.eturn.entity.Course;
import com.eturn.eturn.exception.InvalidDataException;
import com.eturn.eturn.repository.CourseRepository;
import com.eturn.eturn.service.CourseService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final CourseListMapper courseListMapper;

    public CourseServiceImpl(CourseRepository courseRepository, CourseMapper courseMapper, CourseListMapper courseListMapper) {
        this.courseRepository = courseRepository;
        this.courseMapper = courseMapper;
        this.courseListMapper = courseListMapper;
    }

    @Override
    public Course getOneCourse(Long id) {
        return courseRepository.getReferenceById(id);
    }

    @Override
    public Long createCourse(CourseDTO dto) {
        if (!courseRepository.existsByNumberAndEduEnum(Integer.parseInt(dto.number()), dto.eduEnum())){
            Course c = courseRepository.save(courseMapper.DTOtoCourse(dto));
            return c.getId();
        }
        else{
            throw new InvalidDataException("Такой курс уже существует");
        }
    }

    @Override
    public List<CourseDTO> getAllList() {
        return courseListMapper.map(courseRepository.findAll());
    }
}
