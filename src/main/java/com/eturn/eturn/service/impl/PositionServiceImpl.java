package com.eturn.eturn.service.impl;

import com.eturn.eturn.additionalService.member.MemberAccessService;
import com.eturn.eturn.additionalService.member.MemberNotificationService;
import com.eturn.eturn.additionalService.member.MemberRepositoryService;
import com.eturn.eturn.additionalService.member.MemberStatusService;
import com.eturn.eturn.additionalService.member.impl.MemberStatusServiceImpl;
import com.eturn.eturn.dto.*;
import com.eturn.eturn.dto.mapper.DetailedPositionMapper;
import com.eturn.eturn.dto.mapper.PositionListMapper;
import com.eturn.eturn.entity.*;
import com.eturn.eturn.enums.*;
import com.eturn.eturn.exception.member.NoAccessMemberException;
import com.eturn.eturn.exception.position.*;
import com.eturn.eturn.notifications.NotificationController;
import com.eturn.eturn.notifications.PositionsNotificationDTO;
import com.eturn.eturn.repository.PositionRepository;
import com.eturn.eturn.service.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.eturn.eturn.enums.AccessMember.*;
import static com.eturn.eturn.enums.AccessMember.BLOCKED;
import static com.eturn.eturn.enums.AccessMember.MODERATOR;
import static com.eturn.eturn.enums.AccessTurn.FOR_LINK;
import static com.eturn.eturn.enums.ChangeMbrAction.*;
import static com.eturn.eturn.enums.InvitedStatus.*;
import static com.eturn.eturn.enums.MemberListType.*;
import static com.eturn.eturn.enums.MemberListType.MEMBER;


@Service
public class PositionServiceImpl implements PositionService {
    private static final Logger logger = LogManager.getLogger(PositionServiceImpl.class);

    private final MemberRepositoryService mbrRepService;
    private MemberStatusService mbrStatusService;
    private final PositionRepository positionRepository;
    private final UserService userService;
    private final PositionListMapper positionListMapper;
    private final TurnService turnService;
    private final NotificationController notificationController;
    private final DetailedPositionMapper detailedPositionMapper;
    private MemberService memberService;

    public PositionServiceImpl(
            MemberRepositoryService mbrRepService,
            PositionRepository positionRepository,
           UserService userService,
           PositionListMapper positionListMapper,
           TurnService turnService,
           NotificationController notificationController,
           DetailedPositionMapper detailedPositionMapper
    ) {
        this.mbrRepService = mbrRepService;
        this.positionRepository = positionRepository;
        this.userService = userService;
        this.positionListMapper = positionListMapper;
        this.turnService = turnService;
        this.notificationController = notificationController;
        this.detailedPositionMapper = detailedPositionMapper;
    }

    @Autowired
    public void setMemberStatusService(MemberStatusService mbrStatusService){
        this.mbrStatusService = mbrStatusService;
    }

    @Autowired
    public void setMemberService(MemberService memberService){
        this.memberService = memberService;
    }

    /**
     * Удаляет просроченные элементы из очереди на основе таймера.
     * Если таймер активен и прошло достаточно времени, удаляет необходимое количество позиций
     * и обновляет таймер для следующей позиции.
     * @param turn Очередь, из которой необходимо удалить просроченные элементы
     */
    @Transactional
    public void deleteOverdueElements(Turn turn) {
        // Проверяем, активен ли таймер для данной очереди
        if (!isTimerActive(turn)) {
            return;
        }

        // Находим первую позицию в очереди
        positionRepository.findFirstByTurnOrderByIdAsc(turn).ifPresent(positionFirst -> {
            // Проверяем, нужно ли удалять позиции
            if (shouldDeletePositions(positionFirst)) {
                // Вычисляем количество позиций для удаления
                int positionsToDelete = calculatePositionsToDelete(positionFirst, turn.getTimer());
                // Удаляем позиции и обновляем таймер
                deletePositionsAndUpdateTimer(turn, positionsToDelete);
            }
        });
    }

    /**
     * Проверяет, активен ли таймер для данной очереди.
     * @param turn Очередь, для которой проверяется таймер
     * @return true, если таймер активен, иначе false
     */
    private boolean isTimerActive(Turn turn) {
        return turn.getTimer() != 0 && turn.getDateStart().getTime() <= System.currentTimeMillis();
    }

    /**
     * Проверяет, нужно ли удалять позиции для данной позиции.
     * @param position Позиция, для которой выполняется проверка
     * @return true, если позиции нужно удалять, иначе false
     */
    private boolean shouldDeletePositions(Position position) {
        return position.getDateEnd() != null && !position.isStart();
    }

    /**
     * Вычисляет количество позиций, которые нужно удалить, на основе прошедшего времени и таймера.
     * @param position Позиция, для которой вычисляется количество удаляемых элементов
     * @param timer Интервал таймера в минутах
     * @return Количество позиций для удаления
     */
    private int calculatePositionsToDelete(Position position, int timer) {
        long timeElapsed = System.currentTimeMillis() - position.getDateEnd().getTime();
        return (int) (TimeUnit.MILLISECONDS.toMinutes(timeElapsed) / timer);
    }

    /**
     * Удаляет указанное количество позиций из очереди и обновляет таймер для новой первой позиции.
     * @param turn Очередь, из которой удаляются позиции
     * @param positionsToDelete Количество позиций для удаления
     */
    private void deletePositionsAndUpdateTimer(Turn turn, int positionsToDelete) {
        // Удаляем позиции и получаем последнюю удаленную позицию
        positionRepository.resultsPositionDeleteOverdueElements(turn.getId(), positionsToDelete)
                .ifPresent(p -> {
                    // Удаляем все позиции с ID меньше или равным ID последней удаленной позиции
                    positionRepository.deleteByTurnAndIdLessThanEqual(turn, p.getId());
                    // Удаляем участников, оставшихся без позиций
                    memberService.deleteMembersWithoutPositions(turn);
                    // Логируем информацию об удалении
                    logger.info("From turn {} deleted {} elements", turn.getName(), positionsToDelete);
                    // Запускаем таймер для новой первой позиции
                    startTimerForNewFirstPosition(turn);
                });
    }

    /**
     * Запускает таймер для новой первой позиции в очереди.
     * @param turn Очередь, для которой обновляется таймер
     */
    private void startTimerForNewFirstPosition(Turn turn) {
        // Находим новую первую позицию
        positionRepository.findFirstByTurnOrderByIdAsc(turn).ifPresent(position -> {
            // Устанавливаем новое время окончания для позиции
            Date newEndDate = Date.from(Instant.now().plus(turn.getTimer(), ChronoUnit.MINUTES));
            position.setDateEnd(newEndDate);
            // Сохраняем обновленную позицию
            positionRepository.save(position);
            // Логируем информацию о запуске таймера
            logger.info("From turn {} timer starts", turn.getName());
        });
    }

    /**
     * Создание позиции
     * @param login имя пользователя
     * @param hash хэш очереди
     * @return Позиция
     */
    @Override
    @Transactional
    public DetailedPositionDTO createPositionAndSave(String login, String hash) {
        // Получаем очередь и пользователя
        Turn turn = turnService.getTurnFrom(hash);
        User user = userService.getUserFromLogin(login);

        // Получаем участника (Member) или создаем нового, если он не существует
        Member currentMember = getOrCreateMember(user, turn);

        // Проверяем доступ участника
        if (isMemberAllowedToCreatePosition(currentMember)) {
            // Удаляем просроченные элементы очереди
            deleteOverdueElements(turn);

            // Создаем новую позицию или выбрасываем исключение, если это невозможно
            return createOrHandlePosition(currentMember, turn, user);
        }

        // Если участник заблокирован или не имеет доступа, возвращаем null
        return null;
    }

    /**
     * Получает участника (Member) для пользователя и очереди или создает нового, если он не существует.
     * @param user Пользователь
     * @param turn Очередь
     * @return Существующий или новый участник (Member)
     */
    private Member getOrCreateMember(User user, Turn turn) {
        Optional<Member> member = mbrRepService.getMemberWith(user, turn);
        if (member.isEmpty()) {
            return addTurnToUser(user, turn);
        } else {
            Member currentMember = member.get();
            handleMemberStatus(currentMember);
            return currentMember;
        }
    }

    /**
     * Обрабатывает статус участника (Member), если он имеет статус MEMBER_LINK.
     * @param member Участник (Member), статус которого нужно обработать
     */
    private void handleMemberStatus(Member member) {
        if (member.getAccessMember() == MEMBER_LINK) {
            switch (member.getInvitedForTurn()) {
                case ACCESS_IN -> mbrStatusService.changeMemberStatusFrom(
                        member.getId(),
                        "MEMBER",
                        Optional.empty(),
                        Optional.empty()
                );
                case ACCESS_OUT -> mbrStatusService.changeMemberStatusFrom(
                        member.getId(),
                        "MEMBER_LINK",
                        Optional.empty(),
                        Optional.of(ADD_INVITE_STATUS)
                );
            }
        }
    }

    /**
     * Проверяет, может ли участник (Member) создавать позицию в очереди.
     * @param member Участник (Member)
     * @return true, если участник не заблокирован и имеет доступ, иначе false
     */
    private boolean isMemberAllowedToCreatePosition(Member member) {
        return member.getAccessMember() != BLOCKED && member.getInvitedForTurn() == ACCESS_IN;
    }

    /**
     * Создает новую позицию или обрабатывает исключение, если создание невозможно.
     * @param member Участник (Member)
     * @param turn Очередь
     * @param user Пользователь
     * @return DTO с информацией о позиции
     * @throws NoCreatePosException Если создание позиции невозможно
     */
    private DetailedPositionDTO createOrHandlePosition(Member member, Turn turn, User user) {
        // Если в очереди уже есть позиции
        if (positionRepository.countAllByTurn(turn) > 0) {
            long countPositions = mbrRepService.getCountMembersWith(turn, MEMBER);
            int permittedCount = calculatePermittedCount(turn, countPositions);

            // Проверяем, может ли пользователь создать новую позицию
            if (isPositionCreationAllowed(turn, user, countPositions, permittedCount)) {
                Position newPosition = createNewPosition(member);
                int differenceForUser = calculatePositionDifference(newPosition, turn, user);
                return detailedPositionMapper.positionMoreInfoToPositionDTO(newPosition, differenceForUser);
            } else {
                // Если создание позиции невозможно, выбрасываем исключение
                throw new NoCreatePosException(String.valueOf(calculateExceptionDifference(turn, user, permittedCount)));
            }
        } else {
            // Если очередь пуста, создаем новую позицию
            Position newPosition = createNewPosition(member);
            return detailedPositionMapper.positionMoreInfoToPositionDTO(newPosition, 0);
        }
    }

    /**
     * Вычисляет допустимое количество позиций в очереди.
     * @param turn Очередь
     * @param countPositions Текущее количество позиций
     * @return Допустимое количество позиций
     */
    private int calculatePermittedCount(Turn turn, long countPositions) {
        int permittedCount = turn.getPositionCount();
        if (permittedCount == -1) {
            throw new NoCreatePosException(String.valueOf(-1));
        } else if (permittedCount == 0) {
            permittedCount = countPositions >= 20 ? (int) (countPositions * 0.8) : 0;
        }
        return permittedCount;
    }

    /**
     * Проверяет, может ли пользователь создать новую позицию.
     * @param turn Очередь
     * @param user Пользователь
     * @param countPositions Текущее количество позиций
     * @param permittedCount Допустимое количество позиций
     * @return true, если создание позиции разрешено, иначе false
     */
    private boolean isPositionCreationAllowed(Turn turn, User user, long countPositions, int permittedCount) {
        Optional<Position> ourPosition = positionRepository.findFirstByUserAndTurnOrderByIdDesc(user, turn);
        if (countPositions < permittedCount && ourPosition.isPresent()) {
            return false;
        }
        return true;
    }

    /**
     * Вычисляет разницу для позиции пользователя.
     * @param newPosition Новая позиция
     * @param turn Очередь
     * @param user Пользователь
     * @return Разница для позиции
     */
    private int calculatePositionDifference(Position newPosition, Turn turn, User user) {
        Optional<Position> ourPosition = positionRepository.findFirstByUserAndTurnOrderByIdDesc(user, turn);
        return ourPosition.isEmpty() ? (int) positionRepository.countIdLeft(newPosition.getId(), turn) : -1;
    }

    /**
     * Вычисляет разницу для исключения, если создание позиции невозможно.
     * @param turn Очередь
     * @param user Пользователь
     * @param permittedCount Допустимое количество позиций
     * @return Разница для исключения
     */
    private int calculateExceptionDifference(Turn turn, User user, int permittedCount) {
        Optional<Position> lastPos = positionRepository.findFirstByTurnOrderByIdDesc(turn);
        Position positionDelete = positionRepository.resultsPositionDelete(turn.getId(), permittedCount);
        if (positionDelete != null) {
            Optional<Position> positionOfUser = positionRepository.findFirstByTurnAndUserAndIdGreaterThanOrderByIdDesc(turn, user, positionDelete.getId());
            if (positionOfUser.isPresent() && lastPos.isPresent()) {
                return permittedCount - positionRepository.countAllByTurnAndIdBetween(turn, positionOfUser.get().getId(), lastPos.get().getId()) + 1;
            }
        }
        return 0;
    }

    /**
     * Создание позиции и заполнение ее параметров
     * @param currentMember участник, который встает в очередь
     * @return созданная позиция
     */
    public Position createNewPosition(Member currentMember) {
        Turn turn = currentMember.getTurn();
        User user = currentMember.getUser();
        Position newPosition = new Position();
        newPosition.setStart(false);
        newPosition.setUser(user);
        newPosition.setTurn(turn);
        newPosition.setDateEnd(null);
        newPosition.setMember(currentMember);
        if (user.getGroup() != null) {
            newPosition.setGroupName(user.getGroup().getNumber());
        }
        if (turn.getPositionCount() != 0) {
            newPosition.setSkipCount(turn.getPositionCount() / 5);
        } else {
            newPosition.setSkipCount((mbrRepService.getCountMembersWith(turn, MEMBER) / 10));
        }
        return positionRepository.save(newPosition);
    }

    /**
     * Получение позиций очереди в виде списка
     * @param hash хэш очереди
     * @param username пользователь текущий
     * @param page страница
     * @return DTO с текущей позицией, позицией пользователя и списком
     */
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
            Page<Position> positions = positionRepository.findAllByTurnOrderByIdAsc(turn, paging);
            allPositions = positions.isEmpty() ? null : positionListMapper.map(positions);
        } else {
            allPositions = null;
        }
        DetailedPositionDTO userPosition = getFirstUserPosition(hash, username);
        DetailedPositionDTO turnPosition = getFirstPosition(hash, username);
        return new PositionsTurnDTO(userPosition, turnPosition, allPositions);

    }

    /**
     * Обновление позиции
     * @param id позиции
     * @param username имя пользователя текущего
     * @param status вход/выход in/out
     */
    @Override
    @Transactional
    public void update(Long id, String username, String status) {
        // Получаем пользователя и позицию
        User user = userService.getUserFromLogin(username);
        Position position = getPositionById(id);

        // Проверяем условия для обновления позиции
        if (isUpdateNotRequired(position, status)) {
            return;
        }

        // Проверяем, наступила ли дата начала очереди
        checkTurnDate(position.getTurn());

        // Проверяем доступ пользователя к позиции
        checkUserAccess(user, position);

        // Удаляем просроченные элементы очереди
        deleteOverdueElements(position.getTurn());

        // Обновляем позицию
        updatePosition(position, user);
    }

    /**
     * Получает позицию по ID или выбрасывает исключение, если позиция не найдена
     * @param id ID позиции
     * @return Позиция
     * @throws NotFoundPosException Если позиция не найдена
     */
    private Position getPositionById(Long id) {
        return positionRepository.findById(id)
                .orElseThrow(() -> new NotFoundPosException("No positions found"));
    }

    /**
     * Проверяет, нужно ли обновлять позиции
     * @param position Позиция
     * @param status Статус
     * @return true, если обновление не требуется, иначе false
     */
    private boolean isUpdateNotRequired(Position position, String status) {
        return position.isStart() && status.equals("in");
    }

    /**
     * Проверяет, наступила ли дата начала очереди
     * @param turn Очередь
     * @throws DateNotArrivedPosException Если дата начала не наступила
     */
    private void checkTurnDate(Turn turn) {
        if (turn.getDateStart().getTime() > System.currentTimeMillis()) {
            throw new DateNotArrivedPosException("The date has not come yet");
        }
    }

    /**
     * Проверяет доступ пользователя к позиции
     * @param user Пользователь
     * @param position Позиция
     * @throws NoAccessPosException Если доступ запрещен
     */
    private void checkUserAccess(User user, Position position) {
        MemberDTO memberDTO = memberService.getMemberDTO(user, position.getTurn());
        String access = memberDTO.access();
        if (!position.getUser().equals(user) && !access.equals(CREATOR.toString()) && !access.equals(MODERATOR.toString())) {
            throw new NoAccessPosException("No access");
        }
    }

    /**
     * Обновляет позицию
     * @param position Позиция
     * @param user Пользователь
     */
    private void updatePosition(Position position, User user) {
        if (position.isStart()) {
            deletePositionAndUpdateTurn(position, user);
        } else {
            startPosition(position);
        }
    }

    /**
     * Удаляет позицию и обновляет статистику очереди
     * @param position Позиция
     * @param user Пользователь
     */
    private void deletePositionAndUpdateTurn(Position position, User user) {
        delete(position.getId(), user.getUsername());
        updateTurnStatistics(position.getTurn(), position.getDateStart());
    }

    /**
     * Обновляет статистику очереди
     * @param turn Очередь
     * @param startDate Дата начала позиции
     */
    private void updateTurnStatistics(Turn turn, Date startDate) {
        long time = System.currentTimeMillis() - startDate.getTime();
        int countPositions = turn.getCountPositionsLeft();
        if (countPositions == 0) {
            turn.setCountPositionsLeft(1);
            turn.setAverageTime((int) time);
            turn.setTotalTime(time);
            turn.setSmoothedValue((double) time);
        } else {
            countPositions++;
            double smoothedValue = 0.99 * time + (1 - 0.99) * turn.getSmoothedValue();
            long totalTime = turn.getTotalTime() + (long) smoothedValue;
            int averageTime = (int) (totalTime / countPositions);
            turn.setSmoothedValue(smoothedValue);
            turn.setCountPositionsLeft(countPositions);
            turn.setAverageTime(averageTime);
        }
        turnService.saveTurn(turn);
    }

    /**
     * Начинает позицию
     * @param position Позиция
     */
    private void startPosition(Position position) {
        position.setStart(true);
        position.setDateStart(new Date());
        positionRepository.save(position);
    }

    /**
     * Удаляет позицию из очереди, если пользователь имеет на это право.
     * Также обновляет следующую позицию и обрабатывает статус участника.
     * @param id ID позиции для удаления
     * @param username Имя пользователя, выполняющего удаление
     * @throws NoAccessMemberException Если пользователь не является участником очереди
     * @throws NoAccessPosException Если пользователь не имеет доступа для удаления позиции
     * @throws NotFoundPosException Если позиция не найдена
     */
    @Override
    @Transactional
    public void delete(Long id, String username) {
        // Получаем пользователя и позицию
        User user = userService.getUserFromLogin(username);
        Position position = getPositionById(id);

        // Проверяем, является ли пользователь участником очереди
        Member member = getMember(user, position.getTurn());

        // Проверяем доступ пользователя к удалению позиции
        checkDeleteAccess(member, position);

        // Удаляем позицию
        positionRepository.delete(position);

        // Обновляем следующую позицию и обрабатываем статус участника
        updateNextPositionAndMemberStatus(position, member, user);
    }

    /**
     * Получает участника (Member) для пользователя и очереди или выбрасывает исключение, если участник не найден.
     * @param user Пользователь
     * @param turn Очередь
     * @return Участник (Member)
     * @throws NoAccessMemberException Если пользователь не является участником очереди
     */
    private Member getMember(User user, Turn turn) {
        return mbrRepService.getMemberWith(user, turn)
                .orElseThrow(() -> new NoAccessMemberException("You are not a member"));
    }

    /**
     * Проверяет, имеет ли пользователь право на удаление позиции.
     * @param member Участник (Member)
     * @param position Позиция
     * @throws NoAccessPosException Если пользователь не имеет доступа для удаления позиции
     */
    private void checkDeleteAccess(Member member, Position position) {
        AccessMember access = member.getAccessMember();
        if (!(access == AccessMember.MEMBER && position.getUser().getId().equals(member.getUser().getId())
                && access != AccessMember.CREATOR
                && access != MODERATOR)) {
            throw new NoAccessPosException("No access");
        }
    }

    /**
     * Обновляет следующую позицию и обрабатывает статус участника.
     * @param position Удаленная позиция
     * @param member Участник (Member)
     * @param user Пользователь
     */
    private void updateNextPositionAndMemberStatus(Position position, Member member, User user) {
        // Находим следующую позицию в очереди
        positionRepository.findFirstByTurnOrderByIdAsc(position.getTurn()).ifPresent(nextPosition -> {
            // Уведомляем пользователей об изменении позиции
            notificationController.notifyUserOfTurnPositionChange(nextPosition.getTurn().getId());

            // Устанавливаем новое время окончания для следующей позиции
            Date newEndDate = calculateNewEndDate(nextPosition.getTurn().getTimer());
            nextPosition.setDateEnd(newEndDate);
            positionRepository.save(nextPosition);

            // Обрабатываем статус участника, если это необходимо
            handleMemberStatusAfterDeletion(position, member, user);
        });
    }

    /**
     * Вычисляет новое время окончания для позиции на основе таймера.
     * @param timer Таймер очереди в минутах
     * @return Новое время окончания
     */
    private Date calculateNewEndDate(int timer) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, timer);
        return calendar.getTime();
    }

    /**
     * Обрабатывает статус участника после удаления позиции.
     * @param position Удаленная позиция
     * @param member Участник (Member)
     * @param user Пользователь
     */
    private void handleMemberStatusAfterDeletion(Position position, Member member, User user) {
        Optional<Position> userPosition = positionRepository.findFirstByUserAndTurnOrderByIdAsc(user, position.getTurn());
        if (userPosition.isEmpty()) {
            if (member.getAccessMember() == AccessMember.MEMBER && position.getTurn().getAccessTurnType() == FOR_LINK) {
                mbrStatusService.changeMemberStatusFrom(member.getId(), "MEMBER_LINK", Optional.empty(), Optional.empty());
            } else if (member.getAccessMember() == AccessMember.MEMBER) {
                mbrRepService.deleteMemberWith(position.getTurn(), user);
            }
        }
    }

    /**
     * Позволяет пользователю пропускать свою позицию в очереди определенное количество раз.
     * Если пользователь имеет право на пропуск, позиция пропускается, и таймер обновляется.
     * @param id ID позиции, которую нужно пропустить
     * @param username Имя пользователя, выполняющего пропуск
     */
    @Override
    @Transactional
    public void skipPosition(long id, String username) {
        // Получаем пользователя и позицию
        User user = userService.getUserFromLogin(username);
        Position position = getPositionById(id);

        // Удаляем просроченные элементы очереди
        deleteOverdueElements(position.getTurn());

        // Получаем текущую позицию в очереди
        Position currentPosition = getCurrentPosition(position.getTurn());

        // Пропускаем позицию, если это возможно
        skipPositionIfAllowed(position, user, currentPosition);
    }

    /**
     * Получает позицию по ID или выбрасывает исключение, если позиция не найдена.
     * @param id ID позиции
     * @return Позиция
     * @throws NotFoundPosException Если позиция не найдена
     */
    private Position getPositionById(long id) {
        return positionRepository.findById(id)
                .orElseThrow(() -> new NotFoundPosException("Position not found"));
    }

    /**
     * Получает текущую позицию в очереди.
     * @param turn Очередь
     * @return Текущая позиция
     */
    private Position getCurrentPosition(Turn turn) {
        return positionRepository.findFirstByTurnOrderByIdAsc(turn)
                .orElseThrow(() -> new NotFoundPosException("No current position found"));
    }

    /**
     * Пропускает позицию, если пользователь имеет на это право.
     * @param position Позиция, которую нужно пропустить
     * @param user Пользователь, выполняющий пропуск
     * @param currentPosition Текущая позиция в очереди
     */
    private void skipPositionIfAllowed(Position position, User user, Position currentPosition) {
        if (isSkipAllowed(position, user)) {
            Position nextPosition = getNextPosition(position);
            if (nextPosition != null) {
                handleSkip(position, nextPosition, currentPosition);
            }
        }
    }

    /**
     * Проверяет, может ли пользователь пропустить позицию.
     * @param position Позиция, которую нужно пропустить
     * @param user Пользователь, выполняющий пропуск
     * @return true, если пропуск разрешен, иначе false
     */
    private boolean isSkipAllowed(Position position, User user) {
        return position.getUser().equals(user) && position.getSkipCount() > 0;
    }

    /**
     * Получает следующую позицию в очереди.
     * @param position Текущая позиция
     * @return Следующая позиция
     */
    private Position getNextPosition(Position position) {
        return positionRepository.findFirstByTurnAndIdGreaterThanOrderByIdAsc(position.getTurn(), position.getId())
                .orElse(null);
    }

    /**
     * Обрабатывает пропуск позиции.
     * @param position Позиция, которую нужно пропустить
     * @param nextPosition Следующая позиция
     * @param currentPosition Текущая позиция в очереди
     */
    private void handleSkip(Position position, Position nextPosition, Position currentPosition) {
        // Проверяем, не является ли следующая позиция также позицией пользователя
        if (isNextPositionAlsoUserPosition(position, nextPosition)) {
            positionRepository.delete(position);
            return;
        }

        // Удаляем предыдущую позицию, если она принадлежит тому же пользователю
        deletePreviousPositionIfSameUser(position, nextPosition);

        // Обновляем таймеры, если текущая позиция активна
        updateTimersIfCurrent(position, nextPosition, currentPosition);

        // Уменьшаем счетчик пропусков
        position.setSkipCount(position.getSkipCount() - 1);

        // Меняем местами пользователей
        User nextPositionUser = position.getUser();
        position.setUser(nextPosition.getUser());
        nextPosition.setUser(nextPositionUser);

        positionRepository.save(position);
        positionRepository.save(nextPosition);
    }

    /**
     * Проверяет, не является ли следующая позиция также позицией пользователя.
     * @param position Текущая позиция
     * @param nextPosition Следующая позиция
     * @return true, если следующая позиция также принадлежит пользователю, иначе false
     */
    private boolean isNextPositionAlsoUserPosition(Position position, Position nextPosition) {
        Position nextNextPosition = positionRepository.findFirstByTurnAndIdGreaterThanOrderByIdAsc(nextPosition.getTurn(), nextPosition.getId())
                .orElse(null);
        return nextNextPosition != null && nextNextPosition.getUser().equals(position.getUser());
    }

    /**
     * Удаляет предыдущую позицию, если она принадлежит тому же пользователю.
     * @param position Текущая позиция
     * @param nextPosition Следующая позиция
     */
    private void deletePreviousPositionIfSameUser(Position position, Position nextPosition) {
        positionRepository.findFirstByTurnAndIdLessThanOrderByIdDesc(position.getTurn(), position.getId())
                .ifPresent(previousPosition -> {
                    if (previousPosition.getUser().equals(nextPosition.getUser())) {
                        positionRepository.delete(previousPosition);
                    }
                });
    }

    /**
     * Обновляет таймеры, если текущая позиция активна.
     * @param position Текущая позиция
     * @param nextPosition Следующая позиция
     * @param currentPosition Активная позиция в очереди
     */
    private void updateTimersIfCurrent(Position position, Position nextPosition, Position currentPosition) {
        if (position.getTurn().getDateStart().getTime() <= System.currentTimeMillis() && position.getId() == currentPosition.getId()) {
            position.setDateEnd(null);
            Date newEndDate = calculateNewEndDate(nextPosition.getTurn().getTimer());
            nextPosition.setDateEnd(newEndDate);
        }
    }

    /**
     * Получает первую позицию пользователя в очереди.
     *
     * @param hash Хэш очереди
     * @param username Имя пользователя
     * @return DTO с информацией о позиции пользователя
     */
    @Override
    @Transactional
    public DetailedPositionDTO getFirstUserPosition(String hash, String username) {
        User user = userService.getUserFromLogin(username);
        Turn turn = turnService.getTurnFrom(hash);

        // Получаем первую позицию пользователя в очереди
        Optional<Position> userPosition = positionRepository.findTopByTurnAndUserOrderByIdAsc(turn, user);

        // Получаем первую и последнюю позиции в очереди
        Optional<Position> firstPositionInTurn = positionRepository.findFirstByTurnOrderByIdAsc(turn);
        Optional<Position> lastPositionInTurn = positionRepository.findFirstByTurnOrderByIdDesc(turn);

        // Проверяем, является ли позиция пользователя последней в очереди
        boolean isLast = isUserPositionLast(userPosition, lastPositionInTurn);

        // Возвращаем DTO с информацией о позиции пользователя
        return userPosition.map(position -> createUserPositionDTO(position, firstPositionInTurn, turn, isLast))
                .orElse(null);
    }

    /**
     * Получает первую позицию в очереди для модератора или создателя.
     * @param hash Хэш очереди
     * @param username Имя пользователя
     * @return DTO с информацией о первой позиции
     */
    @Override
    public DetailedPositionDTO getFirstPosition(String hash, String username) {
        User user = userService.getUserFromLogin(username);
        Turn turn = turnService.getTurnFrom(hash);

        // Получаем первую позицию в очереди
        Optional<Position> firstPositionInTurn = positionRepository.findFirstByTurnOrderByIdAsc(turn);

        // Проверяем доступ пользователя и возвращаем DTO
        return firstPositionInTurn.map(position -> createFirstPositionDTO(position, user))
                .orElse(null);
    }

    /**
     * Проверяет, является ли позиция пользователя последней в очереди.
     * @param userPosition Позиция пользователя
     * @param lastPositionInTurn Последняя позиция в очереди
     * @return true, если позиция пользователя последняя, иначе false
     */
    private boolean isUserPositionLast(Optional<Position> userPosition, Optional<Position> lastPositionInTurn) {
        return userPosition.isPresent() && lastPositionInTurn.isPresent() &&
                userPosition.get().getId().equals(lastPositionInTurn.get().getId());
    }

    /**
     * Создает DTO для позиции пользователя.
     * @param position Позиция пользователя
     * @param firstPositionInTurn Первая позиция в очереди
     * @param turn Очередь
     * @param isLast Является ли позиция последней
     * @return DTO с информацией о позиции
     */
    private DetailedPositionDTO createUserPositionDTO(Position position, Optional<Position> firstPositionInTurn, Turn turn, boolean isLast) {
        int difference = firstPositionInTurn.map(firstPos -> calculatePositionDifference(position, firstPos, turn))
                .orElse(0);
        return detailedPositionMapper.positionMoreUserToPositionDTO(position, difference, isLast);
    }

    /**
     * Вычисляет разницу между позицией пользователя и первой позицией в очереди.
     * @param position Позиция пользователя
     * @param firstPosition Первая позиция в очереди
     * @param turn Очередь
     * @return Разница в позициях
     */
    private int calculatePositionDifference(Position position, Position firstPosition, Turn turn) {
        return firstPosition.getId().equals(position.getId()) ? 0 : (int) positionRepository.countIdLeft(position.getId(), turn);
    }

    /**
     * Создает DTO для первой позиции в очереди, если пользователь имеет доступ.
     * @param position Первая позиция в очереди
     * @param user Пользователь
     * @return DTO с информацией о позиции
     */
    private DetailedPositionDTO createFirstPositionDTO(Position position, User user) {
        Optional<Member> member = mbrRepService.getMemberWith(user, position.getTurn());
        if (member.isPresent() && (member.get().getAccessMember() == MODERATOR || member.get().getAccessMember() == AccessMember.CREATOR)) {
            return detailedPositionMapper.positionMoreInfoToPositionDTO(position, 0);
        }
        return null;
    }
    
    @Override
    public Member addTurnToUser(User user, Turn turn) {
        AccessTurn turnEnum = turn.getAccessTurnType();
        if (turnEnum == FOR_LINK) {
            notificationController.notifyReceiptRequest(turn.getId(), turn.getName());
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
    public PositionsNotificationDTO getPositionsForNotify(Long turnId) {
        Pageable paging = PageRequest.of(0, 10);
        Page<Position> page = positionRepository.findAllByTurn_IdOrderByIdAsc(turnId,paging);
        List<Position> list = page.toList();
        List<User> users = new ArrayList<>();
        logger.info("The turn is now up to " + list.size() + " values");
        if (!list.isEmpty()) {
            Position p1 = list.get(0);
            users.add(p1.getUser());
            if (list.size() > 4) users.add(list.get(4).getUser());
            if (list.size() > 9) users.add(list.get(9).getUser());
            return new PositionsNotificationDTO(users, p1.getTurn().getName());
        } else {
            logger.warn("No notifications will send for "+ turnId +" turn");
            return new PositionsNotificationDTO(null, null);
        }
    }

    @Override
    public long countPositionsByTurn(Turn turn) {
        return positionRepository.countByTurn(turn);
    }

    @Override
    public boolean existsAllByTurnAndUser(Turn turn, User user) {
        return positionRepository.existsAllByTurnAndUser(turn, user);
    }

    @Override
    public void deleteAllByTurnAndUser(Turn turn, User user) {
        positionRepository.deleteAllByTurnAndUser(turn, user);
    }
}
