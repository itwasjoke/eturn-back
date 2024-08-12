package com.eturn.eturn.dto;

import com.eturn.eturn.entity.Position;
import com.eturn.eturn.enums.TurnEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Schema(description = "Очередь")
public record TurnDTO(
        @Schema(description = "Идентификатор очереди", example="AXKVe4")
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
        @Schema(description = "Дата начала очереди", example="2024-08-12T10:40:33.118Z")
        Date dateStart,
        @Schema(description = "Дата конца очереди", example="2024-08-12T10:40:33.118Z")
        Date dateEnd,
        @Schema(description = "Среднее время", example="5000")
        Integer averageTime,
        @Schema(description = "Доступ", example="CREATOR")
        String access,
        @Schema(description = "Тип доступа", example="FOR_LINK/GROUPS/FACULTIES")
        String accessType,
        @Schema(description = "Тип очереди", example="EDU/ORG")
        TurnEnum turnType,
        @Schema(description = "Количество позиций, через которое можно вставать в очередь", example="1")
        Integer positionCount,
        @Schema(description = "Разрешенные элементы очереди", example="[1, 2, 4, 65, 144]")
        List<Long> accessElements,
        @Schema(description = "Подана ли заявка на вступление в модерацию", example="false")
        boolean invitedModerator,
        @Schema(description = "Подана ли заявка на вступление в очередь", example="false")
        boolean invitedForTurn,
        @Schema(description = "Есть ли заявки для модератора", example="false")
        boolean existsInvited,
        @Schema(description = "Количество участников/модераторов/заявок/заблокированных", example="3, 24, 2, 8, 1")
        MembersCountDTO membersCount,
        @Schema(description = "Время на удаление", example="2")
        Integer timer
) {

}
