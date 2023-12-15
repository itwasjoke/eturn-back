package com.eturn.eturn.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

public record TurnDTO(
        Long id,
        @Size(min = 1, max = 255)
        @NotNull
        String name,
        String description,
        int countUsers,
        String creator,
        long userId
) {
    public TurnDTO(Long id, String name, String description, int countUsers, String creator, long userId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.countUsers = countUsers;
        this.creator = creator;
        this.userId = userId;
    }
}
