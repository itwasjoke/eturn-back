package com.eturn.eturn.dto;

import com.eturn.eturn.entity.*;
import com.eturn.eturn.enums.AccessTurnEnum;
import com.eturn.eturn.enums.TurnEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import org.apache.catalina.util.Introspection;

import java.util.Date;
import java.util.Set;

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
        @Schema(description = "Тип очереди", example="EDU / ORG")
        @Enumerated()
        @NotNull TurnEnum turnType,
        @Schema(description = "Доступ к очереди", example="FOR_ALLOWED_GROUPS")
        @Enumerated()
        @NotNull AccessTurnEnum turnAccess,
        @Schema(description = "Допустимые группы")
        Set<GroupDTO> allowedGroups,
        @Schema(description = "Допустимые факультеты")
        Set<Faculty> allowedFaculties,
        @Schema(description = "Время на то, чтобы войти", example="1")
        @PositiveOrZero
        int timer,
        @Schema(description = "Количество позиций, через которое можно вставать в очередь", example="1")
        @PositiveOrZero
        int positionCount,

        @Schema(description = "Дата начала")
        @FutureOrPresent
        @NotNull
        Date dateStart,
        @Schema(description = "Дата конца")
        @FutureOrPresent
        @NotNull
        Date dateEnd
) {

}
