package com.eturn.eturn.dto;

import com.eturn.eturn.enums.RoleEnum;

public record UserDTO(
        Long id,
        String name,
        String role,
        String faculty,
        String group,
        String course,
        String department
) {
    public UserDTO(Long id, String name, String role, String faculty, String group, String course, String department) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.faculty = faculty;
        this.group = group;
        this.course = course;
        this.department = department;
    }

}
