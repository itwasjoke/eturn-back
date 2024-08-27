package com.eturn.eturn.service.impl;

import com.eturn.eturn.dto.FacultyDTO;
import com.eturn.eturn.dto.mapper.FacultyMapper;
import com.eturn.eturn.entity.Faculty;
import com.eturn.eturn.repository.FacultyRepository;
import com.eturn.eturn.service.FacultyService;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class FacultyServiceImpl implements FacultyService {

    private final FacultyRepository facultyRepository;
    private final FacultyMapper facultyMapper;

    public FacultyServiceImpl(FacultyRepository facultyRepository, FacultyMapper facultyMapper) {
        this.facultyRepository = facultyRepository;
        this.facultyMapper = facultyMapper;
    }


    @Override
    public Faculty createFaculty(FacultyDTO facultyDTO) {
        Faculty f = facultyMapper.dtoToFaculty(facultyDTO);
        if (facultyRepository.existsById(f.getId())){
            Faculty existedFaculty = facultyRepository.getFacultyById(f.getId());
            if (!Objects.equals(existedFaculty.getName(), f.getName())){
                existedFaculty.setName(f.getName());
                return facultyRepository.save(existedFaculty);
            }
            else{
                return existedFaculty;
            }
        }
        else{
            return facultyRepository.save(f);
        }

    }


}
