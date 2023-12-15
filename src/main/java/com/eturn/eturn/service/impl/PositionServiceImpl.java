package com.eturn.eturn.service.impl;

import com.eturn.eturn.entity.Position;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.repository.PositionRepository;
import com.eturn.eturn.service.PositionService;
import com.eturn.eturn.service.UserService;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
public class PositionServiceImpl implements PositionService {
    private final PositionRepository positionRepository;
    private final UserService userService;

    public PositionServiceImpl(PositionRepository positionRepository, UserService userService) {
        this.positionRepository = positionRepository;
        this.userService = userService;
    }

    @Override
    public Position getPositionById(Long id) {
        return positionRepository.getReferenceById(id);
    }

    @Override
    // TODO int pageSize, int pageNumber
    public Optional<Position> getLastPosition(Long idUser, Long idTurn) {
        // TODO positionRepository.findAllByUser(new User(), Pageable.ofSize(pageSize).withPage(pageNumber));
        User user = userService.getUser(idUser);
        return positionRepository.findFirstByUserOrderByNumberDesc(user);
    }

    @Override
    public Position createPosition(Position position) {
        return positionRepository.save(position);
    }
}
