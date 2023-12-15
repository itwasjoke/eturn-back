package com.eturn.eturn.service;

import com.eturn.eturn.dto.TurnDTO;
import com.eturn.eturn.entity.Turn;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
public interface TurnService {
    List<Turn> getAllTurns();
    TurnDTO getTurn(Long id);

    List<TurnDTO> getUserTurns(Long idUser, Map<String,String> params);

    Long createTurn(TurnDTO turn);

    void updateTurn(Long idUser, Turn turn);

    void deleteTurn(Long idUser, Long idTurn);

}
