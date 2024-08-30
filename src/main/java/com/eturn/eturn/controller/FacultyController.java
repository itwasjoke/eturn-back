package com.eturn.eturn.controller;

import com.eturn.eturn.dto.FacultyWithGroupsDTO;
import com.eturn.eturn.service.FacultyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value ="/groups", produces = "application/json; charset=utf-8")
@Tag(name = "Группы и факультеты", description = "Получение групп и факультетов с https://digital.etu.ru/api/mobile/")
public class FacultyController {
    private final FacultyService facultyService;

    public FacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @GetMapping()
    @Operation(
            summary = "Получение факультетов",
            description = "Получает список факультетов с https://digital.etu.ru/api/mobile/groups"
    )
    public List<FacultyWithGroupsDTO> getGroups(){
        return facultyService.getGroups();
    }
}
