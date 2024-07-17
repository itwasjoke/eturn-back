package com.eturn.eturn.service.impl;

import com.eturn.eturn.dto.*;
import com.eturn.eturn.dto.mapper.TurnForListMapper;
import com.eturn.eturn.dto.mapper.TurnMapper;
import com.eturn.eturn.dto.mapper.TurnCreatingMapper;
import com.eturn.eturn.entity.*;
import com.eturn.eturn.enums.AccessMemberEnum;
import com.eturn.eturn.enums.RoleEnum;
import com.eturn.eturn.exception.turn.*;
import com.eturn.eturn.repository.TurnRepository;
import com.eturn.eturn.service.FacultyService;
import com.eturn.eturn.service.GroupService;
import com.eturn.eturn.service.MemberService;
import com.eturn.eturn.service.TurnService;
import com.eturn.eturn.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

@Service
public class TurnServiceImpl implements TurnService {
    private final TurnRepository turnRepository;
    private final UserService userService;
    private final GroupService groupService;
    private final FacultyService facultyService;
    private final MemberService memberService;
    final private  TurnMapper turnMapper;

    final private TurnCreatingMapper turnCreatingMapper;
    final private TurnForListMapper turnForListMapper;


    public TurnServiceImpl(
            TurnRepository turnRepository,
            UserService userService,
            GroupService groupService,
            FacultyService facultyService,
            MemberService memberService,
            TurnMapper turnMapper,
            TurnCreatingMapper turnCreatingMapper, TurnForListMapper turnForListMapper) {
        this.turnRepository = turnRepository;
        this.userService = userService;
        this.groupService = groupService;
        this.facultyService = facultyService;
        this.memberService = memberService;
        this.turnMapper = turnMapper;
        this.turnCreatingMapper = turnCreatingMapper;
        this.turnForListMapper = turnForListMapper;
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
    @Transactional
    @Override
    public List<TurnForListDTO> getUserTurns(String login, Map<String, String> params) {
        User user = userService.findByLogin(login);

//        List<Turn> allTurns = new ArrayList<>();
//        List<String> allAccessMemberTypes = new ArrayList<>();
//        turnRepository.results(user.getId(), user.getIdGroup(), user.getIdFaculty()).forEach((record) -> {
//            Turn turn = (Turn)record[0];
//            String accessMemberType = (String)record[1];
//            assert false;
//            allTurns.add(turn);
//            allAccessMemberTypes.add(accessMemberType);
//        });
        String access = params.get("Access");
        List<Object[]> allTurns = new ArrayList<>();
        Date now = new Date();
        turnRepository.deleteByDateEndIsLessThan(now);
        turnRepository.deleteOldTurns(now);
        if (Objects.equals(access, "memberOut")) {
            allTurns = turnRepository.resultsMemberOut(user.getId(), user.getIdGroup(), user.getIdFaculty(), params.get("Type"));
        } else if (Objects.equals(access, "memberIn")){
            allTurns = turnRepository.resultsMemberIn(user.getId(), params.get("Type"));
        }
        if (allTurns.isEmpty()){
            throw new NotFoundAllTurnsException("No turn in database on getUserTurns method (TurnServiceImpl.java)");
        }
        List<TurnForListDTO> turnForList = new ArrayList<>();
        for ( Object[] obj : allTurns ) {
            turnForList.add ( turnForListMapper.turnToTurnForListDTO( (Turn) obj[0], (String) obj[1] ));
        }
        return turnForList;
    }

    @Override
    @Transactional
    public Long createTurn(TurnCreatingDTO turnDTO, String login) {
        if (turnDTO.dateEnd().getTime() > turnDTO.dateStart().getTime()) {
            UserDTO userDTO = userService.getUser(login);
            User user = userService.getUserFrom(userDTO.id());
            Date now = new Date();
            long timeDiff = turnDTO.dateEnd().getTime() - turnDTO.dateStart().getTime();
            long year = 1000*60*60*24*365L;
            long month = 1000*60*60*24*31L;
            if (user.getRoleEnum() == RoleEnum.STUDENT && (timeDiff > 1000*60*60*24*3 || timeDiff < 0))
                throw new InvalidTimeToCreateTurnException("Invalid turn duration");
            if (user.getRoleEnum() == RoleEnum.EMPLOYEE && (timeDiff > year || timeDiff < 0))
                throw new InvalidTimeToCreateTurnException("Invalid turn duration");
            if (user.getRoleEnum() == RoleEnum.STUDENT && ((turnDTO.dateStart().getTime() - now.getTime() > month) || (turnDTO.dateStart().getTime() - now.getTime() < 0)))
                throw new InvalidLengthTurnException("The turn is too long (or short)");
            if (user.getRoleEnum() == RoleEnum.EMPLOYEE && ((turnDTO.dateStart().getTime() - now.getTime() > month * 3) || (turnDTO.dateStart().getTime() - now.getTime() < 0)))
                throw new InvalidLengthTurnException("The turn is too long (or short)");
            User userCreator = userService.getUserFrom(userDTO.id());
            //Set<Group> groups = groupService.getSetGroups(turn.allowedGroups());
            Turn turn = turnCreatingMapper.turnMoreDTOToTurn(turnDTO, userCreator);
            String allowedGroups = "";
            if (turnDTO.allowedGroups() != null) {
                for (GroupDTO groupDTO : turnDTO.allowedGroups()) {
                    allowedGroups = allowedGroups + groupDTO.name() + " ";
                }
            }
            String allowedFaculties = "";
            if (turnDTO.allowedFaculties() != null) {
                for (Faculty faculty : turnDTO.allowedFaculties()) {
                    allowedFaculties = allowedFaculties + faculty.getName() + " ";
                }
            }
            turn.setTags(turnDTO.name() + " " + turnDTO.description() + " " + allowedGroups + allowedFaculties + user.getName());
            Turn turnNew = turnRepository.save(turn);
            addTurnToUser(turnNew.getId(), login, "CREATOR");
            return turnNew.getId();
        } else
            throw new InvalidDataTurnException("The dateEnd cannot be earlier than the dateStart on createTurn method (TurnServiceImpl.java)");

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
    public void     deleteTurn(String username, Long idTurn) {
        Optional<Turn> turn = turnRepository.findById(idTurn);
        UserDTO userDTO = userService.getUser(username);
        User user = userService.getUserFrom(userDTO.id());
        if (turn.isEmpty()){
            throw new NotFoundTurnException("No turn in database on deleteTurn method (TurnServiceImpl.java)");
        }
        AccessMemberEnum access = memberService.getAccess(user, turn.get());
        if (access == AccessMemberEnum.CREATOR) {
            //memberService.deleteTurnMembers(turn.get());
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
