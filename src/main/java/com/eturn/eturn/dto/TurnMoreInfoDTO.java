package com.eturn.eturn.dto;

import com.eturn.eturn.entity.*;
import com.eturn.eturn.enums.AccessTurnEnum;
import com.eturn.eturn.enums.TurnEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

@Schema(description = "Очередь с подробной информацией")
public record TurnMoreInfoDTO(
        @Schema(description = "Идентификатор очереди", example="1")
        Long id,
        @Schema(description = "Имя очереди", example="Очередь за булочкой")
        @NotNull String name,
        @Schema(description = "Описание очереди", example="Готовим банковские карты. Всем приятного аппетита!")
        String description,
        @Schema(description = "Идентификатор создателя", example="1")
        @NotNull Long creator,
        @Schema(description = "Тип очереди", example="EDU / ORG")
        @NotNull TurnEnum turnType,
        @Schema(description = "Доступ к очереди", example="FOR_ALLOWED_GROUPS")
        @NotNull AccessTurnEnum turnAccess,
        @Schema(description = "Допустимые группы")
        Set<GroupDTO> allowedGroups,
        @Schema(description = "Допустимые факультеты")
        Set<Faculty> allowedFaculties,
        @Schema(description = "Время на то, чтобы войти", example="1")
        int allowedTime
) {

}
