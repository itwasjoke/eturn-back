package com.eturn.eturn.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
@Schema(description = "Позиция")
public record PositionDTO(
        @Schema(description = "Идентификатор позиции", example="1")
        Long id,
        @Size(min = 1, max = 255)
        @NotNull
        @Schema(description = "Имя пользователя", example="Иванов Иван Иванович")
        String name,
        @Schema(description = "Номер группы", example="2000")
        String group,
        @Schema(description = "Статус(вход/выход)", example="false")
        boolean start,
        @Schema(description = "Номер в очереди", example="35")
        int number,
        @Schema(description = "Идентификатор пользователя", example="1")
        Long userId
    ){

}
