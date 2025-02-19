package com.eturn.eturn.additionalService.position;

import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;

public interface PositionDeletionService {
    void delete(Long id, String username);
    void deleteAllByTurnAndUser(Turn turn, User user);
}
