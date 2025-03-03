package com.eturn.eturn.security.entity;

public record EtuIdToken(
        String access_token,
        String refresh_token,
        Long expires_in
) {
}
