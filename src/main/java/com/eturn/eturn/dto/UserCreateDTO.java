package com.eturn.eturn.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Сущность нового пользователя")
public record UserCreateDTO(

        @Schema(description = "Имя пользователя", example="Иванов Иван Иванович")
        String name,
        @Schema(description = "Тип пользователя", example="STUDENT")
        String role,
        @Schema(description = "Идентификатор факультета", example="1")
        Long facultyId,
        @Schema(description = "Идентификатор группы", example="1")
        Long groupId,
        @Schema(description = "Идентификатор курса", example="1")
        Long courseId,
        @Schema(description = "Идентификатор кафедры", example="1")
        Long departmentId,
        @Schema(description = "Логин", example="user123")
        String login,
        @Schema(description = "Пароль", example="password123")
        String password
) {
}
