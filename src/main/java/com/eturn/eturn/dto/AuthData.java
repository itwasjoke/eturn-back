package com.eturn.eturn.dto;

/** Данные для аутентификации */
public record AuthData(
        String tokenETUID,
        String tokenNotify,
        String type
) {
}
