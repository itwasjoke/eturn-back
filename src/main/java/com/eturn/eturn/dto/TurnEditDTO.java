package com.eturn.eturn.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Range;

import java.util.Set;

/** DTO для редактирования очереди */
public record TurnEditDTO(
        @Schema(description = "Идентификатор очереди", example="fwDFw")
        String hash,
        @Schema(description = "Имя очереди", example="Очередь за булочкой")
        @Size(min = 1, max = 30)
        String name,
        @Schema(description = "Описание очереди", example="Готовим банковские карты. Всем приятного аппетита!")
        @Size(max = 200)
        String description,
        @Schema(description = "Допустимые группы")
        Set<GroupDTO> allowedGroups,
        @Schema(description = "Допустимые факультеты")
        Set<FacultyDTO> allowedFaculties,
        @Schema(description = "Время на то, чтобы войти", example="1")
        @Range(min = 0, max = 20)
        Integer timer,
        @Schema(description = "Количество позиций, через которое можно вставать в очередь", example="1")
        @Range(min = -1, max = 40)
        Integer positionCount
) {

}