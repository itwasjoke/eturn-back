package com.eturn.eturn.dto;

import java.util.Date;

public record FeedbackDTO(
        Long id,
        String text,
        Integer evaluation,
        String name,
        String group,
        Date date,
        Long userId
) {

}
