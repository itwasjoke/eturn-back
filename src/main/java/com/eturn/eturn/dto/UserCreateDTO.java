package com.eturn.eturn.dto;

public record UserCreateDTO(
        String name, String role, Long facultyId, Long groupId, Long courseId, Long departmentId, String login, String password
) {
}
