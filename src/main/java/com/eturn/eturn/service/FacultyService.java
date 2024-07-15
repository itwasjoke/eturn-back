package com.eturn.eturn.service;

import com.eturn.eturn.dto.FacultyDTO;
import com.eturn.eturn.entity.Faculty;
import com.eturn.eturn.repository.FacultyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


public interface FacultyService {

    Faculty getOneFaculty(Long id);

    Faculty getOneFacultyOptional(String faculty);

    Long createFaculty(FacultyDTO dto);

    List<FacultyDTO> getAllList();

}
