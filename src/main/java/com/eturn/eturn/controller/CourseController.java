package com.eturn.eturn.controller;

import com.eturn.eturn.dto.CourseDTO;
import com.eturn.eturn.service.CourseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/course", produces = "application/json; charset=utf-8")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public List<CourseDTO> getAllList(){
        return courseService.getAllList();
    }

    @PostMapping
    public Long create(@RequestBody CourseDTO dto){
        return courseService.createCourse(dto);
    }

}
