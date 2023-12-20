package com.eturn.eturn.service;

import com.eturn.eturn.dto.FacultyDTO;
import com.eturn.eturn.entity.Faculty;
import com.eturn.eturn.repository.FacultyRepository;
import org.springframework.stereotype.Service;

import java.util.List;


public interface FacultyService {

    Faculty getOneFaculty(Long id);

    Long createFaculty(FacultyDTO dto);

    List<FacultyDTO> getAllList();

}
