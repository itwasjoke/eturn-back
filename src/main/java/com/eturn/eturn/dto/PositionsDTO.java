package com.eturn.eturn.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PositionsDTO(
        Long id,
        @Size(min = 1, max = 255)
        @NotNull
        String name,
        String group

    //группу сюда
    ){


    public PositionsDTO(Long id, String name,String group){
        this.id=id;
        this.name=name;
        this.group=group;
    }
}
