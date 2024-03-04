package com.eturn.eturn.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Группа")
public record GroupDTO(

        @Schema(description = "Идентификатор группы", example="1")
        Long id,
        @Schema(description = "Номер группы", example="2000")
        String name,
        @Schema(description = "Идентификатор факультета", example="1")
        Long facultyId
) {

}
