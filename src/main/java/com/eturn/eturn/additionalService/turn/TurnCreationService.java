package com.eturn.eturn.additionalService.turn;

import com.eturn.eturn.dto.TurnCreatingDTO;

public interface TurnCreationService {
    String createTurn(TurnCreatingDTO turnDTO, String login);
}
