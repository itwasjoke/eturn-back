package com.eturn.eturn.service;

import com.eturn.eturn.dto.FacultyDTO;
import com.eturn.eturn.dto.FacultyWithGroupsDTO;
import com.eturn.eturn.entity.Faculty;
import com.eturn.eturn.entity.Group;

import java.util.List;
import java.util.Set;


public interface FacultyService {
    Faculty createFaculty(FacultyDTO facultyDTO);
    List<FacultyWithGroupsDTO> getGroups();
}
