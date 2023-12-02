package com.eturn.eturn.service;

import com.eturn.eturn.entity.Turn;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Service
public interface TurnService {
    List<Turn> getAllTurns();
    Turn getTurn(Long id);

    List<Turn> getUserTurns(Long idUser, Map<String,String> params);

    Turn createTurn(Turn turn);

    Turn updateTurn(Long idUser, Turn turnOld, Turn turnNew);

    void deleteTurn(Long idUser, Turn turn);
    void deletePosition(Long idPosition, Long idUser, Turn turn);

    void addPositionToTurn(Long idUser, Turn turn);
}
