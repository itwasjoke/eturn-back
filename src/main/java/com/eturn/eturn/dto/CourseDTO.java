package com.eturn.eturn.dto;

import com.eturn.eturn.enums.EduEnum;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Курс")
public record CourseDTO(
        @Schema(description = "Идентификатор курса", example="1")
        Long id,
        @Schema(description = "Номер курса", example="2")
        String number,
        @Schema(description = "Программа оучения", example="BACCALAUREATE")
        EduEnum eduEnum
) {

}
