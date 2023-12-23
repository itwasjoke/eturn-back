package com.eturn.eturn.service.impl;

import com.eturn.eturn.dto.GroupDTO;
import com.eturn.eturn.dto.TurnDTO;
import com.eturn.eturn.dto.TurnMoreInfoDTO;
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
import com.eturn.eturn.exception.AccessException;
import com.eturn.eturn.exception.InvalidDataException;
import com.eturn.eturn.exception.NotFoundException;
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
//        return turnListMapper.map(turns);
        return turns;
    }

    @Override
    public TurnDTO getTurn(Long id) {
        Optional<Turn> turn = turnRepository.findById(id);
        if (turn.isPresent()){
            return turnMapper.turnToTurnDTO(turn.get());
        }
        else{
            throw new NotFoundException("Очередь не найдена");
        }
    }
    @Override
    public Turn getTurnFrom(Long id) {
        Optional<Turn> turn = turnRepository.findById(id);
        if(turn.isPresent()){
            return turn.get();
        }
        else{
            throw new NotFoundException("Очередь не найдена");
        }
    }
    @Override
    public List<TurnDTO> getUserTurns(Long idUser, Map<String, String> params) {
        try {
            List<Turn> allTurns = turnRepository.findAll();
            if (allTurns.isEmpty()){
                throw new NotFoundException("Очередей нет");
            }
            Stream<Turn> streamTurns = allTurns.stream();
            User user = userService.getUserFrom(idUser);
            if (user.getRoleEnum() == RoleEnum.STUDENT) {
                streamTurns = streamTurns.filter(
                        c -> c.getAccessTurnType() == AccessTurnEnum.FOR_STUDENT ||
                                c.getAccessTurnType() == AccessTurnEnum.FOR_ALLOWED_GROUPS
                );
            }
            if (user.getRoleEnum() == RoleEnum.NO_UNIVERSITY) {
                streamTurns = streamTurns.filter(c -> c.getAccessTurnType() == AccessTurnEnum.FOR_NO_UNIVERSITY);
            }

            for (Map.Entry<String, String> entry : params.entrySet()) {
                String value = entry.getValue();
                switch (entry.getKey()) {
                    case "Access" -> {
                        Set<Turn> userTurns = userService.getUserTurns(idUser);
                        if (value.equals("participates")) {
                            streamTurns = streamTurns.filter(userTurns::contains);
                        } else if (value.equals("available")) {
                            streamTurns = streamTurns.filter(c -> !userTurns.contains(c) && c.getAccessTurnType() != AccessTurnEnum.FOR_LINK);
                        } else {
                            throw new InvalidDataException("Указан неправильный тип очереди");
                        }
                    }
                    case "Type" -> {
                        TurnEnum type;
                        if (Objects.equals(value, "org")) {
                            type = TurnEnum.ORG;
                        } else if (Objects.equals(value, "edu")) {
                            type = TurnEnum.EDU;
                        } else {
                            throw new InvalidDataException("Указан неправильный тип очереди");
                        }
                        streamTurns = streamTurns.filter(c -> c.getTurnType() == type);
                    }
                    case "Group" -> {
                        try {
                            Group group = groupService.getOneGroup(value);
                            streamTurns = streamTurns.filter(c -> c.getAllowedGroups().contains(group));
                        } catch (NotFoundException e) {
                            throw new InvalidDataException(e.getMessage());
                        }
                    }
                    case "Faculty" -> {
                        try {
                            Faculty faculty = facultyService.getOneFaculty(Long.parseLong(value));
                            streamTurns = streamTurns.filter(c -> c.getAllowedFaculties().contains(faculty));
                        } catch (NotFoundException e) {
                            throw new InvalidDataException("Факультет не найден");
                        }

                    }
                    case "Course" -> {
                        try {
                            Course course = courseService.getOneCourse(Long.parseLong(value));
                            streamTurns = streamTurns.filter(c -> c.getAllowedCourses().contains(course));
                        } catch (NotFoundException e) {
                            throw new InvalidDataException("Курс не найден");
                        }
                    }
                }

            }
            List<Turn> endTurns = streamTurns.toList();
            return turnListMapper.map(endTurns);
        }
        catch (NotFoundException e){
            throw new InvalidDataException(e.getMessage());
        }
    }

    @Transactional
    @Override
    public Long createTurn(TurnMoreInfoDTO turn) {
//        var v = validator.validate(turn, TurnDTO.class);
//        if (!v.isEmpty()){
//            throw new InvalidDataException("");
//        }
        User userCreator = userService.getUserFrom(turn.creator());
        Set<Group> groups = groupService.getSetGroups(turn.allowedGroups());
        Turn turnDto = turnMoreInfoMapper.turnMoreDTOToTurn(turn,userCreator, groups);
//        HashSet<User> users = new HashSet<User>();
//        users.add(userCreator);
//        turnDto.setUsers(users);
        turnDto.setCountUsers(1);
        Turn turnNew = turnRepository.save(turnDto);
        memberService.createMember(turnNew.getCreator().getId(), turnNew.getId(), AccessMemberEnum.CREATOR);
        return turnNew.getId();
    }

    @Transactional
    @Override
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
                throw new InvalidDataException("Очередь не найдена");
            }
        }
        else{
            throw new AccessException("Доступ имеет только создатель");
        }
    }
    @Transactional
    @Override
    public void deleteTurn(Long idUser, Long idTurn) {
        AccessMemberEnum access = memberService.getAccess(idUser, idTurn);
        if (access == AccessMemberEnum.CREATOR && turnRepository.existsTurnById(idTurn)) {
            memberService.deleteTurnMembers(idTurn);
            turnRepository.deleteTurnById(idTurn);
        } else if (access != AccessMemberEnum.CREATOR){
            throw new AccessException("Удалить очередь может только создатель");
        }
        else{
            throw new InvalidDataException("Очередь не найдена");
        }
    }

    @Override
    public void countUser(Turn turn) {
        int users = turn.getCountUsers() + 1;
        turn.setCountUsers(users);
        turnRepository.save(turn);
    }

    @Transactional
    @Override
    public void addTurnToUser(Long turnId, Long userId) {
        User user = userService.getUserFrom(userId);
        Turn turn = getTurnFrom(turnId);
        Set<Turn> turns = user.getTurns();
        turns.add(turn);
        userService.updateUser(user);
    }

}
