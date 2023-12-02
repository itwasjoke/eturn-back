package com.eturn.eturn.service.impl;

import com.eturn.eturn.entity.Course;
import com.eturn.eturn.entity.Faculty;
import com.eturn.eturn.entity.Group;
import com.eturn.eturn.entity.Position;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.enums.AccessEnum;
import com.eturn.eturn.enums.TurnEnum;
import com.eturn.eturn.exception.BusinessException;
import com.eturn.eturn.repository.TurnRepository;
import com.eturn.eturn.service.CourseService;
import com.eturn.eturn.service.FacultyService;
import com.eturn.eturn.service.GroupService;
import com.eturn.eturn.service.MemberService;
import com.eturn.eturn.service.PositionService;
import com.eturn.eturn.service.TurnService;
import com.eturn.eturn.service.UserService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

// TODO здесь аннотация @Service
public class TurnServiceImpl implements TurnService {
    // TODO все переменные private final
    private final TurnRepository turnRepository;
    private final UserService userService;
    GroupService groupService;
    FacultyService facultyService;
    CourseService courseService;
    MemberService memberService;
    PositionService positionService;

    public TurnServiceImpl(TurnRepository turnRepository, UserService userService) {
        this.turnRepository = turnRepository;
        this.userService = userService;
    }

    @Override
    public List<Turn> getAllTurns() {
        return turnRepository.findAll();
    }

    @Override
    public Turn getTurn(Long id) {
        return turnRepository.getReferenceById(id);
    }

    @Override
    public List<Turn> getUserTurns(Long idUser, Map<String, String> params) {
        List<Turn> allTurns = turnRepository.findAll();
        List<Turn> userTurns = userService.getUserTurns(idUser);
        Stream<Turn> streamTurns = allTurns.stream();

        try {

        } catch (Exception e) {
            throw new BusinessException("Something error occurred while get user turns");
        }

        // TODO обернуть все в Try/catch и делай rethrow exception'a
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String value = entry.getValue();
            switch (entry.getKey()) {
                case "Access" -> {
                    // TODO Objects.equaal используем только в lambda expression
                    // TODO value.equals("participates")
                    if (Objects.equals(value, "participates")) {
                        streamTurns = streamTurns.filter(userTurns::contains);
                    } else if (Objects.equals(value, "available")) {
                        streamTurns = streamTurns.filter(c -> !userTurns.contains(c));
                    }
                }
                case "Type" -> {
                    TurnEnum type;
                    if (Objects.equals(value, "org")) {
                        type = TurnEnum.ORG;
                    } else if (Objects.equals(value, "edu")) {
                        type = TurnEnum.EDU;
                    } else {
                        type = TurnEnum.EDU;
                    }

                    streamTurns = streamTurns.filter(c -> c.getTurnEnum() == type);
                }
                case "Group" -> {
                    Group group = groupService.getOneGroup(value);
                    // TODO обязательно выбрасывай exception, если не нашел - желательно кастомный
                    // TODO почитай про GlobalExceptionHandler
                    // нужно проработать исключения
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
        return streamTurns.toList();
    }

    // TODO как только вынесешь в DTO, то появится возможность делать такие валидации аннотациями
    class TurnDto {
        @Size(min = 1, max = 255)
        private final String name;

        public TurnDto(String name) {
            this.name = name;
        }
    }

    @Override
    public Turn createTurn(Turn turn) {
        if (turn.getDescription().length() <= 255 && turn.getName().length() <= 50) {
            Turn newTurn = turnRepository.save(turn);
            memberService.createMember(newTurn.getCreator().getId(), newTurn.getId(), AccessEnum.CREATOR);
            return newTurn;
        } else {
            throw new BusinessException("");
        }
    }

    @Override
    public Turn updateTurn(Long idUser, Turn turnOld, Turn turnNew) {
        AccessEnum accessEnum = memberService.getAccess(idUser, turnOld.getId());
        if (accessEnum == AccessEnum.CREATOR) {
            // TODO подумать как это можно сделать без BeanUtils
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
        return null; // TODO null ??
    }

    @Override // TODO над методами в которых есть  несколько мутирующих операция с базой данных - необходимо вешать аннотацию @Transactional
    public void deleteTurn(Long idUser, Turn turn) {
        AccessEnum access = memberService.getAccess(idUser, turn.getId());
        if (access == AccessEnum.CREATOR && turnRepository.existsTurnById(turn.getId())) {
            memberService.deleteTurnMembers(turn.getId());
            turnRepository.deleteTurnById(turn.getId()); // TODO попробовать вот так
            // если бы delete метод возвращал boolean, то мы смогли бы понять удалил он или нет
        } else {
            // TODO thr exc
            // TODO analog action
        }
    }

    @Override
    public void deletePosition(Long idPosition, Long idUser, Turn turn) {
        Position position = positionService.getPositionById(idPosition);
        if (Objects.equals(position.getUser().getId(), idUser)) { // TODO analog
            turn.getPositions().removeIf(c -> Objects.equals(c.getId(), idPosition));
            turnRepository.save(turn);
        } else {
            // TODO thr exc
            // TODO analog action
        }
    }

    // TODO фиксим ошибки текущие
    // TODO созвониться и задеплоить
    // TODO добиваем мелкие комменты
    // TODO снова смотрим

    @Override // TODO @Transactional
    public void addPositionToTurn(Long idUser, Turn turn) {
        positionService.getLastPosition(idUser, turn.getId()).ifPresent(position -> {
            int number = position.getNumber() + 1;

            // TODO MapStruct toPositionEntity
            Position newPosition = new Position();
            User user = userService.getUser(idUser);
            // TODO Position pos = toPositionEntity(user, number);
            newPosition.setUser(user);
            newPosition.setNumber(number);
            turn.getPositions().add(positionService.createPosition(newPosition));
            turnRepository.save(turn);
        });
    }
}
