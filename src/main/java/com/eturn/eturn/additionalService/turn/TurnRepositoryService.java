package com.eturn.eturn.additionalService.turn;

import com.eturn.eturn.dto.TurnDTO;
import com.eturn.eturn.dto.TurnForListDTO;

import java.util.List;
import java.util.Map;

public interface TurnRepositoryService {
    TurnDTO getTurn(String hash, String login);
    List<TurnForListDTO> getUserTurns(String login, Map<String, String> params);
    List<TurnForListDTO> getLinkedTurn(String hash, String username);
}
