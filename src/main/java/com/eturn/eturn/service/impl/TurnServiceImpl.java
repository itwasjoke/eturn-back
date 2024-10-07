package com.eturn.eturn.service.impl;

import com.eturn.eturn.dto.*;
import com.eturn.eturn.dto.mapper.*;
import com.eturn.eturn.entity.*;
import com.eturn.eturn.enums.AccessMember;
import com.eturn.eturn.enums.AccessTurn;
import com.eturn.eturn.enums.Role;
import com.eturn.eturn.exception.member.NoAccessMemberException;
import com.eturn.eturn.exception.turn.*;
import com.eturn.eturn.notifications.NotificationController;
import com.eturn.eturn.repository.TurnRepository;
import com.eturn.eturn.security.HashGenerator;
import com.eturn.eturn.service.MemberService;
import com.eturn.eturn.service.PositionService;
import com.eturn.eturn.service.TurnService;
import com.eturn.eturn.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class TurnServiceImpl implements TurnService {

    private static Logger logger = LogManager.getLogger(TurnServiceImpl.class);
    private final TurnRepository turnRepository;
    private final UserService userService;
    private final MemberService memberService;
    private final TurnCreatingMapper turnCreatingMapper;
    private final TurnForListMapper turnForListMapper;
    private final GroupMapper groupMapper;
    private final FacultyMapper facultyMapper;
    private final NotificationController notificationController;
    private final TurnMapper turnMapper;
    private final PositionService positionService;

    public TurnServiceImpl(
            TurnRepository turnRepository,
            UserService userService,
            @Lazy MemberService memberService,
            TurnCreatingMapper turnCreatingMapper,
            TurnForListMapper turnForListMapper,
            GroupMapper groupMapper,
            FacultyMapper facultyMapper,
            NotificationController notificationController,
            TurnMapper turnMapper,
            @Lazy PositionService positionService) {
        this.turnRepository = turnRepository;
        this.userService = userService;
        this.memberService = memberService;
        this.turnCreatingMapper = turnCreatingMapper;
        this.turnForListMapper = turnForListMapper;
        this.groupMapper = groupMapper;
        this.facultyMapper = facultyMapper;
        this.notificationController = notificationController;
        this.turnMapper = turnMapper;
        this.positionService = positionService;
    }
    @Override
    public Turn getTurnFrom(String hash) {
        Optional<Turn> turn = turnRepository.findTurnByHash(hash);
        if(turn.isPresent()){
            return turn.get();
        }
        else{
            throw new LocalNotFoundTurnException("No turn in database on getTurnFrom method (TurnServiceImpl.java)");
        }
    }
    @Transactional
    @Override
    public List<TurnForListDTO> getUserTurns(String login, Map<String, String> params) {
        User user = userService.getUserFromLogin(login);
        String access = params.get("Access");
        List<Object[]> allTurns = new ArrayList<>();
        Date now = new Date();
        turnRepository.deleteByDateEndIsLessThan(now);
        if (Objects.equals(access, "memberOut")) {
            long facultyId = 0L;
            long groupId = 0L;
            Group group = user.getGroup();
            if (group!=null){
                groupId = group.getId();
                if (group.getFaculty()!=null){
                    facultyId = group.getFaculty().getId();
                }
            }
            allTurns = turnRepository.resultsMemberOut(user.getId(), groupId, facultyId, params.get("Type"));
        } else if (Objects.equals(access, "memberIn")){
            allTurns = turnRepository.resultsMemberIn(user.getId(), params.get("Type"));
        }
        List<TurnForListDTO> turnForList = new ArrayList<>();
        if (allTurns.isEmpty()){
            return turnForList;
        }
        for ( Object[] obj : allTurns ) {
            turnForList.add ( turnForListMapper.turnToTurnForListDTO( (Turn) obj[0], (String) obj[1] ));
        }
        return turnForList;
    }

    @Override
    public TurnDTO getTurn(String hash, String login) {
        User user = userService.getUserFromLogin(login);
        Turn turn = getTurnFrom(hash);
        if (turn.getDateEnd().getTime() < new Date().getTime()) {
            deleteTurn(login, hash);
            throw new NotFoundTurnException("Turn was deleted");
        }
        Optional<Member> m = memberService.getMemberWith(user, turn);
        String access = null;
        boolean invited1 = false;
        String invited2 = "ACCESS_OUT";
        boolean existsInvited = false;
        MembersCountDTO membersCountDTO = null;
        if (m.isPresent()){
            Member member = m.get();
            if (member.getAccessMember() == AccessMember.CREATOR || member.getAccessMember() == AccessMember.MODERATOR) {
                existsInvited = memberService.invitedExists(turn);
                membersCountDTO = new MembersCountDTO(
                        (int) memberService.getCountModerators(turn),
                        (int) memberService.getCountMembers(turn),
                        memberService.countInviteMembers(turn),
                        memberService.countInviteModerators(turn),
                        (int) memberService.countBlocked(turn)
                );
            }
            access = member.getAccessMember().name();
            invited1 = member.isInvited();
            invited2 = member.getInvitedForTurn().toString();
        }
        long count = positionService.countPositionsByTurn(turn);
        String accessType = "for_link";
        List<Long> list = new ArrayList<>();
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
        return turnMapper.turnToTurnDTO(
                turn,
                access,
                accessType,
                invited2,
                invited1,
                existsInvited,
                membersCountDTO,
                list,
                count
        );
    }

    @Override
    @Transactional
    public String createTurn(TurnCreatingDTO turnDTO, String login) {
        if (turnDTO.dateEnd().getTime() > turnDTO.dateStart().getTime()) {
            User user = userService.getUserFromLogin(login);
            Role R = user.getRole();
            int countAvailable = 0;
            int countTurns = turnRepository.countAllByCreator(user);
            switch (R) {
                case STUDENT -> countAvailable = 5 - countTurns;
                case EMPLOYEE -> countAvailable = 50 - countTurns;
            }
            if (countAvailable<1) {
                throw new NoCreateTurnException("too many turns");
            }
            Date now = new Date();
            long timeDiff = turnDTO.dateEnd().getTime() - turnDTO.dateStart().getTime();
            long year = 1000*60*60*24*365L;
            long month = 1000*60*60*24*31L;
            if (user.getRole() == Role.STUDENT && (timeDiff > 1000*60*60*24*3 || timeDiff < 0))
                throw new InvalidTimeToCreateTurnException("Invalid turn duration");
            if (user.getRole() == Role.EMPLOYEE && (timeDiff > year || timeDiff < 0))
                throw new InvalidTimeToCreateTurnException("Invalid turn duration");
            if (user.getRole() == Role.STUDENT && ((turnDTO.dateStart().getTime() - now.getTime() > month) || (turnDTO.dateStart().getTime() - now.getTime() < -1000*60*2)))
                throw new InvalidLengthTurnException("The turn is too long (or short)");
            if (user.getRole() == Role.EMPLOYEE && ((turnDTO.dateStart().getTime() - now.getTime() > month * 3) || (turnDTO.dateStart().getTime() - now.getTime() < -1000*60*2)))
                throw new InvalidLengthTurnException("The turn is too long (or short)");
            Turn turn = turnCreatingMapper.turnMoreDTOToTurn(turnDTO, user);
            StringBuilder allowedElements = new StringBuilder(" ");
            if (turn.getAccessTurnType() == AccessTurn.FOR_ALLOWED_ELEMENTS) {
                if (turn.getAllowedGroups() != null) {
                    for (Group group : turn.getAllowedGroups()) {
                        allowedElements.append(group.getNumber()).append(" ");
                        notificationController.notifyTurnCreated(group.getId(), turn.getName());
                    }
                }
                if (turn.getAllowedFaculties() != null) {
                    for (Faculty faculty : turn.getAllowedFaculties()) {
                        allowedElements.append(faculty.getName()).append(" ");
                    }
                }
            }
            turn.setAccessTags(allowedElements.toString().trim());
            String tags = turn.getName() + " " + turn.getDescription() + allowedElements + user.getName();
            turn.setTags(tags);
            String hash = HashGenerator.generateUniqueCode();
            int count = 0;
            while (turnRepository.existsAllByHash(hash)) {
                hash = HashGenerator.generateUniqueCode();
                count++;
                if (count>50) {
                    break;
                }
            }
            if (count>50) {
                throw new InvalidDataTurnException("error");
            }
            turn.setHash(hash);
            Turn turnWithHash = turnRepository.save(turn);
            memberService.createMember(user, turnWithHash, "CREATOR", false);
            return turnWithHash.getHash();
        } else
            throw new InvalidDataTurnException("The dateEnd cannot be earlier than the dateStart on createTurn method (TurnServiceImpl.java)");
    }

    @Override
    @Transactional
    public void deleteTurn(String username, String hash) {
        Optional<Turn> turn = turnRepository.findTurnByHash(hash);
        User user = userService.getUserFromLogin(username);
        if (turn.isEmpty()){
            throw new NotFoundTurnException("No turn in database on deleteTurn method (TurnServiceImpl.java)");
        }
        AccessMember access = memberService.getAccess(user, turn.get());
        if (access == AccessMember.CREATOR) {
            turnRepository.deleteTurnById(turn.get().getId());
        } else {
            throw new NoAccessDeleteTurnException("Only creator can delete turn information on deleteTurn method (TurnServiceImpl.java)");
        }
    }
    @Override
    public void saveTurn(Turn turn) {
        turnRepository.save(turn);
    }

    @Override
    public int getCountTurnsOfUser(User user) {
        return turnRepository.countAllByCreator(user);
    }

    @Override
    public List<TurnForListDTO> getLinkedTurn(String hash, String username) {
        Optional<Turn> t = turnRepository.findTurnByHash(hash);
        if (t.isEmpty()){
            throw new NotFoundAllTurnsException("no found");
        }
        Turn turn = t.get();
        User user = userService.getUserFromLogin(username);
        Optional<Member> member = memberService.getMemberWith(user, turn);
        String access;
        access = member.map(value -> value.getAccessMember().toString()).orElse(null);
        List<TurnForListDTO> turnList = new ArrayList<>();
        if (access != null) {
            if (access.equals(AccessMember.BLOCKED.toString())) {
                return turnList;
            }
        }
        turnList.add(turnForListMapper.turnToTurnForListDTO(turn, access));
        return turnList;
    }

    @Override
    public void changeTurn(TurnEditDTO turn, String username) {
        Optional<Turn> currentTurn = turnRepository.findTurnByHash(turn.hash());
        User user = userService.getUserFromLogin(username);
        if (currentTurn.isPresent()) {
            Turn newTurn = currentTurn.get();
            if (newTurn.getCreator() == user) {
                if (turn.name() != null) {
                    newTurn.setName(turn.name());
                }
                newTurn.setDescription(turn.description());
                if (turn.allowedGroups() != null && newTurn.getAllowedGroups() != null) {
                    if (!turn.allowedGroups().isEmpty() && !newTurn.getAllowedGroups().isEmpty()) {
                        Set<Group> groups = newTurn.getAllowedGroups();
                        for (GroupDTO groupDTO : turn.allowedGroups()) {
                            groups.add(groupMapper.dtoToGroup(groupDTO));
                        }
                        newTurn.setAllowedGroups(groups);
                    }
                }
                if (turn.allowedFaculties() != null && newTurn.getAllowedFaculties() != null) {
                    if (!turn.allowedFaculties().isEmpty() && !newTurn.getAllowedFaculties().isEmpty()) {
                        Set<Faculty> faculties = newTurn.getAllowedFaculties();
                        for (FacultyDTO facultyDTO : turn.allowedFaculties()) {
                            faculties.add(facultyMapper.dtoToFaculty(facultyDTO));
                        }
                        newTurn.setAllowedFaculties(faculties);
                    }
                }
                if (turn.timer() != null) {
                    newTurn.setTimer(turn.timer());
                }
                if (turn.positionCount() != null) {
                    newTurn.setPositionCount(turn.positionCount());
                }
                Set<String> groupsName = new HashSet<>();
                Set<String> facultiesName = new HashSet<>();
                if (newTurn.getAccessTurnType() == AccessTurn.FOR_ALLOWED_ELEMENTS) {
                    if (newTurn.getAllowedGroups() != null) {
                        for (Group group : newTurn.getAllowedGroups()) {
                            groupsName.add(group.getNumber());
                        }
                    }
                    if (newTurn.getAllowedFaculties() != null) {
                        for (Faculty faculty : newTurn.getAllowedFaculties()) {
                            facultiesName.add(faculty.getName());
                        }
                    }
                }
                String group = String.join(" ", groupsName);
                String faculties = String.join(" ", facultiesName);
                if (turn.allowedGroups() != null) {
                    if (!turn.allowedGroups().isEmpty()) {
                        newTurn.setAccessTags(group);
                    }
                }
                if (turn.allowedFaculties() != null) {
                    if (!turn.allowedFaculties().isEmpty()) {
                        newTurn.setAccessTags(faculties);
                    }
                }
                String tags = newTurn.getName() + " " + newTurn.getDescription() + " " + group + faculties + " " + newTurn.getCreator().getName();
                newTurn.setTags(tags);
                saveTurn(newTurn);
            } else {
                throw new NoAccessMemberException("No access");
            }
        } else {
            throw new NotFoundTurnException("Turn not found");
        }
    }

}
