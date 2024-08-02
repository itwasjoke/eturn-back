package com.eturn.eturn.dto;

import com.eturn.eturn.entity.Position;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Schema(description = "Очередь")
public record TurnDTO(
        @Schema(description = "Идентификатор очереди", example="1")
        String hash,
        @Size(min = 1, max = 255)
        @NotNull
        @Schema(description = "Имя очереди", example="Очередь за булочкой")
        String name,
        @Schema(description = "Описание очереди", example="Всем приятного аппетита!")
        String description,
        @Schema(description = "Количество участников", example="1")
        int countUsers,
        @Schema(description = "Имя создателя", example="Иванов Иван Иванович")
        String creator,
        @Schema(description = "Идентификатор пользователя", example="1")
        long userId,
        Date dateStart,
        Date dateEnd,
        @Schema(description = "Среднее время", example="10000")
        Integer averageTime,
        @Schema(description = "Права участника", example="10000")
        String access,
        String accessType,
        Integer PositionCount
) {

}
