package com.eturn.eturn.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Сущность нового пользователя")
public record UserCreateDTO(

        @Schema(description = "Имя пользователя", example="Иванов Иван Иванович")
        String name,
        @Schema(description = "Тип пользователя", example="STUDENT")
        String role,
        @Schema(description = "Группа", example="1")
        GroupDTO group,
        @Schema(description = "Логин", example="user123")
        String login,
        @Schema(description = "Пароль", example="password123")
        String password
) {
}
