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
        @Schema(description = "Дата начала очереди", example="01.01.2024")
        Date dateStart,
        @Schema(description = "Дата конца очереди", example="31.12.2024")
        Date dateEnd,
        @Schema(description = "Среднее время", example="5")
        Integer averageTime,
        @Schema(description = "Доступ", example="FOR_LINK")
        String access,
        @Schema(description = "Тип доступа", example="FOR_LINK")
        String accessType,
        @Schema(description = "Количество позиций, через которое можно вставать в очередь", example="1")
        Integer PositionCount
) {

}
