package com.eturn.eturn.dto;

import com.eturn.eturn.enums.EduEnum;

public record CourseDTO(
        Long id, String number, EduEnum eduEnum
) {
    public CourseDTO(Long id, String number, EduEnum eduEnum) {
        this.id = id;
        this.number = number;
        this.eduEnum = eduEnum;
    }
}
