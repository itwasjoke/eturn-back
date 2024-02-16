package com.eturn.eturn.service.impl;

import com.eturn.eturn.entity.Department;
import com.eturn.eturn.repository.DepartmentRepository;
import com.eturn.eturn.service.DepartmentService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    public Department getById(Long id) {
        Optional<Department> d = departmentRepository.findById(id);
        return d.orElse(null);
    }
}
