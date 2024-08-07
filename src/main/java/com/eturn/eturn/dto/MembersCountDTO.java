package com.eturn.eturn.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Количество участников/модераторов/заявок/заблокированных")
public record MembersCountDTO(
        @Schema(description = "Количество модераторов", example = "3")
        int moderator,
        @Schema(description = "Количество участников", example = "24")
        int member,
        @Schema(description = "Количество заявок на модератора", example = "2")
        int memberInvited,
        @Schema(description = "Количество заявок в очередь", example = "8")
        int moderatorInvited,
        @Schema(description = "Количество заблокированных", example = "1")
        int blocked
) {

}
