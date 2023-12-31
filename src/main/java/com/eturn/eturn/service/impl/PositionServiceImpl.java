package com.eturn.eturn.service.impl;

import com.eturn.eturn.dto.PositionDTO;
import com.eturn.eturn.dto.PositionMoreInfoDTO;
import com.eturn.eturn.dto.UserDTO;
import com.eturn.eturn.dto.mapper.PositionListMapper;
import com.eturn.eturn.dto.mapper.PositionMapper;
import com.eturn.eturn.dto.mapper.PositionMoreInfoMapper;
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
    private final PositionMoreInfoMapper positionMoreInfoMapper;
    private final PositionMapper positionMapper;
    private final MemberService memberService;


    public PositionServiceImpl(PositionRepository positionRepository, UserService userService,
                               PositionListMapper positionListMapper, TurnService turnService, PositionMoreInfoMapper positionMoreInfoMapper, PositionMapper positionMapper, MemberService memberService) {
        this.positionRepository = positionRepository;
        this.userService = userService;
        this.positionListMapper = positionListMapper;
        this.turnService = turnService;
        this.positionMoreInfoMapper = positionMoreInfoMapper;
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
    public PositionMoreInfoDTO createPositionAndSave(Long idUser, Long idTurn) {
        Turn turn = turnService.getTurnFrom(idTurn);
        User user = userService.getUserFrom(idUser);
        UserDTO userDTO = userService.getUser(idUser);

        long members = memberService.getConutByTurn(idTurn);

        int permittedCountPeople = 2;

        Optional<Position> ourPosition = positionRepository.findFirstByUserAndTurnOrderByNumberDesc(user, turn);
        Optional<Position> lastPositionInTurn = positionRepository.findTopByTurnOrderByNumberDesc(turn);

        int lastNumber = lastPositionInTurn.map(Position::getNumber).orElse(0);

        if (members < permittedCountPeople) {
            if (ourPosition.isPresent()) {
                return new PositionMoreInfoDTO(null, null, null, false, 0, null, 0);
            } else {
                return createPosition(turn, user, userDTO, lastNumber, true);
            }
        } else {
            if (ourPosition.isPresent()) {
                long countBetween = positionRepository.countNumbers(ourPosition.get().getNumber(), turn);
                if (countBetween >= permittedCountPeople) {
                    return createPosition(turn, user, userDTO, lastNumber, false);
                }
                else{
                    int dif = (int) (permittedCountPeople - countBetween);
                    return new PositionMoreInfoDTO(null, null, null, false, 0, null, dif);
                }
            } else {
                return createPosition(turn, user, userDTO, lastNumber, true);
            }
        }
    }

    private PositionMoreInfoDTO createPosition(Turn turn, User user, UserDTO userDTO, int lastNumber, boolean need){
        Position newPosition = new Position();
        newPosition.setStart(false);
        newPosition.setUser(user);
        newPosition.setTurn(turn);
        newPosition.setGroupName(userDTO.group());
        newPosition.setNumber(lastNumber + 1);
        Position p = positionRepository.save(newPosition);
        int difference;
        if (need){
            difference = (int) positionRepository.countNumbersLeft(p.getNumber(), turn);
        }
        else{
            difference = -1;
        }
        return positionMoreInfoMapper.positionMoreInfoToPositionDTO(p, difference);
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
        if (positions.isEmpty()){
            throw new NotFoundException("No positions");
        }
        else return positionListMapper.map(positions);
    }

    @Override
    public void update(Long id, boolean started) {
        Optional<Position> position = positionRepository.findById(id);
        if (position.isPresent()){
            Position pos = position.get();
            if (pos.isStart()){
                delete(id);
            }
            else{
                pos.setStart(true);
                Position posCreated = positionRepository.save(pos);
                int n = 0;
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
    public PositionMoreInfoDTO getFirstUserPosition(Long turnId, Long userId) {
        User user = userService.getUserFrom(userId);
        Turn turn = turnService.getTurnFrom(turnId);
        Optional<Position> p = positionRepository.findTopByTurnAndUser(turn, user);
        Optional<Position> pInTurn = positionRepository.findFirstByTurn(turn);
        if (p.isPresent() && pInTurn.isPresent()){
            if (pInTurn.get()==p.get()){
                int difference = 0;
                return positionMoreInfoMapper.positionMoreInfoToPositionDTO(p.get(), difference);
            }
            else{
                int difference = (int) positionRepository.countNumbersLeft(p.get().getNumber(), turn);
                return positionMoreInfoMapper.positionMoreInfoToPositionDTO(p.get(), difference);
            }
        }
        else{
            return null;
        }
    }

}
