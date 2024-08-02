package com.eturn.eturn.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.apache.catalina.util.Introspection;

import java.util.Date;

public record TurnForListDTO(
        @Schema(description = "Идентификатор очереди", example="1")
        String hash,
        @Size(min = 1, max = 255)
        @NotNull
        @Schema(description = "Имя очереди", example="Очередь за булочкой")
        String name,
        @Schema(description = "Описание очереди", example="Всем приятного аппетита!")
        String description,
        @Schema(description = "Тэги", example="Очередь за булочкой Всем приятного аппетита! 2391 ФКТИ Иванов Иван Иванович")
        String tags,
        @Schema(description = "Тэги доступа", example="FOR_ALLOWED_ELEMENTS")
        String accessTags,
        @Schema(description = "Дата начала очереди", example="01.01.2024")
        Date dateStart,
        @Schema(description = "Дата конца очереди", example="31.12.2024")
        Date dateEnd,
        @Schema(description = "Доступ участника", example="MEMBER")
        String accessMember
) {
}
