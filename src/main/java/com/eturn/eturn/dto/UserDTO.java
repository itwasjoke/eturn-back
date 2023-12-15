package com.eturn.eturn.dto;

import com.eturn.eturn.enums.RoleEnum;

public record UserDTO(Long id, String name, RoleEnum role, Long facultyId, Long groupId, Long courseId, Long departmentId) {
    public UserDTO(Long id, String name, RoleEnum role, Long facultyId, Long groupId, Long courseId, Long departmentId) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.facultyId = facultyId;
        this.groupId = groupId;
        this.courseId = courseId;
        this.departmentId = departmentId;
    }
}
