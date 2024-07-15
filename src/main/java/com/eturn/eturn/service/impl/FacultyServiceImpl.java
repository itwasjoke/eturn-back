package com.eturn.eturn.service.impl;

import com.eturn.eturn.dto.FacultyDTO;
import com.eturn.eturn.dto.mapper.FacultyListMapper;
import com.eturn.eturn.dto.mapper.FacultyMapper;
import com.eturn.eturn.entity.Faculty;
import com.eturn.eturn.exception.faculty.AlreadyExistFacultyException;
import com.eturn.eturn.exception.faculty.NotFoundFacultyException;
import com.eturn.eturn.repository.FacultyRepository;
import com.eturn.eturn.service.FacultyService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
            throw new NotFoundFacultyException("Cannot get faculty by ID.");
        }
    }

    @Transactional
    @Override
    public Faculty getOneFacultyOptional(String faculty) {
        Optional<Faculty> facultyFrom = facultyRepository.getFacultyByName(faculty);
        if (facultyFrom.isPresent()) {
            return facultyFrom.get();
        }
        else{
            Faculty f = new Faculty();
            f.setName(faculty);
            return facultyRepository.save(f);
        }
    }

    @Override
    public Long createFaculty(FacultyDTO dto) {
       if (!facultyRepository.existsByName(dto.name())){
           Faculty f = facultyRepository.save(facultyMapper.DTOtoFaculty(dto));
           return f.getId();
       }
       else{
           throw new AlreadyExistFacultyException("Cannot create Faculty because it exists");
       }
    }

    @Override
    public List<FacultyDTO> getAllList() {
        List<Faculty> f = facultyRepository.findAll();
        if (f.isEmpty()){
            throw new NotFoundFacultyException("Cannot found any faculties");
        }
        return facultyListMapper.map(f);
    }
}
