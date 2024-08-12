package com.eturn.eturn.service.impl;

import com.eturn.eturn.dto.*;
import com.eturn.eturn.dto.mapper.PositionListMapper;
import com.eturn.eturn.dto.mapper.PositionMoreInfoMapper;
import com.eturn.eturn.dto.mapper.TurnMapper;
import com.eturn.eturn.entity.*;
import com.eturn.eturn.enums.AccessMemberEnum;
import com.eturn.eturn.enums.AccessTurnEnum;
import com.eturn.eturn.exception.member.NoAccessMemberException;
import com.eturn.eturn.exception.member.NotFoundMemberException;
import com.eturn.eturn.exception.position.*;
import com.eturn.eturn.repository.PositionRepository;
import com.eturn.eturn.service.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.Date;

@Service
public class PositionServiceImpl implements PositionService {
    private final PositionRepository positionRepository;
    private final UserService userService;
    private final PositionListMapper positionListMapper;
    private final TurnService turnService;

    final private TurnMapper turnMapper;
    private final PositionMoreInfoMapper positionMoreInfoMapper;
    private final MemberService memberService;


    public PositionServiceImpl(PositionRepository positionRepository, UserService userService,
                               PositionListMapper positionListMapper, TurnService turnService, TurnMapper turnMapper, PositionMoreInfoMapper positionMoreInfoMapper, MemberService memberService) {
        this.positionRepository = positionRepository;
        this.userService = userService;
        this.positionListMapper = positionListMapper;
        this.turnService = turnService;
        this.turnMapper = turnMapper;
        this.positionMoreInfoMapper = positionMoreInfoMapper;
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
                if (timeBetween>0){
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
                    Optional<Position> positionFirstO2 = positionRepository.findFirstByTurnOrderByNumberAsc(turn);
                    if (positionFirstO2.isPresent()){
                        Position positionF = positionFirstO2.get();
                        Date date = new Date();
                        Calendar c = Calendar.getInstance();
                        c.setTime(date);
                        c.add(Calendar.MINUTE, turn.getTimer());
                        positionF.setDateEnd(c.getTime());
                        positionRepository.save(positionF);
                    }
                }
            }
        }
    }

    @Override
    @Transactional
    public PositionMoreInfoDTO createPositionAndSave(String login, String hash) {

        // получение основной информации
        Turn turn = turnService.getTurnFrom(hash);
        User user = userService.findByLogin(login);
        Optional<Member> member = memberService.getOptionalMember(user, turn);
        Optional<Position> lastPos = positionRepository.findFirstByTurnOrderByIdDesc(turn);
        Member currentMember;
        if (member.isEmpty()) {
            currentMember = addTurnToUser(user, turn);
        }
        else {
            currentMember = member.get();
            if (currentMember.getAccessMemberEnum() == AccessMemberEnum.MEMBER_LINK && turn.getAccessTurnType() == AccessTurnEnum.FOR_LINK){
                memberService.changeMemberStatusFrom(currentMember.getId(), "MEMBER", -1, -1);
            }
        }
        AccessMemberEnum access = currentMember.getAccessMemberEnum();
        if (access != AccessMemberEnum.BLOCKED && !currentMember.isInvitedForTurn()){
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

                    return positionMoreInfoMapper.positionMoreInfoToPositionDTO(p, differenceForUser);
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
                    return positionMoreInfoMapper.positionMoreInfoToPositionDTO(p, differenceForUser);
                }
            } else {
                Position p = createNewPosition(lastPos, currentMember);
                return positionMoreInfoMapper.positionMoreInfoToPositionDTO(p, 0);
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
        newPosition.setGroupName(user.getGroup().getNumber());
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
        PositionMoreInfoDTO userPosition = getFirstUserPosition(hash, username);
        PositionMoreInfoDTO turnPosition = getFirstPosition(hash, username);
        return new PositionsTurnDTO(userPosition, turnPosition, allPositions);

    }

    @Override
    @Transactional
    public void update(Long id, String username) {
        User user = userService.findByLogin(username);
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
        User user = userService.findByLogin(username);
        Optional<Position> position = positionRepository.findById(id);
        if (position.isPresent()) {
            Position pos = position.get();
            Optional<Member> oMember= memberService.getOptionalMember(user, pos.getTurn());
            if (oMember.isEmpty()){
                throw new NoAccessMemberException("you are not member");
            }
            Member member = oMember.get();
            AccessMemberEnum access = member.getAccessMemberEnum();
            if (access == AccessMemberEnum.MEMBER && pos.getUser()==user || access == AccessMemberEnum.CREATOR || access == AccessMemberEnum.MODERATOR) {
                positionRepository.delete(pos);
                Optional<Position> p = positionRepository.findFirstByTurnOrderByNumberAsc(pos.getTurn());
                if (p.isPresent()){
                    Position changePosition = p.get();
                    Date date = new Date();
                    Calendar c = Calendar.getInstance();
                    c.setTime(date);
                    c.add(Calendar.MINUTE, pos.getTurn().getTimer());
                    changePosition.setDateEnd(c.getTime());
                    positionRepository.save(changePosition);
                }
                Optional<Position> pUser = positionRepository.findFirstByUserAndTurnOrderByNumberAsc(pos.getUser(), pos.getTurn());
                if (pUser.isEmpty() && access == AccessMemberEnum.MEMBER && pos.getTurn().getAccessTurnType()==AccessTurnEnum.FOR_LINK){
                    memberService.changeMemberStatusFrom(member.getId(), "MEMBER_LINK", -1, -1);
                }
                else if (pUser.isEmpty() && access == AccessMemberEnum.MEMBER) {
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
        User user = userService.findByLogin(username);
        Member member = memberService.deleteMember(id, user);
        positionRepository.deletePositionsByUserAndTurn(user,member.getTurn());
    }

    @Override
    @Transactional
    public void changeMemberStatus(long id, String type, String username) {
        User user = userService.findByLogin(username);
        if (!Objects.equals(type, "MEMBER") && !Objects.equals(type, "BLOCKED")){
            throw new NoAccessMemberException("You cant change status on MODERATOR");
        }
        Member member = memberService.changeMemberStatus(id, type, user);
        boolean positionExist = positionRepository.existsAllByTurnAndUser(member.getTurn(), member.getUser());
        if (member.getAccessMemberEnum() == AccessMemberEnum.MEMBER){
            Turn turn = member.getTurn();
            if (
                    !positionExist
                    && turn.getAccessTurnType() == AccessTurnEnum.FOR_ALLOWED_ELEMENTS
            ){
                memberService.deleteMemberFrom(member.getId());
            } else if (
                    !positionExist
                            && turn.getAccessTurnType() == AccessTurnEnum.FOR_LINK
            ) {
                member = memberService.changeMemberStatus(id, "MEMBER_LINK", user);
            }
        }
        if (type.equals("BLOCKED")) {
            positionRepository.deleteAllByTurnAndUser(member.getTurn(), member.getUser());
        }
    }

    @Override
    @Transactional
    public void skipPosition(long id, String username) {
        User user = userService.findByLogin(username);
        Optional<Position> position = positionRepository.findById(id);
        if (position.isPresent()){
            Position p1 = position.get();
            deleteOverdueElements(p1.getTurn());
            Optional<Position> nowPos = positionRepository.findFirstByTurnOrderByNumberAsc(p1.getTurn());
            if (nowPos.isEmpty()) {
                throw new NoSkipPositionException("You cant skip position");
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
            } else {
                throw new NoSkipPositionException("You cant skip position");
            }
        }
    }

    @Override
    @Transactional
    public PositionMoreInfoDTO getFirstUserPosition(String hash, String username) {
        User user = userService.findByLogin(username);
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
                return positionMoreInfoMapper.positionMoreUserToPositionDTO(p.get(), difference, isLast);
            }
            else{
                int difference = (int) positionRepository.countNumbersLeft(p.get().getNumber(), turn);
                return positionMoreInfoMapper.positionMoreUserToPositionDTO(p.get(), difference, isLast);
            }
        }
        else{
            return null;
        }
    }

    @Override
    public PositionMoreInfoDTO getFirstPosition(String hash, String username) {
        User user = userService.findByLogin(username);
        Turn turn = turnService.getTurnFrom(hash);
        Optional<Position> pInTurn = positionRepository.findFirstByTurnOrderByNumberAsc(turn);
        if (pInTurn.isPresent()){
            Position pos = pInTurn.get();
            Optional<Member> optionalMember = memberService.getOptionalMember(user, pos.getTurn());
            if (optionalMember.isPresent()) {
                Member member = optionalMember.get();
                if (member.getAccessMemberEnum() == AccessMemberEnum.MODERATOR || member.getAccessMemberEnum() == AccessMemberEnum.CREATOR) {
                    int difference = 0;
                    return positionMoreInfoMapper.positionMoreInfoToPositionDTO(pos, difference);
                }
            }
        }
        return null;
    }

    @Override
    public Member addTurnToUser(User user, Turn turn) {
        AccessTurnEnum turnEnum = turn.getAccessTurnType();
        if (turnEnum == AccessTurnEnum.FOR_LINK) {
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
    public TurnDTO getTurn(String hash, String login) {
        User user = userService.findByLogin(login);
        Turn turn = turnService.getTurnFrom(hash);
        if (turn.getDateEnd().getTime() < new Date().getTime()) {
            turnService.deleteTurn(login, hash);
        }
        Optional<Member> m = memberService.getOptionalMember(user, turn);
        String access = null;
        boolean invited1 = false;
        boolean invited2 = false;
        boolean existsInvited = false;
        MembersCountDTO membersCountDTO = null;
        if (m.isPresent()){
            if (m.get().getAccessMemberEnum() == AccessMemberEnum.CREATOR || m.get().getAccessMemberEnum() == AccessMemberEnum.MODERATOR) {
                existsInvited = memberService.invitedExists(turn);
                 membersCountDTO = new MembersCountDTO(
                        (int) memberService.getCountModerators(turn),
                        (int) memberService.getCountMembers(turn),
                        memberService.countInviteMembers(turn),
                        memberService.countInviteModerators(turn),
                        (int) memberService.countBlocked(turn)
                );
            }
            access = m.get().getAccessMemberEnum().name();
            invited1 = m.get().isInvited();
            invited2 = m.get().isInvitedForTurn();
        }
        long count = positionRepository.countByTurn(turn);
        String accessType = "for_link";
        List<Long> list = new ArrayList<>();
        turn.setCountUsers((int)count);
        if (!turn.getAllowedGroups().isEmpty()){
            accessType = "groups";
            for (Group item: turn.getAllowedGroups()){
                list.add(item.getId());
            }
        } else if (!turn.getAllowedFaculties().isEmpty()){
            accessType = "faculties";
            for (Faculty f: turn.getAllowedFaculties()) {
                list.add(f.getId());
            }
        }
        turnService.saveTurn(turn);
        return turnMapper.turnToTurnDTO(
                turn,
                access,
                accessType,
                invited2,
                invited1,
                existsInvited,
                membersCountDTO,
                list
        );
    }
    @Override
    @Transactional
    public void inviteUser(String hash, String username) {
        User user = userService.findByLogin(username);
        Turn turn = turnService.getTurnFrom(hash);
        if (memberService.countInviteModerators(turn) > 20 || memberService.getCountModerators(turn) > 20) {
            throw new NoInviteException("You cant invite");
        }
        if (turn.getCreator() == user) {
            throw new NoAccessMemberException("You are creator");
        }
        Optional<Member> memberPresent = memberService.getOptionalMember(user, turn);
        if (memberPresent.isPresent()) {
            if (memberPresent.get().getAccessMemberEnum() == AccessMemberEnum.BLOCKED) {
                throw new NoAccessMemberException("You are blocked");
            }
            memberService.changeMemberInvite(memberPresent.get().getId(), true);
        } else {
            Member member = memberService.createMember(user, turn, "MEMBER_LINK", false);
            memberService.changeMemberInvite(member.getId(), true);
        }
    }

    @Override
    public void changeMemberInvite(Long id, boolean status, boolean isModerator) {
        Optional<Member> memberPresent = memberService.getMemberFrom(id);
        if (memberPresent.isPresent()) {
            Member member = memberPresent.get();
            boolean isInvited = member.isInvited();
            boolean isInvitedForTurn = member.isInvitedForTurn();
            if (status) {
                if (isInvited) {
                    if (isModerator) {
                        memberService.changeMemberStatusFrom(id, "MODERATOR", 0, 0);
                    }
                }
                if (isInvitedForTurn) {
                    if (!isModerator) {
                        if (isInvited) {
                            memberService.changeMemberStatusFrom(id, "MEMBER", 1, 0);
                        } else {
                            memberService.changeMemberStatusFrom(id, "MEMBER", 0, 0);
                        }
                    } else if (!isInvited){
                        throw new NoInviteException("User not invite to moderator");
                    }
                    createPositionAndSave(member.getUser().getLogin(), member.getTurn().getHash());
                }
            } else {
                if (isInvited && isModerator) {
                    memberService.changeMemberInvite(id, false);
                }
                if (isInvitedForTurn && !isModerator) {
                    if (isInvited) {
                        memberService.changeMemberStatusFrom(id, "MEMBER_LINK", 1, 0);
                    } else {
                        memberService.deleteMemberFrom(id);
                    }
                }
            }

        } else {
            throw new NotFoundMemberException("Member not found");
        }
    }

}
