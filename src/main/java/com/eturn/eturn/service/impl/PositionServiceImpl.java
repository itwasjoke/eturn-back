package com.eturn.eturn.service.impl;

import com.eturn.eturn.dto.PositionDTO;
import com.eturn.eturn.dto.PositionMoreInfoDTO;
import com.eturn.eturn.dto.UserDTO;
import com.eturn.eturn.dto.mapper.PositionListMapper;
import com.eturn.eturn.dto.mapper.PositionMapper;
import com.eturn.eturn.dto.mapper.PositionMoreInfoMapper;
import com.eturn.eturn.dto.mapper.TurnMapper;
import com.eturn.eturn.entity.*;
import com.eturn.eturn.exception.position.NoCreatePosException;
import com.eturn.eturn.exception.position.NotFoundPosException;
import com.eturn.eturn.repository.PositionRepository;
import com.eturn.eturn.service.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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

    //TODO Надо ли сохранять?
//    @Override
//    public PositionDTO getPositionById(Long id) {
//        Optional<Position> position = positionRepository.findById(id);
//        if (position.isPresent()){
//            return positionMapper.positionToPositionDTO(position.get());
//        }
//        else throw new
//    }

//    @Override
//    public Optional<Position> getLastPosition(Long idUser, Long idTurn) {
//        User user = userService.getUserFrom(idUser);
//        Turn turn = turnService.getTurnFrom(idTurn);
//        return positionRepository.findFirstByUserAndTurnOrderByNumberDesc(user, turn);
//    }

    @Override
    public PositionMoreInfoDTO createPositionAndSave(String login, Long idTurn) {

        // получение основной информации
        Turn turn = turnService.getTurnFrom(idTurn);
        UserDTO userDTO = userService.getUser(login);
        User user = userService.getUserFrom(userDTO.id());

        // рассчет участников
        long members = memberService.getConutByTurn(idTurn);
        // если человек меньше, чем разрешенное число,
        // то за него берется количество участников
        int PERMITTED_COUNT_PEOPLE_SYSTEM = 2;
        int PERMITTED_COUNT_PEOPLE = PERMITTED_COUNT_PEOPLE_SYSTEM;
        if (members < PERMITTED_COUNT_PEOPLE_SYSTEM){
            PERMITTED_COUNT_PEOPLE = (int) members;
        }
        // получение своей первой позиции
        Optional<Position> ourPosition = positionRepository.findFirstByUserAndTurn(user, turn);
        // Optional<Position> lastPositionInTurn = positionRepository.findTopByTurnOrderByNumberDesc(turn);

        // получение списка из PERMITTED_COUNT_PEOPLE позиций для поиска нашей позиции
        Pageable paging = PageRequest.of(0, PERMITTED_COUNT_PEOPLE);
        Page<Position> positions = positionRepository.findByTurnOrderByIdDesc(turn, paging);
        Stream<Position> allPositionsForEndStream = positions.get();
        List<Position> allPositionsForEnd = allPositionsForEndStream.toList();
//        long countAllPosForEnd = allPositionsForEnd.count();

        // перебор списка
        final int[] countBeforeUserPosFound = {0}; // количество позиций до появления позиции пользователя
        final int[] userPosFound = {0}; // появление позиции пользователя

        if (!allPositionsForEnd.isEmpty()) {
            allPositionsForEnd.forEach((element) -> {
                if (element.getUser() == user && userPosFound[0] == 0) {
                    userPosFound[0] = 1;
                } else if (userPosFound[0] == 0) {
                    countBeforeUserPosFound[0]++;
                }
            });
        }
        // проверка на то, что имеется позиция
        boolean isUserPosExist = userPosFound[0]==1;

        // разница для исключения
        int differenceForException = PERMITTED_COUNT_PEOPLE - countBeforeUserPosFound[0];

        if (isUserPosExist){
            // если позиция существует, то делаем исключение
            throw new NoCreatePosException(String.valueOf(differenceForException));
        }
        else{
            // вычисляем номер позиции
            int lastNumberPosition = 1;
            if (!allPositionsForEnd.isEmpty()){
                Position lastPosition = allPositionsForEnd.get(0);
                lastNumberPosition = lastPosition.getNumber()+1;
            }

            // создаем новую позицию
            Position newPosition = new Position();
            newPosition.setStart(false);
            newPosition.setUser(user);
            newPosition.setTurn(turn);
            newPosition.setGroupName(userDTO.group());
            newPosition.setNumber(lastNumberPosition);
            Position p = positionRepository.save(newPosition);

            // вычисляем разницу для позиции
            int differenceForUser = -1;
            if (ourPosition.isEmpty()){
                differenceForUser = (int) positionRepository.countNumbersLeft(p.getNumber(), turn);
            }
            return positionMoreInfoMapper.positionMoreInfoToPositionDTO(p, differenceForUser);
        }



//        if (members < PERMITTED_COUNT_PEOPLE) {
//            if (ourPosition.isPresent()) {
//                return new PositionMoreInfoDTO(null, null, null, false, 0, null, 0);
//            } else {
//                return createPosition(turn, user, userDTO, lastNumber, true);
//            }
//        } else {
//            if (ourPosition.isPresent()) {
//                long countBetween = positionRepository.countNumbers(ourPosition.get().getNumber(), turn);
//                if (countBetween >= PERMITTED_COUNT_PEOPLE) {
//                    return createPosition(turn, user, userDTO, lastNumber, false);
//                }
//                else{
//                    int dif = (int) (PERMITTED_COUNT_PEOPLE - countBetween);
//                    return new PositionMoreInfoDTO(null, null, null, false, 0, null, dif);
//                }
//            } else {
//                return createPosition(turn, user, userDTO, lastNumber, true);
//            }
//        }
    }

//    private PositionMoreInfoDTO createPosition(Turn turn, User user, UserDTO userDTO, int lastNumber, boolean need){
//        Position newPosition = new Position();
//        newPosition.setStart(false);
//        newPosition.setUser(user);
//        newPosition.setTurn(turn);
//        newPosition.setGroupName(userDTO.group());
//        newPosition.setNumber(lastNumber + 1);
//        Position p = positionRepository.save(newPosition);
//        int difference;
//        if (need){
//            difference = (int) positionRepository.countNumbersLeft(p.getNumber(), turn);
//        }
//        else{
//            difference = -1;
//        }
//        return positionMoreInfoMapper.positionMoreInfoToPositionDTO(p, difference);
//    }
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
            throw new NotFoundPosException("No positions found");
        }
        else return positionListMapper.map(positions);
    }

    @Override
    public void update(Long id) {
        Optional<Position> position = positionRepository.findById(id);
        if (position.isPresent()){
            Position pos = position.get();
            if (pos.isStart()){
                delete(id);
            }
            else{
                pos.setStart(true);
                positionRepository.save(pos);
            }
        }
        else{
            throw new NotFoundPosException("No positions found");
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
            throw new NotFoundPosException("No positions found");
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
            throw new NotFoundPosException("No positions found");
        }
    }

}
