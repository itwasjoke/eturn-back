package com.eturn.eturn.service.impl;

import com.eturn.eturn.dto.MemberDTO;
import com.eturn.eturn.dto.TurnDTO;
import com.eturn.eturn.dto.TurnMoreInfoDTO;
import com.eturn.eturn.dto.UserDTO;
import com.eturn.eturn.dto.mapper.TurnListMapper;
import com.eturn.eturn.dto.mapper.TurnMapper;
import com.eturn.eturn.dto.mapper.TurnMoreInfoMapper;
import com.eturn.eturn.entity.*;
import com.eturn.eturn.enums.AccessMemberEnum;
import com.eturn.eturn.enums.AccessTurnEnum;
import com.eturn.eturn.enums.RoleEnum;
import com.eturn.eturn.enums.TurnEnum;
import com.eturn.eturn.exception.turn.*;
import com.eturn.eturn.repository.TurnRepository;
import com.eturn.eturn.service.FacultyService;
import com.eturn.eturn.service.GroupService;
import com.eturn.eturn.service.MemberService;
import com.eturn.eturn.service.TurnService;
import com.eturn.eturn.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Stream;

@Service
public class TurnServiceImpl implements TurnService {
    private final TurnRepository turnRepository;
    private final UserService userService;
    private final GroupService groupService;
    private final FacultyService facultyService;
    private final MemberService memberService;
    final private  TurnMapper turnMapper;
    final private  TurnListMapper turnListMapper;

    final private TurnMoreInfoMapper turnMoreInfoMapper;


    public TurnServiceImpl(
            TurnRepository turnRepository,
            UserService userService,
            GroupService groupService,
            FacultyService facultyService,
            MemberService memberService,
            TurnMapper turnMapper,
            TurnListMapper turnListMapper,
            TurnMoreInfoMapper turnMoreInfoMapper) {
        this.turnRepository = turnRepository;
        this.userService = userService;
        this.groupService = groupService;
        this.facultyService = facultyService;
        this.memberService = memberService;
        this.turnMapper = turnMapper;
        this.turnListMapper = turnListMapper;
        this.turnMoreInfoMapper = turnMoreInfoMapper;
    }


    @Override
    public List<Turn> getAllTurns() {
        List<Turn> turns = turnRepository.findAll();
        if (turns.isEmpty()){
            throw new LocalNotFoundTurnException("empty turnList on getAllTurns method (TurnServiceImpl.java");
        }
        return turns;
    }

    @Override
    @Transactional
    public TurnDTO getTurn(Long id) {
        Optional<Turn> turn = turnRepository.findById(id);
        if (turn.isPresent()){
            Turn turnIn = turn.get();
            long members = memberService.getCountMembers(turnIn);
            turnIn.setCountUsers((int)members);
            turnRepository.save(turnIn);
            return turnMapper.turnToTurnDTO(turnIn);
        }
        else{
            throw new NotFoundTurnException("No turn in database on getTurn method (TurnServiceImpl.java)");
        }
    }
    @Override
    public Turn getTurnFrom(Long id) {
        Optional<Turn> turn = turnRepository.findById(id);
        if(turn.isPresent()){
            return turn.get();
        }
        else{
            throw new LocalNotFoundTurnException("No turn in database on getTurnFrom method (TurnServiceImpl.java)");
        }
    }
    @Override
    public List<TurnDTO> getUserTurns(String login, Map<String, String> params) {

        List<Turn> allTurns = turnRepository.findAll();
        if (allTurns.isEmpty()){
            throw new NotFoundAllTurnsException("No turn in database on getUserTurns method (TurnServiceImpl.java)");
        }
        Stream<Turn> streamTurns = allTurns.stream();
        User user = userService.findByLogin(login);
        if (user.getRoleEnum() == RoleEnum.STUDENT) {
            streamTurns = streamTurns.filter(
                    c -> c.getAccessTurnType() == AccessTurnEnum.FOR_ALLOWED_ELEMENTS ||
                    c.getAccessTurnType() == AccessTurnEnum.FOR_LINK
            );
        }


        for (Map.Entry<String, String> entry : params.entrySet()) {
            String value = entry.getValue();
            switch (entry.getKey()) {
                case "Access" -> {
                    Set<Turn> userTurns = userService.getUserTurns(user.getId());
                    if (value.equals("memberIn")) {
                        streamTurns = streamTurns.filter(userTurns::contains);
                    } else if (value.equals("memberOut")) {
                        streamTurns = streamTurns.filter(c -> !userTurns.contains(c) && c.getAccessTurnType() != AccessTurnEnum.FOR_LINK);
                        if (user.getRoleEnum()==RoleEnum.STUDENT){
                            if (user.getIdGroup()!=null && user.getIdFaculty()!=null){
                                Group groupThis = groupService.getGroup(user.getIdGroup());
                                Faculty facultyThis = facultyService.getOneFaculty(user.getIdFaculty());
                                if (groupThis!=null && facultyThis !=null){
                                    streamTurns = streamTurns.filter(c-> c.getAllowedGroups().contains(groupThis)
                                            || c.getAllowedFaculties().contains(facultyThis));
                                }
                            }
                        }
                    } else {
                        throw new InvalidTypeTurnException("In function GetUserTurns (TurnServiceImpl.java) error: Turn type is " + value + ". Value can be: 'memberIn' or 'memberOut'.");
                    }
                }
                case "Type" -> {
                    TurnEnum type;
                    if (Objects.equals(value, "org")) {
                        type = TurnEnum.ORG;
                    } else if (Objects.equals(value, "edu")) {
                        type = TurnEnum.EDU;
                    } else {
                        throw new InvalidTypeTurnException("In function GetUserTurns (TurnServiceImpl.java) error: Turn type is " + value + ". Value can be: 'org' or 'edu'.");
                    }
                    streamTurns = streamTurns.filter(c -> c.getTurnType() == type);
                }
                case "Group" -> {
                    Group group = groupService.getOneGroup(value);
                    streamTurns = streamTurns.filter(c -> c.getAllowedGroups().contains(group));
                }
                case "Faculty" -> {
                    Faculty faculty = facultyService.getOneFaculty(Long.parseLong(value));
                    streamTurns = streamTurns.filter(c -> c.getAllowedFaculties().contains(faculty));
                }
            }

        }
        List<Turn> endTurns = streamTurns.toList();
        if (endTurns.isEmpty()){
            throw new NotFoundAllTurnsException("Search filters resulted in zero results.");
        }
        return turnListMapper.map(endTurns);

    }

    @Override
    @Transactional
    public Long createTurn(TurnMoreInfoDTO turn, String login) {
        UserDTO userDTO = userService.getUser(login);
        User userCreator = userService.getUserFrom(userDTO.id());
        Set<Group> groups = groupService.getSetGroups(turn.allowedGroups());
        Turn turnDto = turnMoreInfoMapper.turnMoreDTOToTurn(turn,userCreator, groups);
        Turn turnNew = turnRepository.save(turnDto);
        addTurnToUser(turnNew.getId(), login, "CREATOR");
        return turnNew.getId();
    }

    @Override
    @Transactional
    public void updateTurn(Long idUser, Turn turn) {
        User user = userService.getUserFrom(idUser);
//        Turn turnUpdated = turnMapper.turnDTOToTurn(turn);
        AccessMemberEnum accessMemberEnum = memberService.getAccess(user, turn);
        if (accessMemberEnum == AccessMemberEnum.CREATOR) {
            if (turnRepository.existsTurnById(turn.getId())){
                Turn turnFromDb = turnRepository.getReferenceById(turn.getId());
                turnFromDb.setName(turn.getName());
                turnRepository.save(turnFromDb);
            }
            else{
                throw new NotFoundTurnException("No turn in database on getTurn method (TurnServiceImpl.java)");
            }
        }
        else{
            throw new NoAccessUpdateTurnException("Only creator can update turn information on updateTurn method (TurnServiceImpl.java)");
        }
    }
    @Override
    @Transactional
    public void deleteTurn(Long idUser, Long idTurn) {
        Optional<Turn> turn = turnRepository.findById(idTurn);
        User user = userService.getUserFrom(idUser);
        if (turn.isEmpty()){
            throw new NotFoundTurnException("No turn in database on deleteTurn method (TurnServiceImpl.java)");
        }
        AccessMemberEnum access = memberService.getAccess(user, turn.get());
        if (access == AccessMemberEnum.CREATOR) {
            memberService.deleteTurnMembers(turn.get());
            turnRepository.deleteTurnById(idTurn);
        } else {
            throw new NoAccessDeleteTurnException("Only creator can delete turn information on deleteTurn method (TurnServiceImpl.java)");
        }
    }

    @Override
    public void countUser(Turn turn) {

    }

    @Override
    @Transactional
    public void addTurnToUser(Long turnId, String login, String access) {
        User user = userService.findByLogin(login);
        Turn turn = getTurnFrom(turnId);
        memberService.createMember(user, turn, access);
    }



    @Override
    public MemberDTO getMember(String username, Long idTurn) {

        UserDTO userDTO = userService.getUser(username);
        User user = userService.getUserFrom(userDTO.id());
        Optional<Turn> turn = turnRepository.findById(idTurn);
        if (turn.isEmpty()){
            throw new NotFoundTurnException("turn not found in getMember function");
        }
        return memberService.getMember(user,turn.get());

    }

    @Override
    public List<MemberDTO> getMemberList(String username, String type, Long turnId) {
        Optional<Turn> turn = turnRepository.findById(turnId);
        if (turn.isEmpty()){
            throw new NotFoundTurnException("turn not found in getMember function");
        }
        return memberService.getMemberList(turn.get(), type);
    }

}
