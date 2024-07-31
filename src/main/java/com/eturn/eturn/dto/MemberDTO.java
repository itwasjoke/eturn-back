package com.eturn.eturn.dto;

public record MemberDTO(
        Long id,
        Long userId,
        Long turnId,
        String userName,
        String group,
        String access,
        boolean invited,
        boolean invitedForTurn
) {
}
