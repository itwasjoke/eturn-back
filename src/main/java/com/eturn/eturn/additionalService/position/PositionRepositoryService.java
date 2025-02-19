package com.eturn.eturn.additionalService.position;

import com.eturn.eturn.dto.DetailedPositionDTO;
import com.eturn.eturn.dto.PositionsTurnDTO;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;

public interface PositionRepositoryService {
    DetailedPositionDTO getFirstUserPosition(String hash, String username);
    DetailedPositionDTO getFirstPosition(String hash, String username);
    PositionsTurnDTO getPositionList(String hash, String username, int page);
    boolean existsAllByTurnAndUser(Turn turn, User user);
    long countPositionsByTurn(Turn turn);
}
