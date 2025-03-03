package com.eturn.eturn.security.entity;

public record EtuIdProfile(
        String id,
        String second_name,
        String first_name,
        String middle_name,
        String birthdate,
        String email
) {
}
