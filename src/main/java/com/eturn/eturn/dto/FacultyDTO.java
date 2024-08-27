package com.eturn.eturn.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Парсинг факультета */
@Schema(description = "Факультет")
public record FacultyDTO(
        @Schema(description = "Идентификатор факультета", example="1")
        Long id,
        @Schema(description = "Название факультета", example="ФКТИ")
        String name
) {

}
