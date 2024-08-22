package com.eturn.eturn.service;

import com.eturn.eturn.dto.FacultyDTO;
import com.eturn.eturn.entity.Faculty;


public interface FacultyService {
    Faculty createFaculty(FacultyDTO facultyDTO);
}
