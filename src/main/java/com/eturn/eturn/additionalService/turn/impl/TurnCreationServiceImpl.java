package com.eturn.eturn.additionalService.turn.impl;

import com.eturn.eturn.additionalService.turn.TurnCreationService;
import com.eturn.eturn.dto.TurnCreatingDTO;
import com.eturn.eturn.dto.TurnDTO;
import com.eturn.eturn.dto.mapper.TurnCreatingMapper;
import com.eturn.eturn.entity.Faculty;
import com.eturn.eturn.entity.Group;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.exception.turn.InvalidDataTurnException;
import com.eturn.eturn.exception.turn.InvalidLengthTurnException;
import com.eturn.eturn.exception.turn.InvalidTimeToCreateTurnException;
import com.eturn.eturn.exception.turn.NoCreateTurnException;
import com.eturn.eturn.notifications.NotificationController;
import com.eturn.eturn.repository.TurnRepository;
import com.eturn.eturn.security.HashGenerator;
import com.eturn.eturn.security.TextCensor;
import com.eturn.eturn.service.MemberService;
import com.eturn.eturn.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Date;

import static com.eturn.eturn.enums.AccessTurn.FOR_ALLOWED_ELEMENTS;

@Service
public class TurnCreationServiceImpl implements TurnCreationService {
    private final TurnRepository turnRepository;
    private final MemberService memberService;
    private final UserService userService;
    private final TurnCreatingMapper turnCreatingMapper;
    private final NotificationController notificationController;

    public TurnCreationServiceImpl(
            TurnRepository turnRepository,
            MemberService memberService,
            UserService userService,
            TurnCreatingMapper turnCreatingMapper,
            NotificationController notificationController
    ) {
        this.turnRepository = turnRepository;
        this.memberService = memberService;
        this.userService = userService;
        this.turnCreatingMapper = turnCreatingMapper;
        this.notificationController = notificationController;
    }

    @Override
    @Transactional
    public String createTurn(
            TurnCreatingDTO turnDTO,
            String login
    ) {
        // Проверка корректности дат
        validateTurnDates(turnDTO);

        // Получаем пользователя и проверяем доступное количество очередей
        User user = userService.getUserFromLogin(login);
        validateUserTurnLimit(user);

        // Проверяем допустимую длительность очереди в зависимости от роли пользователя
        validateTurnDuration(turnDTO, user);

        // Проверка на цензуру
        validateCensor(turnDTO);

        // Проверяем корректность времени начала очереди
        validateTurnStartTime(turnDTO, user);

        // Создаем очередь и настраиваем её параметры
        Turn turn = createAndConfigureTurn(turnDTO, user);

        // Генерируем уникальный хэш для очереди
        String hash = generateUniqueHash();
        turn.setHash(hash);

        // Сохраняем очередь и создаем участника (создателя)
        Turn savedTurn = turnRepository.save(turn);
        memberService.createMember(
                user,
                savedTurn,
                "CREATOR",
                false
        );

        return savedTurn.getHash();
    }

    /**
     * Проверка на цензуру
     * @param turnDTO очередь
     */
    private void validateCensor(TurnCreatingDTO turnDTO){
        boolean nameIsCorrect = TextCensor.textIsCorrect(turnDTO.name());
        boolean descriptionIsCorrect = TextCensor.textIsCorrect(turnDTO.description());
        if (!nameIsCorrect || !descriptionIsCorrect){
            throw new InvalidDataTurnException("Censor error");
        }
    }

    /**
     * Проверяет, что дата окончания не раньше даты начала.
     *
     * @param turnDTO DTO с данными для создания очереди.
     * @throws InvalidDataTurnException Если дата окончания раньше даты начала.
     */
    private void validateTurnDates(TurnCreatingDTO turnDTO) {
        if (turnDTO.dateEnd().getTime() <= turnDTO.dateStart().getTime()) {
            throw new InvalidDataTurnException("The dateEnd cannot be earlier than the dateStart");
        }
    }

    /**
     * Проверяет, не превышает ли пользователь лимит на создание очередей.
     *
     * @param user Пользователь, для которого проверяется лимит.
     * @throws NoCreateTurnException Если лимит превышен.
     */
    private void validateUserTurnLimit(User user) {
        int countTurns = turnRepository.countAllByCreator(user);
        int maxTurns = switch (user.getRole()) {
            case STUDENT -> 5;
            case EMPLOYEE -> 50;
            default -> 0;
        };

        if (countTurns >= maxTurns) {
            throw new NoCreateTurnException("Too many turns");
        }
    }

    /**
     * Проверяет допустимую длительность очереди в зависимости от роли пользователя.
     *
     * @param turnDTO DTO с данными для создания очереди.
     * @param user    Пользователь, создающий очередь.
     * @throws InvalidTimeToCreateTurnException Если длительность очереди недопустима.
     */
    private void validateTurnDuration(
            TurnCreatingDTO turnDTO,
            User user
    ) {
        long timeDiff = turnDTO.dateEnd().getTime()
                - turnDTO.dateStart().getTime();
        long maxDuration = switch (user.getRole()) {
            case STUDENT -> 1000L * 60 * 60 * 24 * 3; // 3 дня
            case EMPLOYEE -> 1000L * 60 * 60 * 24 * 365; // 1 год
            default -> 0;
        };

        if (timeDiff < 0 || timeDiff > maxDuration) {
            throw new InvalidTimeToCreateTurnException("Invalid turn duration");
        }
    }

    /**
     * Проверяет корректность времени начала очереди.
     *
     * @param turnDTO DTO с данными для создания очереди.
     * @param user    Пользователь, создающий очередь.
     * @throws InvalidLengthTurnException Если время начала недопустимо.
     */
    private void validateTurnStartTime(
            TurnCreatingDTO turnDTO,
            User user
    ) {
        Date now = new Date();
        long timeDiff = turnDTO.dateStart().getTime() - now.getTime();
        long maxStartTime = switch (user.getRole()) {
            case STUDENT -> 1000L * 60 * 60 * 24 * 31; // 1 месяц
            case EMPLOYEE -> 1000L * 60 * 60 * 24 * 31 * 3; // 3 месяца
            default -> 0;
        };

        if (timeDiff > maxStartTime || timeDiff < -1000L * 60 * 2) { // 2 минуты в прошлом
            throw new InvalidLengthTurnException("The turn is too long (or short)");
        }
    }

    /**
     * Создает и настраивает очередь на основе DTO и пользователя.
     *
     * @param turnDTO DTO с данными для создания очереди.
     * @param user    Пользователь, создающий очередь.
     * @return Созданная очередь.
     */
    private Turn createAndConfigureTurn(
            TurnCreatingDTO turnDTO,
            User user
    ) {
        Turn turn = turnCreatingMapper.
                turnMoreDTOToTurn(turnDTO, user);

        if (turn.getAccessTurnType() == FOR_ALLOWED_ELEMENTS) {
            String allowedElements =
                    buildAllowedElementsString(turn);
            turn.setAccessTags(allowedElements.trim());
        }
        turn.setExtensionTimes(5);

        String tags = buildTagsString(turn, user);
        turn.setTags(tags);

        return turn;
    }

    /**
     * Строит строку с разрешенными элементами (группы/факультеты).
     *
     * @param turn Очередь, для которой строится строка.
     * @return Строка с разрешенными элементами.
     */
    private String buildAllowedElementsString(Turn turn) {
        StringBuilder allowedElements = new StringBuilder(" ");

        if (turn.getAllowedGroups() != null) {
            for (Group group : turn.getAllowedGroups()) {
                allowedElements.append(group.getNumber()).append(" ");
                notificationController.notifyTurnCreated(
                        group.getId(),
                        turn.getName()
                );
            }
        }

        if (turn.getAllowedFaculties() != null) {
            for (Faculty faculty : turn.getAllowedFaculties()) {
                allowedElements.append(faculty.getName()).append(" ");
            }
        }

        return allowedElements.toString();
    }

    /**
     * Строит строку тегов для очереди.
     *
     * @param turn Очередь, для которой строится строка тегов.
     * @param user Пользователь, создающий очередь.
     * @return Строка тегов.
     */
    private String buildTagsString(
            Turn turn,
            User user
    ) {
        return turn.getName() + " "
                + turn.getDescription() + " "
                + turn.getAccessTags() + " "
                + user.getName();
    }

    /**
     * Генерирует уникальный хэш для очереди.
     *
     * @return Уникальный хэш.
     * @throws InvalidDataTurnException Если не удалось сгенерировать уникальный хэш.
     */
    private String generateUniqueHash() {
        String hash;
        int count = 0;

        do {
            hash = HashGenerator.generateUniqueCode();
            count++;
        } while (turnRepository.existsAllByHash(hash) && count <= 50);

        if (count > 50) {
            throw new InvalidDataTurnException("Failed to generate unique hash");
        }

        return hash;
    }
}
