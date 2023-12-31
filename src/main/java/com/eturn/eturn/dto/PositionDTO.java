package com.eturn.eturn.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PositionDTO(
        Long id,
        @Size(min = 1, max = 255)
        @NotNull
        String name,
        String group,
        boolean start,
        int number,
        Long userId

    //группу сюда
    ){
    public PositionDTO(Long id, @Size(min = 1, max = 255)
    @NotNull
    String name, String group, boolean start, int number, Long userId) {
        this.id = id;
        this.name = name;
        this.group = group;
        this.start = start;
        this.number = number;
        this.userId = userId;
    }
}
