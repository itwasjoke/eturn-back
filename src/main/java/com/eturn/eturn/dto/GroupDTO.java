package com.eturn.eturn.dto;

public record GroupDTO(
        Long id, String name
) {
    public GroupDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
