package com.eturn.eturn.additionalService.position.impl;

import com.eturn.eturn.additionalService.position.PositionDeletionService;
import com.eturn.eturn.additionalService.position.PositionTimerService;
import com.eturn.eturn.additionalService.position.PositionUpdateService;
import com.eturn.eturn.dto.MemberDTO;
import com.eturn.eturn.entity.Position;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.exception.position.DateNotArrivedPosException;
import com.eturn.eturn.exception.position.NoAccessPosException;
import com.eturn.eturn.exception.position.NotFoundPosException;
import com.eturn.eturn.repository.PositionRepository;
import com.eturn.eturn.service.MemberService;
import com.eturn.eturn.service.TurnService;
import com.eturn.eturn.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import static com.eturn.eturn.enums.AccessMember.CREATOR;
import static com.eturn.eturn.enums.AccessMember.MODERATOR;

@Service
public class PositionUpdateServiceImpl implements PositionUpdateService {
    private final PositionRepository positionRepository;
    private final UserService userService;
    private final TurnService turnService;
    private final MemberService memberService;
    private final PositionTimerService positionTimerService;
    private final PositionDeletionService positionDeletionService;

    public PositionUpdateServiceImpl(
            PositionRepository positionRepository,
            UserService userService,
            TurnService turnService,
            MemberService memberService,
            PositionTimerService positionTimerService,
            PositionDeletionService positionDeletionService
    ) {
        this.positionRepository = positionRepository;
        this.userService = userService;
        this.turnService = turnService;
        this.memberService = memberService;
        this.positionTimerService = positionTimerService;
        this.positionDeletionService = positionDeletionService;
    }

    /**
     * Обновление позиции
     * @param id позиции
     * @param username имя пользователя текущего
     * @param status вход/выход in/out
     */
    @Override
    @Transactional
    public void update(
            Long id,
            String username,
            String status
    ) {
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
        positionTimerService.
                deleteOverdueElements(
                        position.getTurn()
                );

        // Обновляем позицию
        updatePosition(position, user);
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
        positionTimerService.
                deleteOverdueElements(position.getTurn());

        // Получаем текущую позицию в очереди
        Position currentPosition =
                getCurrentPosition(position.getTurn());

        // Пропускаем позицию, если это возможно
        skipPositionIfAllowed(
                position,
                user,
                currentPosition
        );
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
    private boolean isUpdateNotRequired(
            Position position,
            String status
    ) {
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
    private void checkUserAccess(
            User user,
            Position position
    ) {
        MemberDTO memberDTO = memberService.getMemberDTO(
                user,
                position.getTurn()
        );
        String access = memberDTO.access();
        if (
                !position.getUser().equals(user)
                && !access.equals(CREATOR.toString())
                && !access.equals(MODERATOR.toString())
        ) {
            throw new NoAccessPosException("No access");
        }
    }

    /**
     * Обновляет позицию
     * @param position Позиция
     * @param user Пользователь
     */
    private void updatePosition(
            Position position,
            User user
    ) {
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
    private void deletePositionAndUpdateTurn(
            Position position,
            User user
    ) {
        positionDeletionService.delete(
                position.getId(),
                user.getUsername()
        );
        updateTurnStatistics(
                position.getTurn(),
                position.getDateStart()
        );
    }

    /**
     * Обновляет статистику очереди
     * @param turn Очередь
     * @param startDate Дата начала позиции
     */
    private void updateTurnStatistics(
            Turn turn,
            Date startDate
    ) {
        long time = System.currentTimeMillis()
                - startDate.getTime();
        int countPositions = turn.getCountPositionsLeft();
        if (countPositions == 0) {
            turn.setCountPositionsLeft(1);
            turn.setAverageTime((int) time);
            turn.setTotalTime(time);
            turn.setSmoothedValue((double) time);
        } else {
            countPositions++;
            double smoothedValue =
                    0.99 * time + (1 - 0.99) * turn.getSmoothedValue();
            long totalTime =
                    turn.getTotalTime() + (long) smoothedValue;
            int averageTime =
                    (int) (totalTime / countPositions);
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
    private void skipPositionIfAllowed(
            Position position,
            User user,
            Position currentPosition
    ) {
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
    private boolean isSkipAllowed(
            Position position,
            User user
    ) {
        return position.getUser().equals(user)
                && position.getSkipCount() > 0;
    }

    /**
     * Получает следующую позицию в очереди.
     * @param position Текущая позиция
     * @return Следующая позиция
     */
    private Position getNextPosition(Position position) {
        return positionRepository
                .findFirstByTurnAndIdGreaterThanOrderByIdAsc(
                        position.getTurn(),
                        position.getId()
                )
                .orElse(null);
    }

    /**
     * Обрабатывает пропуск позиции.
     * @param position Позиция, которую нужно пропустить
     * @param nextPosition Следующая позиция
     * @param currentPosition Текущая позиция в очереди
     */
    private void handleSkip(
            Position position,
            Position nextPosition,
            Position currentPosition
    ) {
        // Проверяем, не является ли следующая позиция также позицией пользователя
        if (isNextPositionAlsoUserPosition(
                position,
                nextPosition
        )) {
            positionRepository.delete(position);
            return;
        }

        // Удаляем предыдущую позицию, если она принадлежит тому же пользователю
        deletePreviousPositionIfSameUser(
                position,
                nextPosition
        );

        // Обновляем таймеры, если текущая позиция активна
        updateTimersIfCurrent(
                position,
                currentPosition
        );

        // Уменьшаем счетчик пропусков
        int countPosition = position.getSkipCount();
        int countNextPosition = nextPosition.getSkipCount();
        nextPosition.setSkipCount(
                countPosition - 1
        );
        position.setSkipCount(countNextPosition);

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
    private boolean isNextPositionAlsoUserPosition(
            Position position,
            Position nextPosition
    ) {
        Position nextNextPosition =
                positionRepository.
                        findFirstByTurnAndIdGreaterThanOrderByIdAsc(
                                nextPosition.getTurn(),
                                nextPosition.getId()
                        )
                .orElse(null);
        return nextNextPosition != null
                && nextNextPosition.getUser().equals(position.getUser());
    }

    /**
     * Удаляет предыдущую позицию, если она принадлежит тому же пользователю.
     * @param position Текущая позиция
     * @param nextPosition Следующая позиция
     */
    private void deletePreviousPositionIfSameUser(
            Position position,
            Position nextPosition
    ) {
        positionRepository.
                findFirstByTurnAndIdLessThanOrderByIdDesc(
                        position.getTurn(),
                        position.getId()
                )
                .ifPresent(previousPosition -> {
                    if (previousPosition.getUser().equals(
                            nextPosition.getUser())
                    ) {
                        positionRepository.delete(previousPosition);
                    }
                });
    }

    /**
     * Обновляет таймеры, если текущая позиция активна.
     * @param position Текущая позиция
     * @param currentPosition Активная позиция в очереди
     */
    private void updateTimersIfCurrent(
            Position position,
            Position currentPosition
    ) {
        if (
                position.getTurn().getDateStart().getTime()
                        <= System.currentTimeMillis()
                && Objects.equals(
                        position.getId(),
                        currentPosition.getId()
                    )
        ) {
            position.setDateEnd(null);
            Date newEndDate = calculateNewEndDate(position.getTurn().getTimer());
            position.setDateEnd(newEndDate);
        }
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
}
