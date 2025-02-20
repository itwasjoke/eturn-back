package com.eturn.eturn.additionalService.turn;

import com.eturn.eturn.dto.TurnEditDTO;

public interface TurnUpdateService {
    void updateTurn(
            TurnEditDTO turn,
            String username
    );
    void deleteTurn(
            String username,
            String hash
    );
}
