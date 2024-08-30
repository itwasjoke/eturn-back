package com.eturn.eturn.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** DTO участника очререди */
@Schema(description = "Участник")
public record MemberDTO(
        @Schema(description = "Идентификатор участника очереди", example="1")
        Long id,
        @Schema(description = "Идентификатор пользователя", example="1")
        Long userId,
        @Schema(description = "Идентификатор очереди", example="1")
        Long turnId,
        @Schema(description = "Имя участника", example="Иванов Иван Иванович")
        String userName,
        @Schema(description = "Номер группы", example="2391")
        String group,
        @Schema(description = "Доступ участника", example="MEMBER")
        String access,
        @Schema(description = "Есть ли заявка на модерацию", example="true")
        boolean invited,
        @Schema(description = "Есть ли заявка на вступление в очередь", example="true")
        String invitedForTurn
) {
}
