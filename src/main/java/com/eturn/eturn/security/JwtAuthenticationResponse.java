package com.eturn.eturn.security;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class JwtAuthenticationResponse {
    private String token;
}