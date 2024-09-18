package com.eturn.eturn.service;

import com.eturn.eturn.dto.*;
import com.eturn.eturn.entity.Member;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.notifications.PositionsNotificationDTO;


public interface PositionService {
    DetailedPositionDTO createPositionAndSave(String login, String hash);
    PositionsTurnDTO getPositionList(String hash, String username, int page);
    void update(Long id, String username, String status);
    void delete(Long id, String username);
    void skipPosition(long id, String username);
    DetailedPositionDTO getFirstUserPosition(String hash, String username);
    DetailedPositionDTO getFirstPosition(String hash, String username);
    Member addTurnToUser(User user, Turn turn);
    PositionsNotificationDTO getPositionsForNotify(Long turnId);
    long countPositionsByTurn(Turn turn);

    boolean existsAllByTurnAndUser(Turn turn, User user);
    void deleteAllByTurnAndUser(Turn turn, User user);
}
