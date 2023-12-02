package com.eturn.eturn.service.impl;

import com.eturn.eturn.entity.Position;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.repository.PositionRepository;
import com.eturn.eturn.repository.UserRepository;
import com.eturn.eturn.service.PositionService;
import com.eturn.eturn.service.UserService;

public class PositionServiceImpl implements PositionService {
    PositionRepository positionRepository;
    UserService userService;
    @Override
    public Position getPositionById(Long id) {
        return positionRepository.getReferenceById(id);
    }

    @Override
    public Position getLastPosition(Long idUser, Long idTurn) {
        User user = userService.getUser(idUser);
        return positionRepository.findFirstByUserByOrderByCreatedAtDesc(user);
    }

    @Override
    public Position createPosition(Position position) {
        return positionRepository.save(position);
    }
}
