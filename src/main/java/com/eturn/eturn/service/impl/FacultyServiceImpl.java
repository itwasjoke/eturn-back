package com.eturn.eturn.service.impl;

import com.eturn.eturn.dto.FacultyDTO;
import com.eturn.eturn.dto.FacultyWithGroupsDTO;
import com.eturn.eturn.dto.mapper.FacultyMapper;
import com.eturn.eturn.dto.mapper.FacultyWithGroupsListMapper;
import com.eturn.eturn.entity.Faculty;
import com.eturn.eturn.repository.FacultyRepository;
import com.eturn.eturn.service.FacultyService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
/**
 * Работа с учебными факультетами
 */
@Service
public class FacultyServiceImpl implements FacultyService {

    private final FacultyRepository facultyRepository;
    private final FacultyMapper facultyMapper;
    private final FacultyWithGroupsListMapper facultyWithGroupsListMapper;

    public FacultyServiceImpl(
            FacultyRepository facultyRepository,
            FacultyMapper facultyMapper,
            FacultyWithGroupsListMapper facultyWithGroupsListMapper
    ) {
        this.facultyRepository = facultyRepository;
        this.facultyMapper = facultyMapper;
        this.facultyWithGroupsListMapper = facultyWithGroupsListMapper;
    }

    /**
     * Создание факультета из API
     * @param facultyDTO факультет
     * @return созданный или существующий факультет
     */
    @Override
    public Faculty createFaculty(FacultyDTO facultyDTO) {
        Faculty f = facultyMapper.dtoToFaculty(facultyDTO);
        Optional<Faculty> optionalFaculty =
                facultyRepository.getFacultyByName(f.getName());
        if (optionalFaculty.isEmpty()) {
            f.setId(null);
            return facultyRepository.save(f);
        }
        return optionalFaculty.get();
    }

    /**
     * Получение всех групп с помощью списка факультетов
     * @return список факультетов с вложенными группами
     */
    @Override
    @Cacheable(value = "groups", key = "getGroupsCacheEturn")
    public List<FacultyWithGroupsDTO> getGroups() {
        List<Faculty> faculties = facultyRepository.findAll();
        return facultyWithGroupsListMapper.map(faculties);
    }

}
