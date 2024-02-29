package com.eturn.eturn.controller;

import com.eturn.eturn.dto.CourseDTO;
import com.eturn.eturn.service.CourseService;
//import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/course", produces = "application/json; charset=utf-8")
@Tag(name = "Курсы", description = "Обновление списка курсов")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

//    @GetMapping
////    @PreAuthorize("hasRole('EMPLOYEE')")
////    @PreAuthorize("GRANT(MEMBER)")
//    public List<CourseDTO> getAllList(){
//        return courseService.getAllList();
//    }

    @PostMapping
//    @PreAuthorize("hasRole('EMPLOYEE')")
//    @PreAuthorize("GRANT(ADMIN)")
    @Operation(
            summary = "Создание курса",
            description = "Создает новый объект с номером курса и его программой обучения"
    )
    public Long create(@RequestBody CourseDTO dto){
        return courseService.createCourse(dto);
    }

}
