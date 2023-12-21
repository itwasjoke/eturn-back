package com.eturn.eturn.controller;

import com.eturn.eturn.dto.FacultyDTO;
import com.eturn.eturn.service.FacultyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/faculty", produces = "application/json; charset=utf-8")
public class FacultyController {
    private final FacultyService facultyService;

    public FacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @GetMapping
    private List<FacultyDTO> getAll(){
        return facultyService.getAllList();
    }
    @PostMapping
    private Long create(@RequestBody FacultyDTO dto){
        return facultyService.createFaculty(dto);
    }
}
