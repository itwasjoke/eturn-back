package com.eturn.eturn.additionalService.position;

import com.eturn.eturn.dto.DetailedPositionDTO;
import com.eturn.eturn.entity.Member;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;

public interface PositionCreationService {
    DetailedPositionDTO createPositionAndSave(String login, String hash);
    Member createMemberForPosition(User user, Turn turn);
}
