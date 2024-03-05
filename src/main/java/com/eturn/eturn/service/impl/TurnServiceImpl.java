package com.eturn.eturn.service.impl;

import com.eturn.eturn.dto.TurnDTO;
import com.eturn.eturn.dto.TurnMoreInfoDTO;
import com.eturn.eturn.dto.UserDTO;
import com.eturn.eturn.dto.mapper.TurnListMapper;
import com.eturn.eturn.dto.mapper.TurnMapper;
import com.eturn.eturn.dto.mapper.TurnMoreInfoMapper;
import com.eturn.eturn.entity.Course;
import com.eturn.eturn.entity.Faculty;
import com.eturn.eturn.entity.Group;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.enums.AccessMemberEnum;
import com.eturn.eturn.enums.AccessTurnEnum;
import com.eturn.eturn.enums.RoleEnum;
import com.eturn.eturn.enums.TurnEnum;
import com.eturn.eturn.exception.turn.*;
import com.eturn.eturn.repository.TurnRepository;
import com.eturn.eturn.service.CourseService;
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
    private final CourseService courseService;
    private final MemberService memberService;
    final private  TurnMapper turnMapper;
    final private  TurnListMapper turnListMapper;

    final private TurnMoreInfoMapper turnMoreInfoMapper;


    public TurnServiceImpl(
            TurnRepository turnRepository,
            UserService userService,
            GroupService groupService,
            FacultyService facultyService,
            CourseService courseService,
            MemberService memberService,
            TurnMapper turnMapper,
            TurnListMapper turnListMapper,
            TurnMoreInfoMapper turnMoreInfoMapper) {
        this.turnRepository = turnRepository;
        this.userService = userService;
        this.groupService = groupService;
        this.facultyService = facultyService;
        this.courseService = courseService;
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
    public TurnDTO getTurn(Long id) {
        Optional<Turn> turn = turnRepository.findById(id);
        if (turn.isPresent()){
            return turnMapper.turnToTurnDTO(turn.get());
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
                    c -> c.getAccessTurnType() == AccessTurnEnum.FOR_STUDENT ||
                    c.getAccessTurnType() == AccessTurnEnum.FOR_ALLOWED_ELEMENTS ||
                    c.getAccessTurnType() == AccessTurnEnum.FOR_LINK
            );
        }
        else if(user.getRoleEnum()==RoleEnum.PROFESSOR){
            streamTurns = streamTurns.filter(c ->
                    c.getAccessTurnType()!=AccessTurnEnum.FOR_EMPLOYEE
                    && c.getAccessTurnType()!=AccessTurnEnum.FOR_NO_UNIVERSITY
            );
        }
        else if (user.getRoleEnum() == RoleEnum.NO_UNIVERSITY) {
            streamTurns = streamTurns.filter(c -> c.getAccessTurnType() == AccessTurnEnum.FOR_NO_UNIVERSITY);
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
                            if (user.getIdGroup()!=null && user.getIdFaculty()!=null && user.getIdCourse() !=null){
                                Group groupThis = groupService.getGroup(user.getIdGroup());
                                Faculty facultyThis = facultyService.getOneFaculty(user.getIdFaculty());
                                Course courseThis = courseService.getOneCourse(user.getIdCourse());
                                if (groupThis!=null && facultyThis !=null && courseThis!=null){
                                    streamTurns = streamTurns.filter(c-> c.getAllowedGroups().contains(groupThis)
                                            || c.getAllowedFaculties().contains(facultyThis)
                                            || c.getAllowedCourses().contains(courseThis));
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
                case "Course" -> {
                    Course course = courseService.getOneCourse(Long.parseLong(value));
                    streamTurns = streamTurns.filter(c -> c.getAllowedCourses().contains(course));
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
        turnDto.setCountUsers(0);
        Turn turnNew = turnRepository.save(turnDto);
        addTurnToUser(turnNew.getId(), login, "CREATOR");
        return turnNew.getId();
    }

    @Override
    @Transactional
    public void updateTurn(Long idUser, Turn turn) {
//        Turn turnUpdated = turnMapper.turnDTOToTurn(turn);
        AccessMemberEnum accessMemberEnum = memberService.getAccess(idUser, turn.getId());
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
        AccessMemberEnum access = memberService.getAccess(idUser, idTurn);
        if (access == AccessMemberEnum.CREATOR && turnRepository.existsTurnById(idTurn)) {
            memberService.deleteTurnMembers(idTurn);
            turnRepository.deleteTurnById(idTurn);
        } else if (access != AccessMemberEnum.CREATOR){
            throw new NoAccessDeleteTurnException("Only creator can delete turn information on deleteTurn method (TurnServiceImpl.java)");
        }
        else{
            throw new NotFoundTurnException("No turn in database on deleteTurn method (TurnServiceImpl.java)");
        }
    }

    @Override
    public void countUser(Turn turn) {
        int users = turn.getCountUsers() + 1;
        turn.setCountUsers(users);
        turnRepository.save(turn);
    }

    @Override
    @Transactional
    public void addTurnToUser(Long turnId, String login, String access) {
        User user = userService.findByLogin(login);
        Turn turn = getTurnFrom(turnId);
        int users = turn.getCountUsers()+1;
        turn.setCountUsers(users);
        turnRepository.save(turn);

        memberService.createMember(user.getId(), turnId, access);

        Set<Turn> turns = user.getTurns();
        turns.add(turn);
        user.setTurns(turns);
        userService.updateUser(user);
    }

}
