package com.eturn.eturn.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** DTO позиции для списка */
@Schema(description = "Позиция")
public record PositionDTO(
        @Schema(description = "Идентификатор позиции", example="1")
        Long id,
        @Schema(description = "Имя пользователя", example="Иванов Иван Иванович")
        String name,
        @Schema(description = "Номер группы", example="2000")
        String group,
        @Schema(description = "Статус(вход/выход)", example="false")
        Boolean start,
        @Schema(description = "Идентификатор пользователя", example="1")
        Long userId
    ){

}
