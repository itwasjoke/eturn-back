package com.eturn.eturn.dto;

public record UserCreateDTO(
        String name, String role, Long facultyId, Long groupId, Long courseId, Long departmentId
) {
    public UserCreateDTO(String name, String role, Long facultyId, Long groupId, Long courseId, Long departmentId) {
        this.name = name;
        this.role = role;
        this.facultyId = facultyId;
        this.groupId = groupId;
        this.courseId = courseId;
        this.departmentId = departmentId;
    }
}
