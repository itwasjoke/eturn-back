package com.eturn.eturn.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Сущность существущего пользователя")
public record UserDTO(
        @Schema(description = "Идентификатор пользователя")
        Long id,
        @Schema(description = "Имя пользователя", example="Иванов Иван Иванович")
        String name,
        @Schema(description = "Тип пользователя", example="Студент")
        String role,
        @Schema(description = "Факультет", example="ФКТИ")
        String faculty,
        @Schema(description = "Группа", example="2000")
        String group
) {

}
