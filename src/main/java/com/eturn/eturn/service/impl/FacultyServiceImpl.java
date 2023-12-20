package com.eturn.eturn.service.impl;

import com.eturn.eturn.entity.Faculty;
import com.eturn.eturn.exception.InvalidDataException;
import com.eturn.eturn.exception.NotFoundException;
import com.eturn.eturn.repository.FacultyRepository;
import com.eturn.eturn.service.FacultyService;
import org.springframework.stereotype.Service;

@Service
public class FacultyServiceImpl implements FacultyService {

    private final FacultyRepository facultyRepository;

    public FacultyServiceImpl(FacultyRepository facultyRepository) {
        this.facultyRepository = facultyRepository;
    }

    @Override
    public Faculty getOneFaculty(Long id) {
        if (facultyRepository.existsById(id)) {
            return facultyRepository.getReferenceById(id);
        } else {
            throw new NotFoundException("Факультет не найден");
        }
    }

    @Override
    public Long createFaculty(Faculty faculty) {
       if (facultyRepository.existsByName(faculty.getName())){
           Faculty f = facultyRepository.save(faculty);
           return f.getId();
       }
       else{
           throw new InvalidDataException("Факультет уже существует");
       }
    }
}
