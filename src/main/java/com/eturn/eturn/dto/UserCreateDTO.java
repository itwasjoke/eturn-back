package com.eturn.eturn.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Тестовое создание пользователя */
public record UserCreateDTO(
        Long id,
        String name,
        String role,
        GroupDTO group,
        String login,
        String password,
        String appType,
        String tokenNotify
) {
}
