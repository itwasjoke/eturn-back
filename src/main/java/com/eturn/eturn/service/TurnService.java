package com.eturn.eturn.service;

import com.eturn.eturn.dto.TurnDTO;
import com.eturn.eturn.dto.TurnMoreInfoDTO;
import com.eturn.eturn.entity.Turn;

import java.util.List;
import java.util.Map;

public interface TurnService {
    List<Turn> getAllTurns();

    TurnDTO getTurn(Long id);

    List<TurnDTO> getUserTurns(Long idUser, Map<String, String> params);

    Long createTurn(TurnMoreInfoDTO turn);

    Turn getTurnFrom(Long id);

    void updateTurn(Long idUser, Turn turn);

    void deleteTurn(Long idUser, Long idTurn);

    void countUser(Turn turn);

    void addTurnToUser(Long turnId, Long userId);

}
