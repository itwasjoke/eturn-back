package com.eturn.eturn.service.impl;

import com.eturn.eturn.entity.Faculty;
import com.eturn.eturn.repository.FacultyRepository;
import com.eturn.eturn.service.FacultyService;

public class FacultyServiceImpl implements FacultyService {

    FacultyRepository facultyRepository;
    @Override
    public Faculty getOneFaculty(Long id) {
        return facultyRepository.getReferenceById(id);
    }
}
