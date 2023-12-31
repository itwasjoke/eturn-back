package com.eturn.eturn.service.impl;

import com.eturn.eturn.dto.FacultyDTO;
import com.eturn.eturn.dto.mapper.FacultyListMapper;
import com.eturn.eturn.dto.mapper.FacultyMapper;
import com.eturn.eturn.entity.Faculty;
import com.eturn.eturn.exception.InvalidDataException;
import com.eturn.eturn.exception.NotFoundException;
import com.eturn.eturn.repository.FacultyRepository;
import com.eturn.eturn.service.FacultyService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FacultyServiceImpl implements FacultyService {

    private final FacultyRepository facultyRepository;
    private final FacultyMapper facultyMapper;
    private final FacultyListMapper facultyListMapper;

    public FacultyServiceImpl(FacultyRepository facultyRepository, FacultyMapper facultyMapper, FacultyListMapper facultyListMapper) {
        this.facultyRepository = facultyRepository;
        this.facultyMapper = facultyMapper;
        this.facultyListMapper = facultyListMapper;
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
    public Long createFaculty(FacultyDTO dto) {
       if (!facultyRepository.existsByName(dto.name())){
           Faculty f = facultyRepository.save(facultyMapper.DTOtoFaculty(dto));
           return f.getId();
       }
       else{
           throw new InvalidDataException("Факультет уже существует");
       }
    }

    @Override
    public List<FacultyDTO> getAllList() {
        return facultyListMapper.map(facultyRepository.findAll());
    }
}
