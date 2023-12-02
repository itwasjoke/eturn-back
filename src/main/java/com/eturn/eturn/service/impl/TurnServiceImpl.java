package com.eturn.eturn.service.impl;

import com.eturn.eturn.entity.*;
import com.eturn.eturn.enums.AccessEnum;
import com.eturn.eturn.enums.RoleEnum;
import com.eturn.eturn.enums.TurnEnum;
import com.eturn.eturn.repository.TurnRepository;
import com.eturn.eturn.service.*;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public class TurnServiceImpl implements TurnService {

    TurnRepository turnRepository;
    UserService userService;
    GroupService groupService;
    FacultyService facultyService;
    CourseService courseService;
    MemberService memberService;
    PositionService positionService;

    @Override
    public List<Turn> getAllTurns() {
        return turnRepository.findAll();
    }

    @Override
    public Turn getTurn(Long id) {
        return turnRepository.getReferenceById(id);
    }

    @Override
    public List<Turn> getUserTurns(Long idUser, Map<String,String> params) {
        List<Turn> allTurns = turnRepository.findAll();
        List<Turn> userTurns = userService.getUserTurns(idUser);
        Stream<Turn> streamTurns = allTurns.stream();
        for (Map.Entry<String, String> entry : params.entrySet()){
            String value = entry.getValue();
            switch (entry.getKey()) {
                case "Access" -> {
                    if (Objects.equals(value, "participates")) {
                        streamTurns = streamTurns.filter(userTurns::contains);
                    } else if (Objects.equals(value, "available")) {
                        streamTurns = streamTurns.filter(c -> !userTurns.contains(c));
                    }
                }
                case "Type" -> {
                    TurnEnum type;
                    if (Objects.equals(value, "org")) type = TurnEnum.ORG;
                    else if (Objects.equals(value, "edu")) type = TurnEnum.EDU;
                    else {
                        type = TurnEnum.EDU;
                    }
                    streamTurns = streamTurns.filter(c -> c.getTurnEnum()==type);
                }
                case "Group" -> {
                    Group group = groupService.getOneGroup(value);
                    // нужно проработать исключения
                    streamTurns = streamTurns.filter(c-> c.getAllowedGroups().contains(group));
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
        return streamTurns.toList();
    }

    @Override
    public Turn createTurn(Turn turn) {
        if (turn.getDescription().length()<=255 && turn.getName().length()<=50){
            Turn newTurn = turnRepository.save(turn);
            memberService.createMember(newTurn.getCreator().getId(), newTurn.getId(), AccessEnum.CREATOR);
            return newTurn;
        }
        else{
            return null;
        }

    }

    @Override
    public Turn updateTurn(Long idUser, Turn turnOld, Turn turnNew) {
        AccessEnum accessEnum = memberService.getAccess(idUser,turnOld.getId());
        if (accessEnum == AccessEnum.CREATOR){
            BeanUtils.copyProperties(
                    turnNew,
                    turnOld,
                    "id",
                    "creator",
                    "positions",
                    "positionsCount",
                    "allTime",
                    "averageTime",
                    "elapsedTime",
                    "positionsLeft"
            );
            return turnRepository.save(turnNew);
        }
        return null;
    }

    @Override
    public void deleteTurn(Long idUser, Turn turn) {
        Turn turnFromDb = turnRepository.getReferenceById(turn.getId());
        // как проверить что запись реально существует
        AccessEnum access = memberService.getAccess(idUser, turnFromDb.getId());
        if (access == AccessEnum.CREATOR){
            memberService.deleteTurnMembers(turnFromDb.getId());
            turnRepository.delete(turnFromDb);
        }
    }

    @Override
    public void deletePosition(Long idPosition, Long idUser, Turn turn) {
        Position position = positionService.getPositionById(idPosition);
        if (Objects.equals(position.getUser().getId(), idUser)){
            turn.getPositions().removeIf(c -> Objects.equals(c.getId(), idPosition));
            turnRepository.save(turn);
        }
    }

    @Override
    public void addPositionToTurn(Long idUser, Turn turn) {
        Position position = positionService.getLastPosition(idUser, turn.getId());
        int number = 1;
        if (position!=null){
            number = position.getNumber()+1;
        }
        Position positionNew = new Position();
        User user = userService.getUser(idUser);
        positionNew.setUser(user);
        positionNew.setNumber(number);
        Position positionCreated = positionService.createPosition(positionNew);
        turn.getPositions().add(positionCreated);
        turnRepository.save(turn);
    }
}
