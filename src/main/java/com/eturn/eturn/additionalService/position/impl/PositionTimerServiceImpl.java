package com.eturn.eturn.additionalService.position.impl;

import com.eturn.eturn.additionalService.position.PositionTimerService;
import com.eturn.eturn.entity.Position;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.repository.PositionRepository;
import com.eturn.eturn.service.MemberService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class PositionTimerServiceImpl implements PositionTimerService {
    private static final Logger logger = LogManager.getLogger(PositionTimerServiceImpl.class);
    private final PositionRepository positionRepository;
    private final MemberService memberService;

    public PositionTimerServiceImpl(
            PositionRepository positionRepository,
            MemberService memberService
    ) {
        this.positionRepository = positionRepository;
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
        positionRepository.
                findFirstByTurnOrderByIdAsc(turn).ifPresent(positionFirst -> {
            // Проверяем, нужно ли удалять позиции
            if (shouldDeletePositions(positionFirst)) {
                // Вычисляем количество позиций для удаления
                int positionsToDelete =
                        calculatePositionsToDelete(
                                positionFirst,
                                turn.getTimer()
                        );
                // Удаляем позиции и обновляем таймер
                logger.info("final count to delete"+positionsToDelete);
                if (positionsToDelete>0) {
                    deletePositionsAndUpdateTimer(
                            turn,
                            positionsToDelete
                    );
                }
            }
        });
    }

    /**
     * Проверяет, активен ли таймер для данной очереди.
     * @param turn Очередь, для которой проверяется таймер
     * @return true, если таймер активен, иначе false
     */
    private boolean isTimerActive(Turn turn) {
        return turn.getTimer() != 0
                && turn.getDateStart().getTime() <= System.currentTimeMillis();
    }

    /**
     * Проверяет, нужно ли удалять позиции для данной позиции.
     * @param position Позиция, для которой выполняется проверка
     * @return true, если позиции нужно удалять, иначе false
     */
    private boolean shouldDeletePositions(Position position) {
        return position.getDateEnd() != null
                && !position.isStart();
    }

    /**
     * Вычисляет количество позиций, которые нужно удалить, на основе прошедшего времени и таймера.
     * @param position Позиция, для которой вычисляется количество удаляемых элементов
     * @param timer Интервал таймера в минутах
     * @return Количество позиций для удаления
     */
    private int calculatePositionsToDelete(
            Position position,
            int timer
    ) {
        long now = new Date().getTime();
        long timeElapsed = now - position.getDateEnd().getTime();
        logger.info("time elapsed"+timeElapsed);
        logger.info("time in minutes"+TimeUnit.MILLISECONDS.toMinutes(timeElapsed));
        return (int) (TimeUnit.MILLISECONDS.toMinutes(timeElapsed)
                        / timer);
    }

    /**
     * Удаляет указанное количество позиций из очереди и обновляет таймер для новой первой позиции.
     * @param turn Очередь, из которой удаляются позиции
     * @param positionsToDelete Количество позиций для удаления
     */
    private void deletePositionsAndUpdateTimer(Turn turn, int positionsToDelete) {
        int count = positionRepository.countAllByTurn(turn);
        if (count<=positionsToDelete){
            positionRepository.deleteAllByTurn(turn);
            return;
        }
        // Удаляем позиции и получаем последнюю удаленную позицию
        positionRepository.resultsPositionDeleteOverdueElements(
                    turn.getId(),
                    positionsToDelete
                )
                .ifPresent(p -> {
                    // Удаляем все позиции с ID меньше или равным ID последней удаленной позиции
                    positionRepository.
                            deleteByTurnAndIdLessThanEqual(
                                    turn,
                                    p.getId()
                            );
                    // Удаляем участников, оставшихся без позиций
                    memberService.deleteMembersWithoutPositions(
                            turn
                    );
                    // Логируем информацию об удалении
                    logger.info(
                            "From turn {} deleted {} elements",
                            turn.getName(),
                            positionsToDelete
                    );
                    // Запускаем таймер для новой первой позиции
                    startTimerForNewFirstPosition(turn);
                });
    }

    /**
     * Запускает таймер для новой первой позиции в очереди.
     * @param turn Очередь, для которой обновляется таймер
     */
    public void startTimerForNewFirstPosition(
            Turn turn
    ) {
        // Находим новую первую позицию
        positionRepository.
                findFirstByTurnOrderByIdAsc(turn).ifPresent(position -> {
            // Устанавливаем новое время окончания для позиции
            Date newEndDate = Date.from(
                    Instant.now().plus(turn.getTimer(),
                    ChronoUnit.MINUTES)
            );
            position.setDateEnd(newEndDate);
            // Сохраняем обновленную позицию
            positionRepository.save(position);
            // Логируем информацию о запуске таймера
            logger.info(
                    "From turn {} timer starts",
                    turn.getName()
            );
        });
    }


}
