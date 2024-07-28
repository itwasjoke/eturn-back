package com.eturn.eturn.dto;

import java.util.List;

public record PositionsTurnDTO(
        PositionMoreInfoDTO userPosition,
        PositionMoreInfoDTO turnPosition,
        List<PositionDTO> allPositions
) {
}
