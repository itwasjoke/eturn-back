package com.eturn.eturn.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Date;

@Schema(description = "Позиция с подробной информацией")
public record PositionMoreInfoDTO(
        @Schema(description = "Идентификатор позиции", example="1")
        Long id,
        @Size(min = 1, max = 255)
        @NotNull
        @Schema(description = "Имя пользователя", example="Иванов Иван Иванович")
        String name,
        @Schema(description = "Номер группы", example="2000")
        String group,
        @Schema(description = "Статус (вход/выход)", example="true")
        Boolean start,
        @Schema(description = "Номер в очереди", example="23")
        int number,
        @Schema(description = "Идентификатор пользователя", example="1")
        Long userId,
        @Schema(description = "Сколько позиций осталось ждать", example="54")
        int difference,
        @Schema(description = "Дата начала отсчета", example="01.01.01 10:00")
        Date dateEnd,
        @Schema(description = "Количество возможностей пропустить следующего", example="5")
        Integer skipCount
) {

}
