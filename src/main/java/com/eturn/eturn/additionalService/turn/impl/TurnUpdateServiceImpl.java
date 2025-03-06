package com.eturn.eturn.additionalService.turn.impl;

import com.eturn.eturn.additionalService.turn.TurnUpdateService;
import com.eturn.eturn.dto.FacultyDTO;
import com.eturn.eturn.dto.GroupDTO;
import com.eturn.eturn.dto.TurnCreatingDTO;
import com.eturn.eturn.dto.TurnEditDTO;
import com.eturn.eturn.dto.mapper.FacultyMapper;
import com.eturn.eturn.dto.mapper.GroupMapper;
import com.eturn.eturn.entity.Faculty;
import com.eturn.eturn.entity.Group;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.exception.member.NoAccessMemberException;
import com.eturn.eturn.exception.turn.InvalidDataTurnException;
import com.eturn.eturn.exception.turn.NoAccessDeleteTurnException;
import com.eturn.eturn.exception.turn.NoAccessUpdateTurnException;
import com.eturn.eturn.exception.turn.NotFoundTurnException;
import com.eturn.eturn.repository.TurnRepository;
import com.eturn.eturn.security.TextCensor;
import com.eturn.eturn.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.eturn.eturn.enums.AccessTurn.FOR_ALLOWED_ELEMENTS;

@Service
public class TurnUpdateServiceImpl implements TurnUpdateService {
    private final TurnRepository turnRepository;
    private final UserService userService;
    private final GroupMapper groupMapper;
    private final FacultyMapper facultyMapper;

    public TurnUpdateServiceImpl(
            TurnRepository turnRepository,
            UserService userService,
            GroupMapper groupMapper,
            FacultyMapper facultyMapper
    ) {
        this.turnRepository = turnRepository;
        this.userService = userService;
        this.groupMapper = groupMapper;
        this.facultyMapper = facultyMapper;
    }

    @Override
    public void updateTurn(
            TurnEditDTO turn,
            String username
    ) {
        // Получаем текущую очередь и пользователя
        Turn currentTurn = getTurnByHashOrThrow(turn.hash());
        User user = userService.getUserFromLogin(username);

        // Проверка на цензуру
        validateCensor(turn);

        // Проверяем, имеет ли пользователь право на изменение очереди
        validateUserAccess(currentTurn, user);

        // Обновляем данные очереди
        updateTurnData(currentTurn, turn);

        // Сохраняем обновленную очередь
        saveTurn(currentTurn);
    }

    /**
     * Проверка на цензуру
     * @param turnDTO очередь
     */
    private void validateCensor(TurnEditDTO turnDTO){
        boolean nameIsCorrect = TextCensor.textIsCorrect(turnDTO.name());
        boolean descriptionIsCorrect = TextCensor.textIsCorrect(turnDTO.description());
        if (!nameIsCorrect || !descriptionIsCorrect){
            throw new InvalidDataTurnException("Censor error");
        }
    }

    /**
     * Получает очередь по хэшу или выбрасывает исключение, если очередь не найдена.
     *
     * @param hash Хэш очереди.
     * @return Найденная очередь.
     * @throws NotFoundTurnException Если очередь не найдена.
     */
    private Turn getTurnByHashOrThrow(String hash) {
        return turnRepository.findTurnByHash(hash)
                .orElseThrow(() -> new NotFoundTurnException("Turn not found"));
    }

    /**
     * Проверяет, имеет ли пользователь право на изменение очереди.
     *
     * @param turn Очередь, которую нужно изменить.
     * @param user Пользователь, пытающийся изменить очередь.
     * @throws NoAccessMemberException Если пользователь не имеет доступа.
     */
    private void validateUserAccess(
            Turn turn,
            User user
    ) {
        if (!turn.getCreator().getId().equals(user.getId())) {
            throw new NoAccessMemberException("No access");
        }
    }

    /**
     * Обновляет данные очереди на основе DTO.
     *
     * @param turn Очередь, которую нужно обновить.
     * @param turnDTO DTO с новыми данными.
     */
    private void updateTurnData(
            Turn turn,
            TurnEditDTO turnDTO
    ) {
        // Обновляем имя и описание
        updateNameAndDescription(turn, turnDTO);

        // Обновляем разрешенные группы и факультеты
        updateAllowedGroups(turn, turnDTO);
        updateAllowedFaculties(turn, turnDTO);

        // Обновляем таймер и количество позиций
        updateTimerAndPositionCount(turn, turnDTO);

        // Обновляем теги доступа и общие теги
        updateAccessTags(turn);
        updateTags(turn);
    }

    /**
     * Обновляет имя и описание очереди.
     *
     * @param turn Очередь, которую нужно обновить.
     * @param turnDTO DTO с новыми данными.
     */
    private void updateNameAndDescription(
            Turn turn,
            TurnEditDTO turnDTO
    ) {
        if (turnDTO.name() != null) {
            turn.setName(turnDTO.name());
        }
        turn.setDescription(turnDTO.description());
    }

    /**
     * Обновляет разрешенные группы очереди.
     *
     * @param turn Очередь, которую нужно обновить.
     * @param turnDTO DTO с новыми данными.
     */
    private void updateAllowedGroups(
            Turn turn,
            TurnEditDTO turnDTO
    ) {
        if (
                turnDTO.allowedGroups() != null &&
                !turnDTO.allowedGroups().isEmpty()
        ) {
            Set<Group> groups = new HashSet<>(
                    turn.getAllowedGroups()
            );
            for (GroupDTO groupDTO : turnDTO.allowedGroups()) {
                groups.add(groupMapper.dtoToGroup(groupDTO));
            }
            turn.setAllowedGroups(groups);
        }
    }

    /**
     * Обновляет разрешенные факультеты очереди.
     *
     * @param turn Очередь, которую нужно обновить.
     * @param turnDTO DTO с новыми данными.
     */
    private void updateAllowedFaculties(
            Turn turn,
            TurnEditDTO turnDTO
    ) {
        if (
                turnDTO.allowedFaculties() != null &&
                !turnDTO.allowedFaculties().isEmpty()
        ) {
            Set<Faculty> faculties = new HashSet<>(
                    turn.getAllowedFaculties()
            );
            for (FacultyDTO facultyDTO : turnDTO.allowedFaculties()) {
                faculties.add(facultyMapper.dtoToFaculty(facultyDTO));
            }
            turn.setAllowedFaculties(faculties);
        }
    }

    /**
     * Обновляет таймер и количество позиций очереди.
     *
     * @param turn Очередь, которую нужно обновить.
     * @param turnDTO DTO с новыми данными.
     */
    private void updateTimerAndPositionCount(
            Turn turn,
            TurnEditDTO turnDTO
    ) {
        if (turnDTO.timer() != null) {
            turn.setTimer(turnDTO.timer());
        }
        if (turnDTO.positionCount() != null) {
            turn.setPositionCount(
                    turnDTO.positionCount()
            );
        }
    }

    /**
     * Обновляет теги доступа очереди.
     *
     * @param turn Очередь, которую нужно обновить.
     */
    private void updateAccessTags(Turn turn) {
        if (turn.getAccessTurnType() == FOR_ALLOWED_ELEMENTS) {
            Set<String> groupsName = new HashSet<>();
            Set<String> facultiesName = new HashSet<>();

            if (turn.getAllowedGroups() != null) {
                for (Group group : turn.getAllowedGroups()) {
                    groupsName.add(group.getNumber());
                }
            }
            if (turn.getAllowedFaculties() != null) {
                for (Faculty faculty : turn.getAllowedFaculties()) {
                    facultiesName.add(faculty.getName());
                }
            }

            String groupTags = String.join(" ", groupsName);
            String facultyTags = String.join(" ", facultiesName);
            turn.setAccessTags(groupTags + " " + facultyTags);
        }
    }

    /**
     * Обновляет общие теги очереди.
     *
     * @param turn Очередь, которую нужно обновить.
     */
    private void updateTags(Turn turn) {
        String tags = turn.getName() + " "
                + turn.getDescription() + " "
                + turn.getAccessTags() + " "
                + turn.getCreator().getName();
        turn.setTags(tags);
    }

    /**
     * Сохраняет обновленную очередь в репозитории.
     *
     * @param turn Очередь, которую нужно сохранить.
     */
    private void saveTurn(Turn turn) {
        turnRepository.save(turn);
    }

    /**
     * Удаление очереди
     * @param username имя пользователя
     * @param hash хэш очереди
     */
    @Override
    @Transactional
    public void deleteTurn(
            String username,
            String hash
    ) {
        Optional<Turn> turn =
                turnRepository.findTurnByHash(hash);
        User user = userService.getUserFromLogin(username);

        // проверка на существование
        if (turn.isEmpty()){
            throw new NotFoundTurnException("No turn in database on deleteTurn method (TurnServiceImpl.java)");
        }

        // проверка, что удаляет именно создатель
        if (turn.get().getCreator().getId().equals(user.getId())) {
            turnRepository.deleteTurnById(turn.get().getId());
        } else {
            throw new NoAccessDeleteTurnException("Only creator can delete turn information on deleteTurn method (TurnServiceImpl.java)");
        }
    }

    /**
     * Добавляет час к дате конца очереди, если осталось менее 15 минут до конца очереди
     * @param hash хэш очереди
     * @param username пользователь
     */
    @Override
    public void addHour(String hash, String username) {
        // Получаем текущую очередь и пользователя
        Turn currentTurn = getTurnByHashOrThrow(hash);
        User user = userService.getUserFromLogin(username);

        // Проверяем, имеет ли пользователь право на изменение очереди
        validateUserAccess(currentTurn, user);

        // получение количества попыток
        Integer extensionTimes = currentTurn.getExtensionTimes();
        if (extensionTimes <= 0){
            throw new NoAccessUpdateTurnException("No times left");
        }

        // проверка, что осталось меньше 15 минут до конца очереди
        Date dateEnd = currentTurn.getDateEnd();
        isLessThan15MinutesRemaining(dateEnd);

        // добавление часа и сохранение
        Instant instantDate = dateEnd
                .toInstant()
                .plusSeconds(3600);
        currentTurn.setDateEnd(Date.from(instantDate));
        currentTurn.setExtensionTimes(extensionTimes-1);
        turnRepository.save(currentTurn);
    }

    /**
     * Проверяет, что осталось менее 15 минут до конца очереди
     * @param date1 дата конца очереди
     */
    private void isLessThan15MinutesRemaining(Date date1) {
        // Текущее время
        Instant now = Instant.now();

        // Время date1
        Instant target = date1.toInstant();

        // Разница между датами
        Duration duration = Duration.between(now, target);

        // Проверяем, что разница меньше 15 минут (900 секунд)
        if(duration.isNegative() || duration.getSeconds() >= 900){
            throw new NoAccessUpdateTurnException("Too far");
        }
    }
}
