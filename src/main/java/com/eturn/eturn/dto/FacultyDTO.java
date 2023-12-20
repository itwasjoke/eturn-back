package com.eturn.eturn.dto;

public record FacultyDTO(
        Long id, String name
) {
    public FacultyDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
