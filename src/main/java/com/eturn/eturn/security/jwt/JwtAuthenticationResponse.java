package com.eturn.eturn.security.jwt;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JwtAuthenticationResponse {
    private String token;

    public JwtAuthenticationResponse(String token) {
        this.token = token;
    }
}