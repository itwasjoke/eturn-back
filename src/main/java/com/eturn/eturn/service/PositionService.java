package com.eturn.eturn.service;

import com.eturn.eturn.entity.Position;
import com.eturn.eturn.entity.User;
import org.springframework.stereotype.Service;

import java.util.Optional;


public interface PositionService {
    Position getPositionById(Long id);

    Optional<Position> getLastPosition(Long idUser, Long idTurn);

    Position createPosition(Position position);
}
