package com.eturn.eturn.controller;

import com.eturn.eturn.dto.FacultyDTO;
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

    @GetMapping
    @Operation(
            summary = "Получение факультетов",
            description = "Возвращает список групп"
    )
    private List<FacultyDTO> getAll(){
        return facultyService.getAllList();
    }
    @PostMapping
//    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(
            summary = "Создание факультета",
            description = "Создает новый факультет"
    )
    private Long create(@RequestBody FacultyDTO dto){
        return facultyService.createFaculty(dto);
    }
}
