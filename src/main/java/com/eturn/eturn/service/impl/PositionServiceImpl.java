package com.eturn.eturn.service.impl;

import com.eturn.eturn.dto.PositionsDTO;
import com.eturn.eturn.dto.mapper.PositionsListMapper;
import com.eturn.eturn.dto.mapper.TurnMapper;
import com.eturn.eturn.entity.Position;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.repository.PositionRepository;
import com.eturn.eturn.service.PositionService;
import com.eturn.eturn.service.TurnService;
import com.eturn.eturn.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class PositionServiceImpl implements PositionService {
    private final PositionRepository positionRepository;
    private final UserService userService;
    private final PositionsListMapper positionsListMapper;
    private final TurnService turnService;
    private final TurnMapper turnMapper;

    public PositionServiceImpl(PositionRepository positionRepository, UserService userService,
                               PositionsListMapper positionsListMapper,TurnService turnService,
                               TurnMapper turnMapper) {
        this.positionRepository = positionRepository;
        this.userService = userService;
        this.positionsListMapper=positionsListMapper;
        this.turnService=turnService;
        this.turnMapper=turnMapper;
    }

    @Override
    public Position getPositionById(Long id) {
        return positionRepository.getReferenceById(id);
    }

    @Override
    // TODO int pageSize, int pageNumber
    public Optional<Position> getLastPosition(Long idUser, Long idTurn) {
        // TODO positionRepository.findAllByUser(new User(), Pageable.ofSize(pageSize).withPage(pageNumber));
        User user = userService.getUserFrom(idUser);
        return positionRepository.findFirstByUserOrderByNumberDesc(user);
    }

    @Override
    public Position createPosition(Position position) {
        return positionRepository.save(position);
    } //id вернуть???

    @Override
    public List<PositionsDTO> getPositonList(Long idTurn){
        Turn turn=turnService.getTurnFrom(idTurn);
        List<Position> positions=positionRepository.getPositionByTurn(turn);
        return positionsListMapper.map(positions);
        //тут ошибку можно бросить
    }
}
