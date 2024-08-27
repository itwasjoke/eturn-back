package com.eturn.eturn.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** DTO для отправки списка позиций */
@Schema(description = "Список позиций")
public record PositionsTurnDTO(
        @Schema(description = "Позиция пользователя")
        DetailedPositionDTO userPosition,
        @Schema(description = "Первая позиция (отображается только для модераторов)")
        DetailedPositionDTO turnPosition,
        @Schema(description = "Весь список позиций")
        List<PositionDTO> allPositions
) {
}
