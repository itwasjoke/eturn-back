package com.eturn.eturn.service.impl;

import com.eturn.eturn.dto.*;
import com.eturn.eturn.dto.mapper.DetailedPositionMapper;
import com.eturn.eturn.dto.mapper.PositionListMapper;
import com.eturn.eturn.entity.*;
import com.eturn.eturn.enums.AccessMember;
import com.eturn.eturn.enums.AccessTurn;
import com.eturn.eturn.enums.InvitedStatus;
import com.eturn.eturn.exception.member.NoAccessMemberException;
import com.eturn.eturn.exception.position.*;
import com.eturn.eturn.notifications.NotificationController;
import com.eturn.eturn.notifications.PositionsNotificationDTO;
import com.eturn.eturn.repository.PositionRepository;
import com.eturn.eturn.service.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.Date;

@Service
public class PositionServiceImpl implements PositionService {
    private static final Logger logger = LogManager.getLogger(PositionServiceImpl.class);
    private final PositionRepository positionRepository;
    private final UserService userService;
    private final PositionListMapper positionListMapper;
    private final TurnService turnService;
    private final NotificationController notificationController;
    private final DetailedPositionMapper detailedPositionMapper;
    private final MemberService memberService;

    public PositionServiceImpl(PositionRepository positionRepository,
                               UserService userService,
                               PositionListMapper positionListMapper,
                               TurnService turnService,
                               NotificationController notificationController,
                               DetailedPositionMapper detailedPositionMapper,
                               @Lazy MemberService memberService) {
        this.positionRepository = positionRepository;
        this.userService = userService;
        this.positionListMapper = positionListMapper;
        this.turnService = turnService;
        this.notificationController = notificationController;
        this.detailedPositionMapper = detailedPositionMapper;
        this.memberService = memberService;
    }

    @Transactional
    public void deleteOverdueElements(Turn turn){
        if (turn.getTimer() == 0 || turn.getDateStart().getTime() > new Date().getTime()) {
            return;
        }
        Optional<Position> positionFirstO = positionRepository.findFirstByTurnOrderByNumberAsc(turn);
        if (positionFirstO.isPresent()){
            Position positionFirst = positionFirstO.get();
            if (positionFirst.getDateEnd()!=null && !positionFirst.isStart()){
                Date dateNow = new Date();
                long timeBetween = dateNow.getTime() - positionFirst.getDateEnd().getTime();
                if (timeBetween>0) {
                    timeBetween = timeBetween / 1000;
                    timeBetween = timeBetween / 60;
                    timeBetween = timeBetween / turn.getTimer();
                    Position p = positionRepository.resultsPositionDeleteOverdueElements(turn.getId(), (int) timeBetween);
                    int number = 0;
                    if (p != null) {
                        number = p.getNumber();
                    }
                    positionRepository.deleteByTurnAndNumberLessThanEqual(turn, number);
                    memberService.deleteMembersWithoutPositions(turn);
                    logger.info(String.format("From turn %s deleted %d elements", turn.getName(), timeBetween));
                    Optional<Position> positionFirstO2 = positionRepository.findFirstByTurnOrderByNumberAsc(turn);
                    if (positionFirstO2.isPresent()){
                        Position positionF = positionFirstO2.get();
                        Date date = new Date();
                        Calendar c = Calendar.getInstance();
                        c.setTime(date);
                        c.add(Calendar.MINUTE, turn.getTimer());
                        positionF.setDateEnd(c.getTime());
                        positionRepository.save(positionF);
                        logger.info(String.format("From turn %s timer starts", turn.getName()));
                    }
                }
            }
        }
    }

    @Override
    @Transactional
    public DetailedPositionDTO createPositionAndSave(String login, String hash) {
        Turn turn = turnService.getTurnFrom(hash);
        User user = userService.getUserFromLogin(login);
        Optional<Member> member = memberService.getMemberWith(user, turn);
        Optional<Position> lastPos = positionRepository.findFirstByTurnOrderByIdDesc(turn);
        Member currentMember;
        if (member.isEmpty()) {
            currentMember = addTurnToUser(user, turn);
        } else {
            currentMember = member.get();
            if (currentMember.getAccessMember() == AccessMember.MEMBER_LINK) {
                switch (currentMember.getInvitedForTurn()) {
                    case ACCESS_IN:
                        memberService.changeMemberStatusFrom(
                                currentMember.getId(),
                                "MEMBER",
                                -1,
                                -1
                        );
                    case ACCESS_OUT:
                        memberService.changeMemberStatusFrom(
                                currentMember.getId(),
                                "MEMBER_LINK",
                                -1,
                                1
                        );
                }
            }
        }
        AccessMember access = currentMember.getAccessMember();
        if (access != AccessMember.BLOCKED && currentMember.getInvitedForTurn() == InvitedStatus.ACCESS_IN) {
            deleteOverdueElements(turn);
            // рассчет участников
            if (positionRepository.countAllByTurn(turn) > 0) {
                long countPositions = memberService.getCountMembers(turn);
                boolean isBig = true;
                // получение своей первой позиции
                Optional<Position> ourPosition = positionRepository.findFirstByUserAndTurnOrderByNumberDesc(user, turn);
                int PERMITTED_COUNT_PEOPLE_SYSTEM = turn.getPositionCount();
                if (PERMITTED_COUNT_PEOPLE_SYSTEM == -1 && ourPosition.isPresent()) {
                    throw new NoCreatePosException(String.valueOf(-1));
                } else if (PERMITTED_COUNT_PEOPLE_SYSTEM == -1) {
                    Position p = createNewPosition(lastPos, currentMember);
                    int differenceForUser = (int) positionRepository.countNumbersLeft(p.getNumber(), turn);
                    return detailedPositionMapper.positionMoreInfoToPositionDTO(p, differenceForUser);
                }
                int PERMITTED_COUNT_PEOPLE = PERMITTED_COUNT_PEOPLE_SYSTEM;
                if (PERMITTED_COUNT_PEOPLE_SYSTEM == 0) {
                    if (countPositions >= 20) {
                        PERMITTED_COUNT_PEOPLE = (int) (countPositions * 0.8);
                    } else if (ourPosition.isPresent()) {
                        throw new NoCreatePosException(String.valueOf(-1));
                    } else {
                        isBig = false;
                    }
                }

                if (countPositions < PERMITTED_COUNT_PEOPLE && ourPosition.isPresent()) {
                    throw new NoCreatePosException(String.valueOf(-1));
                }

                boolean isUserPosExist = false;
                int differenceForException = 0;
                if (isBig) {
                    // получение списка из PERMITTED_COUNT_PEOPLE позиций для поиска нашей позиции
                    Position positionDelete = positionRepository.resultsPositionDelete(turn.getId(), PERMITTED_COUNT_PEOPLE);
                    if (positionDelete != null) {
                        Optional<Position> positionOfUser = positionRepository.findFirstByTurnAndUserAndNumberGreaterThanOrderByNumberDesc(turn, user, positionDelete.getNumber());
                        if (positionOfUser.isPresent()) {
                            isUserPosExist = true;
                            if (lastPos.isPresent()){
                                int counts = positionRepository.countAllByNumberBetween(positionOfUser.get().getNumber(), lastPos.get().getNumber());
                                differenceForException = PERMITTED_COUNT_PEOPLE - counts + 1;
                            }

                        }
                    }
                }

                if (isUserPosExist) {
                    // если позиция существует, то делаем исключение
                    throw new NoCreatePosException(String.valueOf(differenceForException));
                } else {
                    Position p = createNewPosition(lastPos, currentMember);

                    // вычисляем разницу для позиции
                    int differenceForUser = -1;
                    if (ourPosition.isEmpty()) {
                        differenceForUser = (int) positionRepository.countNumbersLeft(p.getNumber(), turn);
                    }
                    return detailedPositionMapper.positionMoreInfoToPositionDTO(p, differenceForUser);
                }
            } else {
                Position p = createNewPosition(lastPos, currentMember);
                return detailedPositionMapper.positionMoreInfoToPositionDTO(p, 0);
            }
        }
        else{
            return null;
        }
    }

    public Position createNewPosition(Optional<Position> lastPos, Member currentMember) {
        // вычисляем номер позиции
        int lastNumberPosition = 1;
        if (lastPos.isPresent()) {
            Position lastPosition = lastPos.get();
            lastNumberPosition = lastPosition.getNumber() + 1;
        }
        Turn turn = currentMember.getTurn();
        User user = currentMember.getUser();
        // создаем новую позицию
        Position newPosition = new Position();
        newPosition.setStart(false);
        newPosition.setUser(user);
        newPosition.setTurn(turn);
        newPosition.setDateEnd(null);
        newPosition.setMember(currentMember);
        if (user.getGroup() != null) {
            newPosition.setGroupName(user.getGroup().getNumber());
        }
        newPosition.setNumber(lastNumberPosition);
        if (turn.getPositionCount() != 0) {
            newPosition.setSkipCount(turn.getPositionCount() / 5);
        } else {
            newPosition.setSkipCount((int)(memberService.getCountMembers(turn) / 10));
        }
        return positionRepository.save(newPosition);
    }

    @Override
    @Transactional
    public PositionsTurnDTO getPositionList(String hash, String username, int page) {
        Turn turn = turnService.getTurnFrom(hash);
        deleteOverdueElements(turn);
        long sizePositions = positionRepository.countAllByTurn(turn);
        List<PositionDTO> allPositions;
        int size = (int) Math.min(sizePositions, 20);

        if (size > 0) {
            Pageable paging = PageRequest.of(page, size);
            Page<Position> positions = positionRepository.findAllByTurnOrderByNumberAsc(turn, paging);
            allPositions = positions.isEmpty() ? null : positionListMapper.map(positions);
        } else {
            allPositions = null;
        }
        DetailedPositionDTO userPosition = getFirstUserPosition(hash, username);
        DetailedPositionDTO turnPosition = getFirstPosition(hash, username);
        return new PositionsTurnDTO(userPosition, turnPosition, allPositions);

    }

    @Override
    @Transactional
    public void update(Long id, String username, String status) {
        User user = userService.getUserFromLogin(username);
        Optional<Position> position = positionRepository.findById(id);
        if (position.isPresent()){
            Position posI = position.get();
            if (posI.isStart() && status.equals("in")) {
                return;
            }
            Turn turn = posI.getTurn();
            if (turn.getDateStart().getTime() > new Date().getTime())
                throw new DateNotArrivedPosException("The date has not come yet");
            MemberDTO memberDTO = memberService.getMemberDTO(user, posI.getTurn());
            String access = memberDTO.access();
            if (posI.getUser()==user
                    || access.equals(AccessMember.CREATOR.toString())
                    || access.equals(AccessMember.MODERATOR.toString()))
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
                            turn.setSmoothedValue((double)time);
                            turnService.saveTurn(turn);
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
        User user = userService.getUserFromLogin(username);
        Optional<Position> position = positionRepository.findById(id);
        if (position.isPresent()) {
            Position pos = position.get();
            Optional<Member> oMember= memberService.getMemberWith(user, pos.getTurn());
            if (oMember.isEmpty()){
                throw new NoAccessMemberException("you are not member");
            }
            Member member = oMember.get();
            AccessMember access = member.getAccessMember();
            if (access == AccessMember.MEMBER && pos.getUser()==user || access == AccessMember.CREATOR || access == AccessMember.MODERATOR) {
                positionRepository.delete(pos);
                Optional<Position> p = positionRepository.findFirstByTurnOrderByNumberAsc(pos.getTurn());
                if (p.isPresent()) {
                    Position changePosition = p.get();
                    notificationController.notifyUserOfTurnPositionChange(changePosition.getTurn().getId());
                    Date date = new Date();
                    Calendar c = Calendar.getInstance();
                    c.setTime(date);
                    c.add(Calendar.MINUTE, pos.getTurn().getTimer());
                    changePosition.setDateEnd(c.getTime());
                    positionRepository.save(changePosition);

                    Optional<Position> pUser = positionRepository.findFirstByUserAndTurnOrderByNumberAsc(pos.getUser(), pos.getTurn());
                    if (pUser.isEmpty() && access == AccessMember.MEMBER && pos.getTurn().getAccessTurnType() == AccessTurn.FOR_LINK) {
                        memberService.changeMemberStatusFrom(member.getId(), "MEMBER_LINK", -1, -1);
                    } else if (pUser.isEmpty() && access == AccessMember.MEMBER) {
                        memberService.deleteMemberWith(pos.getTurn(), user);
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
    public void skipPosition(long id, String username) {
        User user = userService.getUserFromLogin(username);
        Optional<Position> position = positionRepository.findById(id);
        if (position.isPresent()){
            Position p1 = position.get();
            deleteOverdueElements(p1.getTurn());
            Optional<Position> nowPos = positionRepository.findFirstByTurnOrderByNumberAsc(p1.getTurn());
            if (nowPos.isEmpty()) {
                return;
            }
            Position currPos = nowPos.get();
            Optional<Position> pNew = positionRepository.findFirstByTurnAndNumberGreaterThanOrderByNumberAsc(p1.getTurn(), position.get().getNumber());
            if (pNew.isPresent() && p1.getUser() == user && p1.getSkipCount() != 0) {
                Position p2 = pNew.get();
                int number1 = p1.getNumber();
                int number2 = p2.getNumber();
                Optional<Position> p3New = positionRepository.findFirstByTurnAndNumberGreaterThanOrderByNumberAsc(p2.getTurn(), p2.getNumber());
                if (p3New.isPresent()) {
                    Position p3 = p3New.get();
                    if (p1.getUser() == p3.getUser()) {
                        positionRepository.delete(p1);
                        return;
                    }
                }
                Optional<Position> pOld = positionRepository.findFirstByTurnAndNumberLessThanOrderByNumberDesc(p1.getTurn(), p1.getNumber());
                if (pOld.isPresent()) {
                    Position p0 = pOld.get();
                    if (p0.getUser() == p2.getUser()) {
                        positionRepository.delete(p0);
                    }
                }

                if (p1.getTurn().getDateStart().getTime() <= new Date().getTime() && Objects.equals(p1.getId(), currPos.getId())) {
                    p1.setDateEnd(null);
                    Date date = new Date();
                    Calendar c = Calendar.getInstance();
                    c.setTime(date);
                    c.add(Calendar.MINUTE, p2.getTurn().getTimer());
                    p2.setDateEnd(c.getTime());
                }
                p1.setNumber(number2);
                p2.setNumber(number1);
                p1.setSkipCount(p1.getSkipCount() - 1);
                positionRepository.save(p1);
                positionRepository.save(p2);
            }
        }
    }

    @Override
    @Transactional
    public DetailedPositionDTO getFirstUserPosition(String hash, String username) {
        User user = userService.getUserFromLogin(username);
        Turn turn = turnService.getTurnFrom(hash);
        Optional<Position> p = positionRepository.findTopByTurnAndUserOrderByNumberAsc(turn, user);
        Optional<Position> pInTurn = positionRepository.findFirstByTurnOrderByNumberAsc(turn);
        Optional<Position> pLast = positionRepository.findFirstByTurnOrderByNumberDesc(turn);
        boolean isLast = false;
        if (pLast.isPresent() && p.isPresent()) {
            if (p.get().getId().equals(pLast.get().getId())){
                isLast = true;
            }
        }
        if (p.isPresent() && pInTurn.isPresent()){
            if (pInTurn.get().getId().equals(p.get().getId())){
                int difference = 0;
                return detailedPositionMapper.positionMoreUserToPositionDTO(p.get(), difference, isLast);
            }
            else{
                int difference = (int) positionRepository.countNumbersLeft(p.get().getNumber(), turn);
                return detailedPositionMapper.positionMoreUserToPositionDTO(p.get(), difference, isLast);
            }
        }
        else{
            return null;
        }
    }

    @Override
    public DetailedPositionDTO getFirstPosition(String hash, String username) {
        User user = userService.getUserFromLogin(username);
        Turn turn = turnService.getTurnFrom(hash);
        Optional<Position> pInTurn = positionRepository.findFirstByTurnOrderByNumberAsc(turn);
        if (pInTurn.isPresent()){
            Position pos = pInTurn.get();
            Optional<Member> optionalMember = memberService.getMemberWith(user, pos.getTurn());
            if (optionalMember.isPresent()) {
                Member member = optionalMember.get();
                if (member.getAccessMember() == AccessMember.MODERATOR || member.getAccessMember() == AccessMember.CREATOR) {
                    int difference = 0;
                    return detailedPositionMapper.positionMoreInfoToPositionDTO(pos, difference);
                }
            }
        }
        return null;
    }

    @Override
    public Member addTurnToUser(User user, Turn turn) {
        AccessTurn turnEnum = turn.getAccessTurnType();
        if (turnEnum == AccessTurn.FOR_LINK) {
            notificationController.notifyReceiptRequest(turn.getId(), turn.getName());
            return memberService.createMember(user, turn, "MEMBER_LINK", true);
        } else {
            Set<Group> groups = turn.getAllowedGroups();
            Set<Faculty> faculties = turn.getAllowedFaculties();
            if (groups.contains(user.getGroup()) || faculties.contains(user.getGroup().getFaculty())) {
                return memberService.createMember(user, turn, "MEMBER", false);
            }
            else {
                throw new NoAccessMemberException("You are not this user!");
            }
        }
    }

    @Override
    public PositionsNotificationDTO getPositionsForNotify(Long turnId) {
        Pageable paging = PageRequest.of(0, 10);
        Page<Position> page = positionRepository.findAllByTurn_IdOrderByNumberAsc(turnId,paging);
        List<Position> list = page.toList();
        List<User> users = new ArrayList<>();
        logger.info("The turn is now up to " + list.size() + " values");
        if (!list.isEmpty()) {
            Position p1 = list.get(0);
            users.add(p1.getUser());
            if (list.size() > 4) users.add(list.get(4).getUser());
            if (list.size() > 9) users.add(list.get(9).getUser());
            return new PositionsNotificationDTO(users, p1.getTurn().getName());
        } else {
            logger.warn("No notifications will send for "+ turnId +" turn");
            return new PositionsNotificationDTO(null, null);
        }
    }

    @Override
    public long countPositionsByTurn(Turn turn) {
        return positionRepository.countByTurn(turn);
    }

    @Override
    public boolean existsAllByTurnAndUser(Turn turn, User user) {
        return positionRepository.existsAllByTurnAndUser(turn, user);
    }

    @Override
    public void deleteAllByTurnAndUser(Turn turn, User user) {
        positionRepository.deleteAllByTurnAndUser(turn, user);
    }
}
