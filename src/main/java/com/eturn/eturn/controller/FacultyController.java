package com.eturn.eturn.controller;

import com.eturn.eturn.dto.FacultyDTO;
import com.eturn.eturn.entity.Faculty;
import com.eturn.eturn.service.FacultyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/faculty", produces = "application/json; charset=utf-8")
@Tag(name = "Факультеты", description = "Обновление списка факультетов")
public class FacultyController {
    private final FacultyService facultyService;

    public FacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @PostMapping
//    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(
            summary = "Создание факультета",
            description = "Создает все факультет"
    )
    private void create(@RequestBody FacultyDTO facultyDTO){
        facultyService.createFaculty(facultyDTO);
    }
}
