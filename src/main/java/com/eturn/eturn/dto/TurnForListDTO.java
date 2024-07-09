package com.eturn.eturn.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Date;

public record TurnForListDTO(
        @Schema(description = "Идентификатор очереди", example="1")
        Long id,
        @Size(min = 1, max = 255)
        @NotNull
        @Schema(description = "Имя очереди", example="Очередь за булочкой")
        String name,
        @Schema(description = "Описание очереди", example="Всем приятного аппетита!")
        String description,
        @Schema(description = "Количество участников", example="1")
        int countUsers,
        @Schema(description = "Идентификатор пользователя", example="1")
        long userId,
        int allowedTime,
        Date dateStart,
        Date dateEnd,
        String accessMember
) {
}