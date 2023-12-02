package com.eturn.eturn.service;

import com.eturn.eturn.entity.Faculty;
import com.eturn.eturn.repository.FacultyRepository;
import org.springframework.stereotype.Service;

@Service
public interface FacultyService {

    Faculty getOneFaculty(Long id);

}
