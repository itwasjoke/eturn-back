package com.eturn.eturn.additionalService.position.impl;

import com.eturn.eturn.additionalService.member.MemberRepositoryService;
import com.eturn.eturn.additionalService.position.PositionRepositoryService;
import com.eturn.eturn.additionalService.position.PositionTimerService;
import com.eturn.eturn.dto.DetailedPositionDTO;
import com.eturn.eturn.dto.PositionDTO;
import com.eturn.eturn.dto.PositionsTurnDTO;
import com.eturn.eturn.dto.mapper.DetailedPositionMapper;
import com.eturn.eturn.dto.mapper.PositionListMapper;
import com.eturn.eturn.entity.Member;
import com.eturn.eturn.entity.Position;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.enums.AccessMember;
import com.eturn.eturn.notifications.NotificationListener;
import com.eturn.eturn.repository.PositionRepository;
import com.eturn.eturn.service.TurnService;
import com.eturn.eturn.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.eturn.eturn.enums.AccessMember.MODERATOR;

@Service
public class PositionRepositoryServiceImpl implements PositionRepositoryService {
    private static final Logger logger = LogManager.getLogger(NotificationListener.class);
    private final PositionRepository positionRepository;
    private final TurnService turnService;
    private final PositionTimerService positionTimerService;
    private final PositionListMapper positionListMapper;
    private final UserService userService;
    private final DetailedPositionMapper detailedPositionMapper;
    private final MemberRepositoryService mbrRepService;

    public PositionRepositoryServiceImpl(
            PositionRepository positionRepository,
            TurnService turnService,
            PositionTimerService positionTimerService,
            PositionListMapper positionListMapper,
            UserService userService,
            DetailedPositionMapper detailedPositionMapper,
            MemberRepositoryService mbrRepService
    ) {
        this.positionRepository = positionRepository;
        this.turnService = turnService;
        this.positionTimerService = positionTimerService;
        this.positionListMapper = positionListMapper;
        this.userService = userService;
        this.detailedPositionMapper = detailedPositionMapper;
        this.mbrRepService = mbrRepService;
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
    public PositionsTurnDTO getPositionList(
            String hash,
            String username,
            int page
    ) {
        Turn turn = turnService.getTurnFrom(hash);
        positionTimerService.deleteOverdueElements(turn);
        long sizePositions =
                positionRepository.countAllByTurn(turn);
        List<PositionDTO> allPositions;
        int size = (int) Math.min(sizePositions, 20);

        if (size > 0) {
            Pageable paging = PageRequest.of(page, size);
            Page<Position> positions =
                    positionRepository.findAllByTurnOrderByIdAsc(
                            turn,
                            paging
                    );
            allPositions = positions.isEmpty()
                    ? null
                    : positionListMapper.map(positions);
        } else {
            allPositions = null;
        }
        DetailedPositionDTO userPosition =
                getFirstUserPosition(hash, username);
        DetailedPositionDTO turnPosition =
                getFirstPosition(hash, username);
        return new PositionsTurnDTO(
                userPosition,
                turnPosition,
                allPositions
        );

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
    public DetailedPositionDTO getFirstUserPosition(
            String hash,
            String username
    ) {
        User user = userService.getUserFromLogin(username);
        Turn turn = turnService.getTurnFrom(hash);

        // Получаем первую позицию пользователя в очереди
        Optional<Position> userPosition =
                positionRepository.findTopByTurnAndUserOrderByIdAsc(
                        turn,
                        user
                );

        // Получаем первую и последнюю позиции в очереди
        Optional<Position> firstPositionInTurn =
                positionRepository.
                        findFirstByTurnOrderByIdAsc(turn);
        Optional<Position> lastPositionInTurn =
                positionRepository.
                        findFirstByTurnOrderByIdDesc(turn);

        // Проверяем, является ли позиция пользователя последней в очереди
        boolean isLast = isUserPositionLast(
                userPosition,
                lastPositionInTurn
        );

        // Возвращаем DTO с информацией о позиции пользователя
        return userPosition.map(position ->
                        createUserPositionDTO(
                                position,
                                firstPositionInTurn,
                                turn,
                                isLast
                        ))
                .orElse(null);
    }

    /**
     * Получает первую позицию в очереди для модератора или создателя.
     * @param hash Хэш очереди
     * @param username Имя пользователя
     * @return DTO с информацией о первой позиции
     */
    @Override
    public DetailedPositionDTO getFirstPosition(
            String hash,
            String username
    ) {
        User user = userService.getUserFromLogin(username);
        Turn turn = turnService.getTurnFrom(hash);

        // Получаем первую позицию в очереди
        Optional<Position> firstPositionInTurn =
                positionRepository.
                        findFirstByTurnOrderByIdAsc(turn);

        // Проверяем доступ пользователя и возвращаем DTO
        return firstPositionInTurn.map(position ->
                        createFirstPositionDTO(
                                position,
                                user
                        ))
                .orElse(null);
    }

    /**
     * Проверяет, является ли позиция пользователя последней в очереди.
     * @param userPosition Позиция пользователя
     * @param lastPositionInTurn Последняя позиция в очереди
     * @return true, если позиция пользователя последняя, иначе false
     */
    private boolean isUserPositionLast(
            Optional<Position> userPosition,
            Optional<Position> lastPositionInTurn
    ) {
        return userPosition.isPresent()
                && lastPositionInTurn.isPresent()
                && userPosition.get().getId().equals(
                        lastPositionInTurn.get().getId()
                );
    }

    /**
     * Создает DTO для позиции пользователя.
     * @param position Позиция пользователя
     * @param firstPositionInTurn Первая позиция в очереди
     * @param turn Очередь
     * @param isLast Является ли позиция последней
     * @return DTO с информацией о позиции
     */
    private DetailedPositionDTO createUserPositionDTO(
            Position position,
            Optional<Position> firstPositionInTurn,
            Turn turn,
            boolean isLast
    ) {
        int difference =
                firstPositionInTurn.map(firstPos ->
                                calculatePositionDifference(
                                        position,
                                        firstPos,
                                        turn
                                ))
                .orElse(0);
        return detailedPositionMapper.
                positionMoreUserToPositionDTO(
                        position,
                        difference,
                        isLast
                );
    }

    /**
     * Вычисляет разницу между позицией пользователя и первой позицией в очереди.
     * @param position Позиция пользователя
     * @param firstPosition Первая позиция в очереди
     * @param turn Очередь
     * @return Разница в позициях
     */
    private int calculatePositionDifference(
            Position position,
            Position firstPosition,
            Turn turn
    ) {
        return firstPosition.getId().equals(position.getId())
                ? 0
                : (int) positionRepository.countIdLeft(
                            position.getId(),
                            turn
                        );
    }

    /**
     * Создает DTO для первой позиции в очереди, если пользователь имеет доступ.
     * @param position Первая позиция в очереди
     * @param user Пользователь
     * @return DTO с информацией о позиции
     */
    private DetailedPositionDTO createFirstPositionDTO(
            Position position,
            User user
    ) {
        Optional<Member> member =
                mbrRepService.getMemberWith(
                        user,
                        position.getTurn()
                );
        if (member.isPresent()
                && (member.get().getAccessMember() == MODERATOR
                || member.get().getAccessMember() == AccessMember.CREATOR)) {
            return detailedPositionMapper.
                    positionMoreInfoToPositionDTO(
                            position,
                            0
                    );
        }
        return null;
    }

    /**
     * Подсчет позиций в очереди
     * @param turn очередь
     * @return количество
     */
    @Override
    public long countPositionsByTurn(Turn turn) {
        return positionRepository.countByTurn(turn);
    }

}
