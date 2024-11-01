package com.eturn.eturn.dto;

import io.swagger.v3.oas.annotations.media.Schema;
/** Парсинг группы */
@Schema(description = "Группа")
public record GroupDTO(
        @Schema(description = "Идентификатор группы", example="1")
        Long id,
        @Schema(description = "Номер группы", example="2000")
        String number,
        @Schema(description = "Номер курса", example="1")
        Integer course,
        @Schema(description = "Факультет")
        FacultyDTO faculty
) {

}
