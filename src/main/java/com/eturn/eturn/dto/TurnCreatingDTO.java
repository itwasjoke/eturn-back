package com.eturn.eturn.dto;

import com.eturn.eturn.enums.AccessTurn;
import com.eturn.eturn.enums.TurnType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Range;

import java.util.Date;
import java.util.Set;

/** DTO для создания очереди */
@Schema(description = "Очередь с подробной информацией")
public record TurnCreatingDTO(
        @Schema(description = "Идентификатор очереди", example="1")
        Long id,
        @Schema(description = "Имя очереди", example="Очередь за булочкой")
        @NotBlank
        @Size(min = 1, max = 30)
        String name,
        @Schema(description = "Описание очереди", example="Готовим банковские карты. Всем приятного аппетита!")
        @Size(min = 1, max = 200)
        String description,
        @Schema(description = "Тип очереди", example="EDU")
        @Enumerated()
        @NotNull TurnType turnType,
        @Schema(description = "Доступ к очереди", example="FOR_ALLOWED_GROUPS")
        @Enumerated()
        @NotNull AccessTurn turnAccess,
        @Schema(description = "Допустимые группы")
        Set<GroupDTO> allowedGroups,
        @Schema(description = "Допустимые факультеты")
        Set<FacultyDTO> allowedFaculties,
        @Schema(description = "Время на то, чтобы войти", example="1")
        @PositiveOrZero
        @Range(min = 0, max = 20)
        Integer timer,
        @Schema(description = "Количество позиций, через которое можно вставать в очередь", example="1")
        @Range(min = -1, max = 40)
        Integer positionCount,

        @Schema(description = "Дата начала")
        @NotNull
        Date dateStart,
        @Schema(description = "Дата конца")
        @FutureOrPresent
        @NotNull
        Date dateEnd
) {

}
