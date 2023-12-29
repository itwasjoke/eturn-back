package com.eturn.eturn.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PositionMoreInfoDTO(
        Long id,
        @Size(min = 1, max = 255)
        @NotNull
        String name,
        String group,
        boolean start,
        int number,
        Long userId,
        int difference
) {
    public PositionMoreInfoDTO(Long id, @Size(min = 1, max = 255)
    @NotNull
    String name, String group, boolean start, int number, Long userId, int difference) {
        this.id = id;
        this.name = name;
        this.group = group;
        this.start = start;
        this.number = number;
        this.userId = userId;
        this.difference = difference;
    }
}
