package com.eturn.eturn.dto;

import com.eturn.eturn.entity.Counter;

import java.util.List;

public record StatisticDTO(
        List<Counter> counters,
        List<TurnForListDTO> turns,
        List<String> users
) {
}
