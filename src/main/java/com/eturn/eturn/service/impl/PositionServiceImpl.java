package com.eturn.eturn.service.impl;

import com.eturn.eturn.dto.*;
import com.eturn.eturn.dto.mapper.PositionListMapper;
import com.eturn.eturn.dto.mapper.PositionMapper;
import com.eturn.eturn.dto.mapper.PositionMoreInfoMapper;
import com.eturn.eturn.entity.*;
import com.eturn.eturn.enums.AccessMemberEnum;
import com.eturn.eturn.exception.position.DateNotArrivedPosException;
import com.eturn.eturn.exception.position.NoAccessPosException;
import com.eturn.eturn.exception.position.NoCreatePosException;
import com.eturn.eturn.exception.position.NotFoundPosException;
import com.eturn.eturn.repository.PositionRepository;
import com.eturn.eturn.service.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Stream;
import java.util.Date;

@Service
public class PositionServiceImpl implements PositionService {
    private final PositionRepository positionRepository;
    private final UserService userService;
    private final PositionListMapper positionListMapper;
    private final TurnService turnService;
    private final PositionMoreInfoMapper positionMoreInfoMapper;
    private final MemberService memberService;


    public PositionServiceImpl(PositionRepository positionRepository, UserService userService,
                               PositionListMapper positionListMapper, TurnService turnService, PositionMoreInfoMapper positionMoreInfoMapper, MemberService memberService) {
        this.positionRepository = positionRepository;
        this.userService = userService;
        this.positionListMapper = positionListMapper;
        this.turnService = turnService;
        this.positionMoreInfoMapper = positionMoreInfoMapper;
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
                    Optional<Position> positionFirstO2 = positionRepository.findFirstByTurnOrderByIdAsc(turn);
                    if (positionFirstO2.isPresent()){
                        Position positionF = positionFirstO2.get();
                        Date date = new Date();
                        Calendar c = Calendar.getInstance();
                        c.setTime(date);
                        c.add(Calendar.MINUTE, 2);
                        positionF.setDateEnd(c.getTime());
                        positionRepository.save(positionF);
                    }
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
        MemberDTO memberDTO = memberService.getMember(user, turn);
        if (!memberDTO.access().equals("BLOCKED")){
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
        else{
            throw new NoAccessPosException("You are blocked");
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
    public void update(Long id, String username) {
        UserDTO userDTO = userService.getUser(username);
        User user = userService.getUserFrom(userDTO.id());
        Optional<Position> position = positionRepository.findById(id);
        if (position.isPresent()){
            Position posI = position.get();
            Turn turn = posI.getTurn();
            if (turn.getDateStart().getTime() > new Date().getTime())
                throw new DateNotArrivedPosException("The date has not come yet");
            MemberDTO memberDTO = memberService.getMember(user, posI.getTurn());
            String access = memberDTO.access();
            if (posI.getUser()==user
                    || access.equals(AccessMemberEnum.CREATOR.toString())
                    || access.equals(AccessMemberEnum.MODERATOR.toString()))
            {
                deleteOverdueElements(posI.getTurn());
                Optional<Position> positionO = positionRepository.findById(id);
                if (positionO.isPresent()){
                    Position pos = positionO.get();
                    if (pos.isStart()){
                        delete(id, user.getUsername());
                        long time = new Date().getTime() - pos.getDateStart().getTime();
                        int countPositions = turn.getCountPositionsLeft();
                        if (countPositions == 0) {
                            countPositions++;
                            turn.setCountPositionsLeft(countPositions);
                            turn.setAverageTime((int)time);
                            turn.setTotalTime(time);
                            turn.setSmoothedValue((int)time);
                        }
                        else {
                            countPositions++;
                            double smoothedValue = 0.99 * time + (1-0.99) * turn.getSmoothedValue();
                            long totalTime = turn.getTotalTime();
                            totalTime += (long) smoothedValue;
                            int averageTime = (int) totalTime/countPositions;
                            turn.setSmoothedValue(smoothedValue);
                            turn.setCountPositionsLeft(countPositions);
                            turn.setAverageTime(averageTime);
                            turnService.saveTurn(turn);
                        }
                    }
                    else{
                        pos.setStart(true);
                        pos.setDateStart(new Date());
                        positionRepository.save(pos);
                    }
                }
            }
            else{
                throw new NoAccessPosException("No access");
            }

        }
        else{
            throw new NotFoundPosException("No positions found");
        }

    }

    @Override
    @Transactional
    public void delete(Long id, String username) {
        UserDTO userDTO = userService.getUser(username);
        User user = userService.getUserFrom(userDTO.id());
        Optional<Position> position = positionRepository.findById(id);
        if (position.isPresent()) {
            Position pos = position.get();
            MemberDTO memberDTO = memberService.getMember(user, pos.getTurn());
            String access = memberDTO.access();
            if ((pos.getUser()==user && access.equals(AccessMemberEnum.MEMBER.toString()))
                    || access.equals(AccessMemberEnum.CREATOR.toString())
                    || access.equals(AccessMemberEnum.MODERATOR.toString()))
            {
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
                else if (access.equals(AccessMemberEnum.MEMBER.toString())) {
                    memberService.deleteMemberFrom(pos.getTurn(), user);
                }
            }
            else{
                throw new NoAccessPosException("No access");
            }

        }
        else{
            throw new NotFoundPosException("No positions found");
        }
    }

    @Override
    @Transactional
    public void deleteMember(long id, String username) {
        UserDTO userDTO = userService.getUser(username);
        User user = userService.getUserFrom(userDTO.id());
        Member member = memberService.getMemberFrom(id);
        User userMember = member.getUser();
        Turn turn = member.getTurn();
        memberService.deleteMember(id, user);
        positionRepository.deletePositionsByUserAndTurn(userMember,turn);
    }

    @Override
    @Transactional
    public void changeMemberStatus(long id, String type, String username) {
        UserDTO userDTO = userService.getUser(username);
        User user = userService.getUserFrom(userDTO.id());
        Member member = memberService.getMemberFrom(id);
        User userMember = member.getUser();
        Turn turn = member.getTurn();
        memberService.changeMemberStatus(id, type, user);
        if (type.equals("BLOCKED")){
            positionRepository.deletePositionsByUserAndTurn(userMember,turn);
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

    @Override
    public PositionMoreInfoDTO getFirstPosition(Long turnId, String username) {
        UserDTO userDTO = userService.getUser(username);
        User user = userService.getUserFrom(userDTO.id());
        Turn turn = turnService.getTurnFrom(turnId);

        deleteOverdueElements(turn);
        Optional<Position> pInTurn = positionRepository.findFirstByTurnOrderByIdAsc(turn);
        if (pInTurn.isPresent()){
            Position pos = pInTurn.get();
            MemberDTO memberDTO = memberService.getMember(user, pos.getTurn());
            if (memberDTO.access().equals("MODERATOR") || memberDTO.access().equals("CREATOR")){
                int difference = 0;
                return positionMoreInfoMapper.positionMoreInfoToPositionDTO(pos, difference);
            }
            else{
                throw new NoAccessPosException("No access");
            }
        }
        else{
            throw new NotFoundPosException("No positions found");
        }
    }

    @Override
    public void addTurnToUser(User user, Turn turn) {
        memberService.createMember(user, turn, "MEMBER");
    }

}
