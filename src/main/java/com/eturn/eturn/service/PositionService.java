package com.eturn.eturn.service;

import com.eturn.eturn.entity.Position;
import com.eturn.eturn.entity.User;
import org.springframework.stereotype.Service;

@Service
public interface PositionService {
    Position getPositionById(Long id);

    Position getLastPosition(Long idUser, Long idTurn);

    Position createPosition(Position position);
}
