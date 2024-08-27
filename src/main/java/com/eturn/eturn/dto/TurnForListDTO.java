package com.eturn.eturn.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

/** DTO очереди для списка */
public record TurnForListDTO(
        @Schema(description = "Идентификатор очереди", example="SqkF5")
        String hash,
        @NotNull
        @Schema(description = "Имя очереди", example="Очередь за булочкой")
        String name,
        @Schema(description = "Описание очереди", example="Всем приятного аппетита!")
        String description,
        @Schema(description = "Тэги для поиска", example="Очередь за булочкой Всем приятного аппетита! 2391 ФКТИ Иванов Иван Иванович")
        String tags,
        @Schema(description = "Тэги доступа", example="FOR_ALLOWED_ELEMENTS")
        String accessTags,
        @Schema(description = "Дата начала очереди")
        Date dateStart,
        @Schema(description = "Дата конца очереди")
        Date dateEnd,
        @Schema(description = "Доступ участника", example="MEMBER")
        String accessMember
) {
}
