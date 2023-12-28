package com.eturn.eturn.service.impl;

import com.eturn.eturn.dto.PositionDTO;
import com.eturn.eturn.dto.UserDTO;
import com.eturn.eturn.dto.mapper.PositionListMapper;
import com.eturn.eturn.dto.mapper.PositionMapper;
import com.eturn.eturn.dto.mapper.TurnMapper;
import com.eturn.eturn.entity.*;
import com.eturn.eturn.exception.InvalidDataException;
import com.eturn.eturn.exception.NotFoundException;
import com.eturn.eturn.repository.PositionRepository;
import com.eturn.eturn.service.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PositionServiceImpl implements PositionService {
    private final PositionRepository positionRepository;
    private final UserService userService;
    private final PositionListMapper positionListMapper;
    private final TurnService turnService;
    private final TurnMapper turnMapper;
    private final PositionMapper positionMapper;
    private final MemberService memberService;


    public PositionServiceImpl(PositionRepository positionRepository, UserService userService,
                               PositionListMapper positionListMapper, TurnService turnService,
                               TurnMapper turnMapper, PositionMapper positionMapper, MemberService memberService) {
        this.positionRepository = positionRepository;
        this.userService = userService;
        this.positionListMapper = positionListMapper;
        this.turnService = turnService;
        this.turnMapper = turnMapper;
        this.positionMapper = positionMapper;
        this.memberService = memberService;

    }

    @Override
    public PositionDTO getPositionById(Long id) {
        return positionMapper.positionToPositionDTO(positionRepository.getReferenceById(id));
    }

    @Override
    public Optional<Position> getLastPosition(Long idUser, Long idTurn) {
        User user = userService.getUserFrom(idUser);
        Turn turn = turnService.getTurnFrom(idTurn);
        return positionRepository.findFirstByUserAndTurnOrderByNumberDesc(user, turn);
    }

    @Override
    public long createPositionAndSave(Long idUser, Long idTurn) {
        Turn turn = turnService.getTurnFrom(idTurn);
        User user = userService.getUserFrom(idUser);
        UserDTO userDTO = userService.getUser(idUser);

        long members = memberService.getConutByTurn(idTurn);

        int permittedCountPeople = 2;

        Optional<Position> ourPosition = positionRepository.findFirstByUserAndTurnOrderByNumberDesc(user, turn);
        Optional<Position> lastPositionInTurn = positionRepository.findTopByTurnOrderByNumberDesc(turn);

        int lastNumber = lastPositionInTurn.map(Position::getNumber).orElse(0);
        int ourUserLastNumber = ourPosition.map(Position::getNumber).orElse(0);

        if (members < permittedCountPeople) {
            if (ourPosition.isPresent()) {
                throw new InvalidDataException("Позиция существует");
            } else {
                return createPosition(turn, user, userDTO, lastNumber);
            }
        } else {
            if (ourPosition.isPresent()) {
                if (lastNumber - ourUserLastNumber > permittedCountPeople) {
                    return createPosition(turn, user, userDTO, lastNumber);
                }
                else{
                    return ourUserLastNumber - lastNumber;
                }
            } else {
                return createPosition(turn, user, userDTO, lastNumber);
            }
        }
    }

    private long createPosition(Turn turn, User user, UserDTO userDTO, int lastNumber){
        Position newPosition = new Position();
        newPosition.setStarted(false);
        newPosition.setUser(user);
        newPosition.setTurn(turn);
        newPosition.setGroupName(userDTO.group());
        newPosition.setNumber(lastNumber + 1);
        Position p = positionRepository.save(newPosition);
        return p.getId();
    }
    @Override
    public List<PositionDTO> getPositonList(Long idTurn, int page) {
        Turn turn = turnService.getTurnFrom(idTurn);
        long sizePositions = positionRepository.countByTurn(turn);
        int size;
        if (sizePositions>20){
            size = 20;
        }
        else{
            size = (int) sizePositions;
        }
        Pageable paging = PageRequest.of(page, size);
        Page<Position> positions = positionRepository.findAllByTurn(turn, paging);
        return positionListMapper.map(positions);
        //тут ошибку можно бросить
    }

    @Override
    public void update(Long id, boolean started) {
        Optional<Position> position = positionRepository.findById(id);
        if (position.isPresent()){
            Position pos = position.get();
            if (pos.isStarted()){
                delete(id);
            }
            else{
                pos.setStarted(true);
                positionRepository.save(pos);
            }

        }

    }

    @Override
    public void delete(Long id) {
        Optional<Position> position = positionRepository.findById(id);
        if (position.isPresent()) {
            Position pos = position.get();
            positionRepository.delete(pos);
        }
        else{
            throw new NotFoundException("Позиция не найдена");
        }
    }

    @Override
    public PositionDTO getFirstUserPosition(Long turnId, Long userId) {
        User user = userService.getUserFrom(userId);
        Turn turn = turnService.getTurnFrom(turnId);
        Optional<Position> p = positionRepository.findTopByTurnAndUser(turn, user);
        return p.map(positionMapper::positionToPositionDTO).orElse(null);
    }

}
