package com.eturn.eturn.service.impl;

import com.eturn.eturn.dto.mapper.FacultyMapper;
import com.eturn.eturn.entity.Faculty;
import com.eturn.eturn.exception.faculty.AlreadyExistFacultyException;
import com.eturn.eturn.exception.faculty.NotFoundFacultyException;
import com.eturn.eturn.repository.FacultyRepository;
import com.eturn.eturn.security.EtuIdUser;
import com.eturn.eturn.security.FacultyResponse;
import com.eturn.eturn.service.FacultyService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Service
public class FacultyServiceImpl implements FacultyService {
    @Value("${faculties}")
    private String externalApiUrl;
    private final FacultyRepository facultyRepository;
    private final FacultyMapper facultyMapper;
    private final RestTemplate restTemplate;
    public FacultyServiceImpl(FacultyRepository facultyRepository, FacultyMapper facultyMapper, RestTemplate restTemplate) {
        this.facultyRepository = facultyRepository;
        this.facultyMapper = facultyMapper;
        this.restTemplate = restTemplate;
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
    public void createFaculty() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<List<FacultyResponse>> response = restTemplate.exchange(
                externalApiUrl,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<FacultyResponse>>(){}
        );
        if (response.getStatusCode().is2xxSuccessful() && !response.getBody().isEmpty()) {
            List<FacultyResponse> facultyResponses = response.getBody();
            // Получаем первый элемент массива
            // ... остальной код
        } else {
            throw new RuntimeException("Ошибка аутентификации на внешнем сервере");
        }
//       if (!facultyRepository.existsByName(dto.name())){
//           Faculty f = facultyRepository.save(facultyMapper.DTOtoFaculty(dto));
//           return f.getId();
//       }
//       else{
//           throw new AlreadyExistFacultyException("Cannot create Faculty because it exists");
//       }
    }

    @Override
    public Faculty getFacultyForUser(String faculty) {
        getOneFacultyOptional(faculty);
    }
}
