package com.eturn.eturn.service.impl;

import com.eturn.eturn.dto.PositionDTO;
import com.eturn.eturn.dto.PositionMoreInfoDTO;
import com.eturn.eturn.dto.UserDTO;
import com.eturn.eturn.dto.mapper.PositionListMapper;
import com.eturn.eturn.dto.mapper.PositionMapper;
import com.eturn.eturn.dto.mapper.PositionMoreInfoMapper;
import com.eturn.eturn.entity.*;
import com.eturn.eturn.exception.position.NoCreatePosException;
import com.eturn.eturn.exception.position.NotFoundPosException;
import com.eturn.eturn.repository.PositionRepository;
import com.eturn.eturn.service.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
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

    @Transactional
    void deleteOverdueElements(Turn turn){
        Optional<Position> positionFirstO = positionRepository.findFirstByTurnOrderByIdAsc(turn);
        if (positionFirstO.isPresent()){
            Position positionFirst = positionFirstO.get();
            if (positionFirst.getDateEnd()!=null && !positionFirst.isStart()){
                Date dateNow = new Date();
                long timeBetween = dateNow.getTime() - positionFirst.getDateEnd().getTime();
                if (timeBetween>0){
                    timeBetween = timeBetween / 1000;
                    timeBetween = timeBetween / 60;
                    timeBetween = timeBetween / 2;
                    timeBetween++;
                    Pageable  paging = PageRequest.of(0, (int)timeBetween);
                    Page<Position> positionsPage = positionRepository.findAllByTurnOrderByIdAsc(turn,paging);
                    List<Position> positionList = positionsPage.stream().toList();
                    Position p = positionList.get(positionList.size()-1);
                    positionRepository.tryToDelete(turn, p.getNumber());
                }
            }
        }
    }

    @Override
    @Transactional
    public PositionMoreInfoDTO createPositionAndSave(String login, Long idTurn) {

        // получение основной информации
        Turn turn = turnService.getTurnFrom(idTurn);
        UserDTO userDTO = userService.getUser(login);
        User user = userService.getUserFrom(userDTO.id());

        deleteOverdueElements(turn);
        // рассчет участников
        long members = memberService.getCountMembers(turn);
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
            newPosition.setDateEnd(null);
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
    }

    @Override
    @Transactional
    public List<PositionDTO> getPositionList(Long idTurn, int page) {
        Turn turn = turnService.getTurnFrom(idTurn);
        deleteOverdueElements(turn);
        long sizePositions = positionRepository.countByTurn(turn);
        int size;
        if (sizePositions>20){
            size = 20;
        }
        else{
            size = (int) sizePositions;
        }
        if (size==0){
            throw new NotFoundPosException("No positions found");
        }
        else{
            Pageable paging = PageRequest.of(page, size);
            Page<Position> positions = positionRepository.findAllByTurnOrderByIdAsc(turn, paging);
            if (positions.isEmpty()){
                throw new NotFoundPosException("No positions found");
            }
            else return positionListMapper.map(positions);
        }

    }

    @Override
    public void update(Long id) {
        Optional<Position> position = positionRepository.findById(id);
        if (position.isPresent()){
            Position posI = position.get();
            deleteOverdueElements(posI.getTurn());
            Optional<Position> positionO = positionRepository.findById(id);
            if (positionO.isPresent()){
                Position pos = positionO.get();
                if (pos.isStart()){
                    delete(id);
                }
                else{
                    pos.setStart(true);
                    positionRepository.save(pos);
                }
            }
        }
        else{
            throw new NotFoundPosException("No positions found");
        }

    }

    @Override
    @Transactional
    public void delete(Long id) {
        Optional<Position> position = positionRepository.findById(id);
        if (position.isPresent()) {
            Position pos = position.get();
            positionRepository.delete(pos);
            Optional<Position> p = positionRepository.findFirstByTurnOrderByIdAsc(pos.getTurn());
            if (p.isPresent()){
                Position changePosition = p.get();
                Date date = new Date();
                Calendar c = Calendar.getInstance();
                c.setTime(date);
                c.add(Calendar.MINUTE, 2);
                changePosition.setDateEnd(c.getTime());
                positionRepository.save(changePosition);
            }
        }
        else{
            throw new NotFoundPosException("No positions found");
        }
    }

    @Override
    @Transactional
    public PositionMoreInfoDTO getFirstUserPosition(Long turnId, String username) {


        UserDTO userDTO = userService.getUser(username);
        User user = userService.getUserFrom(userDTO.id());
        Turn turn = turnService.getTurnFrom(turnId);

        deleteOverdueElements(turn);

        Optional<Position> p = positionRepository.findTopByTurnAndUserOrderByIdAsc(turn, user);
        Optional<Position> pInTurn = positionRepository.findFirstByTurnOrderByIdAsc(turn);
        if (p.isPresent() && pInTurn.isPresent()){
            if (pInTurn.get().getId().equals(p.get().getId())){
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
